package com.traverse.geomancy.client;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.Vec3;

public class ResonanceFillRenderState extends BlockEntityRenderState {
    public float fill;
    public int color;

    // Only a Geode Jar binds a node; the Battery Crystals leave these untouched.
    public @Nullable Vec3 nodeOffset;
    public int nodeColor;
    public float nodeTime;
    public Vec3 cameraOffset = Vec3.ZERO;
}
