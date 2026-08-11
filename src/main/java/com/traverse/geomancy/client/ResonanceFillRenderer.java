package com.traverse.geomancy.client;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.resonance.ResonanceStorage;

// Shared by every plain ResonanceStorage crystal - the Geode Jar and all three Battery
// Crystal sizes - so upgrading a battery's storage tier is a visible glow, not just a tooltip.
public class ResonanceFillRenderer<T extends BlockEntity & ResonanceStorage>
        implements BlockEntityRenderer<T, ResonanceFillRenderState> {
    private static final int RESONANCE_COLOR = 0xB0804CFF;

    public ResonanceFillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ResonanceFillRenderState createRenderState() {
        return new ResonanceFillRenderState();
    }

    @Override
    public AABB getRenderBoundingBox(T crystal) {
        return new AABB(crystal.getBlockPos());
    }

    @Override
    public void extractRenderState(T crystal, ResonanceFillRenderState state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(crystal, state, partialTicks, cameraPosition, breakProgress);
        state.fill = crystal.capacity() == 0 ? 0.0F : (float) crystal.resonance() / crystal.capacity();
    }

    @Override
    public void submit(ResonanceFillRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        if (state.fill <= 0.0F) {
            return;
        }
        float min = 4.1F / 16.0F;
        float max = 11.9F / 16.0F;
        float bottom = 2.1F / 16.0F;
        float top = bottom + state.fill * 10.7F / 16.0F;
        collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(),
                (pose, buffer) -> box(pose, buffer, min, bottom, min, max, top, max, RESONANCE_COLOR));
    }

    private static void box(PoseStack.Pose pose, VertexConsumer buffer, float x1, float y1, float z1,
            float x2, float y2, float z2, int color) {
        quad(pose, buffer, color, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1);
        quad(pose, buffer, color, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2);
        quad(pose, buffer, color, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2);
        quad(pose, buffer, color, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1);
        quad(pose, buffer, color, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2);
        quad(pose, buffer, color, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int color, float... points) {
        for (int i = 0; i < points.length; i += 3) {
            buffer.addVertex(pose, points[i], points[i + 1], points[i + 2]).setColor(color);
        }
    }
}
