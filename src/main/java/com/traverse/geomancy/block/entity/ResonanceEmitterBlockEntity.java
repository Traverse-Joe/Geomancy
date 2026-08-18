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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.block.ResonanceEmitterBlock;
import com.traverse.geomancy.block.ResonanceReceiverBlock;
import com.traverse.geomancy.registry.ModAttachments;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.resonance.ResonanceBeam;
import com.traverse.geomancy.resonance.ResonanceStorage;
import com.traverse.geomancy.resonance.ResonanceTypes;
import com.traverse.geomancy.resonance.ResonanceType;
import com.traverse.geomancy.resonance.TypedResonancePool;

public class ResonanceEmitterBlockEntity extends BlockEntity implements ResonanceStorage {
    private static final int PULSE_INTERVAL = 10;

    // A small buffer, not a battery: normally unused, since the emitter pulls straight
    // from its host each pulse. It only fills when something else feeds the emitter
    // directly (a receiver mounted on its back, say), and pulse() drains it first when
    // that happens - letting emitter/receiver pairs relay resonance in hops rather than
    // only ever a single point-to-point link.
    private static final int BUFFER_CAPACITY = 200;

    // Spreads emitters across the 10-tick cycle instead of all firing on the same tick.
    private final int stagger;
    private @Nullable BlockPos target;
    private boolean wasDelivering;
    private final TypedResonancePool pool = new TypedResonancePool();

    public ResonanceEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_EMITTER.get(), pos, state);
        this.stagger = Math.floorMod(pos.hashCode(), PULSE_INTERVAL);
    }

    public @Nullable BlockPos target() {
        return target;
    }

    public void bind(BlockPos target) {
        unregisterFromIndex();
        this.target = target.immutable();
        wasDelivering = false;
        registerWithIndex();
        sync();
    }

    public void clear() {
        unregisterFromIndex();
        target = null;
        wasDelivering = false;
        sync();
    }

    // Server-only bookkeeping so EmitterLinkRevalidation can find and clear this emitter's
    // binding when the receiver it targets is actually broken - never for obstruction, which
    // must not touch the binding.
    private void registerWithIndex() {
        if (level != null && !level.isClientSide() && target != null) {
            level.getData(ModAttachments.EMITTER_INDEX).register(worldPosition, target);
        }
    }

    private void unregisterFromIndex() {
        if (level != null && !level.isClientSide() && target != null) {
            level.getData(ModAttachments.EMITTER_INDEX).unregister(worldPosition, target);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerWithIndex();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        unregisterFromIndex();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        unregisterFromIndex();
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
        int accepted = pool.insert(type, amount, BUFFER_CAPACITY, simulate);
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonanceEmitterBlockEntity emitter) {
        if (emitter.target == null || (level.getGameTime() + emitter.stagger) % PULSE_INTERVAL != 0) {
            return;
        }
        if (!emitter.pulse(level, pos, state)) {
            emitter.wasDelivering = false;
        }
    }

    // Pulls a fixed pulse from a source, dampens it across the path, and delivers what
    // survives. Ordered so nothing is extracted that cannot be delivered: a fully muffled
    // or unreachable link never touches the source, and a full destination stalls the
    // link rather than burning the battery for nothing.
    //
    // The source is the emitter's own buffer if something has relayed resonance into it
    // directly, otherwise whatever it's physically mounted on - the same code path serves
    // both a battery-mounted emitter and a mid-chain relay hop.
    private boolean pulse(Level level, BlockPos pos, BlockState state) {
        BlockPos targetPos = target;
        if (targetPos == null || !level.isLoaded(pos) || !level.isLoaded(targetPos)) {
            return false;
        }
        ResonanceStorage source;
        if (resonance() > 0) {
            source = this;
        } else {
            Direction facing = state.getValue(ResonanceEmitterBlock.FACING);
            if (!(level.getBlockEntity(pos.relative(facing.getOpposite())) instanceof ResonanceStorage host)) {
                return false;
            }
            source = host;
        }
        BlockState targetState = level.getBlockState(targetPos);
        if (!(targetState.getBlock() instanceof ResonanceReceiverBlock)) {
            return false;
        }
        double reach = source.emitterReach();
        if (pos.distSqr(targetPos) > reach * reach) {
            return false;
        }
        ResourceKey<ResonanceType> type = source.resonanceType();
        float multiplier = ResonanceBeam.pathMultiplier(level, pos, targetPos, ResonanceTypes.resolve(level, type));
        if (multiplier <= 0.0F) {
            return false;
        }
        int available = source.extractResonance(source.pulseSize(), true);
        if (available <= 0) {
            return false;
        }
        int arriving = (int) (available * multiplier);
        int accepted = ResonanceReceiverBlock.insert(level, targetPos, targetState, type, arriving, true);
        if (accepted <= 0) {
            return false;
        }
        int needed = Math.min(available, (int) Math.ceil(accepted / multiplier));
        int extracted = source.extractResonance(needed, false);
        int delivered = Math.min(accepted, (int) (extracted * multiplier));
        if (delivered <= 0) {
            return false;
        }
        ResonanceReceiverBlock.insert(level, targetPos, targetState, type, delivered, false);
        feedback(level, pos, type, !wasDelivering);
        wasDelivering = true;
        return true;
    }

    private static void feedback(Level level, BlockPos from, @Nullable ResourceKey<ResonanceType> type,
            boolean startingUp) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 at = Vec3.atCenterOf(from);
        serverLevel.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 3, 0.15, 0.15, 0.15, 0.01);
        if (startingUp) {
            serverLevel.playSound(null, from, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.4F,
                    1.3F * ResonanceTypes.pitch(level, type));
        }
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
        output.storeNullable("target", BlockPos.CODEC, target);
        pool.save(output, "buffer");
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        target = input.read("target", BlockPos.CODEC).orElse(null);
        pool.load(input, "buffer", BUFFER_CAPACITY);
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
