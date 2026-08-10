package com.traverse.geomancy.item;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import com.traverse.geomancy.network.GeodeRevealPayload;
import com.traverse.geomancy.prospecting.GeodeSurvey;

public class IronTuningForkItem extends Item {
    private static final int COOLDOWN_TICKS = 60;
    private static final int DURABILITY_COST = 1;

    // The reading is chunk-wide: particles ring the player instead of pointing anywhere, so
    // the fork narrows the search to a column without revealing a bearing.
    private static final double RING_RADIUS = 2.2;
    private static final int RING_POINTS = 16;
    private static final int PULSE_DURATION_TICKS = 14;
    private static final int PULSE_COLOR = 0x9A5CC6;

    private static final int CLEAR_COUNT = 6;
    private static final int STRONG_COUNT = 18;
    private static final int VERTICAL_DEADBAND = 8;

    // Close enough that the player has already dug to the right depth: the chunk reading
    // narrows the column, this confirms the find and shows its shape through the stone.
    private static final int REVEAL_RADIUS = 20;

    public IronTuningForkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        // The cost models the strike itself, so a barren reading is not free to spam.
        stack.hurtAndBreak(DURABILITY_COST, player, hand);

        GeodeSurvey.ChunkSignal signal = GeodeSurvey.surveyChunk(serverLevel, player.chunkPosition());
        if (signal.isEmpty()) {
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.7F, 0.5F);
            feedback(player, "silent");
            return InteractionResult.SUCCESS_SERVER;
        }

        float strength = Mth.clamp((float) signal.count() / STRONG_COUNT, 0.0F, 1.0F);
        ringPulse(serverLevel, player, strength);

        float pitch = 0.7F + 0.9F * strength;
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.9F, pitch);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, 0.4F, pitch);

        reveal(serverLevel, player);
        feedbackReading(player, signal.count(), signal.centroidY() - Mth.floor(player.getY()));
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void reveal(ServerLevel level, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        List<BlockPos> nearby = GeodeSurvey.findNearby(level, player.getEyePosition(), REVEAL_RADIUS,
                GeodeRevealPayload.MAX_POSITIONS);
        if (nearby.isEmpty()) {
            return;
        }
        PacketDistributor.sendToPlayer(serverPlayer, new GeodeRevealPayload(nearby));
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
                0.8F, 1.6F);
    }

    // Particles rise from a ring on the ground and converge on the player, reading as the
    // chunk answering rather than as a direction marker.
    private static void ringPulse(ServerLevel level, Player player, float strength) {
        Vec3 centre = new Vec3(player.getX(), player.getY() + 0.1, player.getZ());
        Vec3 target = centre.add(0.0, 1.0 + strength, 0.0);
        int perPoint = 1 + Mth.floor(strength * 2.0F);
        for (int i = 0; i < RING_POINTS; i++) {
            double angle = (Math.PI * 2.0 / RING_POINTS) * i;
            double x = centre.x + Math.cos(angle) * RING_RADIUS;
            double z = centre.z + Math.sin(angle) * RING_RADIUS;
            level.sendParticles(new TrailParticleOption(target, PULSE_COLOR, PULSE_DURATION_TICKS), false, true,
                    x, centre.y, z, perPoint, 0.05, 0.15, 0.05, 0.0);
        }
    }

    private static void feedbackReading(Player player, int count, int deltaY) {
        String band = count >= STRONG_COUNT ? "strong" : count >= CLEAR_COUNT ? "clear" : "faint";
        String vertical = deltaY < -VERTICAL_DEADBAND ? "below" : deltaY > VERTICAL_DEADBAND ? "above" : "level";
        feedback(player, band, Component.translatable("item.geomancy.iron_tuning_fork." + vertical));
    }

    private static void feedback(Player player, String key, Object... args) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("item.geomancy.iron_tuning_fork." + key, args), true);
        }
    }
}
