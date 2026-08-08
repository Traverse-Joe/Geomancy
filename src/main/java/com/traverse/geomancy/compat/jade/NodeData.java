package com.traverse.geomancy.compat.jade;

import net.minecraft.resources.Identifier;

import com.traverse.geomancy.Geomancy;

final class NodeData {
    private NodeData() {
    }

    // Jade requires the server data provider and the client component provider to be
    // separate classes, so the keys they agree on live here.
    static final Identifier UID = Identifier.fromNamespaceAndPath(Geomancy.MODID, "tectonic_node");
    static final String ESSENCE = "essence";
    static final String PROGRESS = "progress";
    static final String DURATION = "duration";
}
