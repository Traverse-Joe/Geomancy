package com.traverse.geomancy.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import com.traverse.geomancy.block.entity.EssenceWorkerBlockEntity;
import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;
import com.traverse.geomancy.block.entity.ItemPedestalBlockEntity;
import com.traverse.geomancy.essence.EssenceCharge;
import com.traverse.geomancy.essence.EssenceForm;
import com.traverse.geomancy.network.EssenceProvider;
import com.traverse.geomancy.network.EssenceRelay;

public enum EssenceNetworkDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return EssenceNetworkData.UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        var blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof EssenceProvider provider) {
            EssenceCharge charge = provider.charge();
            data.putInt(EssenceNetworkData.AMOUNT, charge.amount());
            data.putInt(EssenceNetworkData.CAPACITY, provider.capacity());
            if (!charge.isEmpty()) {
                data.putString(EssenceNetworkData.ESSENCE, charge.essence().getSerializedName());
                data.putString(EssenceNetworkData.FORM, charge.form().getSerializedName());
            } else if (blockEntity instanceof TectonicNodeBlockEntity node) {
                data.putString(EssenceNetworkData.ESSENCE, node.essence().getSerializedName());
                data.putString(EssenceNetworkData.FORM, EssenceForm.RAW.getSerializedName());
            }
        }
        if (blockEntity instanceof EssenceRelay relay) {
            data.putBoolean(EssenceNetworkData.LINKED, relay.upstream() != null);
            data.putBoolean(EssenceNetworkData.LINK_VALID, relay.linkValid());
        }
        if (blockEntity instanceof EssenceWorkerBlockEntity worker && worker.hasJob()) {
            data.putInt(EssenceNetworkData.PROGRESS, worker.jobProgress());
            data.putInt(EssenceNetworkData.DURATION, worker.jobDuration());
        } else if (blockEntity instanceof ItemPedestalBlockEntity pedestal && pedestal.isProcessing()) {
            data.putInt(EssenceNetworkData.PROGRESS, pedestal.progress());
            data.putInt(EssenceNetworkData.DURATION, pedestal.duration());
        }
    }
}
