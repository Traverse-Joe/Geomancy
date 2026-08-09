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
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.block.entity.ItemPedestalBlockEntity;
import com.traverse.geomancy.essence.EssenceCharge;

public class ItemPedestalRenderer implements BlockEntityRenderer<ItemPedestalBlockEntity, ItemPedestalRenderState> {
    private static final int NEUTRAL_COLOR = 0xFFB0B7C6;
    private static final int OBSTRUCTED_COLOR = 0xFFFF4040;
    private final ItemModelResolver itemModels;

    public ItemPedestalRenderer(BlockEntityRendererProvider.Context context) {
        itemModels = context.itemModelResolver();
    }

    @Override
    public ItemPedestalRenderState createRenderState() {
        return new ItemPedestalRenderState();
    }

    @Override
    public AABB getRenderBoundingBox(ItemPedestalBlockEntity pedestal) {
        AABB box = new AABB(pedestal.getBlockPos()).inflate(0.5);
        return pedestal.upstream() == null ? box : box.minmax(new AABB(pedestal.upstream())).inflate(LinkBeamRenderer.MAX_REACH);
    }

    @Override
    public void extractRenderState(ItemPedestalBlockEntity pedestal, ItemPedestalRenderState state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pedestal, state, partialTicks, cameraPosition, breakProgress);
        itemModels.updateForTopItem(state.item, pedestal.getItem(0), ItemDisplayContext.FIXED,
                pedestal.getLevel(), null, 0);
        BlockPos origin = pedestal.getBlockPos();
        BlockPos upstream = pedestal.upstream();
        state.upstreamOffset = upstream == null ? null : new Vec3(upstream.getX() - origin.getX(),
                upstream.getY() - origin.getY(), upstream.getZ() - origin.getZ());
        state.cameraOffset = cameraPosition.subtract(Vec3.atLowerCornerOf(origin));
        EssenceCharge charge = pedestal.charge();
        state.essenceColor = charge.isEmpty() ? NEUTRAL_COLOR : ARGB.opaque(charge.essence().color());
        state.linkValid = pedestal.linkValid();
        state.time = pedestal.getLevel() == null ? 0.0F : pedestal.getLevel().getGameTime() + partialTicks;
        state.progress = pedestal.duration() <= 0 ? 0.0F
                : Math.min(1.0F, (pedestal.progress() + partialTicks) / pedestal.duration());
    }

    @Override
    public void submit(ItemPedestalRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        if (state.upstreamOffset != null) {
            LinkBeamRenderer.submit(poseStack, collector, state.upstreamOffset, state.cameraOffset,
                    state.linkValid ? state.essenceColor : OBSTRUCTED_COLOR, state.time, state.progress);
        }
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
