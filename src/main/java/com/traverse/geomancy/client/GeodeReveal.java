package com.traverse.geomancy.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterDebugRenderersEvent;

import com.traverse.geomancy.Geomancy;

// Client-only memory of the blocks a tuning fork strike uncovered. Purely cosmetic: it
// decays on its own and is never consulted for gameplay decisions.
@EventBusSubscriber(modid = Geomancy.MODID, value = Dist.CLIENT)
public final class GeodeReveal {
    public static final int DURATION_TICKS = 400;

    private static final int HUM_INTERVAL_TICKS = 12;
    private static final double HUM_RANGE = 24.0;

    private static List<BlockPos> positions = List.of();
    private static int remainingTicks;
    private static int humCooldown;

    private GeodeReveal() {
    }

    public static void accept(List<BlockPos> revealed) {
        positions = List.copyOf(revealed);
        remainingTicks = positions.isEmpty() ? 0 : DURATION_TICKS;
        humCooldown = 0;
    }

    public static List<BlockPos> positions() {
        return remainingTicks > 0 ? positions : List.of();
    }

    // Fades over the last second so the outlines dissolve instead of blinking out.
    public static float alpha() {
        return Mth.clamp(remainingTicks / 20.0F, 0.0F, 1.0F);
    }

    @SubscribeEvent
    static void registerRenderer(RegisterDebugRenderersEvent event) {
        event.register(GeodeRevealRenderer::new);
    }

    @SubscribeEvent
    static void tick(ClientTickEvent.Post event) {
        if (remainingTicks <= 0) {
            return;
        }
        remainingTicks--;
        if (remainingTicks == 0) {
            positions = List.of();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.isPaused()) {
            return;
        }
        if (--humCooldown > 0) {
            return;
        }
        humCooldown = HUM_INTERVAL_TICKS;
        hum(level, player);
    }

    // The hum tightens as the player closes on the nearest signature block, giving a
    // continuous bearing that the one-shot strike feedback cannot.
    private static void hum(ClientLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        double nearestSq = Double.MAX_VALUE;
        for (BlockPos pos : positions) {
            nearestSq = Math.min(nearestSq, eye.distanceToSqr(Vec3.atCenterOf(pos)));
        }
        double nearest = Math.sqrt(nearestSq);
        if (nearest > HUM_RANGE) {
            return;
        }

        float closeness = (float) (1.0 - nearest / HUM_RANGE);
        float volume = 0.15F + 0.45F * closeness;
        float pitch = 0.6F + 0.9F * closeness;
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, volume, pitch, false);
    }
}
