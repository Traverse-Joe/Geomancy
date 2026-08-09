package com.traverse.geomancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

// Shelved effect, kept for a later pass: rings riding the link beam. Nothing submits it —
// the beam's own width flux carries the processing animation instead.
public final class ProcessingRingRenderer {
    public static final float MAX_REACH = 0.75F;

    private static final int SEGMENTS = 20;
    private static final int MIN_RINGS = 1;
    private static final int MAX_RINGS = 4;
    private static final float BLOCKS_PER_RING = 5.0F;
    private static final float TRAVEL_TICKS = 80.0F;
    private static final float PROGRESS_SPEEDUP = 0.35F;
    private static final float START_RADIUS = 0.65F;
    private static final float END_RADIUS = 0.22F;
    private static final float PULSE = 0.045F;
    private static final float PULSE_SPEED = 0.25F;
    private static final float PULSE_STAGGER = 7.0F;
    private static final float SPIN_SPEED = 0.03F;
    private static final float WIDTH = 4.0F;
    private static final float FADE_ZONE = 0.12F;
    private static final float IDLE_BRIGHTNESS = 0.8F;
    private static final float MIN_ALPHA = 0.45F;

    private ProcessingRingRenderer() {
    }

    // Rings ride the beam from node to target, tightening as they converge on the block
    // being transmuted. They live in the beam's plane, which the hammer's line-of-sight
    // check already proved clear, so nothing renders buried inside terrain.
    public static void submit(TectonicNodeRenderState state, Vec3 offset, PoseStack poseStack,
            SubmitNodeCollector collector) {
        Vector3f axis = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z);
        float length = axis.length();
        if (length < 1.0E-2F) {
            return;
        }
        axis.div(length);

        Vector3f seed = Math.abs(axis.y()) > 0.9F ? new Vector3f(1.0F, 0.0F, 0.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f right = new Vector3f(axis).cross(seed).normalize();
        Vector3f up = new Vector3f(axis).cross(right).normalize();

        int rings = Mth.clamp(Mth.ceil(length / BLOCKS_PER_RING), MIN_RINGS, MAX_RINGS);
        // Both terms only ever grow, so the phase accelerates smoothly as the job matures.
        float phase = state.jobTime * (1.0F + PROGRESS_SPEEDUP * state.jobProgress) / TRAVEL_TICKS;
        float head = Mth.frac(state.jobTime * SPIN_SPEED);
        float brightness = IDLE_BRIGHTNESS + (1.0F - IDLE_BRIGHTNESS) * state.jobProgress;
        int rgb = state.outlineColor & 0x00FFFFFF;

        collector.submitCustomGeometry(poseStack, RenderTypes.LINES_TRANSLUCENT, (pose, buffer) -> {
            Vector3f center = new Vector3f();
            for (int ring = 0; ring < rings; ring++) {
                float travel = Mth.frac(phase + (float) ring / rings);
                center.set(0.5F, 0.5F, 0.5F).fma(travel * length, axis);
                float radius = Mth.lerp(travel, START_RADIUS, END_RADIUS)
                        + PULSE * Mth.sin((state.jobTime + ring * PULSE_STAGGER) * PULSE_SPEED);
                submitRing(pose, buffer, center, right, up, radius, head, rgb, brightness * fade(travel));
            }
        });
    }

    private static void submitRing(PoseStack.Pose pose, VertexConsumer buffer, Vector3f center, Vector3f right,
            Vector3f up, float radius, float head, int rgb, float brightness) {
        Vector3f from = new Vector3f();
        Vector3f to = new Vector3f();
        Vector3f normal = new Vector3f();
        for (int segment = 0; segment < SEGMENTS; segment++) {
            float fromTurn = (float) segment / SEGMENTS;
            float toTurn = (float) (segment + 1) / SEGMENTS;
            ringPoint(center, right, up, radius, fromTurn, from);
            ringPoint(center, right, up, radius, toTurn, to);
            to.sub(from, normal).normalize();
            buffer.addVertex(pose, from.x(), from.y(), from.z())
                    .setColor(rgb | trailAlpha(fromTurn, head, brightness)).setNormal(pose, normal).setLineWidth(WIDTH);
            buffer.addVertex(pose, to.x(), to.y(), to.z())
                    .setColor(rgb | trailAlpha(toTurn, head, brightness)).setNormal(pose, normal).setLineWidth(WIDTH);
        }
    }

    private static void ringPoint(Vector3f center, Vector3f right, Vector3f up, float radius, float turn, Vector3f out) {
        float angle = turn * Mth.TWO_PI;
        out.set(center).fma(radius * Mth.cos(angle), right).fma(radius * Mth.sin(angle), up);
    }

    private static float fade(float travel) {
        return Math.min(1.0F, Math.min(travel, 1.0F - travel) / FADE_ZONE);
    }

    // Alpha falls off with the distance travelled backwards from the head, so the
    // bright point chases itself around the ring instead of the ring flashing whole.
    private static int trailAlpha(float fraction, float head, float brightness) {
        float behind = fraction - head;
        behind -= Mth.floor(behind);
        float alpha = brightness * (MIN_ALPHA + (1.0F - MIN_ALPHA) * (1.0F - behind));
        return (int) (255.0F * Mth.clamp(alpha, 0.0F, 1.0F)) << 24;
    }
}
