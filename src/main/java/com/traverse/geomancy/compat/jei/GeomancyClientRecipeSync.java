package com.traverse.geomancy.compat.jei;

import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import com.traverse.geomancy.Geomancy;

@EventBusSubscriber(modid = Geomancy.MODID, value = Dist.CLIENT)
public final class GeomancyClientRecipeSync {
    private static RecipeMap syncedRecipes = RecipeMap.EMPTY;

    private GeomancyClientRecipeSync() {
    }

    @SubscribeEvent
    static void onRecipesReceived(RecipesReceivedEvent event) {
        syncedRecipes = event.getRecipeMap();
    }

    static RecipeMap syncedRecipes() {
        return syncedRecipes;
    }
}
