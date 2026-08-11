package com.traverse.geomancy.block.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.resonance.ResonanceStorage;

// A small buffer, not a battery: primarily an overflow catch for whatever the host behind
// it can't accept, but it also makes the Receiver itself a valid ResonanceStorage - so
// another Emitter can be mounted on it and relay what arrives further on, letting a chain
// of hops disperse resonance farther than one emitter's reach.
public class ResonanceReceiverBlockEntity extends BlockEntity implements ResonanceStorage {
    private static final int BUFFER_CAPACITY = 200;

    private int buffer;

    public ResonanceReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_RECEIVER.get(), pos, state);
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

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("buffer", buffer);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
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
}
