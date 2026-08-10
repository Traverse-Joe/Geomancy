package com.traverse.geomancy.compat.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.recipe.HearthSynthesisRecipe;
import com.traverse.geomancy.registry.ModBlocks;
import com.traverse.geomancy.registry.ModRecipes;

@JeiPlugin
public class GeomancyJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Geomancy.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new HearthSynthesisCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<HearthSynthesisRecipe>> recipes = new ArrayList<>(
                GeomancyClientRecipeSync.syncedRecipes().byType(ModRecipes.HEARTH_SYNTHESIS_TYPE.get()));
        registration.addRecipes(HearthSynthesisCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(HearthSynthesisCategory.RECIPE_TYPE, ModBlocks.RESONANT_HEARTH_ITEM.get());
    }
}
