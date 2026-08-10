package com.traverse.geomancy.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import com.traverse.geomancy.Geomancy;

// Striking a budding block shakes its ripe clusters loose. Only fully grown clusters fall;
// buds are left on the block so a strike never destroys growth the player is waiting on.
@EventBusSubscriber(modid = Geomancy.MODID)
public final class TuningForkStrikeEvents {
    private static final int DURABILITY_COST = 1;

    private TuningForkStrikeEvents() {
    }

    @SubscribeEvent
    static void onStrike(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof IronTuningForkItem)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (!event.getLevel().getBlockState(pos).is(Blocks.BUDDING_AMETHYST)) {
            return;
        }

        // Cancelled on both sides so the block never shows mining progress; a strike
        // resonates the crystal rather than chipping at it.
        event.setCanceled(true);
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Player player = event.getEntity();
        List<BlockPos> ripe = ripeClusters(level, pos);

        // The block answers whether or not anything was ready to fall.
        float pitch = 0.7F + 0.5F * Mth.clamp(ripe.size() / 4.0F, 0.0F, 1.0F);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, pitch);
        if (ripe.isEmpty()) {
            return;
        }

        for (BlockPos clusterPos : ripe) {
            level.destroyBlock(clusterPos, true, player, 512);
        }
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.2F);
        stack.hurtAndBreak(DURABILITY_COST, player, event.getHand());
    }

    private static List<BlockPos> ripeClusters(ServerLevel level, BlockPos buddingPos) {
        List<BlockPos> ripe = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos side = buddingPos.relative(direction);
            BlockState state = level.getBlockState(side);
            // The facing check keeps a neighbouring geode's clusters from being pulled in.
            if (state.is(Blocks.AMETHYST_CLUSTER) && state.getValue(AmethystClusterBlock.FACING) == direction) {
                ripe.add(side);
            }
        }
        return ripe;
    }
}
