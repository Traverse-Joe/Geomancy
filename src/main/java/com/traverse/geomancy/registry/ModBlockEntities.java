package com.traverse.geomancy.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.block.entity.ResonanceJarBlockEntity;
import com.traverse.geomancy.block.entity.ResonancePillarBlockEntity;
import com.traverse.geomancy.block.entity.GeodeJarBlockEntity;
import com.traverse.geomancy.block.entity.ItemPedestalBlockEntity;
import com.traverse.geomancy.block.entity.ResonantHearthBlockEntity;
import com.traverse.geomancy.block.entity.ResonantBrazierBlockEntity;
import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;

public final class ModBlockEntities {
    private ModBlockEntities() {
    }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Geomancy.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TectonicNodeBlockEntity>> TECTONIC_NODE =
            BLOCK_ENTITIES.register("tectonic_node", () -> new BlockEntityType<>(
                    TectonicNodeBlockEntity::new, ModBlocks.TECTONIC_NODE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonancePillarBlockEntity>> RESONANCE_PILLAR =
            BLOCK_ENTITIES.register("resonance_pillar", () -> new BlockEntityType<>(
                    ResonancePillarBlockEntity::new, ModBlocks.RESONANCE_PILLAR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonanceJarBlockEntity>> RESONANCE_JAR =
            BLOCK_ENTITIES.register("resonance_jar", () -> new BlockEntityType<>(
                    ResonanceJarBlockEntity::new, ModBlocks.RESONANCE_JAR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPedestalBlockEntity>> ITEM_PEDESTAL =
            BLOCK_ENTITIES.register("item_pedestal", () -> new BlockEntityType<>(
                    ItemPedestalBlockEntity::new, ModBlocks.ITEM_PEDESTAL.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeodeJarBlockEntity>> GEODE_JAR =
            BLOCK_ENTITIES.register("geode_jar", () -> new BlockEntityType<>(
                    GeodeJarBlockEntity::new, ModBlocks.GEODE_JAR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonantHearthBlockEntity>> RESONANT_HEARTH =
            BLOCK_ENTITIES.register("resonant_hearth", () -> new BlockEntityType<>(
                    ResonantHearthBlockEntity::new, ModBlocks.RESONANT_HEARTH.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonantBrazierBlockEntity>> RESONANT_BRAZIER =
            BLOCK_ENTITIES.register("resonant_brazier", () -> new BlockEntityType<>(
                    ResonantBrazierBlockEntity::new, ModBlocks.RESONANT_BRAZIER.get()));

    public static void bootstrap() {
    }
}
