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
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.block.entity.GeodeJarBlockEntity;
import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;
import com.traverse.geomancy.resonance.ResonanceStorage;

// Shared by every plain ResonanceStorage crystal - the Geode Jar and all three Battery
// Crystal sizes - so upgrading a battery's storage tier is a visible glow, not just a tooltip.
public class ResonanceFillRenderer<T extends BlockEntity & ResonanceStorage>
        implements BlockEntityRenderer<T, ResonanceFillRenderState> {
    private static final int FILL_ALPHA = 0xB0;
    private static final int SEVERED_COLOR = 0xFFFF4040;

    public ResonanceFillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ResonanceFillRenderState createRenderState() {
        return new ResonanceFillRenderState();
    }

    @Override
    public AABB getRenderBoundingBox(T crystal) {
        AABB box = new AABB(crystal.getBlockPos());
        if (crystal instanceof GeodeJarBlockEntity jar && jar.node() instanceof BlockPos node) {
            box = box.minmax(new AABB(node)).inflate(LinkBeamRenderer.MAX_REACH);
        }
        return box;
    }

    @Override
    public void extractRenderState(T crystal, ResonanceFillRenderState state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(crystal, state, partialTicks, cameraPosition, breakProgress);
        state.fill = crystal.capacity() == 0 ? 0.0F : (float) crystal.resonance() / crystal.capacity();
        state.color = FILL_ALPHA << 24 | crystal.resonanceColor() & 0x00FFFFFF;

        BlockPos origin = crystal.getBlockPos();
        BlockPos node = crystal instanceof GeodeJarBlockEntity jar ? jar.node() : null;
        if (node == null) {
            state.nodeOffset = null;
            return;
        }
        state.cameraOffset = cameraPosition.subtract(Vec3.atLowerCornerOf(origin));
        state.nodeOffset = new Vec3(node.getX() - origin.getX(), node.getY() - origin.getY(),
                node.getZ() - origin.getZ());
        state.nodeColor = nodeColor(crystal, node);
        state.nodeTime = crystal.getLevel() == null ? 0.0F : crystal.getLevel().getGameTime() + partialTicks;
    }

    // The node's own essence, so the beam is coloured the moment it is bound rather than
    // waiting for the first resonance to arrive. A node that is merely unloaded keeps the
    // jar's colour - the binding survives that - while one that is genuinely gone reads red.
    private static <C extends BlockEntity & ResonanceStorage> int nodeColor(C crystal, BlockPos node) {
        Level level = crystal.getLevel();
        if (level == null) {
            return SEVERED_COLOR;
        }
        if (level.getBlockEntity(node) instanceof TectonicNodeBlockEntity source) {
            return ARGB.opaque(source.essence().color());
        }
        if (level.isLoaded(node)) {
            return SEVERED_COLOR;
        }
        return ARGB.opaque(crystal.resonanceColor());
    }

    @Override
    public void submit(ResonanceFillRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        if (state.nodeOffset != null) {
            LinkBeamRenderer.submit(poseStack, collector, state.nodeOffset, state.cameraOffset,
                    state.nodeColor, state.nodeTime, 0.0F);
        }
        if (state.fill <= 0.0F) {
            return;
        }
        float min = 4.1F / 16.0F;
        float max = 11.9F / 16.0F;
        float bottom = 2.1F / 16.0F;
        float top = bottom + state.fill * 10.7F / 16.0F;
        collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(),
                (pose, buffer) -> box(pose, buffer, min, bottom, min, max, top, max, state.color));
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
