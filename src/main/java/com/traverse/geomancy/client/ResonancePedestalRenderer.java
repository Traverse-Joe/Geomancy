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
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.block.entity.ResonancePedestalBlockEntity;

public class ResonancePedestalRenderer implements BlockEntityRenderer<ResonancePedestalBlockEntity, ResonancePedestalRenderState> {
    private final ItemModelResolver itemModels;

    public ResonancePedestalRenderer(BlockEntityRendererProvider.Context context) {
        itemModels = context.itemModelResolver();
    }

    @Override
    public ResonancePedestalRenderState createRenderState() {
        return new ResonancePedestalRenderState();
    }

    @Override
    public void extractRenderState(ResonancePedestalBlockEntity pedestal, ResonancePedestalRenderState state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pedestal, state, partialTicks, cameraPosition, breakProgress);
        itemModels.updateForTopItem(state.item, pedestal.held(), ItemDisplayContext.FIXED, pedestal.getLevel(), null, 0);
        state.time = pedestal.getLevel() == null ? 0.0F : pedestal.getLevel().getGameTime() + partialTicks;
        state.progress = pedestal.progressFraction(partialTicks);
    }

    @Override
    public void submit(ResonancePedestalRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        if (state.item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F + 0.05F * (float) Math.sin(state.time * 0.1F), 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.time * 2.0F));
        float scale = 0.5F + state.progress * 0.08F;
        poseStack.scale(scale, scale, scale);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
