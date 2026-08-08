package com.traverse.geomancy.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;

public final class ModBlockEntities {
    private ModBlockEntities() {
    }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Geomancy.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TectonicNodeBlockEntity>> TECTONIC_NODE =
            BLOCK_ENTITIES.register("tectonic_node", () -> new BlockEntityType<>(
                    TectonicNodeBlockEntity::new, ModBlocks.TECTONIC_NODE.get()));

    public static void bootstrap() {
    }
}
