package com.traverse.geomancy.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.block.TectonicNodeBlock;

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

    // Forces static init before Geomancy.BLOCKS.register(bus) runs.
    public static void bootstrap() {
    }
}
