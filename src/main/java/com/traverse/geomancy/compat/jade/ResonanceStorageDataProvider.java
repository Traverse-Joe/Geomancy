package com.traverse.geomancy.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import com.traverse.geomancy.resonance.ResonanceStorage;

public enum ResonanceStorageDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return ResonanceStorageData.UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ResonanceStorage storage) {
            data.putInt(ResonanceStorageData.AMOUNT, storage.resonance());
            data.putInt(ResonanceStorageData.CAPACITY, storage.capacity());
        }
    }
}
