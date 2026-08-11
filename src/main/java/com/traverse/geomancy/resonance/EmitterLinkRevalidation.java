package com.traverse.geomancy.resonance;

import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.block.ResonanceReceiverBlock;
import com.traverse.geomancy.block.entity.ResonanceEmitterBlockEntity;
import com.traverse.geomancy.registry.ModAttachments;

// A receiver's position is the only state an emitter's binding stores. Obstruction (a
// piston pushing wool into the path) must never clear that binding, but the receiver
// actually being broken is different - there is nothing left to bind to, so any emitter
// still pointing at it needs to forget, or binding mode would keep drawing a link to
// nothing forever.
@EventBusSubscriber(modid = Geomancy.MODID)
public final class EmitterLinkRevalidation {
    private EmitterLinkRevalidation() {
    }

    @SubscribeEvent
    static void onReceiverBroken(BreakBlockEvent event) {
        if (!(event.getState().getBlock() instanceof ResonanceReceiverBlock)
                || !(event.getLevel() instanceof Level level) || level.isClientSide()) {
            return;
        }
        BlockPos brokenPos = event.getPos().immutable();
        LongSet emitters = level.getData(ModAttachments.EMITTER_INDEX).emittersTargeting(brokenPos);
        if (emitters.isEmpty()) {
            return;
        }
        // Copy first: Emitter#clear() unregisters itself, which would mutate this same set
        // mid-iteration.
        for (long packed : emitters.toLongArray()) {
            if (level.getBlockEntity(BlockPos.of(packed)) instanceof ResonanceEmitterBlockEntity emitter) {
                emitter.clear();
            }
        }
    }
}
