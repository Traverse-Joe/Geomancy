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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.block.entity.ResonantHearthBlockEntity;

public class ResonantHearthRenderer implements BlockEntityRenderer<ResonantHearthBlockEntity, ResonantHearthRenderState> {
    private final ItemModelResolver itemModels;

    public ResonantHearthRenderer(BlockEntityRendererProvider.Context context) {
        itemModels = context.itemModelResolver();
    }

    @Override
    public ResonantHearthRenderState createRenderState() {
        return new ResonantHearthRenderState();
    }

    @Override
    public AABB getRenderBoundingBox(ResonantHearthBlockEntity hearth) {
        return new AABB(hearth.getBlockPos()).inflate(0.5, 1.0, 0.5);
    }

    @Override
    public void extractRenderState(ResonantHearthBlockEntity hearth, ResonantHearthRenderState state,
            float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(hearth, state, partialTicks, cameraPosition, breakProgress);
        for (int i = 0; i < state.ingredients.length; i++) {
            itemModels.updateForTopItem(state.ingredients[i], hearth.ingredients().get(i), ItemDisplayContext.FIXED,
                    hearth.getLevel(), null, i);
        }
        itemModels.updateForTopItem(state.focus, hearth.focus(), ItemDisplayContext.FIXED,
                hearth.getLevel(), null, 31);
        state.time = hearth.getLevel() == null ? 0.0F : hearth.getLevel().getGameTime() + partialTicks;
    }

    @Override
    public void submit(ResonantHearthRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        if (!state.focus.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.28F + 0.045F * (float) Math.sin(state.time * 0.08F), 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.time * 1.25F));
            poseStack.scale(0.52F, 0.52F, 0.52F);
            state.focus.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        int count = 0;
        for (var ingredient : state.ingredients) {
            if (!ingredient.isEmpty()) {
                count++;
            }
        }
        if (count == 0) {
            return;
        }

        int visibleIndex = 0;
        for (var ingredient : state.ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            float angle = Mth.TWO_PI * visibleIndex++ / count + state.time * 0.015F;
            float radius = count == 1 ? 0.0F : 0.24F;
            poseStack.pushPose();
            poseStack.translate(0.5F + Mth.cos(angle) * radius,
                    1.0F + Mth.sin(state.time * 0.08F + visibleIndex) * 0.035F,
                    0.5F + Mth.sin(angle) * radius);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.time * 1.5F + visibleIndex * 45.0F));
            poseStack.scale(0.34F, 0.34F, 0.34F);
            ingredient.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
