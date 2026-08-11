package com.traverse.geomancy.block;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// Thin plate hugging whichever face a device is mounted against, shared by the Emitter
// and Receiver since both attach the same way.
final class FaceMountShapes {
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        SHAPES.put(Direction.NORTH, Shapes.box(0.2, 0.2, 0.75, 0.8, 0.8, 1.0));
        SHAPES.put(Direction.SOUTH, Shapes.box(0.2, 0.2, 0.0, 0.8, 0.8, 0.25));
        SHAPES.put(Direction.EAST, Shapes.box(0.0, 0.2, 0.2, 0.25, 0.8, 0.8));
        SHAPES.put(Direction.WEST, Shapes.box(0.75, 0.2, 0.2, 1.0, 0.8, 0.8));
        SHAPES.put(Direction.UP, Shapes.box(0.2, 0.0, 0.2, 0.8, 0.25, 0.8));
        SHAPES.put(Direction.DOWN, Shapes.box(0.2, 0.75, 0.2, 0.8, 1.0, 0.8));
    }

    private FaceMountShapes() {
    }

    // `facing` points away from the block this device is mounted against, so the plate
    // hugs the matching side of the device's own cell (FACING north -> host to the south
    // -> plate hugs the cell's south face).
    static VoxelShape forFacing(Direction facing) {
        return SHAPES.get(facing);
    }
}
