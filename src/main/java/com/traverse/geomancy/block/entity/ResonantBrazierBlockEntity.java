package com.traverse.geomancy.block.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import com.traverse.geomancy.block.ResonantBrazierBlock;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.resonance.ResonanceStorage;

public class ResonantBrazierBlockEntity extends BlockEntity implements ResonanceStorage {
    private static final int RESONANCE_PER_PULSE = 5;
    private static final int PULSE_INTERVAL = 20;
    // A shard burns for ten seconds at the outside. The wear check has to fit inside that: at
    // 5 resonance a second the whole life is only 50 resonance, so checking per 100 would mean
    // no shard ever broke early.
    private static final int MAX_BURN_TICKS = 200;
    private static final int BREAK_CHECK_INTERVAL = 10;
    private static final float BREAK_CHANCE = 0.1F;

    // Enforced from the moment the slot empties, so there is a real gap between shards rather
    // than one that a full-length burn has already paid off.
    private static final int INSERT_DELAY_TICKS = 40;

    // A small catch buffer, not a battery: it exists so an emitter can be mounted directly
    // on the Brazier and draw off whatever adjacent storage couldn't absorb, rather than
    // that overflow simply evaporating.
    private static final int BUFFER_CAPACITY = 200;

    private ItemStack crystal = ItemStack.EMPTY;
    private int resonanceSinceBreakCheck;
    private int burnTicks;
    private long nextInsertTick;
    private int buffer;
    private final CrystalSlot itemHandler = new CrystalSlot();

