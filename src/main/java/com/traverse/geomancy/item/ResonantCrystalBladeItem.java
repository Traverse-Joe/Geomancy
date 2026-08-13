package com.traverse.geomancy.item;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

import com.traverse.geomancy.entity.ResonantWaveProjectile;
import com.traverse.geomancy.registry.ModAttachments;
import com.traverse.geomancy.registry.ModDataComponents;
import com.traverse.geomancy.resonance.PortableResonance;

public class ResonantCrystalBladeItem extends Item {
    private static final int WAVE_COST = 30;
    private static final float WAVE_DAMAGE = 10.0F;
    private static final float WAVE_CHANCE = 0.4F;
    private static final int WAVE_INTERVAL_TICKS = 10;

    private static final int PULSE_COST = 120;
    private static final float PULSE_DAMAGE = 8.0F;
    private static final double PULSE_RADIUS = 4.0;
    private static final double PULSE_KNOCKBACK = 1.1;
    private static final double PULSE_LIFT = 0.35;
    private static final int PULSE_COOLDOWN_TICKS = 60;
    private static final int PULSE_RING_POINTS = 48;
    private static final DustParticleOptions PULSE_DUST =
            new DustParticleOptions(ResonantWaveProjectile.AMETHYST_HIGHLIGHT, 2.0F);

    public ResonantCrystalBladeItem(Properties properties) {
        super(properties);
    }

    // Must return CONSUME, never SUCCESS/SUCCESS_SERVER: those carry a SwingSource, and the
    // resulting arm swing reaches onEntitySwing below, throwing a wave on right-click.
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                return InteractionResult.CONSUME;
            }
            toggleActivated((ServerLevel) level, player, stack);
            return InteractionResult.CONSUME;
        }
        if (!isActivated(stack)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        return concussionPulse(serverLevel, player, stack) ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    // Always returns false so the ordinary melee swing still resolves.
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !isActivated(stack)
                || !(entity instanceof ServerPlayer player) || !(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        // Absolute deadline rather than a subtraction, so no sentinel default can wrap around
        // and permanently suppress the wave.
        long now = level.getGameTime();
        long last = player.getData(ModAttachments.LAST_WAVE_TICK);
        if (now < last + WAVE_INTERVAL_TICKS && now >= last) {
            return false;
        }
        // Rolled before anything is spent, so resonance is charged per wave, not per swing.
        if (level.getRandom().nextFloat() >= WAVE_CHANCE) {
            return false;
        }
        if (!PortableResonance.consume(player, WAVE_COST)) {
            feedback(player, "insufficient_resonance", WAVE_COST);
            return false;
        }
        player.setData(ModAttachments.LAST_WAVE_TICK, now);
        fireWave(level, player);
        return false;
    }

    private void toggleActivated(ServerLevel level, Player player, ItemStack stack) {
        boolean active = !isActivated(stack);
        stack.set(ModDataComponents.BLADE_ACTIVATED.get(), active);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                0.8F, active ? 1.6F : 1.0F);
        feedback(player, active ? "activated" : "deactivated");
    }

    private static boolean isActivated(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.BLADE_ACTIVATED.get(), Boolean.FALSE);
    }

    private static void fireWave(ServerLevel level, Player player) {
        ResonantWaveProjectile wave = new ResonantWaveProjectile(level, player);
        wave.setDamage(WAVE_DAMAGE);
        wave.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, ResonantWaveProjectile.SPEED, 0.0F);
        level.addFreshEntity(wave);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.6F, 1.4F);
    }

    private boolean concussionPulse(ServerLevel level, Player player, ItemStack stack) {
        if (!PortableResonance.consume(player, PULSE_COST)) {
            feedback(player, "insufficient_resonance", PULSE_COST);
            return false;
        }

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(PULSE_RADIUS), entity -> entity != player && entity.isAlive())) {
            if (target.distanceToSqr(player) > PULSE_RADIUS * PULSE_RADIUS) {
                continue;
            }
            target.hurtServer(level, level.damageSources().sonicBoom(player), PULSE_DAMAGE);
            target.knockback(PULSE_KNOCKBACK, target.getX() - player.getX(), target.getZ() - player.getZ());
            target.addDeltaMovement(new Vec3(0.0, PULSE_LIFT, 0.0));
            // Needed for player targets, whose own client would otherwise discard this.
            target.hurtMarked = true;
        }

        double cx = player.getX();
        double cy = player.getY() + 1.0;
        double cz = player.getZ();
        // Dust is a far smaller particle than the SONIC_BOOM this replaced, so the ring needs
        // enough points to still read as a closed circle at PULSE_RADIUS.
        for (int i = 0; i < PULSE_RING_POINTS; i++) {
            double angle = i * (Math.PI * 2.0 / PULSE_RING_POINTS);
            double x = cx + Math.cos(angle) * PULSE_RADIUS;
            double z = cz + Math.sin(angle) * PULSE_RADIUS;
            level.sendParticles(PULSE_DUST, x, cy, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0F, 0.9F);
        player.getCooldowns().addCooldown(stack, PULSE_COOLDOWN_TICKS);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(isActivated(stack)
                ? "item.geomancy.resonant_crystal_blade.tooltip_active"
                : "item.geomancy.resonant_crystal_blade.tooltip_inactive"));
    }

    private static void feedback(Player player, String key, Object... args) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("item.geomancy.resonant_crystal_blade." + key, args), true);
        }
    }
}
