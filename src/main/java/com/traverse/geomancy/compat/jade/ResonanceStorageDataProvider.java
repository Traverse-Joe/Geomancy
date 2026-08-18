package com.traverse.geomancy.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import com.traverse.geomancy.resonance.ResonanceStorage;
import com.traverse.geomancy.resonance.ResonanceType;

public enum ResonanceStorageDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return ResonanceStorageData.UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof ResonanceStorage storage)) {
            return;
        }
        data.putInt(ResonanceStorageData.AMOUNT, storage.resonance());
        data.putInt(ResonanceStorageData.CAPACITY, storage.capacity());
        ResourceKey<ResonanceType> type = storage.resonanceType();
        if (type != null) {
            data.putString(ResonanceStorageData.TYPE, type.identifier().toString());
        }
    }
}
