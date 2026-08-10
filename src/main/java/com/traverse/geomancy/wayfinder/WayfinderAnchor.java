package com.traverse.geomancy.wayfinder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record WayfinderAnchor(ResourceKey<Level> dimension, BlockPos pos) {
    public static final Codec<WayfinderAnchor> CODEC = RecordCodecBuilder.create(i -> i.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(WayfinderAnchor::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(WayfinderAnchor::pos)
    ).apply(i, WayfinderAnchor::new));

    public static final StreamCodec<ByteBuf, WayfinderAnchor> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), WayfinderAnchor::dimension,
            BlockPos.STREAM_CODEC, WayfinderAnchor::pos,
            WayfinderAnchor::new);
}
