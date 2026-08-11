package com.traverse.geomancy.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import com.traverse.geomancy.Geomancy;

public final class ModTags {
    private ModTags() {
    }

    // No type resists a muffler, ender included: absorbed completely wherever it appears
    // in a resonance path, without breaking the emitter's binding.
    public static final TagKey<Block> RESONANCE_MUFFLER =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Geomancy.MODID, "resonance_muffler"));
}
