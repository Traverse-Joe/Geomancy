package com.traverse.geomancy.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.CuriosSlotTypes;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.registry.ModItems;

public class GeomancyCuriosProvider extends CuriosDataProvider {
    public GeomancyCuriosProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(Geomancy.MODID, output, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries) {
        createEntities(Geomancy.MODID).addPlayer().addPresetSlots(CuriosSlotTypes.Preset.RING);
        tag(CuriosSlotTypes.Preset.RING.id()).add(ModItems.RESONANCE_VESSEL.get(), ModItems.VIBRANIC_RING.get());
    }
}
