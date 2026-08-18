package com.traverse.geomancy.block.entity;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.item.ResonanceVesselItem;
import com.traverse.geomancy.recipe.PedestalSynthesisRecipe;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.registry.ModRecipes;
import com.traverse.geomancy.resonance.ResonanceCost;
import com.traverse.geomancy.resonance.ResonanceStorage;
import com.traverse.geomancy.resonance.ResonanceType;
import com.traverse.geomancy.resonance.TypedResonancePool;

// No externally-triggered "activate" step, unlike the Hearth's Tuning-Fork strike - every
// server tick this checks the held item and either trickle-charges a Resonance Vessel (the
// early realization of the deferred Vibration Altar, crystal_change.md SS4.12) or advances a
// pedestal_synthesis recipe, whichever applies. Anything else just sits there.
public class ResonancePedestalBlockEntity extends BlockEntity implements ResonanceStorage {
    // Smaller than the Hearth's 1000: pedestal recipes are meant to be the cheap on-ramp.
    private static final int BUFFER_CAPACITY = 500;
    private static final int CHARGE_INTERVAL = 10;
    private static final int CHARGE_PER_PULSE = 20;

    private ItemStack held = ItemStack.EMPTY;
    private final TypedResonancePool pool = new TypedResonancePool();
    private int progress;
    private int duration;

    public ResonancePedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_PEDESTAL.get(), pos, state);
    }

    public ItemStack held() {
        return held;
    }

    public boolean isEmpty() {
        return held.isEmpty();
    }

    public void setHeld(ItemStack stack) {
        held = stack;
        clearProgress();
        sync();
    }

    public ItemStack removeHeld() {
        ItemStack removed = held;
        held = ItemStack.EMPTY;
        clearProgress();
        sync();
        return removed;
    }

    public float progressFraction(float partialTicks) {
        return duration <= 0 ? 0.0F : Math.min(1.0F, (progress + partialTicks) / duration);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonancePedestalBlockEntity pedestal) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (pedestal.held.isEmpty()) {
            pedestal.clearProgress();
            return;
        }
        if (pedestal.held.getItem() instanceof ResonanceVesselItem) {
            pedestal.clearProgress();
            if (level.getGameTime() % CHARGE_INTERVAL == 0) {
                pedestal.chargeVessel(serverLevel);
            }
            return;
        }
        if (pedestal.progress > 0) {
            pedestal.tickSynthesis(serverLevel);
        } else if (level.getGameTime() % CHARGE_INTERVAL == 0) {
            pedestal.tryStartSynthesis(serverLevel);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ResonancePedestalBlockEntity pedestal) {
        if (pedestal.progress > 0 && pedestal.progress < pedestal.duration) {
            pedestal.progress++;
        }
    }

    private void chargeVessel(ServerLevel level) {
        int stored = ResonanceVesselItem.stored(held);
        int wanted = Math.min(CHARGE_PER_PULSE, ResonanceVesselItem.CAPACITY - stored);
        if (wanted <= 0) {
            return;
        }
        ResonanceStorage source = partialResonanceSource(level);
        if (source == null) {
            return;
        }
        int taken = source.extractResonance(wanted, false);
        if (taken <= 0) {
            return;
        }
        ResonanceVesselItem.setStored(held, stored + taken);
        sync();
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.3F, 1.6F);
        level.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5, 3, 0.15, 0.15, 0.15, 0.01);
    }

    private Optional<RecipeHolder<PedestalSynthesisRecipe>> findRecipe(ServerLevel level) {
        return level.recipeAccess().getRecipeFor(ModRecipes.PEDESTAL_SYNTHESIS_TYPE.get(),
                new SingleRecipeInput(held), level);
    }

    private void tryStartSynthesis(ServerLevel level) {
        Optional<RecipeHolder<PedestalSynthesisRecipe>> holder = findRecipe(level);
        if (holder.isEmpty()) {
            return;
        }
        progress = 1;
        duration = holder.get().value().duration();
        sync();
    }

    private void tickSynthesis(ServerLevel level) {
        Optional<RecipeHolder<PedestalSynthesisRecipe>> holder = findRecipe(level);
        if (holder.isEmpty()) {
            clearProgress();
            return;
        }
        PedestalSynthesisRecipe recipe = holder.get().value();
        duration = recipe.duration();
        int cost = recipe.cost().amount();
        ResonanceStorage source = resonanceSource(level, recipe.cost());
        if (source == null) {
            // Pause rather than reset: resonance merely isn't available yet this tick.
            return;
        }
        if (++progress < duration) {
            sync();
            return;
        }
        if (source.extractResonance(cost, false) != cost) {
            throw new IllegalStateException("Simulated pedestal resonance extraction did not match actual extraction");
        }

        ItemStack input = held;
        input.shrink(1);
        ItemStack result = recipe.assemble(new SingleRecipeInput(input));
        if (input.isEmpty()) {
            held = result;
        } else if (ItemStack.isSameItemSameComponents(input, result)
                && input.getCount() + result.getCount() <= input.getMaxStackSize()) {
            input.grow(result.getCount());
        } else {
            held = input;
        }
        clearProgress();
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.4F);
        level.sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5, 24, 0.4, 0.3, 0.4, 0.15);
        sync();
    }

    private void clearProgress() {
        if (progress != 0 || duration != 0) {
            progress = 0;
            duration = 0;
            sync();
        }
    }

    // Self-then-adjacent lookup requiring the full amount, mirrors
    // ResonantHearthBlockEntity.resonanceSource - used for synthesis, where a partial
    // extraction would mean paying for an unfinished craft.
    private @Nullable ResonanceStorage resonanceSource(Level level, ResonanceCost cost) {
        int required = cost.amount();
        if (satisfies(this, cost, required)) {
            return this;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ResonanceStorage storage
                    && satisfies(storage, cost, required)) {
                return storage;
            }
        }
        return null;
    }

    private static boolean satisfies(ResonanceStorage storage, ResonanceCost cost, int required) {
        return cost.accepts(storage.resonanceType()) && storage.extractResonance(required, true) == required;
    }

    // Same lookup, but any nonzero amount is acceptable - a Vessel trickle-charges fine off
    // a partial pull.
    private @Nullable ResonanceStorage partialResonanceSource(Level level) {
        if (extractResonance(1, true) > 0) {
            return this;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ResonanceStorage storage
                    && storage.extractResonance(1, true) > 0) {
                return storage;
            }
        }
        return null;
    }

    @Override
    public int resonance() {
        return pool.amount();
    }

    @Override
    public @Nullable ResourceKey<ResonanceType> resonanceType() {
        return pool.type();
    }

    @Override
    public int resonanceColor() {
        return pool.color(getLevel());
    }

    @Override
    public int capacity() {
        return BUFFER_CAPACITY;
    }

    @Override
    public int insertResonance(@Nullable ResourceKey<ResonanceType> type, int amount, boolean simulate) {
        int accepted = pool.insert(type, amount, capacity(), simulate);
        if (!simulate && accepted > 0) {
            sync();
        }
        return accepted;
    }

    @Override
    public int extractResonance(int amount, boolean simulate) {
        int extracted = pool.extract(amount, simulate);
        if (!simulate && extracted > 0) {
            sync();
        }
        return extracted;
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("held", ItemStack.OPTIONAL_CODEC, held);
        pool.save(output, "buffer");
        output.putInt("progress", progress);
        output.putInt("duration", duration);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        held = input.read("held", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        pool.load(input, "buffer", capacity());
        progress = input.getIntOr("progress", 0);
        duration = input.getIntOr("duration", 0);
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
