package com.traverse.geomancy.resonance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.registry.ModTags;

// Resonance behaves like sound: it travels a straight line and is weakened by whatever
// solid matter it crosses. Never reuse network.LineOfSight here - it clips to the first
// hit and answers yes/no, whereas dampening must walk every block on the line and
// accumulate loss.
public final class ResonanceBeam {
    public static final float BASE_DAMPENING_FACTOR = 0.6F;

    private ResonanceBeam() {
    }

    // Fraction of a pulse that survives the straight line from `from` to `to`, in [0, 1].
    // Only blocks strictly between the two endpoints are tested. Never force-loads: an
    // unloaded interior block is treated as passable rather than fetched.
    public static float pathMultiplier(Level level, BlockPos from, BlockPos to, Optional<Holder<ResonanceType>> type) {
        float resistance = type.map(holder -> holder.value().dampeningResistance()).orElse(0.0F);
        float factor = 1.0F - (1.0F - BASE_DAMPENING_FACTOR) * (1.0F - resistance);

        float multiplier = 1.0F;
        for (BlockPos pos : path(from, to)) {
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(ModTags.RESONANCE_MUFFLER)) {
                return 0.0F;
            }
            if (state.isSolidRender()) {
                multiplier *= factor;
            }
        }
        return multiplier;
    }

    // Amanatides-Woo voxel traversal along the segment between block centers, excluding
    // both endpoints. A fine-sampled walk would double-count or skip cells at shallow
    // angles; this visits each crossed cell exactly once.
    private static List<BlockPos> path(BlockPos from, BlockPos to) {
        if (from.equals(to)) {
            return List.of();
        }

        Vec3 start = Vec3.atCenterOf(from);
        Vec3 end = Vec3.atCenterOf(to);
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;

        int x = from.getX();
        int y = from.getY();
        int z = from.getZ();
        int endX = to.getX();
        int endY = to.getY();
        int endZ = to.getZ();

        int stepX = Double.compare(dx, 0.0);
        int stepY = Double.compare(dy, 0.0);
        int stepZ = Double.compare(dz, 0.0);

        double tMaxX = stepX == 0 ? Double.POSITIVE_INFINITY : ((x + (stepX > 0 ? 1 : 0)) - start.x) / dx;
        double tMaxY = stepY == 0 ? Double.POSITIVE_INFINITY : ((y + (stepY > 0 ? 1 : 0)) - start.y) / dy;
        double tMaxZ = stepZ == 0 ? Double.POSITIVE_INFINITY : ((z + (stepZ > 0 ? 1 : 0)) - start.z) / dz;

        double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
        double tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
        double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);

        int maxSteps = Math.abs(endX - x) + Math.abs(endY - y) + Math.abs(endZ - z) + 4;
        List<BlockPos> result = new ArrayList<>();
        for (int i = 0; i < maxSteps && !(x == endX && y == endY && z == endZ); i++) {
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                x += stepX;
                tMaxX += tDeltaX;
            } else if (tMaxY < tMaxZ) {
                y += stepY;
                tMaxY += tDeltaY;
            } else {
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
            if (!(x == endX && y == endY && z == endZ)) {
                result.add(new BlockPos(x, y, z));
            }
        }
        return result;
    }
}
