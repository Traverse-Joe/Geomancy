package com.traverse.geomancy.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import com.traverse.geomancy.essence.Essence;

public enum EssenceNetworkComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return EssenceNetworkData.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        int amount = data.getIntOr(EssenceNetworkData.AMOUNT, 0);
        int capacity = data.getIntOr(EssenceNetworkData.CAPACITY, 0);

        data.getString(EssenceNetworkData.ESSENCE).flatMap(Essence::byName).ifPresentOrElse(essence -> {
            String form = data.getString(EssenceNetworkData.FORM).orElse("raw");
            tooltip.add(Component.translatable("geomancy.jade.charge",
                    essence.displayName().copy().withColor(essence.color() & 0xFFFFFF),
                    Component.translatable("geomancy.essence_form." + form), amount, capacity));
        }, () -> {
            if (data.contains(EssenceNetworkData.CAPACITY)) {
                tooltip.add(Component.translatable("geomancy.jade.empty", capacity));
            }
        });

        if (data.contains(EssenceNetworkData.LINKED)) {
            boolean linked = data.getBooleanOr(EssenceNetworkData.LINKED, false);
            boolean valid = data.getBooleanOr(EssenceNetworkData.LINK_VALID, false);
            String key = !linked ? "geomancy.jade.link_none"
                    : valid ? "geomancy.jade.link_ok" : "geomancy.jade.link_broken";
            tooltip.add(Component.translatable(key));
        }

        int duration = data.getIntOr(EssenceNetworkData.DURATION, 0);
        if (duration > 0) {
            int percent = Math.round(100.0F * data.getIntOr(EssenceNetworkData.PROGRESS, 0) / duration);
            tooltip.add(Component.translatable("geomancy.jade.transmuting", percent));
        }
    }
}
