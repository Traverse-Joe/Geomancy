package com.traverse.geomancy.essence;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

// Single source of truth for biome -> essence: node tint, reveal outline, transmutation
// laser and the Jade tooltip all read Essence through this class, so they cannot drift.
//
// First-match order, most to least specific, settling CLAUDE.md §6C:
//  1-2  dimension gates (Nether/End) take priority over any overworld family tag
//  3-5  lush/cave/mushroom before the broader family tags they can overlap with
//  6-11 named biome families per §6B (desert->Vitri, ocean->Aqua, etc.)
//  12+  climate families, driest/warmest before coldest so e.g. windswept isn't
//       misread as Cryo and frozen ocean stays Aqua rather than Cryo
//  last Terra is the fallback: mountains, meadow, and anything a datapack adds
public final class BiomeEssence {
    private BiomeEssence() {
    }

    public static Essence at(LevelReader level, BlockPos pos) {
        return of(level.getBiome(pos));
    }

    public static Essence of(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_NETHER)) return Essence.IGNIS;
        if (biome.is(BiomeTags.IS_END)) return Essence.INANIS;

        if (biome.is(Tags.Biomes.IS_LUSH)) return Essence.VITAE;
        if (biome.is(Tags.Biomes.IS_CAVE)) return Essence.INANIS;
        if (biome.is(Tags.Biomes.IS_MUSHROOM)) return Essence.VITAE;

        if (biome.is(Tags.Biomes.IS_DESERT)) return Essence.VITRI;
        if (biome.is(Tags.Biomes.IS_BADLANDS)) return Essence.METALLUM;
        if (biome.is(Tags.Biomes.IS_SAVANNA)) return Essence.METALLUM;

        if (biome.is(Tags.Biomes.IS_OCEAN)) return Essence.AQUA;
        if (biome.is(Tags.Biomes.IS_RIVER)) return Essence.AQUA;
        if (biome.is(Tags.Biomes.IS_BEACH)) return Essence.AQUA;

        if (biome.is(Tags.Biomes.IS_SWAMP)) return Essence.VITAE;
        if (biome.is(Tags.Biomes.IS_JUNGLE)) return Essence.VITAE;

        if (biome.is(Tags.Biomes.IS_WINDSWEPT)) return Essence.MOTUS;
        if (biome.is(Tags.Biomes.IS_SNOWY_PLAINS)) return Essence.CRYO;
        if (biome.is(Tags.Biomes.IS_PLAINS)) return Essence.MOTUS;
        if (biome.is(Tags.Biomes.IS_TAIGA)) return Essence.CRYO;
        if (biome.is(Tags.Biomes.IS_COLD_OVERWORLD)) return Essence.CRYO;

        if (biome.is(Tags.Biomes.IS_FOREST)) return Essence.VITAE;

        return Essence.TERRA;
    }
}
