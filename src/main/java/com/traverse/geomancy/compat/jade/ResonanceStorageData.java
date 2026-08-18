package com.traverse.geomancy.compat.jade;

import net.minecraft.resources.Identifier;

import com.traverse.geomancy.Geomancy;

final class ResonanceStorageData {
    private ResonanceStorageData() {
    }

    static final Identifier UID = Identifier.fromNamespaceAndPath(Geomancy.MODID, "resonance_storage");
    static final String AMOUNT = "resonance";
    static final String CAPACITY = "capacity";
    // Absent rather than empty when the storage holds plain generator output, so the
    // tooltip can tell "untyped" apart from "a type Jade could not read".
    static final String TYPE = "resonance_type";
}
