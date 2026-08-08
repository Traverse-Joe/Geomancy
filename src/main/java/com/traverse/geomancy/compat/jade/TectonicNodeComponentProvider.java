package com.traverse.geomancy.compat.jade;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import com.traverse.geomancy.essence.Essence;

public enum TectonicNodeComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return NodeData.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        Essence essence = data.getString(NodeData.ESSENCE)
                .map(TectonicNodeComponentProvider::parseEssence)
                .orElse(Essence.TERRA);

        tooltip.add(Component.translatable("geomancy.jade.node_type",
                Component.translatable("geomancy.essence." + essence.name().toLowerCase(Locale.ROOT))
                        .withColor(essence.color() & 0xFFFFFF)));

        int duration = data.getIntOr(NodeData.DURATION, 0);
        if (duration > 0) {
            int percent = Math.round(100.0F * data.getIntOr(NodeData.PROGRESS, 0) / duration);
            tooltip.add(Component.translatable("geomancy.jade.transmuting", percent));
        }
    }

    private static Essence parseEssence(String name) {
        for (Essence essence : Essence.values()) {
            if (essence.name().equals(name)) {
                return essence;
            }
        }
        return Essence.TERRA;
    }
}
