package com.traverse.geomancy.compat.curios;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import com.traverse.geomancy.Geomancy;

@EventBusSubscriber(modid = Geomancy.MODID)
public final class CuriosVesselRegistration {
    private CuriosVesselRegistration() {
    }

    @SubscribeEvent
    static void setup(FMLCommonSetupEvent event) {
        if (CuriosCompat.isLoaded()) {
            event.enqueueWork(CuriosVessels::registerCurioItem);
        }
    }
}
