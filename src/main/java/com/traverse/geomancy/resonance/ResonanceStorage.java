package com.traverse.geomancy.resonance;

public interface ResonanceStorage {
    int resonance();

    int capacity();

    int insertResonance(int amount, boolean simulate);

    int extractResonance(int amount, boolean simulate);
}
