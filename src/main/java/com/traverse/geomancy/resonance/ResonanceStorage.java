package com.traverse.geomancy.resonance;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.ResourceKey;

public interface ResonanceStorage {
    int resonance();

    int capacity();

    // Null while empty, or while holding plain generator output. Storage carries one type
    // at a time; see TypedResonancePool for the locking rule.
    default @Nullable ResourceKey<ResonanceType> resonanceType() {
        return null;
    }

    int insertResonance(@Nullable ResourceKey<ResonanceType> type, int amount, boolean simulate);

    default int insertResonance(int amount, boolean simulate) {
        return insertResonance(null, amount, simulate);
    }

    int extractResonance(int amount, boolean simulate);

    // Resolved tint for this storage's contents, so renderers never touch the registry.
    default int resonanceColor() {
        return ResonanceTypes.UNTYPED_COLOR;
    }

    // The power axis an emitter reads from whatever storage feeds it. Non-battery storage
    // (the Geode Jar, the Hearth's own draw) behaves like a small battery by default.
    default int pulseSize() {
        return BatterySize.SMALL.pulse();
    }

    default int emitterReach() {
        return BatterySize.SMALL.reach();
    }
}
