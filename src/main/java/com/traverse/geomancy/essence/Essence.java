package com.traverse.geomancy.essence;

public enum Essence {
    TERRA(0xFF8B7355),
    METALLUM(0xFFB0B7C6),
    VITRI(0xFF7FD4E8),
    MOTUS(0xFFB8E986),
    IGNIS(0xFFE8663D),
    INANIS(0xFF6B4C9A);

    private final int color;

    Essence(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }
}
