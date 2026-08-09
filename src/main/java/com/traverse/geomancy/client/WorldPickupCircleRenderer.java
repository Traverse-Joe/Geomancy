package com.traverse.geomancy.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class WorldPickupCircleRenderer {
    private static final int SEGMENTS = 48;
    private static final float WIDTH = 4.0F;

    private WorldPickupCircleRenderer() {
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 center, float radius, int rgb) {
        int color = 0xD0000000 | rgb & 0x00FFFFFF;
        collector.submitCustomGeometry(poseStack, RenderTypes.LINES_TRANSLUCENT, (pose, buffer) -> {
            for (int segment = 0; segment < SEGMENTS; segment++) {
                float from = Mth.TWO_PI * segment / SEGMENTS;
                float to = Mth.TWO_PI * (segment + 1) / SEGMENTS;
                float x1 = (float) center.x + radius * Mth.cos(from);
                float z1 = (float) center.z + radius * Mth.sin(from);
                float x2 = (float) center.x + radius * Mth.cos(to);
                float z2 = (float) center.z + radius * Mth.sin(to);
                float nx = x2 - x1;
                float nz = z2 - z1;
                float length = Mth.sqrt(nx * nx + nz * nz);
                buffer.addVertex(pose, x1, (float) center.y, z1).setColor(color)
                        .setNormal(pose, nx / length, 0.0F, nz / length).setLineWidth(WIDTH);
                buffer.addVertex(pose, x2, (float) center.y, z2).setColor(color)
                        .setNormal(pose, nx / length, 0.0F, nz / length).setLineWidth(WIDTH);
            }
        });
    }
}
