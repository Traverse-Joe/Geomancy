package com.traverse.geomancy.client;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.Vec3;

public class EssenceLinkRenderState extends BlockEntityRenderState {
    public @Nullable Vec3 upstreamOffset;
    public boolean linkValid;
    public int linkColor;
    public float linkTime;

    public @Nullable Vec3 jobOffset;
    public int jobColor;
    public float jobProgress;
    public float jobTime;

    public Vec3 cameraOffset = Vec3.ZERO;
    public @Nullable Vec3 logisticsPickupOffset;
    public boolean logisticsWorldPickup;
    public float logisticsPickupRadius;
    public List<Vec3> logisticsOutputOffsets = List.of();
}
