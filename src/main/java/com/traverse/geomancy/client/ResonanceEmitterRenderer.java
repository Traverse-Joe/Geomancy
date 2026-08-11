package com.traverse.geomancy.client;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.block.entity.ResonanceEmitterBlockEntity;
import com.traverse.geomancy.item.ResonantTuningForkItem;
import com.traverse.geomancy.registry.ModDataComponents;

// Links are invisible in normal play. Holding the Resonant Tuning Fork in binding mode
// and looking at an emitter or its bound receiver draws a line between them; nothing
// renders otherwise. This is a simplification of "any bound block draws its link on
// hover" scoped to the emitter side, since the emitter is the only endpoint that holds
// the binding.
public class ResonanceEmitterRenderer implements BlockEntityRenderer<ResonanceEmitterBlockEntity, ResonanceLinkRenderState> {
    private static final int LINK_COLOR = 0xFFB0B7C6;

    public ResonanceEmitterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ResonanceLinkRenderState createRenderState() {
        return new ResonanceLinkRenderState();
    }

    @Override
    public AABB getRenderBoundingBox(ResonanceEmitterBlockEntity emitter) {
        AABB box = new AABB(emitter.getBlockPos());
        BlockPos target = emitter.target();
        if (target != null) {
            box = box.minmax(new AABB(target));
        }
        return box.inflate(LinkBeamRenderer.MAX_REACH);
    }

    @Override
    public void extractRenderState(ResonanceEmitterBlockEntity emitter, ResonanceLinkRenderState state,
            float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(emitter, state, partialTicks, cameraPosition, breakProgress);

        BlockPos origin = emitter.getBlockPos();
        state.cameraOffset = cameraPosition.subtract(Vec3.atLowerCornerOf(origin));
        state.linkTime = emitter.getLevel() == null ? 0.0F : emitter.getLevel().getGameTime() + partialTicks;

        BlockPos target = emitter.target();
        state.targetOffset = target != null && bindingModeVisible(origin, target)
                ? offsetFrom(origin, target)
                : null;
    }

    @Override
    public void submit(ResonanceLinkRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        if (state.targetOffset != null) {
            LinkBeamRenderer.submit(poseStack, collector, state.targetOffset, state.cameraOffset, LINK_COLOR,
                    state.linkTime, 0.0F);
        }
    }

    private static boolean bindingModeVisible(BlockPos origin, BlockPos target) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !holdingBindingFork(player)) {
            return false;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos looked = hit.getBlockPos();
        return looked.equals(origin) || looked.equals(target);
    }

    private static boolean holdingBindingFork(Player player) {
        return isBindingFork(player.getMainHandItem()) || isBindingFork(player.getOffhandItem());
    }

    private static boolean isBindingFork(ItemStack stack) {
        return stack.getItem() instanceof ResonantTuningForkItem
                && stack.getOrDefault(ModDataComponents.BINDING_MODE.get(), false);
    }

    private static Vec3 offsetFrom(BlockPos origin, BlockPos target) {
        return new Vec3(target.getX() - origin.getX(), target.getY() - origin.getY(), target.getZ() - origin.getZ());
    }
}
