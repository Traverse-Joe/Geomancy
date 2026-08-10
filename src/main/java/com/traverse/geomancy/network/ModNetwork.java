package com.traverse.geomancy.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.client.GeodeReveal;

@EventBusSubscriber(modid = Geomancy.MODID)
public final class ModNetwork {
    private static final String VERSION = "1";

    private ModNetwork() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(VERSION).playToClient(GeodeRevealPayload.TYPE, GeodeRevealPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> GeodeReveal.accept(payload.positions())));
    }
}
