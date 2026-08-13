package com.traverse.geomancy.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PedestalChargeComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return PedestalChargeData.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.getBooleanOr(PedestalChargeData.CHARGING, false)) {
            return;
        }
        tooltip.add(Component.translatable("geomancy.jade.pedestal_charging",
                data.getIntOr(PedestalChargeData.STORED, 0),
                data.getIntOr(PedestalChargeData.CAPACITY, 0)));
    }
}
