package com.traverse.geomancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

// Camera-facing ribbons stacked around the link, drawn additively so the overlap burns out
// to a white core with the essence colour bleeding off the edges.
public final class LinkBeamRenderer {
    public static final float MAX_REACH = 0.75F;

    private static final float CORE_WIDTH = 0.035F;
    private static final float INNER_WIDTH = 0.11F;
    private static final float OUTER_WIDTH = 0.34F;
    private static final float PROGRESS_SWELL = 0.4F;
    // Two detuned sines so the breathing never settles into an obvious loop.
    private static final float FLUX = 0.28F;
    private static final float FLUX_SPEED = 0.16F;
    private static final float FLUX_DETAIL = 0.1F;
    private static final float FLUX_DETAIL_SPEED = 0.41F;
    private static final float CORE_WHITENESS = 0.85F;
    private static final float EDGE_WHITENESS = 0.35F;
    private static final int CORE_ALPHA = 235;
    private static final int CORE_EDGE_ALPHA = 130;
    private static final int INNER_ALPHA = 130;
    private static final int OUTER_ALPHA = 55;
    private static final float SHIMMER = 0.18F;
    private static final float SHIMMER_SPEED = 0.11F;
    private static final int WHITE = 0xFFFFFF;

    private LinkBeamRenderer() {
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 offset, Vec3 cameraOffset,
            int color, float time, float progress) {
        Vector3f start = new Vector3f(0.5F, 0.5F, 0.5F);
        Vector3f end = new Vector3f((float) offset.x + 0.5F, (float) offset.y + 0.5F, (float) offset.z + 0.5F);
        Vector3f axis = new Vector3f(end).sub(start);
        float length = axis.length();
        if (length < 1.0E-2F) {
            return;
        }
        axis.div(length);

        Vector3f mid = new Vector3f(start).lerp(end, 0.5F);
        Vector3f toCamera = new Vector3f((float) cameraOffset.x, (float) cameraOffset.y, (float) cameraOffset.z).sub(mid);
        Vector3f side = new Vector3f(axis).cross(toCamera);
        if (side.lengthSquared() < 1.0E-6F) {
            Vector3f seed = Math.abs(axis.y()) > 0.9F ? new Vector3f(1.0F, 0.0F, 0.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
            side.set(axis).cross(seed);
        }
        side.normalize();

        float flux = 1.0F + FLUX * Mth.sin(time * FLUX_SPEED) + FLUX_DETAIL * Mth.sin(time * FLUX_DETAIL_SPEED);
        float swell = (1.0F + PROGRESS_SWELL * progress) * flux;
        float shimmer = 1.0F + SHIMMER * Mth.sin(time * SHIMMER_SPEED);
        int essence = color & 0x00FFFFFF;
        int coreCenter = ARGB.color(CORE_ALPHA, ARGB.srgbLerp(CORE_WHITENESS, essence, WHITE));
        int coreEdge = ARGB.color(CORE_EDGE_ALPHA, ARGB.srgbLerp(EDGE_WHITENESS, essence, WHITE));
        int innerCenter = ARGB.color(scaleAlpha(INNER_ALPHA, shimmer), essence);
        int outerCenter = ARGB.color(scaleAlpha(OUTER_ALPHA, shimmer), essence);
        int fadedEdge = ARGB.color(0, essence);

        collector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            ribbon(pose, buffer, start, end, side, OUTER_WIDTH * swell, outerCenter, fadedEdge);
            ribbon(pose, buffer, start, end, side, INNER_WIDTH * swell, innerCenter, fadedEdge);
            ribbon(pose, buffer, start, end, side, CORE_WIDTH * swell, coreCenter, coreEdge);
        });
    }

    private static void ribbon(PoseStack.Pose pose, VertexConsumer buffer, Vector3f start, Vector3f end,
            Vector3f side, float halfWidth, int centerColor, int edgeColor) {
        Vector3f startLeft = new Vector3f(start).fma(-halfWidth, side);
        Vector3f endLeft = new Vector3f(end).fma(-halfWidth, side);
        Vector3f startRight = new Vector3f(start).fma(halfWidth, side);
        Vector3f endRight = new Vector3f(end).fma(halfWidth, side);
        quad(pose, buffer, startLeft, endLeft, end, start, edgeColor, edgeColor, centerColor, centerColor);
        quad(pose, buffer, start, end, endRight, startRight, centerColor, centerColor, edgeColor, edgeColor);
    }

    // Emitted with both windings: the lightning pipeline culls back faces, so exactly one
    // copy survives no matter which side of the ribbon the camera ends up on.
    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, Vector3f a, Vector3f b, Vector3f c,
            Vector3f d, int colorA, int colorB, int colorC, int colorD) {
        vertex(pose, buffer, a, colorA);
        vertex(pose, buffer, b, colorB);
        vertex(pose, buffer, c, colorC);
        vertex(pose, buffer, d, colorD);
        vertex(pose, buffer, d, colorD);
        vertex(pose, buffer, c, colorC);
        vertex(pose, buffer, b, colorB);
        vertex(pose, buffer, a, colorA);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vector3f position, int color) {
        buffer.addVertex(pose, position.x(), position.y(), position.z()).setColor(color);
    }

    private static int scaleAlpha(int alpha, float scale) {
        return Mth.clamp((int) (alpha * scale), 0, 255);
    }
}
