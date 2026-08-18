package com.traverse.geomancy.block.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
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
import com.traverse.geomancy.resonance.ResonanceType;
import com.traverse.geomancy.resonance.TypedResonancePool;

public class BatteryCrystalBlockEntity extends BlockEntity implements ResonanceStorage {
    private final TypedResonancePool pool = new TypedResonancePool();

    public BatteryCrystalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_CRYSTAL.get(), pos, state);
    }

    private BatterySize size() {
        return ((BatteryCrystalBlock) getBlockState().getBlock()).size();
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
    public int insertResonance(@Nullable ResourceKey<ResonanceType> type, int amount, boolean simulate) {
        int accepted = pool.insert(type, amount, capacity(), simulate);
        if (!simulate && accepted > 0) {
            sync();
            updateFillLevel();
        }
        return accepted;
    }

    @Override
    public int extractResonance(int amount, boolean simulate) {
        int extracted = pool.extract(amount, simulate);
        if (!simulate && extracted > 0) {
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
    //
    // This is also what keeps BatteryCrystalTint correct without a re-mesh of its own: a
    // pool only takes a type on an insert into an empty pool, and only loses one on
    // draining to empty, so the type can never change without the bucket changing too.
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
        int stored = pool.amount();
        if (capacity <= 0 || stored <= 0) {
            return 0;
        }
        float fraction = (float) stored / capacity;
        return Mth.clamp((int) Math.ceil(fraction * 4.0F), 1, 4);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        pool.set(components.get(ModDataComponents.STORED_RESONANCE_TYPE.get()),
                Math.min(components.getOrDefault(ModDataComponents.STORED_RESONANCE.get(), 0), capacity()));
        updateFillLevel();
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModDataComponents.STORED_RESONANCE.get(), pool.amount());
        components.set(ModDataComponents.STORED_RESONANCE_TYPE.get(), pool.type());
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("resonance");
        output.discard("resonance_type");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        pool.save(output, "resonance");
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        pool.load(input, "resonance", capacity());
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
