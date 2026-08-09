package com.traverse.geomancy.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import com.traverse.geomancy.block.entity.ResonantHearthBlockEntity;

public class ResonantHearthRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState[] ingredients = new ItemStackRenderState[ResonantHearthBlockEntity.MAX_INGREDIENTS];
    public final ItemStackRenderState focus = new ItemStackRenderState();
    public float time;

    public ResonantHearthRenderState() {
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = new ItemStackRenderState();
        }
    }
}
