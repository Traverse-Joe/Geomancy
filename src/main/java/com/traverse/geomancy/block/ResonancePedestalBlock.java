package com.traverse.geomancy.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.traverse.geomancy.block.entity.ResonancePedestalBlockEntity;
import com.traverse.geomancy.registry.ModBlockEntities;

public class ResonancePedestalBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Shapes.box(0.0625, 0.0, 0.0625, 0.9375, 0.3125, 0.9375),
            Shapes.box(0.25, 0.3125, 0.25, 0.75, 0.6875, 0.75),
            Shapes.box(0.125, 0.6875, 0.125, 0.875, 0.9375, 0.875));

    public ResonancePedestalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ResonancePedestalBlockEntity pedestal) || !pedestal.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            pedestal.setHeld(stack.split(1));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ResonancePedestalBlockEntity pedestal) || pedestal.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack stack = pedestal.removeHeld();
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative()
                && level.getBlockEntity(pos) instanceof ResonancePedestalBlockEntity pedestal && !pedestal.isEmpty()) {
            Block.popResource(level, pos, pedestal.removeHeld());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonancePedestalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return Tickers.helper(type, ModBlockEntities.RESONANCE_PEDESTAL.get(),
                level.isClientSide() ? ResonancePedestalBlockEntity::clientTick : ResonancePedestalBlockEntity::serverTick);
    }
}
