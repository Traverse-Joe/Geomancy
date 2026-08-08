package com.traverse.geomancy.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.traverse.geomancy.Geomancy;

public final class ModDataComponents {
    private ModDataComponents() {
    }

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Geomancy.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> SELECTED_NODE =
            DATA_COMPONENTS.registerComponentType("selected_node", b -> b
                    .persistent(BlockPos.CODEC)
                    .networkSynchronized(BlockPos.STREAM_CODEC));

    public static void bootstrap() {
    }
}
