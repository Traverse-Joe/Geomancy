package com.traverse.geomancy.item;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;
import com.traverse.geomancy.registry.ModDataComponents;

public class GeomancerTuningHammerItem extends Item {
    private static final int MAX_RANGE = 16;

    public GeomancerTuningHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clicked = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.getBlockEntity(clicked) instanceof TectonicNodeBlockEntity node) {
            stack.set(ModDataComponents.SELECTED_NODE.get(), clicked);
            node.reveal(level);
            chime(level, clicked, 1.4F);
            feedback(player, "item.geomancy.geomancer_tuning_hammer.attuned");
            return InteractionResult.SUCCESS_SERVER;
        }

        BlockPos nodePos = stack.get(ModDataComponents.SELECTED_NODE.get());
        if (nodePos == null) {
            feedback(player, "item.geomancy.geomancer_tuning_hammer.no_node");
            return fail(level, clicked);
        }

        if (!(level.getBlockEntity(nodePos) instanceof TectonicNodeBlockEntity node)) {
            stack.remove(ModDataComponents.SELECTED_NODE.get());
            feedback(player, "item.geomancy.geomancer_tuning_hammer.node_gone");
            return fail(level, clicked);
        }

        if (nodePos.distSqr(clicked) > MAX_RANGE * MAX_RANGE) {
            feedback(player, "item.geomancy.geomancer_tuning_hammer.out_of_range");
            return fail(level, clicked);
        }

        if (!hasLineOfSight(level, nodePos, clicked, player)) {
            feedback(player, "item.geomancy.geomancer_tuning_hammer.obstructed");
            return fail(level, clicked);
        }

        if (!node.startJob(level, clicked)) {
            feedback(player, "item.geomancy.geomancer_tuning_hammer.no_recipe");
            return fail(level, clicked);
        }

        stack.remove(ModDataComponents.SELECTED_NODE.get());
        chime(level, clicked, 1.8F);
        feedback(player, "item.geomancy.geomancer_tuning_hammer.linked");
        return InteractionResult.SUCCESS_SERVER;
    }

    private static boolean hasLineOfSight(Level level, BlockPos from, BlockPos to, Player player) {
        Vec3 start = Vec3.atCenterOf(from);
        Vec3 end = Vec3.atCenterOf(to);
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(to);
    }

    private static void chime(Level level, BlockPos pos, float pitch) {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, pitch);
    }

    private static InteractionResult fail(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 0.5F);
        return InteractionResult.FAIL;
    }

    private static void feedback(@Nullable Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(key), true);
        }
    }
}
