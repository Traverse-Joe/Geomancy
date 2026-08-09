package com.traverse.geomancy.client;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.Vec3;

public class TectonicNodeRenderState extends BlockEntityRenderState {
    public boolean revealed;
    public int outlineColor;
    public @Nullable Vec3 jobOffset;
    public float jobProgress;
    public float jobTime;
    public Vec3 cameraOffset = Vec3.ZERO;
}
