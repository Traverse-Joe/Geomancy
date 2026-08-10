package com.traverse.geomancy.item;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import com.traverse.geomancy.Geomancy;

@EventBusSubscriber(modid = Geomancy.MODID)
public final class WayfinderInterruptEvents {
    private WayfinderInterruptEvents() {
    }

    @SubscribeEvent
    static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.isUsingItem() && entity.getUseItem().getItem() instanceof ResonantWayfinderItem) {
            entity.stopUsingItem();
        }
    }
}
