package com.traverse.geomancy.essence;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

public final class BiomeEssence {
    private BiomeEssence() {
    }

    public static Essence at(LevelReader level, BlockPos pos) {
        return of(level.getBiome(pos));
    }

    public static Essence of(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_NETHER)) return Essence.IGNIS;
        if (biome.is(BiomeTags.IS_END)) return Essence.INANIS;
        if (biome.is(BiomeTags.IS_OCEAN)) return Essence.VITRI;
        if (biome.is(BiomeTags.IS_FOREST)) return Essence.MOTUS;
        if (biome.is(BiomeTags.IS_BADLANDS)) return Essence.METALLUM;
        return Essence.TERRA;
    }
}
