package com.traverse.geomancy.resonance;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;

import net.minecraft.core.BlockPos;

// Server-side, per-level, rebuilt from BlockEntity lifecycle rather than persisted: target
// position -> the emitters currently bound to it. Lets breaking a receiver clear the
// binding on whatever emitter pointed at it, instead of leaving a stale target that would
// otherwise still draw a binding-mode link line to nothing.
public final class EmitterIndex {
    private final Long2ObjectMap<LongSet> byTarget = new Long2ObjectOpenHashMap<>();

    public void register(BlockPos emitter, BlockPos target) {
        byTarget.computeIfAbsent(target.asLong(), key -> new LongOpenHashSet()).add(emitter.asLong());
    }

    public void unregister(BlockPos emitter, BlockPos target) {
        LongSet emitters = byTarget.get(target.asLong());
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter.asLong());
        if (emitters.isEmpty()) {
            byTarget.remove(target.asLong());
        }
    }

    public LongSet emittersTargeting(BlockPos target) {
        LongSet emitters = byTarget.get(target.asLong());
        return emitters == null ? LongSets.EMPTY_SET : emitters;
    }
}
