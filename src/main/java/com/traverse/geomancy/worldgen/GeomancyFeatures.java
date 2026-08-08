package com.traverse.geomancy.worldgen;

import java.util.List;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.registry.ModFeatures;

public final class GeomancyFeatures {
    private GeomancyFeatures() {
    }

    public static final ResourceKey<ConfiguredFeature<?, ?>> TECTONIC_NODE_CONFIGURED = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Geomancy.MODID, "tectonic_node"));

    public static final ResourceKey<PlacedFeature> TECTONIC_NODE_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Geomancy.MODID, "tectonic_node"));

    public static final ResourceKey<BiomeModifier> TECTONIC_NODE_BIOME_MODIFIER = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS, Identifier.fromNamespaceAndPath(Geomancy.MODID, "tectonic_node"));

    public static final Identifier TECTONIC_ALTAR_TEMPLATE =
            Identifier.fromNamespaceAndPath(Geomancy.MODID, "tectonic_altar");

    public static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(TECTONIC_NODE_CONFIGURED, new ConfiguredFeature<>(ModFeatures.TEMPLATE.get(),
                new TemplateFeatureConfiguration(TECTONIC_ALTAR_TEMPLATE, true, true)));
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(TECTONIC_NODE_PLACED, new PlacedFeature(
                configuredFeatures.getOrThrow(TECTONIC_NODE_CONFIGURED),
                List.of(
                        RarityFilter.onAverageOnceEvery(16),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        BiomeFilter.biome())));
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        context.register(TECTONIC_NODE_BIOME_MODIFIER, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(TECTONIC_NODE_PLACED)),
                GenerationStep.Decoration.SURFACE_STRUCTURES));
    }
}
