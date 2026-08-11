package com.traverse.geomancy.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.resonance.ResonanceType;

public final class ModRegistries {
    private ModRegistries() {
    }

    public static final ResourceKey<Registry<ResonanceType>> RESONANCE_TYPE =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Geomancy.MODID, "resonance_type"));

    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        // Networked with the same codec: clients need color and pitch to render links and particles.
        event.dataPackRegistry(RESONANCE_TYPE, ResonanceType.DIRECT_CODEC, ResonanceType.DIRECT_CODEC);
    }
}
