package com.traverse.geomancy.network;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.registry.ModAttachments;

// Marks affected relays dirty; the actual raycast is deferred to the relay's own next
// transfer tick (see EssenceRelay#markLinkDirty). This matters beyond being cheap:
// BreakBlockEvent fires before the block is actually removed, so a raycast run inside
// this handler would still see the doomed block and wrongly report the link as clear.
@EventBusSubscriber(modid = Geomancy.MODID)
public final class LinkRevalidation {
    private LinkRevalidation() {
    }

    @SubscribeEvent
    static void onBreak(BreakBlockEvent event) {
        invalidate(event);
    }

    @SubscribeEvent
    static void onPlace(BlockEvent.EntityPlaceEvent event) {
        invalidate(event);
    }

    private static void invalidate(BlockEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            level.getData(ModAttachments.LINK_INDEX).invalidate(level, event.getPos());
        }
    }
}
