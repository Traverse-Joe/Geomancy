package com.traverse.geomancy.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import com.traverse.geomancy.block.TectonicNodeBlock;
import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;
import com.traverse.geomancy.essence.Essence;

public class TectonicNodeRenderer implements BlockEntityRenderer<TectonicNodeBlockEntity, TectonicNodeRenderState> {
    private static final VoxelShape OUTLINE = Block.cube(6.6);
    private static final float OUTLINE_WIDTH = 4.0F;
    private static final float BEAM_MIN_WIDTH = 3.0F;
    private static final float BEAM_MAX_WIDTH = 8.0F;

    public TectonicNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public TectonicNodeRenderState createRenderState() {
        return new TectonicNodeRenderState();
    }

    @Override
    public void extractRenderState(TectonicNodeBlockEntity node, TectonicNodeRenderState state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(node, state, partialTicks, cameraPosition, breakProgress);

        state.revealed = node.getBlockState().getValue(TectonicNodeBlock.REVEALED);
        BlockPos target = node.jobTarget();

        if (state.revealed || target != null) {
            Essence essence = node.getLevel() == null ? Essence.TERRA : node.essence();
            state.outlineColor = ARGB.opaque(essence.color());
        }

        if (target == null) {
            state.jobOffset = null;
            state.jobProgress = 0.0F;
        } else {
            BlockPos origin = node.getBlockPos();
            state.jobOffset = new Vec3(target.getX() - origin.getX(), target.getY() - origin.getY(),
                    target.getZ() - origin.getZ());
            int duration = node.jobDuration();
            state.jobProgress = duration <= 0 ? 0.0F : Math.min(1.0F, (float) node.jobProgress() / duration);
        }
    }

    @Override
    public void submit(TectonicNodeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.revealed) {
            collector.submitCustomGeometry(poseStack, RenderTypes.LINES, (pose, buffer) -> {
                PoseStack local = new PoseStack();
                local.last().set(pose);
                ShapeRenderer.renderShape(local, buffer, OUTLINE, 0.0, 0.0, 0.0, state.outlineColor, OUTLINE_WIDTH);
            });
        }

        Vec3 offset = state.jobOffset;
        if (offset != null) {
            float width = BEAM_MIN_WIDTH + (BEAM_MAX_WIDTH - BEAM_MIN_WIDTH) * state.jobProgress;
            collector.submitCustomGeometry(poseStack, RenderTypes.LINES, (pose, buffer) -> {
                Vector3f normal = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z).normalize();
                buffer.addVertex(pose, 0.5F, 0.5F, 0.5F)
                        .setColor(state.outlineColor).setNormal(pose, normal).setLineWidth(width);
                buffer.addVertex(pose, (float) offset.x + 0.5F, (float) offset.y + 0.5F, (float) offset.z + 0.5F)
                        .setColor(state.outlineColor).setNormal(pose, normal).setLineWidth(width);
            });
        }
    }
}
