package com.traverse.geomancy.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.essence.EssenceCharge;
import com.traverse.geomancy.wayfinder.WayfinderAnchor;

public final class ModDataComponents {
    private ModDataComponents() {
    }

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Geomancy.MODID);

    // Holds the position of the node/pillar/jar most recently attuned by the tuning
    // hammer, so a subsequent click can link it to a relay or start a job on it.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> LINKED_SOURCE =
            DATA_COMPONENTS.registerComponentType("linked_source", b -> b
                    .persistent(BlockPos.CODEC)
                    .networkSynchronized(BlockPos.STREAM_CODEC));

    // The refined essence a Resonance Jar carries, so breaking it and re-placing it
    // elsewhere does not lose its contents.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EssenceCharge>> STORED_ESSENCE =
            DATA_COMPONENTS.registerComponentType("stored_essence", b -> b
                    .persistent(EssenceCharge.CODEC)
                    .networkSynchronized(EssenceCharge.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> LOGISTICS_MODE =
            DATA_COMPONENTS.registerComponentType("logistics_mode", b -> b
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_RESONANCE =
            DATA_COMPONENTS.registerComponentType("stored_resonance", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    // The Hearth Crystal a Resonant Wayfinder is bound to. The crystal itself stores
    // nothing; presence at this position is re-checked live at recall time.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WayfinderAnchor>> WAYFINDER_ANCHOR =
            DATA_COMPONENTS.registerComponentType("wayfinder_anchor", b -> b
                    .persistent(WayfinderAnchor.CODEC)
                    .networkSynchronized(WayfinderAnchor.STREAM_CODEC));

    public static void bootstrap() {
    }
}
