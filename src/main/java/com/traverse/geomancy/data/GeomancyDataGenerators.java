package com.traverse.geomancy.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.worldgen.GeomancyFeatures;

@EventBusSubscriber(modid = Geomancy.MODID)
public final class GeomancyDataGenerators {
    private GeomancyDataGenerators() {
    }

    @SubscribeEvent
    static void gatherData(GatherDataEvent.Client event) {
        event.addProvider(new GeomancyModelProvider(event.getGenerator().getPackOutput()));
        event.createProvider(GeomancyRecipeProvider.Runner::new);

        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, GeomancyFeatures::bootstrapConfigured)
                .add(Registries.PLACED_FEATURE, GeomancyFeatures::bootstrapPlaced)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, GeomancyFeatures::bootstrapBiomeModifiers));
    }
}