    public ResonantBrazierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_BRAZIER.get(), pos, state);
    }

    // A hopper or pipe feeds the crystal slot the same way right-click does; both go
    // through this single field, so there's no separate copy to fall out of sync.
    public ResourceHandler<ItemResource> itemHandler() {
        return itemHandler;
    }

    public boolean insert(ItemStack held) {
        if (!held.is(Items.AMETHYST_SHARD) || !crystal.isEmpty() || !acceptsCrystal()) {
            return false;
        }
        crystal = held.split(1);
        syncCrystal();
        return true;
    }

    public ItemStack removeIngredient() {
        if (crystal.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = crystal;
        clearCrystal();
        extinguish();
        syncCrystal();
        return removed;
    }

    // Destroys the shard instead of yielding it, for every way a burning Brazier can lose one:
    // taken by hand, broken with the block, or lost to the wear check.
    public boolean shatterCrystal(ServerLevel level) {
        if (crystal.isEmpty()) {
            return false;
        }
        Item shattered = crystal.getItem();
        clearCrystal();
        extinguish();
        syncCrystal();
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, shattered),
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.7, worldPosition.getZ() + 0.5,
                12, 0.15, 0.15, 0.15, 0.05);
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 1.25F);
        return true;
    }

    public boolean ignite() {
        if (isLit() || crystal.isEmpty()) {
            return false;
        }
        setLit(true);
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.1F);
        }
        return true;
    }

    public ItemStack crystal() {
        return crystal;
    }

    private void clearCrystal() {
        crystal = ItemStack.EMPTY;
        onCrystalEmptied();
    }

    // Both counters belong to the shard that just left, so they never carry into the next one,
    // and the slot closes for a moment before it will take another.
    private void onCrystalEmptied() {
        resonanceSinceBreakCheck = 0;
        burnTicks = 0;
        if (level != null) {
            nextInsertTick = level.getGameTime() + INSERT_DELAY_TICKS;
        }
    }

    // Compared as an absolute deadline, with a sanity bound so a stored tick left ahead of the
    // world clock - a rolled-back save - cannot close the slot permanently.
    private boolean acceptsCrystal() {
        if (level == null) {
            return true;
        }
        long now = level.getGameTime();
        return now >= nextInsertTick || nextInsertTick - now > INSERT_DELAY_TICKS;
    }

    // A lit Brazier shatters its shard whoever breaks it - only the unlit drop is game-mode
    // dependent, so a creative break still gets the break sound rather than silently voiding it.
    public void dropContents(ServerLevel level, boolean dropItems) {
        if (isLit()) {
            shatterCrystal(level);
            return;
        }
        if (dropItems && !crystal.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.7,
                    worldPosition.getZ() + 0.5, crystal.copy()));
        }
        clearCrystal();
        extinguish();
        syncCrystal();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonantBrazierBlockEntity brazier) {
        if (!brazier.isLit()) {
            return;
        }
        if (brazier.crystal.isEmpty() || level.isRainingAt(pos.above())) {
            brazier.extinguish();
            return;
        }

        if (level.getGameTime() % PULSE_INTERVAL == 0) {
            int generated = brazier.generate(level);
            brazier.resonanceSinceBreakCheck += generated;
            while (brazier.resonanceSinceBreakCheck >= BREAK_CHECK_INTERVAL) {
                brazier.resonanceSinceBreakCheck -= BREAK_CHECK_INTERVAL;
                if (level.getRandom().nextFloat() < BREAK_CHANCE && level instanceof ServerLevel serverLevel) {
                    brazier.shatterCrystal(serverLevel);
                    return;
                }
            }
            // The hard ceiling; the wear check above is the chance of losing it sooner. Aged per
            // pulse rather than per tick because a full destination pauses delivery without
            // preserving the shard, so the two must not drift apart.
            brazier.burnTicks += PULSE_INTERVAL;
            if (brazier.burnTicks >= MAX_BURN_TICKS && level instanceof ServerLevel serverLevel) {
                brazier.shatterCrystal(serverLevel);
                return;
            }
            brazier.setChanged();
        }
    }

    private int generate(Level level) {
        int remaining = RESONANCE_PER_PULSE;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ResonanceStorage storage) {
                remaining -= storage.insertResonance(remaining, false);
                if (remaining == 0) {
                    break;
                }
            }
        }
        if (remaining > 0) {
            remaining -= insertResonance(remaining, false);
        }
        int generated = RESONANCE_PER_PULSE - remaining;
        if (generated > 0) {
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.25F, 1.6F);
        }
        return generated;
    }

    @Override
    public int resonance() {
        return buffer;
    }

    @Override
    public int capacity() {
        return BUFFER_CAPACITY;
    }

    @Override
    public int insertResonance(int amount, boolean simulate) {
        int accepted = Math.min(Math.max(amount, 0), capacity() - buffer);
        if (!simulate && accepted > 0) {
            buffer += accepted;
            sync();
        }
        return accepted;
    }

    @Override
    public int extractResonance(int amount, boolean simulate) {
        int extracted = Math.min(Math.max(amount, 0), buffer);
        if (!simulate && extracted > 0) {
            buffer -= extracted;
            sync();
        }
        return extracted;
    }

    private boolean isLit() {
        return getBlockState().getValue(ResonantBrazierBlock.LIT);
    }

    private void extinguish() {
        setLit(false);
    }

    private void setLit(boolean lit) {
        if (level != null && getBlockState().getValue(ResonantBrazierBlock.LIT) != lit) {
            level.setBlock(worldPosition, getBlockState().setValue(ResonantBrazierBlock.LIT, lit), Block.UPDATE_ALL);
        }
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // Comparator output reads the crystal slot, so anything touching it has to poke neighbours as
    // well - a plain block update misses comparators reading through a solid block. Neither the
    // resonance buffer nor LIT routes here: both change without moving the signal.
    private void syncCrystal() {
        sync();
        if (level != null) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("crystal", ItemStack.OPTIONAL_CODEC, crystal);
        output.putInt("resonance_since_break_check", resonanceSinceBreakCheck);
        output.putInt("burn_ticks", burnTicks);
        output.putLong("next_insert_tick", nextInsertTick);
        output.putInt("buffer", buffer);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        crystal = input.read("crystal", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        resonanceSinceBreakCheck = input.getIntOr("resonance_since_break_check", 0);
        burnTicks = Math.max(input.getIntOr("burn_ticks", 0), 0);
        nextInsertTick = input.getLongOr("next_insert_tick", 0L);
        buffer = Math.min(input.getIntOr("buffer", 0), BUFFER_CAPACITY);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    // A thin adapter, not a second inventory: getStack/setStack read and write the same
    // `crystal` field every other method here uses, so a hopper insert and a right-click
    // insert can never disagree about what's in the slot.
    private class CrystalSlot extends ItemStackResourceHandler {
        @Override
        protected ItemStack getStack() {
            return crystal;
        }

        @Override
        protected void setStack(ItemStack stack) {
            crystal = stack;
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return resource.is(Items.AMETHYST_SHARD);
        }

        // A burning shard cannot be pulled back out. Refused rather than shattered: a hopper
        // retrying every tick would otherwise quietly void a whole chest of shards.
        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return isLit() ? 0 : super.extract(index, resource, amount, transaction);
        }

        // The refill delay has to bind here too, or a hopper simply outruns it.
        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return acceptsCrystal() ? super.insert(index, resource, amount, transaction) : 0;
        }

        @Override
        protected int getCapacity(ItemResource resource) {
            return 1;
        }

        @Override
        protected void onRootCommit(ItemStack originalState) {
            // A hopper emptying the slot has to reset the burn timer too, or the next shard would
            // inherit however much of its predecessor's life was already spent.
            if (crystal.isEmpty()) {
                onCrystalEmptied();
            }
            syncCrystal();
        }
    }
}
