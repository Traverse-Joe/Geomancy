package com.traverse.geomancy.worldgen;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class TemplateFeature extends Feature<TemplateFeatureConfiguration> {
    public TemplateFeature(Codec<TemplateFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<TemplateFeatureConfiguration> context) {
        TemplateFeatureConfiguration config = context.config();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();

        Optional<StructureTemplate> found = level.getLevel().getStructureManager().get(config.template());
        if (found.isEmpty()) {
            return false;
        }
        StructureTemplate template = found.get();

        Rotation rotation = config.randomRotation() ? Rotation.getRandom(random) : Rotation.NONE;
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(true)
                .setRandom(random);
        if (config.ignoreAir()) {
            settings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        }

        // Centre the footprint on the feature position so the origin column, not a
        // corner, is what the rarity/heightmap placement actually picked.
        Vec3i size = template.getSize(rotation);
        BlockPos origin = context.origin().offset(-size.getX() / 2, 0, -size.getZ() / 2);
        settings.setRotationPivot(new BlockPos(size.getX() / 2, 0, size.getZ() / 2));

        return template.placeInWorld(level, origin, origin, settings, random, Block.UPDATE_CLIENTS);
    }
}
