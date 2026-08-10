package com.traverse.geomancy.item;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.block.HearthCrystalBlock;
import com.traverse.geomancy.registry.ModDataComponents;
import com.traverse.geomancy.resonance.PortableResonance;
import com.traverse.geomancy.wayfinder.WayfinderAnchor;
import com.traverse.geomancy.wayfinder.WayfinderRecall;

public class ResonantWayfinderItem extends Item {
    public static final int CHANNEL_DURATION_TICKS = 100;
    private static final int RECALL_COOLDOWN_TICKS = 100;
    private static final int PULSE_INTERVAL_TICKS = 10;

    public ResonantWayfinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof HearthCrystalBlock)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        stack.set(ModDataComponents.WAYFINDER_ANCHOR.get(),
                new WayfinderAnchor(context.getLevel().dimension(), context.getClickedPos().immutable()));
        context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 1.0F, 1.2F);
        feedback(context.getPlayer(), "bound", context.getClickedPos().getX(), context.getClickedPos().getY(),
                context.getClickedPos().getZ());
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        WayfinderAnchor anchor = stack.get(ModDataComponents.WAYFINDER_ANCHOR.get());
        if (anchor == null) {
            feedback(player, "not_bound");
            return InteractionResult.FAIL;
        }
        if (player.isPassenger() || player.isSleeping() || player.isUsingItem()) {
            feedback(player, "busy");
            return InteractionResult.FAIL;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            double distance = player.position().distanceTo(Vec3.atCenterOf(anchor.pos()));
            feedback(serverPlayer, "channeling", (int) Math.ceil(distance), WayfinderRecall.cost(distance));
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return CHANNEL_DURATION_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int ticksRemaining) {
        if (!(level instanceof ServerLevel serverLevel) || ticksRemaining % PULSE_INTERVAL_TICKS != 0) {
            return;
        }
        WayfinderAnchor anchor = stack.get(ModDataComponents.WAYFINDER_ANCHOR.get());
        if (anchor == null || !anchor.dimension().equals(serverLevel.dimension()) || !serverLevel.isLoaded(anchor.pos())) {
            return;
        }
        BlockPos pos = anchor.pos();
        serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                10, 0.3, 0.3, 0.3, 0.02);
        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6F, 1.6F);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return stack;
        }

        WayfinderAnchor anchor = stack.get(ModDataComponents.WAYFINDER_ANCHOR.get());
        if (anchor == null) {
            feedback(player, "not_bound");
            return stack;
        }
        if (!anchor.dimension().equals(serverLevel.dimension())) {
            feedback(player, "wrong_dimension");
            return stack;
        }
        if (!serverLevel.isLoaded(anchor.pos())
                || !(serverLevel.getBlockState(anchor.pos()).getBlock() instanceof HearthCrystalBlock)) {
            feedback(player, "missing_anchor");
            return stack;
        }

        Direction facing = serverLevel.getBlockState(anchor.pos()).getValue(HearthCrystalBlock.FACING);
        double distance = player.position().distanceTo(Vec3.atCenterOf(anchor.pos()));
        int cost = WayfinderRecall.cost(distance);

        Optional<BlockPos> landing = WayfinderRecall.findSafeLanding(serverLevel, anchor.pos(), facing);
        if (landing.isEmpty()) {
            feedback(player, "obstructed");
            return stack;
        }
        if (!PortableResonance.consume(player, cost)) {
            feedback(player, "insufficient_resonance", cost);
            return stack;
        }

        Vec3 origin = player.position();
        BlockPos dest = landing.get();
        player.teleportTo(serverLevel, dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5, Set.of(),
                player.getYRot(), player.getXRot(), false);
        player.getCooldowns().addCooldown(stack, RECALL_COOLDOWN_TICKS);

        serverLevel.playSound(null, BlockPos.containing(origin), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        serverLevel.playSound(null, dest, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.PORTAL, dest.getX() + 0.5, dest.getY() + 1.0, dest.getZ() + 0.5,
                40, 0.4, 0.6, 0.4, 0.1);
        feedback(player, "recalled");
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        WayfinderAnchor anchor = stack.get(ModDataComponents.WAYFINDER_ANCHOR.get());
        if (anchor != null) {
            BlockPos pos = anchor.pos();
            tooltip.accept(Component.translatable("item.geomancy.resonant_wayfinder.bound",
                    pos.getX(), pos.getY(), pos.getZ()));
        } else {
            tooltip.accept(Component.translatable("item.geomancy.resonant_wayfinder.not_bound"));
        }
    }

    // releaseUsing/onStoppedUsing are intentionally not overridden: the default no-op
    // behavior on an early manual release or a damage-triggered stopUsingItem() already
    // means neither path calls finishUsingItem, so neither can charge or teleport.

    private static void feedback(Player player, String key, Object... args) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("item.geomancy.resonant_wayfinder." + key, args), true);
        }
    }
}
