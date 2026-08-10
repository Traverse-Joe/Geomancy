package com.traverse.geomancy.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

// Has no BlockEntity: its identity as a Wayfinder anchor is fully captured by its
// (dimension, pos) plus this live FACING state, so breaking it invalidates any bound
// Wayfinder's destination for free (the block is simply no longer there to find).
public class HearthCrystalBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<HearthCrystalBlock> CODEC = simpleCodec(HearthCrystalBlock::new);

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
}
