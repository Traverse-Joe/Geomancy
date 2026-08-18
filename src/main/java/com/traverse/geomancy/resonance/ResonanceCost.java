package com.traverse.geomancy.resonance;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.ExtraCodecs;

import com.traverse.geomancy.registry.ModRegistries;

/**
 * A resonance spend embedded in a recipe. An omitted type accepts resonance of any type,
 * which is how existing untyped-only recipe JSON stays valid.
 */
public record ResonanceCost(Optional<Holder<ResonanceType>> type, int amount) {
    public static final Codec<ResonanceCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryFixedCodec.create(ModRegistries.RESONANCE_TYPE).optionalFieldOf("type").forGetter(ResonanceCost::type),
            ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(ResonanceCost::amount)
    ).apply(instance, ResonanceCost::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonanceCost> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.holderRegistry(ModRegistries.RESONANCE_TYPE)), ResonanceCost::type,
            ByteBufCodecs.VAR_INT, ResonanceCost::amount,
            ResonanceCost::new);

    public static ResonanceCost any(int amount) {
        return new ResonanceCost(Optional.empty(), amount);
    }

    public boolean accepts(Optional<Holder<ResonanceType>> suppliedType) {
        return type.isEmpty() || type.equals(suppliedType);
    }

    // A recipe with no type accepts anything, including plain generator output. A recipe
    // that names one only matches storage already holding that type.
    public boolean accepts(@Nullable ResourceKey<ResonanceType> suppliedType) {
        return type.isEmpty() || type.flatMap(Holder::unwrapKey).filter(key -> key.equals(suppliedType)).isPresent();
    }

    public Optional<ResourceKey<ResonanceType>> typeKey() {
        return type.flatMap(Holder::unwrapKey);
    }
}
