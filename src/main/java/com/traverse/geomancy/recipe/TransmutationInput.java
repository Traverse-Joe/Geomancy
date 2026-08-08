package com.traverse.geomancy.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.state.BlockState;

import com.traverse.geomancy.essence.Essence;

public record TransmutationInput(BlockState state, Essence essence) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }

    // RecipeMap#getRecipesFor short-circuits to an empty stream for "empty" inputs,
    // which a zero-item input is by default. Without this, nothing ever matches.
    @Override
    public boolean isEmpty() {
        return false;
    }
}
