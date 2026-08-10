package com.traverse.geomancy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;

// Draws the revealed geode through terrain. Gizmos are the only stock geometry that can be
// flagged always-on-top, which is the whole point of the reveal.
public class GeodeRevealRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final int STROKE = 0xC084FC;
    private static final int FILL = 0x9A5CC6;
    private static final float STROKE_WIDTH = 2.5F;
    private static final int FILL_ALPHA = 60;

    public GeodeRevealRenderer(Minecraft minecraft) {
    }

    @Override
    public void emitGizmos(double cameraX, double cameraY, double cameraZ, DebugValueAccess values, Frustum frustum,
            float partialTicks) {
        float alpha = GeodeReveal.alpha();
        if (alpha <= 0.0F) {
            return;
        }

        GizmoStyle style = GizmoStyle.strokeAndFill(
                ARGB.color(Math.round(255 * alpha), STROKE), STROKE_WIDTH,
                ARGB.color(Math.round(FILL_ALPHA * alpha), FILL));
        for (BlockPos pos : GeodeReveal.positions()) {
            Gizmos.cuboid(pos, style).setAlwaysOnTop();
        }
    }
}
