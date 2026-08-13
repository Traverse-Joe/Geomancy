package com.traverse.geomancy.compat.jade;

import net.minecraft.resources.Identifier;

import com.traverse.geomancy.Geomancy;

final class PedestalChargeData {
    private PedestalChargeData() {
    }

    static final Identifier UID = Identifier.fromNamespaceAndPath(Geomancy.MODID, "pedestal_charge");
    static final String CHARGING = "charging";
    static final String STORED = "stored";
    static final String CAPACITY = "capacity";
}
