package com.traverse.geomancy.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import com.traverse.geomancy.block.entity.ResonancePedestalBlockEntity;
import com.traverse.geomancy.item.ResonanceVesselItem;

// Separate from ResonanceStorageDataProvider: that one reports the pedestal's own small
// relay buffer, this one reports the held Resonance Vessel's actual charge - the number the
// player is really watching while a Vessel sits charging on the pedestal.
public enum PedestalChargeDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return PedestalChargeData.UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof ResonancePedestalBlockEntity pedestal)) {
            return;
        }
        ItemStack held = pedestal.held();
        if (held.getItem() instanceof ResonanceVesselItem) {
            data.putBoolean(PedestalChargeData.CHARGING, true);
            data.putInt(PedestalChargeData.STORED, ResonanceVesselItem.stored(held));
            data.putInt(PedestalChargeData.CAPACITY, ResonanceVesselItem.CAPACITY);
        }
    }
}
