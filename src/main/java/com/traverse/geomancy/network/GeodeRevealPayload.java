package com.traverse.geomancy.network;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import com.traverse.geomancy.Geomancy;

// Positions of the signature blocks a tuning fork strike uncovered nearby. The client
// outlines them for a fixed window; nothing about them is authoritative.
public record GeodeRevealPayload(List<BlockPos> positions) implements CustomPacketPayload {
    public static final int MAX_POSITIONS = 96;

    public static final Type<GeodeRevealPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Geomancy.MODID, "geode_reveal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GeodeRevealPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_POSITIONS)), GeodeRevealPayload::positions,
            GeodeRevealPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
