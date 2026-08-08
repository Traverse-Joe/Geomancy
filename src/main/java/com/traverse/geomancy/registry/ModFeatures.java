package com.traverse.geomancy.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.worldgen.TemplateFeature;
import com.traverse.geomancy.worldgen.TemplateFeatureConfiguration;

public final class ModFeatures {
    private ModFeatures() {
    }

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Geomancy.MODID);

    public static final DeferredHolder<Feature<?>, TemplateFeature> TEMPLATE =
            FEATURES.register("template", () -> new TemplateFeature(TemplateFeatureConfiguration.CODEC));

    public static void bootstrap() {
    }
}
