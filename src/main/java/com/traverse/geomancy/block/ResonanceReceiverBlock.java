package com.traverse.geomancy.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.traverse.geomancy.block.entity.ResonanceReceiverBlockEntity;
import com.traverse.geomancy.resonance.ResonanceStorage;
import com.traverse.geomancy.resonance.ResonanceType;

// Placed on a face of any resonance-accepting device; inserts arriving resonance into
// whatever it's mounted against, spilling into its own small buffer if the host can't
// take it all - which also makes the receiver itself a valid mount point for another
// emitter, so hops can relay resonance further than one emitter's reach.
public class ResonanceReceiverBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public ResonanceReceiverBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FaceMountShapes.forFacing(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos hostPos = pos.relative(state.getValue(FACING).getOpposite());
        return !level.getBlockState(hostPos).isAir();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return directionToNeighbour == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonanceReceiverBlockEntity(pos, state);
    }

    // Returns the amount actually accepted, matching ResonanceStorage#insertResonance.
    // Tries the host behind it first; whatever the host won't take spills into the
    // receiver's own buffer instead of being lost.
    public static int insert(Level level, BlockPos receiverPos, BlockState receiverState,
            @Nullable ResourceKey<ResonanceType> type, int amount, boolean simulate) {
        BlockPos hostPos = receiverPos.relative(receiverState.getValue(FACING).getOpposite());
        int remaining = Math.max(amount, 0);
        if (level.getBlockEntity(hostPos) instanceof ResonanceStorage host) {
            remaining -= host.insertResonance(type, remaining, simulate);
        }
        if (remaining > 0 && level.getBlockEntity(receiverPos) instanceof ResonanceStorage self) {
            remaining -= self.insertResonance(type, remaining, simulate);
        }
        return amount - remaining;
    }
}
