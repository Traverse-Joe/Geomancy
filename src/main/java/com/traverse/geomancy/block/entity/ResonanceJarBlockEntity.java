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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.Config;
import com.traverse.geomancy.essence.Essence;
import com.traverse.geomancy.essence.EssenceCharge;
import com.traverse.geomancy.essence.EssenceForm;
import com.traverse.geomancy.network.EssenceProvider;
import com.traverse.geomancy.network.EssenceRelay;
import com.traverse.geomancy.network.LinkResult;
import com.traverse.geomancy.network.RelayLink;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.registry.ModDataComponents;

// A refinery and battery: pulls raw essence off the network into a small intake buffer,
// converts it to refined essence at a lossy ratio, and offers the refined result
// downstream (as an EssenceProvider) to whatever a pillar chains off it. Refined charge
// is exposed as a DataComponent so a jar carries its contents when broken and carried
// home; the raw intake is ordinary transient processing state and does not survive that
// trip, matching a furnace losing unfinished smelting progress.
public class ResonanceJarBlockEntity extends BlockEntity implements EssenceRelay {
    private final RelayLink link;
    private EssenceCharge intake = EssenceCharge.EMPTY;
    private EssenceCharge charge = EssenceCharge.EMPTY;

    public ResonanceJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_JAR.get(), pos, state);
        link = new RelayLink(pos);
    }

    @Override
    public @Nullable BlockPos upstream() {
        return link.upstream();
    }

    @Override
    public LinkResult setUpstream(Level level, @Nullable BlockPos pos) {
        LinkResult result = link.set(level, pos);
        if (result == LinkResult.OK) {
            sync();
        }
        return result;
    }

    @Override
    public boolean linkValid() {
        return link.valid();
    }

    @Override
    public void markLinkDirty() {
        link.markDirty();
    }

    // What this jar offers downstream: always refined, never the raw intake buffer.
    @Override
    public EssenceCharge charge() {
        return charge;
    }

    @Override
    public int capacity() {
        return Config.JAR_REFINED_CAPACITY.get();
    }

    @Override
    public int extract(int amount, boolean simulate) {
        int taken = Math.min(amount, charge.amount());
        if (!simulate && taken > 0) {
            charge = charge.with(charge.amount() - taken);
            setChanged();
        }
        return taken;
    }

    private void pullRaw(Level level) {
        BlockPos upstream = link.upstream();
        if (!link.valid() || upstream == null || intake.amount() >= Config.JAR_RAW_INTAKE.get()) {
            return;
        }
        if (!(level.getBlockEntity(upstream) instanceof EssenceProvider source)) {
            link.invalidate();
            sync();
            return;
        }
        EssenceCharge available = source.charge();
        if (available.isEmpty() || available.form() != EssenceForm.RAW
                || !intake.compatibleWith(available.essence(), EssenceForm.RAW)) {
            return;
        }
        int want = Math.min(Config.RELAY_TRANSFER_AMOUNT.get(),
                Math.min(available.amount(), Config.JAR_RAW_INTAKE.get() - intake.amount()));
        int taken = source.extract(want, false);
        if (taken > 0) {
            intake = intake.grow(available.essence(), EssenceForm.RAW, taken);
            setChanged();
        }
    }

    private void refine(Level level) {
        int ratio = Config.JAR_REFINE_RATIO.get();
        if (intake.amount() < ratio || charge.amount() >= capacity()
                || !charge.compatibleWith(intake.essence(), EssenceForm.REFINED)) {
            return;
        }
        Essence essence = intake.essence();
        intake = intake.with(intake.amount() - ratio);
        charge = charge.grow(essence, EssenceForm.REFINED, 1);
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonanceJarBlockEntity jar) {
        long time = level.getGameTime();

        if (jar.link.dueForRevalidate(time) && jar.link.revalidate(level)) {
            jar.sync();
        }
        if (jar.link.dueForTransfer(time)) {
            jar.pullRaw(level);
        }
        if (time % Config.JAR_REFINE_INTERVAL.get() == 0) {
            jar.refine(level);
        }
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null) {
            link.onLoad(level);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null) {
            link.onUnload(level);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            link.onUnload(level);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        charge = components.getOrDefault(ModDataComponents.STORED_ESSENCE.get(), EssenceCharge.EMPTY);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModDataComponents.STORED_ESSENCE.get(), charge);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("charge");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        link.save(output);
        output.store("intake", EssenceCharge.CODEC, intake);
        output.store("charge", EssenceCharge.CODEC, charge);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        link.load(input);
        intake = input.read("intake", EssenceCharge.CODEC).orElse(EssenceCharge.EMPTY);
        charge = input.read("charge", EssenceCharge.CODEC).orElse(EssenceCharge.EMPTY);
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
