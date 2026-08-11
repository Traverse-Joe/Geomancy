package com.traverse.geomancy.block.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.block.BatteryCrystalBlock;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.registry.ModDataComponents;
import com.traverse.geomancy.resonance.BatterySize;
import com.traverse.geomancy.resonance.ResonanceStorage;

public class BatteryCrystalBlockEntity extends BlockEntity implements ResonanceStorage {
    private int resonance;

    public BatteryCrystalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_CRYSTAL.get(), pos, state);
    }

    private BatterySize size() {
        return ((BatteryCrystalBlock) getBlockState().getBlock()).size();
    }

    @Override
    public int resonance() {
        return resonance;
    }

    @Override
    public int capacity() {
        return size().capacity();
    }

    @Override
    public int pulseSize() {
        return size().pulse();
    }

    @Override
    public int emitterReach() {
        return size().reach();
    }

    @Override
    public int insertResonance(int amount, boolean simulate) {
        int accepted = Math.min(Math.max(amount, 0), capacity() - resonance);
        if (!simulate && accepted > 0) {
            resonance += accepted;
            sync();
            updateFillLevel();
        }
        return accepted;
    }

    @Override
    public int extractResonance(int amount, boolean simulate) {
        int extracted = Math.min(Math.max(amount, 0), resonance);
        if (!simulate && extracted > 0) {
            resonance -= extracted;
            sync();
            updateFillLevel();
        }
        return extracted;
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // Drives the block's actual light emission: a battery glows brighter in the room as it
    // fills, rather than only reporting its charge through a tooltip. Quantized to 5 steps
    // so this only touches the blockstate (and triggers a relight) when the visible
    // brightness changes, not on every insert/extract.
    private void updateFillLevel() {
        if (level == null) {
            return;
        }
        int bucket = fillBucket();
        BlockState state = getBlockState();
        if (state.getValue(BatteryCrystalBlock.FILL_LEVEL) != bucket) {
            level.setBlock(worldPosition, state.setValue(BatteryCrystalBlock.FILL_LEVEL, bucket), Block.UPDATE_ALL);
        }
    }

    private int fillBucket() {
        int capacity = capacity();
        if (capacity <= 0 || resonance <= 0) {
            return 0;
        }
        float fraction = (float) resonance / capacity;
        return Mth.clamp((int) Math.ceil(fraction * 4.0F), 1, 4);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        resonance = Math.min(components.getOrDefault(ModDataComponents.STORED_RESONANCE.get(), 0), capacity());
        updateFillLevel();
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModDataComponents.STORED_RESONANCE.get(), resonance);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("resonance");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("resonance", resonance);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        resonance = Math.min(input.getIntOr("resonance", 0), capacity());
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
