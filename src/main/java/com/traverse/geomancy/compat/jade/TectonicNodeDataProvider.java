package com.traverse.geomancy.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;

public enum TectonicNodeDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return NodeData.UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof TectonicNodeBlockEntity node) {
            data.putString(NodeData.ESSENCE, node.essence().name());
            if (node.hasJob()) {
                data.putInt(NodeData.PROGRESS, node.jobProgress());
                data.putInt(NodeData.DURATION, node.jobDuration());
            }
        }
    }
}
