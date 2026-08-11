package com.traverse.geomancy.resonance;

// Size is the power axis: it sets capacity and also the emitter pulse size and reach fed
// from a battery of that size.
public enum BatterySize {
    SMALL(2_000, 25, 8),
    MEDIUM(10_000, 100, 16),
    LARGE(50_000, 400, 24);

    private final int capacity;
    private final int pulse;
    private final int reach;

    BatterySize(int capacity, int pulse, int reach) {
        this.capacity = capacity;
        this.pulse = pulse;
        this.reach = reach;
    }

    public int capacity() {
        return capacity;
    }

    public int pulse() {
        return pulse;
    }

    public int reach() {
        return reach;
    }
}
