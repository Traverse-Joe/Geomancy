package com.traverse.geomancy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class LineOfSight {
    private LineOfSight() {
    }

    public static boolean clear(Level level, BlockPos from, BlockPos to) {
        Vec3 end = Vec3.atCenterOf(to);
        Vec3 start = outsideSource(Vec3.atCenterOf(from), end);
        return clear(level, start, end, to);
    }

    public static boolean clearToTop(Level level, BlockPos from, BlockPos to) {
        Vec3 end = Vec3.atBottomCenterOf(to).add(0.0D, 1.01D, 0.0D);
        Vec3 start = outsideSource(Vec3.atCenterOf(from), end);
        return clear(level, start, end, to);
    }

    public static boolean clearFromTop(Level level, BlockPos from, BlockPos to) {
        Vec3 start = Vec3.atBottomCenterOf(from).add(0.0D, 1.01D, 0.0D);
        Vec3 end = Vec3.atCenterOf(to);
        return clear(level, start, end, to);
    }

    private static boolean clear(Level level, Vec3 start, Vec3 end, BlockPos allowedHit) {
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(allowedHit);
    }

    private static Vec3 outsideSource(Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start);
        double largestAxis = Math.max(Math.abs(direction.x), Math.max(Math.abs(direction.y), Math.abs(direction.z)));
        if (largestAxis == 0.0) {
            return start;
        }
        return start.add(direction.scale((0.5001 / largestAxis)));
    }
}
