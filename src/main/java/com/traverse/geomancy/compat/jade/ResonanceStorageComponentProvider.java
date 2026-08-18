package com.traverse.geomancy.compat.jade;

import org.jspecify.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import com.traverse.geomancy.registry.ModRegistries;
import com.traverse.geomancy.resonance.ResonanceType;
import com.traverse.geomancy.resonance.ResonanceTypes;

public enum ResonanceStorageComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return ResonanceStorageData.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        tooltip.add(Component.translatable("geomancy.jade.resonance",
                data.getIntOr(ResonanceStorageData.AMOUNT, 0),
                data.getIntOr(ResonanceStorageData.CAPACITY, 0)));
        tooltip.add(Component.translatable("geomancy.jade.resonance_type",
                ResonanceTypes.coloredName(accessor.getLevel().registryAccess(),
                        parse(data.getStringOr(ResonanceStorageData.TYPE, "")))));
    }

    private static @Nullable ResourceKey<ResonanceType> parse(String id) {
        if (id.isEmpty()) {
            return null;
        }
        Identifier parsed = Identifier.tryParse(id);
        return parsed == null ? null : ResourceKey.create(ModRegistries.RESONANCE_TYPE, parsed);
    }
}
