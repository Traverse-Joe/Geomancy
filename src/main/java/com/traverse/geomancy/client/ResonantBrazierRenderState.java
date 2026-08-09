package com.traverse.geomancy.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class ResonantBrazierRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState fuel = new ItemStackRenderState();
    public final ItemStackRenderState crystal = new ItemStackRenderState();
    public float time;
}
