package com.traverse.geomancy.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

// What a job is actually running against, at match time. Never serialized -
// constructed fresh from the block or item entity the worker is acting on.
public sealed interface TransmutationTarget {
    record OfBlock(BlockState state) implements TransmutationTarget {
    }

    record OfItem(ItemStack stack) implements TransmutationTarget {
    }
}
