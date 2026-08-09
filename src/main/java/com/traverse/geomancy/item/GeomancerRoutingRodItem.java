package com.traverse.geomancy.item;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.traverse.geomancy.block.entity.ResonancePillarBlockEntity;
import com.traverse.geomancy.network.LinkResult;
import com.traverse.geomancy.registry.ModDataComponents;

public class GeomancerRoutingRodItem extends Item {
    public GeomancerRoutingRodItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.getItemInHand(hand).remove(ModDataComponents.LINKED_SOURCE.get());
            feedback(player, "item.geomancy.geomancer_routing_rod.cleared");
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        BlockPos controllerPos = stack.get(ModDataComponents.LINKED_SOURCE.get());

        if (controllerPos == null) {
            if (!(level.getBlockEntity(clicked) instanceof ResonancePillarBlockEntity)) {
                feedback(player, "item.geomancy.geomancer_routing_rod.need_pillar");
                return fail(level, clicked);
            }
            stack.set(ModDataComponents.LINKED_SOURCE.get(), clicked);
            feedback(player, "item.geomancy.geomancer_routing_rod.selected");
            play(level, clicked, 1.2F);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!(level.getBlockEntity(controllerPos) instanceof ResonancePillarBlockEntity pillar)) {
            stack.remove(ModDataComponents.LINKED_SOURCE.get());
            feedback(player, "item.geomancy.geomancer_routing_rod.source_gone");
            return fail(level, clicked);
        }
        if (pillar.logisticsTargets().contains(clicked)) {
            pillar.removeLogisticsTarget(clicked);
            feedback(player, "item.geomancy.geomancer_routing_rod.removed");
            return InteractionResult.SUCCESS_SERVER;
        }

        boolean selectingPickup = pillar.logisticsPickup() == null;
        LinkResult result = pillar.addLogisticsTarget(level, clicked);
        if (result != LinkResult.OK) {
            feedback(player, failureKey(result));
            return fail(level, clicked);
        }
        feedback(player, selectingPickup ? "item.geomancy.geomancer_routing_rod.pickup_linked"
                : "item.geomancy.geomancer_routing_rod.output_linked");
        play(level, clicked, 1.5F);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static String failureKey(LinkResult result) {
        String suffix = switch (result) {
            case OK -> throw new IllegalStateException("OK is not a failure");
            case NO_SOURCE -> "not_inventory";
            case TOO_FAR -> "too_far";
            case OBSTRUCTED -> "obstructed";
            case CYCLE, TOO_DEEP -> "invalid";
        };
        return "item.geomancy.geomancer_routing_rod." + suffix;
    }

    private static InteractionResult fail(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 0.5F);
        return InteractionResult.FAIL;
    }

    private static void play(Level level, BlockPos pos, float pitch) {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_FALL, SoundSource.PLAYERS, 1.0F, pitch);
    }

    private static void feedback(@Nullable Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(key), true);
        }
    }
}
