package com.traverse.geomancy.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import com.traverse.geomancy.block.entity.EssenceWorkerBlockEntity;
import com.traverse.geomancy.block.entity.ResonancePillarBlockEntity;
import com.traverse.geomancy.Config;
import com.traverse.geomancy.essence.EssenceCharge;
import com.traverse.geomancy.essence.Essence;
import com.traverse.geomancy.network.EssenceRelay;

// Shared by every relay type - the Resonance Pillar and the Resonance Jar: draws the
// upstream beam back to its source, tinted red when the link is obstructed, plus the job
// beam and target outline for whichever relays also run jobs (only pillars, currently;
// a jar has no EssenceWorkerBlockEntity to read job state from, so those fields stay empty).
public class EssenceLinkRenderer<T extends BlockEntity & EssenceRelay>
        implements BlockEntityRenderer<T, EssenceLinkRenderState> {
    private static final int NEUTRAL_COLOR = 0xFFB0B7C6;
    private static final int OBSTRUCTED_COLOR = 0xFFFF4040;

    public EssenceLinkRenderer(BlockEntityRendererProvider.Context context) {
    }

    // The upstream beam, job beam and target outline all reach past this block's own
    // space, so the default single-block box would cull them whenever only one end
    // is on screen.
    @Override
    public AABB getRenderBoundingBox(T relay) {
        AABB box = new AABB(relay.getBlockPos());
        BlockPos upstream = relay.upstream();
        if (upstream != null) {
            box = box.minmax(new AABB(upstream));
        }
        if (relay instanceof EssenceWorkerBlockEntity worker && worker.jobRenderPos() instanceof BlockPos target) {
            box = box.minmax(new AABB(target));
        }
        if (relay instanceof ResonancePillarBlockEntity pillar) {
            for (BlockPos target : pillar.logisticsTargets()) {
                AABB targetBox = new AABB(target);
                if (target.equals(pillar.logisticsPickup()) && pillar.isLogisticsWorldPickup()) {
                    targetBox = targetBox.inflate(Config.LOGISTICS_PICKUP_RADIUS.get(), 1.0D,
                            Config.LOGISTICS_PICKUP_RADIUS.get());
                }
                box = box.minmax(targetBox);
            }
        }
        return box.inflate(LinkBeamRenderer.MAX_REACH);
    }

    @Override
    public EssenceLinkRenderState createRenderState() {
        return new EssenceLinkRenderState();
    }

    @Override
    public void extractRenderState(T relay, EssenceLinkRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(relay, state, partialTicks, cameraPosition, breakProgress);

        BlockPos origin = relay.getBlockPos();
        state.cameraOffset = cameraPosition.subtract(Vec3.atLowerCornerOf(origin));

        EssenceCharge charge = relay.charge();
        state.linkColor = charge.isEmpty() ? NEUTRAL_COLOR : ARGB.opaque(charge.essence().color());
        state.linkValid = relay.linkValid();
        state.linkTime = relay.getLevel() == null ? 0.0F : relay.getLevel().getGameTime() + partialTicks;

        BlockPos upstream = relay.upstream();
        state.upstreamOffset = upstream == null ? null : offsetFrom(origin, upstream);
        if (relay instanceof ResonancePillarBlockEntity pillar) {
            BlockPos pickup = pillar.logisticsPickup();
            state.logisticsPickupOffset = pickup == null ? null : offsetFrom(origin, pickup);
            state.logisticsWorldPickup = pillar.isLogisticsWorldPickup();
            state.logisticsPickupRadius = Config.LOGISTICS_PICKUP_RADIUS.get().floatValue();
            state.logisticsOutputOffsets = pillar.logisticsOutputs().stream()
                    .map(target -> offsetFrom(origin, target)).toList();
        } else {
            state.logisticsPickupOffset = null;
            state.logisticsWorldPickup = false;
            state.logisticsOutputOffsets = java.util.List.of();
        }

        BlockPos target = relay instanceof EssenceWorkerBlockEntity worker ? worker.jobRenderPos() : null;
        if (target == null) {
            state.jobOffset = null;
            state.jobProgress = 0.0F;
            state.jobTime = 0.0F;
        } else {
            EssenceWorkerBlockEntity worker = (EssenceWorkerBlockEntity) relay;
            state.jobOffset = offsetFrom(origin, target);
            state.jobColor = state.linkColor;
            int duration = worker.jobDuration();
            state.jobTime = worker.jobProgress() + partialTicks;
            state.jobProgress = duration <= 0 ? 0.0F : Math.min(1.0F, state.jobTime / duration);
        }
    }

    private static Vec3 offsetFrom(BlockPos origin, BlockPos target) {
        return new Vec3(target.getX() - origin.getX(), target.getY() - origin.getY(), target.getZ() - origin.getZ());
    }

    @Override
    public void submit(EssenceLinkRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.upstreamOffset != null) {
            int color = state.linkValid ? state.linkColor : OBSTRUCTED_COLOR;
            LinkBeamRenderer.submit(poseStack, collector, state.upstreamOffset, state.cameraOffset, color, state.linkTime, 0.0F);
        }

        if (state.jobOffset != null) {
            LinkBeamRenderer.submit(poseStack, collector, state.jobOffset, state.cameraOffset, state.jobColor,
                    state.jobTime, state.jobProgress);
            Outlines.submit(poseStack, collector, Outlines.TARGET_OUTLINE, state.jobOffset.x, state.jobOffset.y,
                    state.jobOffset.z, state.jobColor);
        }
        Vec3 pickup = state.logisticsPickupOffset;
        if (pickup != null) {
            LinkBeamRenderer.submit(poseStack, collector, pickup, state.cameraOffset, Essence.MOTUS.color(),
                    state.linkTime, 0.0F);
            if (state.logisticsWorldPickup) {
                WorldPickupCircleRenderer.submit(poseStack, collector,
                        pickup.add(0.5D, 1.005D, 0.5D), state.logisticsPickupRadius, Essence.MOTUS.color());
            }
            for (Vec3 output : state.logisticsOutputOffsets) {
                poseStack.pushPose();
                poseStack.translate(pickup.x, pickup.y, pickup.z);
                LinkBeamRenderer.submit(poseStack, collector, output.subtract(pickup),
                        state.cameraOffset.subtract(pickup), Essence.MOTUS.color(), state.linkTime, 0.0F);
                poseStack.popPose();
            }
        }
    }
}
