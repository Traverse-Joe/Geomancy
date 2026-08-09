package com.traverse.geomancy.block.entity;

import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.essence.Essence;
import com.traverse.geomancy.essence.EssenceForm;
import com.traverse.geomancy.recipe.TransmutationInput;
import com.traverse.geomancy.recipe.TransmutationRecipe;
import com.traverse.geomancy.recipe.TransmutationResult;
import com.traverse.geomancy.recipe.TransmutationTarget;
import com.traverse.geomancy.registry.ModRecipes;

// Shared transmutation job machinery for anything that can run a TransmutationRecipe: the
// wild node (working directly, at a penalty) and resonance pillars (the intended work
// site, which also accept dropped item entities - see ResonancePillarBlockEntity).
// A job targets either a block (jobTarget) or an item entity (jobItemId), never both.
public abstract class EssenceWorkerBlockEntity extends BlockEntity {
    private static final int PROGRESS_SYNC_INTERVAL = 20;

    private @Nullable BlockPos jobTarget;
    private @Nullable UUID jobItemId;
    private int jobProgress;
    private int jobDuration;

    protected EssenceWorkerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract Essence workEssence();

    protected abstract EssenceForm workForm();

    protected abstract int costMultiplier();

    // How far, in blocks, this worker can reach to act on a job target. Exposed publicly
    // so the tuning hammer can validate a click before ever attempting to start a job.
    public abstract int workRange();

    // simulate=true reports what would be consumed without mutating state, used to test
    // whether a tick's worth of work can be paid for before committing to it.
    protected abstract int consume(int amount, boolean simulate);

    protected void onJobStarted(Level level) {
    }

    public boolean hasJob() {
        return jobTarget != null || jobItemId != null;
    }

    public @Nullable BlockPos jobTarget() {
        return jobTarget;
    }

    // The block position a job should render its beam and outline toward. Block jobs
    // point at the real target; item jobs point at the fixed work zone above the worker,
    // since the item itself can drift and isn't worth tracking precisely for rendering.
    public @Nullable BlockPos jobRenderPos() {
        if (jobTarget != null) {
            return jobTarget;
        }
        return jobItemId != null ? worldPosition.above() : null;
    }

    public int jobProgress() {
        return jobProgress;
    }

    public int jobDuration() {
        return jobDuration;
    }

    public boolean startJob(Level level, BlockPos target) {
        Optional<RecipeHolder<TransmutationRecipe>> recipe =
                findRecipe(level, new TransmutationTarget.OfBlock(level.getBlockState(target)));
        if (recipe.isEmpty()) {
            return false;
        }
        jobTarget = target.immutable();
        jobItemId = null;
        jobProgress = 0;
        jobDuration = recipe.get().value().duration();
        onJobStarted(level);
        sync();
        return true;
    }

    protected boolean startItemJob(Level level, ItemEntity item) {
        Optional<RecipeHolder<TransmutationRecipe>> recipe =
                findRecipe(level, new TransmutationTarget.OfItem(item.getItem()));
        if (recipe.isEmpty()) {
            return false;
        }
        jobTarget = null;
        jobItemId = item.getUUID();
        jobProgress = 0;
        jobDuration = recipe.get().value().duration();
        onJobStarted(level);
        sync();
        return true;
    }

    private Optional<RecipeHolder<TransmutationRecipe>> findRecipe(Level level, TransmutationTarget target) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        TransmutationInput input = new TransmutationInput(target, workEssence(), workForm());
        return serverLevel.recipeAccess().getRecipeFor(ModRecipes.TRANSMUTATION_TYPE.get(), input, level);
    }

    private void clearJob() {
        jobTarget = null;
        jobItemId = null;
        jobProgress = 0;
        jobDuration = 0;
        sync();
    }

    protected void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    protected void clientAdvanceJob() {
        if (hasJob() && jobProgress < jobDuration) {
            jobProgress++;
        }
    }

    protected void tickJob(Level level) {
        if (jobItemId != null) {
            tickItemJob(level);
        } else if (jobTarget != null) {
            tickBlockJob(level);
        }
    }

    private void tickBlockJob(Level level) {
        BlockPos target = jobTarget;
        // Re-resolved every tick so breaking or replacing the target cancels the job.
        Optional<RecipeHolder<TransmutationRecipe>> recipeHolder =
                findRecipe(level, new TransmutationTarget.OfBlock(level.getBlockState(target)));
        if (recipeHolder.isEmpty()) {
            clearJob();
            return;
        }
        TransmutationRecipe recipe = recipeHolder.get().value();
        jobDuration = recipe.duration();

        if (!advance(recipe)) {
            return;
        }

        // A block-target job only ever completes an AsBlock recipe; matches() already
        // filtered on the subject/target pair, so a mismatched AsItem result is a content
        // error, not a runtime state to design around, and is left as a world no-op here.
        if (recipe.result() instanceof TransmutationResult.AsBlock(var block)) {
            level.setBlock(target, block.value().defaultBlockState(), Block.UPDATE_ALL);
        } else if (recipe.result() instanceof TransmutationResult.AsLoot && level instanceof ServerLevel serverLevel) {
            BlockState sourceState = level.getBlockState(target);
            BlockEntity sourceEntity = level.getBlockEntity(target);
            Block.dropResources(sourceState, serverLevel, target, sourceEntity, null, new ItemStack(Items.STONE_PICKAXE));
            level.setBlock(target, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.playSound(null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.2F);
        clearJob();
    }

    private void tickItemJob(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        // Picking the item up, it despawning, or another process consuming it all cancel
        // the job the same way breaking a block target does for a block job.
        if (!(serverLevel.getEntity(jobItemId) instanceof ItemEntity item) || item.isRemoved()) {
            clearJob();
            return;
        }
        Optional<RecipeHolder<TransmutationRecipe>> recipeHolder =
                findRecipe(level, new TransmutationTarget.OfItem(item.getItem()));
        if (recipeHolder.isEmpty()) {
            clearJob();
            return;
        }
        TransmutationRecipe recipe = recipeHolder.get().value();
        jobDuration = recipe.duration();

        if (!advance(recipe)) {
            return;
        }

        ItemStack source = item.getItem();
        source.shrink(1);
        if (source.isEmpty()) {
            item.discard();
        }
        if (recipe.result() instanceof TransmutationResult.AsItem(var template)) {
            level.addFreshEntity(new ItemEntity(level, item.getX(), item.getY(), item.getZ(), template.create()));
        }
        level.playSound(null, item.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.2F);
        clearJob();
    }

    // Advances progress by one tick and reports whether the job is ready to complete.
    // Returns false when either starved (not enough essence, progress held) or still
    // mid-job (progress advanced but short of duration) - the caller only proceeds to
    // finish the job when this returns true, meaning duration was just reached.
    private boolean advance(TransmutationRecipe recipe) {
        int totalCost = recipe.cost() * costMultiplier();
        if (consume(totalCost, true) < totalCost) {
            return false;
        }

        if (++jobProgress < jobDuration) {
            if (jobProgress % PROGRESS_SYNC_INTERVAL == 0) {
                sync();
            } else {
                setChanged();
            }
            return false;
        }

        consume(totalCost, false);
        return true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("job_target", BlockPos.CODEC, jobTarget);
        output.storeNullable("job_item", UUIDUtil.CODEC, jobItemId);
        output.putInt("job_progress", jobProgress);
        output.putInt("job_duration", jobDuration);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        jobTarget = input.read("job_target", BlockPos.CODEC).orElse(null);
        jobItemId = input.read("job_item", UUIDUtil.CODEC).orElse(null);
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
