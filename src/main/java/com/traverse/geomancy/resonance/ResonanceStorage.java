package com.traverse.geomancy.resonance;

public interface ResonanceStorage {
    int resonance();

    int capacity();

    int insertResonance(int amount, boolean simulate);

    int extractResonance(int amount, boolean simulate);

    // The power axis an emitter reads from whatever storage feeds it. Non-battery storage
    // (the Geode Jar, the Hearth's own draw) behaves like a small battery by default.
    default int pulseSize() {
        return BatterySize.SMALL.pulse();
    }

    default int emitterReach() {
        return BatterySize.SMALL.reach();
    }
}
