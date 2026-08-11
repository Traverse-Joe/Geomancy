package com.traverse.geomancy.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.traverse.geomancy.block.entity.ResonantHearthBlockEntity;
import com.traverse.geomancy.item.ResonantTuningForkItem;

public class ResonantHearthBlock extends Block implements EntityBlock {
    // The current model is a solid altar on corner legs, not an open-fronted basin, so a
    // single box matching its footprint (full width) and height (up to the rim at y=13/16)
    // fits better than the old walled-basin composite.
    private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0);

    public ResonantHearthBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.getBlockEntity(pos) instanceof ResonantHearthBlockEntity hearth) {
            if (stack.getItem() instanceof ResonantTuningForkItem) {
                if (!level.isClientSide()) {
                    hearth.activate(player);
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.is(com.traverse.geomancy.registry.ModItems.RESONANT_AMETHYST_FOCUS.get())) {
                if (!level.isClientSide() && !hearth.installFocus(stack)) {
                    return InteractionResult.FAIL;
                }
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide() && !hearth.addIngredient(stack)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ResonantHearthBlockEntity hearth)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack removed = player.isShiftKeyDown() ? hearth.removeFocus() : hearth.removeLastIngredient();
            if (removed.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (!player.getInventory().add(removed)) {
                player.drop(removed, false);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && !player.isCreative()
                && level.getBlockEntity(pos) instanceof ResonantHearthBlockEntity hearth) {
            hearth.dropIngredients(serverLevel);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantHearthBlockEntity(pos, state);
    }
}
