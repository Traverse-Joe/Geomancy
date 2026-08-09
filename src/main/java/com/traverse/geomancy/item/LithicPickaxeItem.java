package com.traverse.geomancy.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import com.traverse.geomancy.resonance.PortableResonance;

public class LithicPickaxeItem extends Item {
    private static final int GROWTH_COST = 100;

    public LithicPickaxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)
                || !context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.BUDDING_AMETHYST)) {
            return context.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        Direction direction = context.getClickedFace();
        BlockPos growthPos = context.getClickedPos().relative(direction);
        BlockState current = level.getBlockState(growthPos);
        Block next = nextStage(current, direction);
        if (next == null || context.getPlayer() == null || !PortableResonance.consume(context.getPlayer(), GROWTH_COST)) {
            return InteractionResult.FAIL;
        }

        BlockState grown = next.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, current.getFluidState().is(Fluids.WATER));
        level.setBlockAndUpdate(growthPos, grown);
        context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand().asEquipmentSlot());
        level.playSound(null, growthPos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.4F);
        level.sendParticles(ParticleTypes.END_ROD, growthPos.getX() + 0.5, growthPos.getY() + 0.5,
                growthPos.getZ() + 0.5, 12, 0.25, 0.25, 0.25, 0.03);
        context.getPlayer().getCooldowns().addCooldown(context.getItemInHand(), 10);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static Block nextStage(BlockState state, Direction direction) {
        if (BuddingAmethystBlock.canClusterGrowAtState(state)) return Blocks.SMALL_AMETHYST_BUD;
        if (!state.hasProperty(AmethystClusterBlock.FACING) || state.getValue(AmethystClusterBlock.FACING) != direction) return null;
        if (state.is(Blocks.SMALL_AMETHYST_BUD)) return Blocks.MEDIUM_AMETHYST_BUD;
        if (state.is(Blocks.MEDIUM_AMETHYST_BUD)) return Blocks.LARGE_AMETHYST_BUD;
        if (state.is(Blocks.LARGE_AMETHYST_BUD)) return Blocks.AMETHYST_CLUSTER;
        return null;
    }
}
