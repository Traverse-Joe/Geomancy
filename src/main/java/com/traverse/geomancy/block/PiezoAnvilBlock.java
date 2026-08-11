package com.traverse.geomancy.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;

import com.traverse.geomancy.block.entity.PiezoAnvilBlockEntity;
import com.traverse.geomancy.resonance.ResonanceStorage;

public class PiezoAnvilBlock extends Block implements EntityBlock {
    public PiezoAnvilBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance);
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof FallingBlockEntity falling)
                || !falling.getBlockState().is(BlockTags.ANVIL) || fallDistance < 2.0) {
            return;
        }

        int remaining = Mth.clamp((int) Math.round(fallDistance * 100.0), 100, 2_000);
        int generated = remaining;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(pos.relative(direction)) instanceof ResonanceStorage storage) {
                remaining -= storage.insertResonance(remaining, false);
                if (remaining == 0) {
                    break;
                }
            }
        }
        if (remaining > 0 && level.getBlockEntity(pos) instanceof ResonanceStorage self) {
            remaining -= self.insertResonance(remaining, false);
        }

        if (remaining < generated) {
            serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.25F,
                    Mth.clamp(0.75F + generated / 4_000.0F, 0.75F, 1.25F));
            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    24, 0.55, 0.12, 0.55, 0.08);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PiezoAnvilBlockEntity(pos, state);
    }
}
