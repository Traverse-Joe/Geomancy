package com.traverse.geomancy.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// Has no BlockEntity: its identity as a Wayfinder anchor is fully captured by its
// (dimension, pos) plus this live FACING state, so breaking it invalidates any bound
// Wayfinder's destination for free (the block is simply no longer there to find).
//
// The Pylon model this uses is deliberately oversized (its wings reach roughly a block
// past either side), a look chosen deliberately for this one block as a dramatic
// centerpiece rather than scaled to fit a single cell. Collision stays a normal small
// footprint - only the outline/selection box grows to match the visual - so it doesn't
// block movement or placement in the space the wings occupy.
public class HearthCrystalBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<HearthCrystalBlock> CODEC = simpleCodec(HearthCrystalBlock::new);

    // The wings run along the model's local X axis; NORTH/SOUTH facings (0/180 rotation)
    // keep them on world X, EAST/WEST (90/270) swap them onto world Z.
    private static final VoxelShape OUTLINE_WINGS_X = Shapes.box(-0.35, 0.0, 0.15, 1.35, 1.3, 0.85);
    private static final VoxelShape OUTLINE_WINGS_Z = Shapes.box(0.15, 0.0, -0.35, 0.85, 1.3, 1.35);
    private static final VoxelShape COLLISION_SHAPE = Shapes.box(0.28, 0.0, 0.28, 0.72, 1.0, 0.72);

    public HearthCrystalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<HearthCrystalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? OUTLINE_WINGS_X : OUTLINE_WINGS_Z;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }
}
