package com.traverse.geomancy.registry;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import com.traverse.geomancy.Geomancy;

// Exposes block entities to hoppers and modded item pipes alike, since NeoForge's patched
// vanilla Hopper checks Capabilities.Item.BLOCK before falling back to Container.
@EventBusSubscriber(modid = Geomancy.MODID)
public final class ModCapabilities {
    private ModCapabilities() {
    }

    @SubscribeEvent
    static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.RESONANT_BRAZIER.get(),
                (brazier, side) -> brazier.itemHandler());
    }
}
