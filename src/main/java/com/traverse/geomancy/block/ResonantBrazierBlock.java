package com.traverse.geomancy.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import com.traverse.geomancy.block.entity.ResonantBrazierBlockEntity;
import com.traverse.geomancy.registry.ModBlockEntities;

public class ResonantBrazierBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape SHAPE = Shapes.or(
            Shapes.box(0.125, 0.0, 0.125, 0.875, 0.25, 0.875),
            Shapes.box(0.25, 0.25, 0.25, 0.75, 0.75, 0.75));

    public ResonantBrazierBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
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
        if (!(level.getBlockEntity(pos) instanceof ResonantBrazierBlockEntity brazier)) {
            return InteractionResult.PASS;
        }
        if (stack.is(Items.FLINT_AND_STEEL)) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            if (!brazier.ignite()) {
                return InteractionResult.FAIL;
            }
            // Automation lights the Brazier through a fake player holding Flint and Steel, so
            // this path touches only the held stack and the block entity - never the player's
            // inventory, position, or client connection.
            stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!level.isClientSide() && !brazier.insert(stack)) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ResonantBrazierBlockEntity brazier)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        // Reaching into a burning Brazier destroys the shard rather than returning it.
        if (state.getValue(LIT) && level instanceof ServerLevel serverLevel) {
            return brazier.shatterCrystal(serverLevel) ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
        }
        ItemStack removed = brazier.removeIngredient();
        if (removed.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!player.getInventory().add(removed)) {
            player.drop(removed, false);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof ResonantBrazierBlockEntity brazier) {
            brazier.dropContents(serverLevel, !player.isCreative());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // The route a Dispenser's Flint and Steel takes, the same one that lights campfires and
    // candles. It sets the returned state directly rather than going through the block entity,
    // so the shard has to be checked here or a Dispenser could light an empty Brazier.
    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility ability,
            boolean simulate) {
        if (ability == ItemAbilities.FIRESTARTER_LIGHT && !state.getValue(LIT)
                && context.getLevel().getBlockEntity(context.getClickedPos())
                        instanceof ResonantBrazierBlockEntity brazier
                && !brazier.crystal().isEmpty()) {
            if (!simulate) {
                context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.FLINTANDSTEEL_USE,
                        SoundSource.BLOCKS, 1.0F, 1.1F);
            }
            return state.setValue(LIT, true);
        }
        return super.getToolModifiedState(state, context, ability, simulate);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    // Whether a shard is loaded, nothing more - burning or not reads the same.
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof ResonantBrazierBlockEntity brazier
                && !brazier.crystal().isEmpty() ? 1 : 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.78;
        double z = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + random.nextGaussian() * 0.08, y,
                z + random.nextGaussian() * 0.08, 0.0, 0.012, 0.0);
        if (random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantBrazierBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return Tickers.helper(type, ModBlockEntities.RESONANT_BRAZIER.get(), ResonantBrazierBlockEntity::serverTick);
    }
}
