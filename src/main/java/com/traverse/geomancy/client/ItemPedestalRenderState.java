package com.traverse.geomancy.client;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.Vec3;

public class ItemPedestalRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState item = new ItemStackRenderState();
    public @Nullable Vec3 upstreamOffset;
    public Vec3 cameraOffset = Vec3.ZERO;
    public int essenceColor;
    public boolean linkValid;
    public float time;
    public float progress;
}
