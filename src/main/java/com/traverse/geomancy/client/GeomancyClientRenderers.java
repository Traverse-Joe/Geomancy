package com.traverse.geomancy.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.registry.ModBlockEntities;

@EventBusSubscriber(modid = Geomancy.MODID, value = Dist.CLIENT)
public final class GeomancyClientRenderers {
    private GeomancyClientRenderers() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TECTONIC_NODE.get(), TectonicNodeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RESONANCE_PILLAR.get(), EssenceLinkRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RESONANCE_JAR.get(), EssenceLinkRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ITEM_PEDESTAL.get(), ItemPedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RESONANT_HEARTH.get(), ResonantHearthRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RESONANT_BRAZIER.get(), ResonantBrazierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GEODE_JAR.get(), GeodeJarRenderer::new);
    }
}
