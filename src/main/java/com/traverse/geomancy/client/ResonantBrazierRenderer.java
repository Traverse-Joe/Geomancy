package com.traverse.geomancy.client;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.block.entity.ResonantBrazierBlockEntity;

public class ResonantBrazierRenderer implements BlockEntityRenderer<ResonantBrazierBlockEntity, ResonantBrazierRenderState> {
    private final ItemModelResolver itemModels;

    public ResonantBrazierRenderer(BlockEntityRendererProvider.Context context) {
        itemModels = context.itemModelResolver();
    }

    @Override
    public ResonantBrazierRenderState createRenderState() {
        return new ResonantBrazierRenderState();
    }

    @Override
    public AABB getRenderBoundingBox(ResonantBrazierBlockEntity brazier) {
        return new AABB(brazier.getBlockPos()).inflate(0.25, 0.75, 0.25);
    }

    @Override
    public void extractRenderState(ResonantBrazierBlockEntity brazier, ResonantBrazierRenderState state,
            float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(brazier, state, partialTicks, cameraPosition, breakProgress);
        itemModels.updateForTopItem(state.crystal, brazier.crystal(),
                ItemDisplayContext.FIXED, brazier.getLevel(), null, 0);
        state.time = brazier.getLevel() == null ? 0.0F : brazier.getLevel().getGameTime() + partialTicks;
    }

    @Override
    public void submit(ResonantBrazierRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        if (!state.crystal.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.08F + 0.04F * (float) Math.sin(state.time * 0.1F), 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.time * 2.0F));
            poseStack.scale(0.48F, 0.48F, 0.48F);
            state.crystal.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
