package com.traverse.geomancy.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

import com.traverse.geomancy.registry.ModBlocks;

// Without a mineable tag, a block declaring requiresCorrectToolForDrops() can never be
// mined correctly by anything and so drops nothing at all, however long it is hit for.
// Every breakable block here is stone or crystal, so pickaxe is the tool for all of them.
//
// tectonic_node is excluded deliberately: it is unbreakable and has no loot table, so
// listing it as mineable would advertise an interaction that cannot happen.
public class GeomancyBlockTagsProvider extends IntrinsicHolderTagsProvider<Block> {
    public GeomancyBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Registries.BLOCK, registries, block -> block.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ModBlocks.RESONANCE_PILLAR.get(),
                ModBlocks.RESONANCE_JAR.get(),
                ModBlocks.ITEM_PEDESTAL.get(),
                ModBlocks.GEODE_JAR.get(),
                ModBlocks.PIEZO_ANVIL.get(),
                ModBlocks.RESONANT_HEARTH.get(),
                ModBlocks.RESONANT_BRAZIER.get(),
                ModBlocks.HEARTH_CRYSTAL.get(),
                ModBlocks.SMALL_BATTERY_CRYSTAL.get(),
                ModBlocks.MEDIUM_BATTERY_CRYSTAL.get(),
                ModBlocks.LARGE_BATTERY_CRYSTAL.get(),
                ModBlocks.RESONANCE_EMITTER.get(),
                ModBlocks.RESONANCE_RECEIVER.get(),
                ModBlocks.RESONANCE_PEDESTAL.get());

        // Only the blocks that actually declare requiresCorrectToolForDrops() get a tier.
        // Jars, batteries, emitters and receivers are deliberately hand-breakable: they are
        // placed and repositioned constantly while a build is being tuned.
        tag(BlockTags.NEEDS_STONE_TOOL).add(
                ModBlocks.RESONANCE_PILLAR.get(),
                ModBlocks.ITEM_PEDESTAL.get(),
                ModBlocks.RESONANT_HEARTH.get(),
                ModBlocks.RESONANT_BRAZIER.get(),
                ModBlocks.HEARTH_CRYSTAL.get(),
                ModBlocks.RESONANCE_PEDESTAL.get());

        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.PIEZO_ANVIL.get());
    }
}
