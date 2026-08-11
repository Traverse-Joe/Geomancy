package com.traverse.geomancy.client;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.Vec3;

public class ResonanceLinkRenderState extends BlockEntityRenderState {
    public @Nullable Vec3 targetOffset;
    public float linkTime;
    public Vec3 cameraOffset = Vec3.ZERO;
}
