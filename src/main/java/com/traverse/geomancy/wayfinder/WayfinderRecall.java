package com.traverse.geomancy.wayfinder;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public final class WayfinderRecall {
    public static final int BASE_COST = 20;
    public static final int COST_DISTANCE_STEP = 100;
    private static final int LANDING_HORIZONTAL_RADIUS = 2;
    private static final int[] LANDING_VERTICAL_OFFSETS = {0, 1, -1, 2};

    private WayfinderRecall() {
    }

    // cost = 20 x max(1, ceil(distance / 100)), per the early-game specification.
    public static int cost(double distance) {
        return BASE_COST * Math.max(1, (int) Math.ceil(distance / COST_DISTANCE_STEP));
    }

    // Checks the intended arrival cell in front of the anchor first, then expands outward
    // in square rings across a small set of vertical offsets, returning the feet position
    // of the first pair of vertically stacked passable cells found.
    public static Optional<BlockPos> findSafeLanding(ServerLevel level, BlockPos anchorPos, Direction facing) {
        BlockPos front = anchorPos.relative(facing);
        for (int dy : LANDING_VERTICAL_OFFSETS) {
            for (int radius = 0; radius <= LANDING_HORIZONTAL_RADIUS; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                            continue;
                        }
                        BlockPos feet = front.offset(dx, dy, dz);
                        if (passable(level, feet) && passable(level, feet.above())) {
                            return Optional.of(feet);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static boolean passable(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).blocksMotion();
    }
}
