package com.traverse.geomancy.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record TemplateFeatureConfiguration(Identifier template, boolean randomRotation, boolean ignoreAir)
        implements FeatureConfiguration {

    public static final Codec<TemplateFeatureConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("template").forGetter(TemplateFeatureConfiguration::template),
            Codec.BOOL.optionalFieldOf("random_rotation", true).forGetter(TemplateFeatureConfiguration::randomRotation),
            Codec.BOOL.optionalFieldOf("ignore_air", true).forGetter(TemplateFeatureConfiguration::ignoreAir)
    ).apply(i, TemplateFeatureConfiguration::new));
}
