package com.traverse.geomancy.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;

public class GeomancerBellItem extends Item {
    private static final int RADIUS = 16;
    private static final int COOLDOWN_TICKS = 40;
    private static final int MIN_TRAVEL_TICKS = 5;

    public GeomancerBellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.getCooldowns().addCooldown(player.getItemInHand(hand), COOLDOWN_TICKS);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        serverLevel.playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);

        Vec3 ringOrigin = new Vec3(player.getX(), player.getEyeY() - 0.3, player.getZ());
        for (TectonicNodeBlockEntity node : nodesInRange(serverLevel, ringOrigin)) {
            BlockPos nodePos = node.getBlockPos();
            node.reveal(serverLevel);

            int travelTicks = Math.max(MIN_TRAVEL_TICKS, Mth.floor(ringOrigin.distanceTo(Vec3.atCenterOf(nodePos))));
            serverLevel.sendParticles(new VibrationParticleOption(new BlockPositionSource(nodePos), travelTicks),
                    ringOrigin.x, ringOrigin.y, ringOrigin.z, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.playSound(null, nodePos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.5F);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    private static List<TectonicNodeBlockEntity> nodesInRange(ServerLevel level, Vec3 origin) {
        List<TectonicNodeBlockEntity> nodes = new ArrayList<>();
        int minChunkX = SectionPos.blockToSectionCoord(origin.x - RADIUS);
        int maxChunkX = SectionPos.blockToSectionCoord(origin.x + RADIUS);
        int minChunkZ = SectionPos.blockToSectionCoord(origin.z - RADIUS);
        int maxChunkZ = SectionPos.blockToSectionCoord(origin.z + RADIUS);

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof TectonicNodeBlockEntity node
                            && Vec3.atCenterOf(node.getBlockPos()).closerThan(origin, RADIUS)) {
                        nodes.add(node);
                    }
                }
            }
        }
        return nodes;
    }
}
