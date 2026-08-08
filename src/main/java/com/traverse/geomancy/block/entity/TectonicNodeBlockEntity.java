package com.traverse.geomancy.block.entity;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.block.TectonicNodeBlock;
import com.traverse.geomancy.essence.BiomeEssence;
import com.traverse.geomancy.essence.Essence;
import com.traverse.geomancy.recipe.TransmutationInput;
import com.traverse.geomancy.recipe.TransmutationRecipe;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.registry.ModRecipes;

public class TectonicNodeBlockEntity extends BlockEntity {
    public static final int REVEAL_DURATION_TICKS = 200;

    private long revealedUntil;

    private @Nullable BlockPos jobTarget;
    private int jobProgress;
    private int jobDuration;

    public TectonicNodeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TECTONIC_NODE.get(), pos, state);
    }

    public Essence essence() {
        return level == null ? Essence.TERRA : BiomeEssence.at(level, worldPosition);
    }

    public void reveal(Level level) {
        revealedUntil = level.getGameTime() + REVEAL_DURATION_TICKS;
        if (!level.getBlockState(worldPosition).getValue(TectonicNodeBlock.REVEALED)) {
            level.setBlock(worldPosition, getBlockState().setValue(TectonicNodeBlock.REVEALED, true), Block.UPDATE_ALL);
        }
    }

    public boolean hasJob() {
        return jobTarget != null;
    }

    public @Nullable BlockPos jobTarget() {
        return jobTarget;
    }

    public int jobProgress() {
        return jobProgress;
    }

    public int jobDuration() {
        return jobDuration;
    }

    public boolean startJob(Level level, BlockPos target) {
        Optional<RecipeHolder<TransmutationRecipe>> recipe = findRecipe(level, target);
        if (recipe.isEmpty()) {
            return false;
        }
        jobTarget = target.immutable();
        jobProgress = 0;
        jobDuration = recipe.get().value().duration();
        reveal(level);
        sync();
        return true;
    }

    private Optional<RecipeHolder<TransmutationRecipe>> findRecipe(Level level, BlockPos target) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        TransmutationInput input = new TransmutationInput(level.getBlockState(target), essence());
        return serverLevel.recipeAccess().getRecipeFor(ModRecipes.TRANSMUTATION_TYPE.get(), input, level);
    }

    private void clearJob() {
        jobTarget = null;
        jobProgress = 0;
        jobDuration = 0;
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // revealedUntil is intentionally not persisted, so a node reloaded mid-reveal
    // expires on its first tick rather than staying visible forever.
    public static void serverTick(Level level, BlockPos pos, BlockState state, TectonicNodeBlockEntity node) {
        if (node.jobTarget != null) {
            node.tickJob(level);
        }

        if (node.jobTarget == null && level.getGameTime() >= node.revealedUntil
                && state.getValue(TectonicNodeBlock.REVEALED)) {
            level.setBlock(pos, state.setValue(TectonicNodeBlock.REVEALED, false), Block.UPDATE_ALL);
        }
    }

    private void tickJob(Level level) {
        BlockPos target = jobTarget;
        // Re-resolved every tick so breaking or replacing the target cancels the job.
        Optional<RecipeHolder<TransmutationRecipe>> recipe = findRecipe(level, target);
        if (recipe.isEmpty()) {
            clearJob();
            return;
        }

        jobDuration = recipe.get().value().duration();
        if (++jobProgress < jobDuration) {
            setChanged();
            return;
        }

        level.setBlock(target, recipe.get().value().result().value().defaultBlockState(), Block.UPDATE_ALL);
        level.playSound(null, target, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.8F);
        clearJob();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("job_target", BlockPos.CODEC, jobTarget);
        output.putInt("job_progress", jobProgress);
        output.putInt("job_duration", jobDuration);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        jobTarget = input.read("job_target", BlockPos.CODEC).orElse(null);
        jobProgress = input.getIntOr("job_progress", 0);
        jobDuration = input.getIntOr("job_duration", 0);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }
}
