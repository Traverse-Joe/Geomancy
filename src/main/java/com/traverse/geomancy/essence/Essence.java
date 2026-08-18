package com.traverse.geomancy.essence;

import java.util.Locale;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum Essence implements StringRepresentable {
    NATURAL(0xFF8B7355),
    METALLIC(0xFFB0B7C6),
    CRYSTAL(0xFF7FD4E8),
    AERY(0xFFE8D98A),
    INFERNAL(0xFFE8663D),
    ENDER(0xFF6B4C9A),
    VITAL(0xFF2E9B3F),
    AQUATIC(0xFF2A6FD4),
    CRYO(0xFFDDF2FF);

    public static final Codec<Essence> CODEC = StringRepresentable.fromEnum(Essence::values);
    public static final StreamCodec<FriendlyByteBuf, Essence> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(Essence.class);

    private final int color;

    Essence(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }

    public Component displayName() {
        return Component.translatable("geomancy.essence." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<Essence> byName(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        for (Essence essence : values()) {
            if (essence.getSerializedName().equals(key)) {
                return Optional.of(essence);
            }
        }
        return Optional.empty();
    }
}
