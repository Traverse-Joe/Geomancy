package com.traverse.geomancy.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import com.traverse.geomancy.essence.Essence;
import com.traverse.geomancy.essence.EssenceForm;

public record TransmutationInput(TransmutationTarget target, Essence essence, EssenceForm form) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return target instanceof TransmutationTarget.OfItem(ItemStack stack) ? stack : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return target instanceof TransmutationTarget.OfItem ? 1 : 0;
    }

    // RecipeMap#getRecipesFor short-circuits to an empty stream for "empty" inputs,
    // which a zero-item block target is by default. Without this, nothing ever matches.
    @Override
    public boolean isEmpty() {
        return false;
    }
}
