package com.traverse.geomancy.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;

public sealed interface TransmutationResult {
    Codec<TransmutationResult> CODEC = Codec.STRING.dispatch(TransmutationResult::kind, TransmutationResult::mapCodecFor);

    // Hand-rolled rather than StreamCodec#dispatch - see TransmutationSubject.STREAM_CODEC.
    StreamCodec<RegistryFriendlyByteBuf, TransmutationResult> STREAM_CODEC = StreamCodec.of(
            (buf, result) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, result.kind());
                switch (result) {
                    case AsBlock asBlock -> AsBlock.STREAM_CODEC.encode(buf, asBlock);
                    case AsItem asItem -> AsItem.STREAM_CODEC.encode(buf, asItem);
                    case AsLoot asLoot -> AsLoot.STREAM_CODEC.encode(buf, asLoot);
                }
            },
            buf -> {
                String kind = ByteBufCodecs.STRING_UTF8.decode(buf);
                return switch (kind) {
                    case AsBlock.KIND -> AsBlock.STREAM_CODEC.decode(buf);
                    case AsItem.KIND -> AsItem.STREAM_CODEC.decode(buf);
                    case AsLoot.KIND -> AsLoot.STREAM_CODEC.decode(buf);
                    default -> throw new IllegalArgumentException("Unknown transmutation result type '" + kind + "'");
                };
            });

    String kind();

    private static MapCodec<? extends TransmutationResult> mapCodecFor(String kind) {
        return switch (kind) {
            case AsBlock.KIND -> AsBlock.MAP_CODEC;
            case AsItem.KIND -> AsItem.MAP_CODEC;
            case AsLoot.KIND -> AsLoot.MAP_CODEC;
            default -> throw new IllegalArgumentException("Unknown transmutation result type '" + kind + "'");
        };
    }

    record AsBlock(Holder<Block> block) implements TransmutationResult {
        static final String KIND = "block";
        static final MapCodec<AsBlock> MAP_CODEC =
                BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").xmap(AsBlock::new, AsBlock::block);
        static final StreamCodec<RegistryFriendlyByteBuf, AsBlock> STREAM_CODEC =
                ByteBufCodecs.holderRegistry(Registries.BLOCK).map(AsBlock::new, AsBlock::block);

        @Override
        public String kind() {
            return KIND;
        }
    }

    // ItemStackTemplate rather than a plain ItemStack: an ItemStack construction eagerly
    // reads the item's default DataComponentMap off its registry Holder, which is not yet
    // bound at datagen time. ItemStackTemplate defers that until #create() is called at
    // actual job-completion time, which is well after registries have finished loading.
    record AsItem(ItemStackTemplate template) implements TransmutationResult {
        static final String KIND = "item";
        static final MapCodec<AsItem> MAP_CODEC =
                ItemStackTemplate.MAP_CODEC.codec().fieldOf("item").xmap(AsItem::new, AsItem::template);
        static final StreamCodec<RegistryFriendlyByteBuf, AsItem> STREAM_CODEC =
                ItemStackTemplate.STREAM_CODEC.map(AsItem::new, AsItem::template);

        @Override
        public String kind() {
            return KIND;
        }
    }

    record AsLoot() implements TransmutationResult {
        static final String KIND = "loot";
        static final MapCodec<AsLoot> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.point(new AsLoot()));
        static final StreamCodec<RegistryFriendlyByteBuf, AsLoot> STREAM_CODEC = StreamCodec.unit(new AsLoot());

        @Override
        public String kind() {
            return KIND;
        }
    }
}
