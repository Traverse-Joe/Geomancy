package com.traverse.geomancy.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.entity.ResonantWaveProjectile;

public final class ModEntities {
    private ModEntities() {
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Geomancy.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ResonantWaveProjectile>> RESONANT_WAVE =
            ENTITY_TYPES.register("resonant_wave", id -> EntityType.Builder
                    .<ResonantWaveProjectile>of(ResonantWaveProjectile::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static void bootstrap() {
    }
}
