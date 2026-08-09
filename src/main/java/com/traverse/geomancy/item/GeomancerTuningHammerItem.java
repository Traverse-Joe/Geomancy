package com.traverse.geomancy.item;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.traverse.geomancy.block.entity.EssenceWorkerBlockEntity;
import com.traverse.geomancy.block.entity.ItemPedestalBlockEntity;
import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;
import com.traverse.geomancy.network.EssenceProvider;
import com.traverse.geomancy.network.EssenceRelay;
import com.traverse.geomancy.network.LineOfSight;
import com.traverse.geomancy.network.LinkResult;
import com.traverse.geomancy.registry.ModDataComponents;

public class GeomancerTuningHammerItem extends Item {
    public GeomancerTuningHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            stack.remove(ModDataComponents.LINKED_SOURCE.get());
            feedback(player, "item.geomancy.geomancer_tuning_hammer.source_cleared");
        }
        return InteractionResult.SUCCESS;
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
        BlockEntity clickedBe = level.getBlockEntity(clicked);

        if (player != null && player.isShiftKeyDown() && clickedBe instanceof EssenceRelay relay) {
            relay.setUpstream(level, null);
            play(level, clicked, SoundEvents.AMETHYST_BLOCK_FALL, 1.0F, 0.8F);
            feedback(player, "item.geomancy.geomancer_tuning_hammer.relay_unlinked");
            return InteractionResult.SUCCESS_SERVER;
        }

        BlockPos sourcePos = stack.get(ModDataComponents.LINKED_SOURCE.get());

        // A source is already selected and the clicked block can receive a link: attempt
        // to chain it in, rather than re-selecting it (a relay is also an EssenceProvider,
        // so this has to be checked before the generic "select as source" branch below).
        if (sourcePos != null && clickedBe instanceof EssenceRelay relay) {
            LinkResult result = relay.setUpstream(level, sourcePos);
            if (result != LinkResult.OK) {
                feedback(player, linkFailureKey(result));
                return fail(level, clicked);
            }
            if (clickedBe instanceof ItemPedestalBlockEntity) {
                stack.remove(ModDataComponents.LINKED_SOURCE.get());
            } else {
                stack.set(ModDataComponents.LINKED_SOURCE.get(), clicked);
            }
            play(level, clicked, SoundEvents.AMETHYST_BLOCK_FALL, 1.0F, 1.2F);
            feedback(player, "item.geomancy.geomancer_tuning_hammer.linked_relay");
            return InteractionResult.SUCCESS_SERVER;
        }

        if (clickedBe instanceof EssenceProvider) {
            stack.set(ModDataComponents.LINKED_SOURCE.get(), clicked);
            if (clickedBe instanceof TectonicNodeBlockEntity node) {
                node.reveal(level);
            }
            play(level, clicked, SoundEvents.AMETHYST_BLOCK_FALL, 1.0F, 1.2F);
            feedback(player, "item.geomancy.geomancer_tuning_hammer.attuned");
            return InteractionResult.SUCCESS_SERVER;
        }

        if (sourcePos == null) {
            feedback(player, "item.geomancy.geomancer_tuning_hammer.no_source");
            return fail(level, clicked);
        }

        if (!(level.getBlockEntity(sourcePos) instanceof EssenceWorkerBlockEntity worker)) {
            stack.remove(ModDataComponents.LINKED_SOURCE.get());
            feedback(player, "item.geomancy.geomancer_tuning_hammer.source_gone");
            return fail(level, clicked);
        }

        int range = worker.workRange();
        if (sourcePos.distSqr(clicked) > (double) range * range) {
            feedback(player, "item.geomancy.geomancer_tuning_hammer.out_of_range");
            return fail(level, clicked);
        }

        if (!LineOfSight.clear(level, sourcePos, clicked)) {
            feedback(player, "item.geomancy.geomancer_tuning_hammer.obstructed");
            return fail(level, clicked);
        }

        if (!worker.startJob(level, clicked)) {
            feedback(player, "item.geomancy.geomancer_tuning_hammer.no_recipe");
            return fail(level, clicked);
        }

        stack.remove(ModDataComponents.LINKED_SOURCE.get());
        play(level, clicked, SoundEvents.AMETHYST_BLOCK_HIT, 1.0F, 1.4F);
        feedback(player, "item.geomancy.geomancer_tuning_hammer.linked");
        return InteractionResult.SUCCESS_SERVER;
    }

    private static String linkFailureKey(LinkResult result) {
        String suffix = switch (result) {
            case OK -> throw new IllegalStateException("OK is not a failure");
            case NO_SOURCE -> "no_source";
            case TOO_FAR -> "link_too_far";
            case OBSTRUCTED -> "link_obstructed";
            case CYCLE -> "link_cycle";
            case TOO_DEEP -> "link_too_deep";
        };
        return "item.geomancy.geomancer_tuning_hammer." + suffix;
    }

    private static void play(Level level, BlockPos pos, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, pos, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static InteractionResult fail(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.AMETHYST_BLOCK_CHIME, 0.7F, 0.5F);
        return InteractionResult.FAIL;
    }

    private static void feedback(@Nullable Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(key), true);
        }
    }
}
