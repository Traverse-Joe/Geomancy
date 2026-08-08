package com.traverse.geomancy.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;

public class GeomancerBellItem extends Item {
    private static final int RADIUS = 16;

    public GeomancerBellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos origin = player.blockPosition();
        int found = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-RADIUS, -RADIUS, -RADIUS), origin.offset(RADIUS, RADIUS, RADIUS))) {
            if (level.getBlockEntity(pos) instanceof TectonicNodeBlockEntity node) {
                node.reveal(level);
                found++;
            }
        }

        level.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, found > 0 ? 1.5F : 0.6F);
        player.getCooldowns().addCooldown(player.getItemInHand(hand), 20);
        return InteractionResult.SUCCESS_SERVER;
    }
}
