package com.traverse.geomancy.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

// What a recipe accepts as input: either a set of blocks (worked in place) or an item
// ingredient (worked as a dropped item entity). Recipe authors pick one per recipe.
public sealed interface TransmutationSubject {
    Codec<TransmutationSubject> CODEC = Codec.STRING.dispatch(TransmutationSubject::kind, TransmutationSubject::mapCodecFor);

    // Hand-rolled rather than StreamCodec#dispatch: the per-variant codecs below are
    // registry-aware (holderSet/Ingredient), so their buffer type only ever widens from
    // RegistryFriendlyByteBuf - dispatch's own buffer-type variance runs the other way.
    StreamCodec<RegistryFriendlyByteBuf, TransmutationSubject> STREAM_CODEC = StreamCodec.of(
            (buf, subject) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, subject.kind());
                switch (subject) {
                    case OfBlock ofBlock -> OfBlock.STREAM_CODEC.encode(buf, ofBlock);
                    case OfItem ofItem -> OfItem.STREAM_CODEC.encode(buf, ofItem);
                }
            },
            buf -> {
                String kind = ByteBufCodecs.STRING_UTF8.decode(buf);
                return switch (kind) {
                    case OfBlock.KIND -> OfBlock.STREAM_CODEC.decode(buf);
                    case OfItem.KIND -> OfItem.STREAM_CODEC.decode(buf);
                    default -> throw new IllegalArgumentException("Unknown transmutation subject type '" + kind + "'");
                };
            });

    String kind();

    boolean matches(TransmutationTarget target);

    private static MapCodec<? extends TransmutationSubject> mapCodecFor(String kind) {
        return switch (kind) {
            case OfBlock.KIND -> OfBlock.MAP_CODEC;
            case OfItem.KIND -> OfItem.MAP_CODEC;
            default -> throw new IllegalArgumentException("Unknown transmutation subject type '" + kind + "'");
        };
    }

    record OfBlock(HolderSet<Block> blocks) implements TransmutationSubject {
        static final String KIND = "block";
        static final MapCodec<OfBlock> MAP_CODEC =
                RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").xmap(OfBlock::new, OfBlock::blocks);
        static final StreamCodec<RegistryFriendlyByteBuf, OfBlock> STREAM_CODEC =
                ByteBufCodecs.holderSet(Registries.BLOCK).map(OfBlock::new, OfBlock::blocks);

        @Override
        public String kind() {
            return KIND;
        }

        @Override
        public boolean matches(TransmutationTarget target) {
            return target instanceof TransmutationTarget.OfBlock(var state)
                    && blocks.contains(state.getBlock().builtInRegistryHolder());
        }
    }

    record OfItem(Ingredient ingredient) implements TransmutationSubject {
        static final String KIND = "item";
        static final MapCodec<OfItem> MAP_CODEC =
                Ingredient.CODEC.fieldOf("ingredient").xmap(OfItem::new, OfItem::ingredient);
        static final StreamCodec<RegistryFriendlyByteBuf, OfItem> STREAM_CODEC =
                Ingredient.CONTENTS_STREAM_CODEC.map(OfItem::new, OfItem::ingredient);

        @Override
        public String kind() {
            return KIND;
        }

        @Override
        public boolean matches(TransmutationTarget target) {
            return target instanceof TransmutationTarget.OfItem(var stack) && ingredient.test(stack);
        }
    }
}
