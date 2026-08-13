package com.traverse.geomancy.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.registry.ModAttachments;

@EventBusSubscriber(modid = Geomancy.MODID)
public final class VibranicRingEvents {
    // Height scales with the square of this, so 0.58 against a vanilla jump's 0.42 lifts about
    // 2.4 blocks rather than 1.25 - a distinctly stronger kick than an ordinary jump.
    private static final double EXTRA_JUMP_POWER = 0.58;
    // Keeps a very early press (still climbing fast) from stacking into a launch.
    private static final double MAX_JUMP_VELOCITY = 0.95;
    // Comfortably covers the ~2.4 blocks the boost adds, so an ordinary double jump lands
    // safely, while a long drop only ever gets this much of it waived.
    private static final double FALL_CREDIT_BLOCKS = 3.5;

    private VibranicRingEvents() {
    }

    // Arm-on-release rather than edge-detect-on-press. Comparing this tick's raw jump input
    // against last tick's looks equivalent but races the network: the input packet can arrive
    // on or after the tick the player has already left the ground, so the very press that
    // caused the ground jump shows up as a false->true edge in mid-air and instantly burns the
    // extra jump - the reported "particle plays on the first jump and you get no second one."
    // Requiring the key to be seen released while airborne cannot race, because a release is
    // only ever observable after the ground jump is already underway.
    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        if (player.onGround()) {
            player.setData(ModAttachments.VIBRANIC_EXTRA_JUMP, true);
            player.setData(ModAttachments.VIBRANIC_JUMP_ARMED, false);
            player.setData(ModAttachments.VIBRANIC_FALL_CREDIT, 0.0D);
            return;
        }
        if (!player.getLastClientInput().jump()) {
            player.setData(ModAttachments.VIBRANIC_JUMP_ARMED, true);
            return;
        }
        if (!player.getData(ModAttachments.VIBRANIC_JUMP_ARMED)
                || !player.getData(ModAttachments.VIBRANIC_EXTRA_JUMP)
                || !VibranicRingItem.isWorn(player)) {
            return;
        }

        player.setData(ModAttachments.VIBRANIC_JUMP_ARMED, false);
        player.setData(ModAttachments.VIBRANIC_EXTRA_JUMP, false);

        // Deliberately not jumpFromGround(): that raises velocity *to* the jump power via
        // Math.max, so pressing while still rising at nearly jump speed adds almost nothing -
        // the "only a quarter block" result. Adding a full jump's worth of impulse on top of
        // any remaining climb makes the boost the same size wherever in the arc it is used.
        Vec3 movement = player.getDeltaMovement();
        double lift = Math.min(Math.max(movement.y, 0.0) + EXTRA_JUMP_POWER, MAX_JUMP_VELOCITY);
        player.setDeltaMovement(movement.x, lift, movement.z);
        player.setData(ModAttachments.VIBRANIC_FALL_CREDIT, FALL_CREDIT_BLOCKS);
        // A player's movement is client-authoritative, so a server-side velocity change is
        // simply overwritten by the next position packet unless the new motion is pushed back
        // to them. This is what turns the jump from a particle effect into actual lift.
        player.hurtMarked = true;
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.6F);
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 12, 0.3, 0.05, 0.3, 0.02);
    }

    // Subtracts the credit instead of cancelling the fall: stepping off a cliff and double
    // jumping on the way down still deals almost all of the damage, because only the handful
    // of blocks the jump itself added is ever waived.
    @SubscribeEvent
    static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        double credit = player.getData(ModAttachments.VIBRANIC_FALL_CREDIT);
        if (credit <= 0.0D) {
            return;
        }
        player.setData(ModAttachments.VIBRANIC_FALL_CREDIT, 0.0D);
        event.setDistance(Math.max(0.0D, event.getDistance() - credit));
    }
}
