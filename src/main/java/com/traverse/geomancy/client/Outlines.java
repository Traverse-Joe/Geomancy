package com.traverse.geomancy.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class Outlines {
    public static final float WIDTH = 4.0F;
    // Proud of the block faces so the lines do not z-fight with the target's own surface.
    public static final VoxelShape TARGET_OUTLINE = Shapes.box(-0.005, -0.005, -0.005, 1.005, 1.005, 1.005);

    private Outlines() {
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, VoxelShape shape,
            double offsetX, double offsetY, double offsetZ, int color) {
        collector.submitCustomGeometry(poseStack, RenderTypes.LINES, (pose, buffer) -> {
            PoseStack local = new PoseStack();
            local.last().set(pose);
            ShapeRenderer.renderShape(local, buffer, shape, offsetX, offsetY, offsetZ, color, WIDTH);
        });
    }
}
