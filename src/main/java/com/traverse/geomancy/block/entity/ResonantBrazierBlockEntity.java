package com.traverse.geomancy.block.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;

import com.traverse.geomancy.block.ResonantBrazierBlock;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.resonance.ResonanceStorage;

public class ResonantBrazierBlockEntity extends BlockEntity implements ResonanceStorage {
    private static final int RESONANCE_PER_PULSE = 5;
    private static final int PULSE_INTERVAL = 20;
    private static final int BREAK_CHECK_INTERVAL = 100;
    private static final float BREAK_CHANCE = 0.1F;

    // A small catch buffer, not a battery: it exists so an emitter can be mounted directly
    // on the Brazier and draw off whatever adjacent storage couldn't absorb, rather than
    // that overflow simply evaporating.
    private static final int BUFFER_CAPACITY = 200;

    private ItemStack crystal = ItemStack.EMPTY;
    private int resonanceSinceBreakCheck;
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
        if (!held.is(Items.AMETHYST_SHARD) || !crystal.isEmpty()) {
            return false;
        }
        crystal = held.split(1);
        sync();
        return true;
    }

    public ItemStack removeIngredient() {
        if (crystal.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = crystal;
        crystal = ItemStack.EMPTY;
        extinguish();
        sync();
        return removed;
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

    public void dropContents(ServerLevel level) {
        if (!crystal.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.7,
                    worldPosition.getZ() + 0.5, crystal.copy()));
        }
        crystal = ItemStack.EMPTY;
        extinguish();
        sync();
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
                if (level.getRandom().nextFloat() < BREAK_CHANCE) {
                    brazier.crystal = ItemStack.EMPTY;
                    brazier.extinguish();
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 1.25F);
                    return;
                }
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

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("crystal", ItemStack.OPTIONAL_CODEC, crystal);
        output.putInt("resonance_since_break_check", resonanceSinceBreakCheck);
        output.putInt("buffer", buffer);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        crystal = input.read("crystal", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        resonanceSinceBreakCheck = input.getIntOr("resonance_since_break_check", 0);
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

        @Override
        protected int getCapacity(ItemResource resource) {
            return 1;
        }

        @Override
        protected void onRootCommit(ItemStack originalState) {
            sync();
        }
    }
}
