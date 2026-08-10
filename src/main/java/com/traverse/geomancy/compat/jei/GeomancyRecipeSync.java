package com.traverse.geomancy.compat.jei;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.registry.ModRecipes;

// Vanilla no longer sends full custom recipe objects to the client, only recipe-book
// display data. JEI needs the real HearthSynthesisRecipe instances, so this explicitly
// requests them via NeoForge's recipe-sync opt-in (see GeomancyClientRecipeSync for the
// client-side receiver).
@EventBusSubscriber(modid = Geomancy.MODID)
public final class GeomancyRecipeSync {
    private GeomancyRecipeSync() {
    }

    @SubscribeEvent
    static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(ModRecipes.HEARTH_SYNTHESIS_TYPE.get());
    }
}
