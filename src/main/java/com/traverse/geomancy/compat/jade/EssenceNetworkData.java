package com.traverse.geomancy.compat.jade;

import net.minecraft.resources.Identifier;

import com.traverse.geomancy.Geomancy;

final class EssenceNetworkData {
    private EssenceNetworkData() {
    }

    static final Identifier UID = Identifier.fromNamespaceAndPath(Geomancy.MODID, "essence_network");
    static final String ESSENCE = "essence";
    static final String FORM = "form";
    static final String AMOUNT = "amount";
    static final String CAPACITY = "capacity";
    static final String LINKED = "linked";
    static final String LINK_VALID = "link_valid";
    static final String PROGRESS = "progress";
    static final String DURATION = "duration";
}
