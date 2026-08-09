package com.traverse.geomancy.compat.jade;

import net.minecraft.resources.Identifier;

import com.traverse.geomancy.Geomancy;

final class ResonanceStorageData {
    private ResonanceStorageData() {
    }

    static final Identifier UID = Identifier.fromNamespaceAndPath(Geomancy.MODID, "resonance_storage");
    static final String AMOUNT = "resonance";
    static final String CAPACITY = "capacity";
}
