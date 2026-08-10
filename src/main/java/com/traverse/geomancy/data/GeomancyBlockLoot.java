package com.traverse.geomancy.data;

import java.util.List;
import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import com.traverse.geomancy.registry.ModBlocks;
import com.traverse.geomancy.registry.ModDataComponents;
import com.traverse.geomancy.registry.ModItems;

public class GeomancyBlockLoot extends BlockLootSubProvider {
    public GeomancyBlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    // The base class's default iterates every block in the game and throws for any that
    // has a loot table this provider didn't build, so it must be scoped to our own blocks.
    // tectonic_node is deliberately excluded: it is unbreakable in survival and uses
    // noLootTable(), so it has no loot table at all to generate.
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(ModBlocks.RESONANCE_PILLAR.get(), ModBlocks.RESONANCE_JAR.get(), ModBlocks.ITEM_PEDESTAL.get(),
                ModBlocks.GEODE_JAR.get(), ModBlocks.PIEZO_ANVIL.get(), ModBlocks.RESONANT_HEARTH.get(),
                ModBlocks.RESONANT_BRAZIER.get(), ModBlocks.HEARTH_CRYSTAL.get());
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.RESONANCE_PILLAR.get());
        dropSelf(ModBlocks.ITEM_PEDESTAL.get());
        dropSelf(ModBlocks.PIEZO_ANVIL.get());
        dropSelf(ModBlocks.RESONANT_HEARTH.get());
        dropSelf(ModBlocks.RESONANT_BRAZIER.get());
        dropSelf(ModBlocks.HEARTH_CRYSTAL.get());

        Block jar = ModBlocks.RESONANCE_JAR.get();
        add(jar, LootTable.lootTable().withPool(applyExplosionCondition(jar,
                LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(
                        LootItem.lootTableItem(ModItems.RESONANCE_JAR.get()).apply(
                                CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                        .include(ModDataComponents.STORED_ESSENCE.get()))))));

        Block geodeJar = ModBlocks.GEODE_JAR.get();
        add(geodeJar, LootTable.lootTable().withPool(applyExplosionCondition(geodeJar,
                LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(
                        LootItem.lootTableItem(ModItems.GEODE_JAR.get()).apply(
                                CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                        .include(ModDataComponents.STORED_RESONANCE.get()))))));
    }
}
