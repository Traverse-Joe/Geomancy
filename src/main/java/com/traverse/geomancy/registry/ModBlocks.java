package com.traverse.geomancy.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.block.BatteryCrystalBlock;
import com.traverse.geomancy.block.ResonanceEmitterBlock;
import com.traverse.geomancy.block.ResonanceJarBlock;
import com.traverse.geomancy.block.ResonancePillarBlock;
import com.traverse.geomancy.block.ResonanceReceiverBlock;
import com.traverse.geomancy.block.GeodeJarBlock;
import com.traverse.geomancy.block.HearthCrystalBlock;
import com.traverse.geomancy.block.ItemPedestalBlock;
import com.traverse.geomancy.block.PiezoAnvilBlock;
import com.traverse.geomancy.block.ResonantHearthBlock;
import com.traverse.geomancy.block.ResonantBrazierBlock;
import com.traverse.geomancy.block.TectonicNodeBlock;
import com.traverse.geomancy.resonance.BatterySize;

public final class ModBlocks {
    private ModBlocks() {
    }

    public static final DeferredBlock<TectonicNodeBlock> TECTONIC_NODE = Geomancy.BLOCKS.registerBlock(
            "tectonic_node", TectonicNodeBlock::new, p -> p
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3_600_000.0F)
                    .noCollision()
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(TectonicNodeBlock.REVEALED) ? 10 : 0));

    public static final DeferredItem<BlockItem> TECTONIC_NODE_ITEM = Geomancy.ITEMS.registerSimpleBlockItem(TECTONIC_NODE);

    public static final DeferredBlock<ResonancePillarBlock> RESONANCE_PILLAR = Geomancy.BLOCKS.registerBlock(
            "resonance_pillar", ResonancePillarBlock::new, p -> p
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .lightLevel(state -> state.getValue(ResonancePillarBlock.LINKED) ? 7 : 0));

    public static final DeferredItem<BlockItem> RESONANCE_PILLAR_ITEM =
            Geomancy.ITEMS.registerSimpleBlockItem(RESONANCE_PILLAR);

    // Item is custom (ResonanceJarItem, in ModItems) so its stored-essence tooltip and
    // component survive round-tripping through the item form; registered here block-only.
    public static final DeferredBlock<ResonanceJarBlock> RESONANCE_JAR = Geomancy.BLOCKS.registerBlock(
            "resonance_jar", ResonanceJarBlock::new, p -> p
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(1.0F)
                    .noOcclusion());

    public static final DeferredBlock<ItemPedestalBlock> ITEM_PEDESTAL = Geomancy.BLOCKS.registerBlock(
            "item_pedestal", ItemPedestalBlock::new, p -> p
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F, 6.0F)
                    .noOcclusion());

    public static final DeferredItem<BlockItem> ITEM_PEDESTAL_ITEM =
            Geomancy.ITEMS.registerSimpleBlockItem(ITEM_PEDESTAL);

    public static final DeferredBlock<GeodeJarBlock> GEODE_JAR = Geomancy.BLOCKS.registerBlock(
            "geode_jar", GeodeJarBlock::new, p -> p
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.5F)
                    .noOcclusion());

    public static final DeferredBlock<PiezoAnvilBlock> PIEZO_ANVIL = Geomancy.BLOCKS.registerBlock(
            "piezo_anvil", PiezoAnvilBlock::new, p -> p
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 12.0F)
                    .noOcclusion());

    public static final DeferredItem<BlockItem> PIEZO_ANVIL_ITEM = Geomancy.ITEMS.registerSimpleBlockItem(PIEZO_ANVIL);

    public static final DeferredBlock<ResonantHearthBlock> RESONANT_HEARTH = Geomancy.BLOCKS.registerBlock(
            "resonant_hearth", ResonantHearthBlock::new, p -> p
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(4.0F, 10.0F)
                    .noOcclusion());

    public static final DeferredItem<BlockItem> RESONANT_HEARTH_ITEM =
            Geomancy.ITEMS.registerSimpleBlockItem(RESONANT_HEARTH);

    public static final DeferredBlock<ResonantBrazierBlock> RESONANT_BRAZIER = Geomancy.BLOCKS.registerBlock(
            "resonant_brazier", ResonantBrazierBlock::new, p -> p
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 8.0F)
                    .lightLevel(state -> state.getValue(ResonantBrazierBlock.LIT) ? 11 : 0)
                    .noOcclusion());

    public static final DeferredItem<BlockItem> RESONANT_BRAZIER_ITEM =
            Geomancy.ITEMS.registerSimpleBlockItem(RESONANT_BRAZIER);

    public static final DeferredBlock<HearthCrystalBlock> HEARTH_CRYSTAL = Geomancy.BLOCKS.registerBlock(
            "hearth_crystal", HearthCrystalBlock::new, p -> p
                    .mapColor(MapColor.COLOR_PURPLE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .lightLevel(state -> 6)
                    .noOcclusion());

    public static final DeferredItem<BlockItem> HEARTH_CRYSTAL_ITEM =
            Geomancy.ITEMS.registerSimpleBlockItem(HEARTH_CRYSTAL);

    // Item is custom (BatteryCrystalItem, in ModItems) so its stored-resonance tooltip
    // reads the right capacity per size and its component survives round-tripping.
    public static final DeferredBlock<BatteryCrystalBlock> SMALL_BATTERY_CRYSTAL = Geomancy.BLOCKS.registerBlock(
            "small_battery_crystal", p -> new BatteryCrystalBlock(BatterySize.SMALL, p), p -> p
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F)
                    .lightLevel(state -> BatteryCrystalBlock.lightForLevel(state.getValue(BatteryCrystalBlock.FILL_LEVEL)))
                    .noOcclusion());

    public static final DeferredBlock<BatteryCrystalBlock> MEDIUM_BATTERY_CRYSTAL = Geomancy.BLOCKS.registerBlock(
            "medium_battery_crystal", p -> new BatteryCrystalBlock(BatterySize.MEDIUM, p), p -> p
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.5F)
                    .lightLevel(state -> BatteryCrystalBlock.lightForLevel(state.getValue(BatteryCrystalBlock.FILL_LEVEL)))
                    .noOcclusion());

    public static final DeferredBlock<BatteryCrystalBlock> LARGE_BATTERY_CRYSTAL = Geomancy.BLOCKS.registerBlock(
            "large_battery_crystal", p -> new BatteryCrystalBlock(BatterySize.LARGE, p), p -> p
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F)
                    .lightLevel(state -> BatteryCrystalBlock.lightForLevel(state.getValue(BatteryCrystalBlock.FILL_LEVEL)))
                    .noOcclusion());

    public static final DeferredBlock<ResonanceEmitterBlock> RESONANCE_EMITTER = Geomancy.BLOCKS.registerBlock(
            "resonance_emitter", ResonanceEmitterBlock::new, p -> p
                    .mapColor(MapColor.STONE)
                    .strength(1.5F)
                    .noOcclusion());

    public static final DeferredItem<BlockItem> RESONANCE_EMITTER_ITEM =
            Geomancy.ITEMS.registerSimpleBlockItem(RESONANCE_EMITTER);

    public static final DeferredBlock<ResonanceReceiverBlock> RESONANCE_RECEIVER = Geomancy.BLOCKS.registerBlock(
            "resonance_receiver", ResonanceReceiverBlock::new, p -> p
                    .mapColor(MapColor.STONE)
                    .strength(1.5F)
                    .noOcclusion());

    public static final DeferredItem<BlockItem> RESONANCE_RECEIVER_ITEM =
            Geomancy.ITEMS.registerSimpleBlockItem(RESONANCE_RECEIVER);

    // Forces static init before Geomancy.BLOCKS.register(bus) runs.
    public static void bootstrap() {
    }
}
