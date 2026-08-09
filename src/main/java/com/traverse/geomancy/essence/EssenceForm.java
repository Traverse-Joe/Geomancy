package com.traverse.geomancy.essence;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

// Raw is drawn live from a node's pool and flows down the pillar chain; refined only exists
// inside a Resonance Jar, produced by consuming raw essence that transits the network. Basic
// recipes accept raw; advanced recipes require refined.
public enum EssenceForm implements StringRepresentable {
    RAW,
    REFINED;

    public static final Codec<EssenceForm> CODEC = StringRepresentable.fromEnum(EssenceForm::values);
    public static final StreamCodec<FriendlyByteBuf, EssenceForm> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(EssenceForm.class);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
