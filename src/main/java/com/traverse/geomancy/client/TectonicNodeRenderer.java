package com.traverse.geomancy.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import com.traverse.geomancy.block.TectonicNodeBlock;
import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;
import com.traverse.geomancy.essence.Essence;

public class TectonicNodeRenderer implements BlockEntityRenderer<TectonicNodeBlockEntity, TectonicNodeRenderState> {
    private static final VoxelShape NODE_OUTLINE = Block.cube(13.2);

    public TectonicNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    // The beam, its rings and the target outline reach as far as the job target, so the
    // default single-block box would cull them whenever only the target end is on screen.
    @Override
    public AABB getRenderBoundingBox(TectonicNodeBlockEntity node) {
        AABB box = new AABB(node.getBlockPos());
        BlockPos target = node.jobRenderPos();
        return target == null ? box : box.minmax(new AABB(target)).inflate(LinkBeamRenderer.MAX_REACH);
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
        BlockPos target = node.jobRenderPos();

        if (state.revealed || target != null) {
            Essence essence = node.getLevel() == null ? Essence.TERRA : node.essence();
            state.outlineColor = ARGB.opaque(essence.color());
        }

        if (target == null) {
            state.jobOffset = null;
            state.jobProgress = 0.0F;
            state.jobTime = 0.0F;
        } else {
            BlockPos origin = node.getBlockPos();
            state.jobOffset = new Vec3(target.getX() - origin.getX(), target.getY() - origin.getY(),
                    target.getZ() - origin.getZ());
            state.cameraOffset = cameraPosition.subtract(Vec3.atLowerCornerOf(origin));
            int duration = node.jobDuration();
            state.jobTime = node.jobProgress() + partialTicks;
            state.jobProgress = duration <= 0 ? 0.0F : Math.min(1.0F, state.jobTime / duration);
        }
    }

    @Override
    public void submit(TectonicNodeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.revealed) {
            Outlines.submit(poseStack, collector, NODE_OUTLINE, 0.0, 0.0, 0.0, state.outlineColor);
        }

        Vec3 offset = state.jobOffset;
        if (offset != null) {
            LinkBeamRenderer.submit(poseStack, collector, offset, state.cameraOffset, state.outlineColor,
                    state.jobTime, state.jobProgress);
            Outlines.submit(poseStack, collector, Outlines.TARGET_OUTLINE, offset.x, offset.y, offset.z, state.outlineColor);
        }
    }
}
