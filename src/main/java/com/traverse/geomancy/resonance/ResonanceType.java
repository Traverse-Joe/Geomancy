package com.traverse.geomancy.resonance;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * A datapack-defined character resonance takes on while in flight through an Attuned Crystal.
 * Never stored: {@link ResonanceStorage} holds plain integers, and a type only exists between
 * an Attuned Crystal's emitter and wherever its beam lands.
 */
public record ResonanceType(int color, float pitch, float rangeMultiplier, float dampeningResistance,
                             float furnaceSpeed, Optional<TagKey<Biome>> sourceBiomes) {
    public static final Codec<ResonanceType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("color").forGetter(ResonanceType::color),
            Codec.FLOAT.optionalFieldOf("pitch", 1.0F).forGetter(ResonanceType::pitch),
            Codec.FLOAT.optionalFieldOf("range_multiplier", 1.0F).forGetter(ResonanceType::rangeMultiplier),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("dampening_resistance", 0.0F).forGetter(ResonanceType::dampeningResistance),
            Codec.FLOAT.optionalFieldOf("furnace_speed", 0.0F).forGetter(ResonanceType::furnaceSpeed),
            TagKey.hashedCodec(Registries.BIOME).optionalFieldOf("source_biomes").forGetter(ResonanceType::sourceBiomes)
    ).apply(instance, ResonanceType::new));
}
