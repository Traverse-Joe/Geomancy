package com.traverse.geomancy.block.entity;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import com.traverse.geomancy.Config;
import com.traverse.geomancy.block.ResonancePillarBlock;
import com.traverse.geomancy.essence.Essence;
import com.traverse.geomancy.essence.EssenceCharge;
import com.traverse.geomancy.essence.EssenceForm;
import com.traverse.geomancy.network.EssenceProvider;
import com.traverse.geomancy.network.EssenceRelay;
import com.traverse.geomancy.network.LinkResult;
import com.traverse.geomancy.network.LineOfSight;
import com.traverse.geomancy.network.RelayLink;
import com.traverse.geomancy.registry.ModBlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// The Resonance Pillar is both a relay - it pulls essence from an upstream node or pillar
// into a small buffer - and the work site: transmutation jobs run from here, at full range
// and no cost penalty, unlike a wild node worked directly.
public class ResonancePillarBlockEntity extends EssenceWorkerBlockEntity implements EssenceRelay {
    private static final int WORK_RANGE = 16;

    private final RelayLink link;
    private EssenceCharge charge = EssenceCharge.EMPTY;
    private @Nullable BlockPos logisticsPickup;
    private boolean logisticsWorldPickup;
    private final List<BlockPos> logisticsOutputs = new ArrayList<>();

    public ResonancePillarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_PILLAR.get(), pos, state);
        link = new RelayLink(pos);
    }

    @Override
    protected Essence workEssence() {
        return charge.essence();
    }

    @Override
    protected EssenceForm workForm() {
        return charge.form();
    }

    @Override
    protected int costMultiplier() {
        return 1;
    }

    @Override
    protected int consume(int amount, boolean simulate) {
        return extract(amount, simulate);
    }

    @Override
    public int workRange() {
        return WORK_RANGE;
    }

    @Override
    public @Nullable BlockPos upstream() {
        return link.upstream();
    }

    @Override
    public LinkResult setUpstream(Level level, @Nullable BlockPos pos) {
        LinkResult result = link.set(level, pos);
        if (result == LinkResult.OK) {
            syncLinkedState(level);
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

    @Override
    public EssenceCharge charge() {
        return charge;
    }

    @Override
    public int capacity() {
        return Config.RELAY_BUFFER_CAPACITY.get();
    }

    public List<BlockPos> logisticsTargets() {
        if (logisticsPickup == null) {
            return List.of();
        }
        List<BlockPos> targets = new ArrayList<>(logisticsOutputs.size() + 1);
        targets.add(logisticsPickup);
        targets.addAll(logisticsOutputs);
        return List.copyOf(targets);
    }

    public @Nullable BlockPos logisticsPickup() {
        return logisticsPickup;
    }

    public List<BlockPos> logisticsOutputs() {
        return List.copyOf(logisticsOutputs);
    }

    public boolean isLogisticsWorldPickup() {
        return logisticsWorldPickup;
    }

    public LinkResult addLogisticsTarget(Level level, BlockPos target) {
        if (target.equals(worldPosition)) {
            return LinkResult.NO_SOURCE;
        }
        int range = Config.LOGISTICS_RANGE.get();
        BlockPos linkFrom = logisticsPickup == null ? worldPosition : logisticsPickup;
        if (linkFrom.distSqr(target) > (double) range * range) {
            return LinkResult.TOO_FAR;
        }
        boolean selectingWorldPickup = logisticsPickup == null
                && !(level.getBlockEntity(target) instanceof Container);
        boolean clearRoute = logisticsPickup == null
                ? (selectingWorldPickup ? LineOfSight.clearToTop(level, worldPosition, target)
                        : LineOfSight.clear(level, worldPosition, target))
                : (logisticsWorldPickup ? LineOfSight.clearFromTop(level, logisticsPickup, target)
                        : LineOfSight.clear(level, logisticsPickup, target));
        if (!clearRoute) {
            return LinkResult.OBSTRUCTED;
        }
        if (logisticsPickup == null) {
            if (level.getBlockState(target).isAir()) {
                return LinkResult.NO_SOURCE;
            }
            logisticsPickup = target.immutable();
            logisticsWorldPickup = selectingWorldPickup;
            sync();
            return LinkResult.OK;
        }
        if (!hasInsertableInventory(level, target)) {
            return LinkResult.NO_SOURCE;
        }
        if (!logisticsOutputs.contains(target)) {
            logisticsOutputs.add(target.immutable());
            sync();
        }
        return LinkResult.OK;
    }

    public boolean removeLogisticsTarget(BlockPos target) {
        boolean removed;
        if (target.equals(logisticsPickup)) {
            logisticsPickup = null;
            logisticsWorldPickup = false;
            logisticsOutputs.clear();
            removed = true;
        } else {
            removed = logisticsOutputs.remove(target);
        }
        if (removed) {
            sync();
        }
        return removed;
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

    private void pullEssence(Level level) {
        BlockPos upstream = link.upstream();
        if (!link.valid() || upstream == null || charge.amount() >= capacity()) {
            return;
        }
        if (!(level.getBlockEntity(upstream) instanceof EssenceProvider source)) {
            link.invalidate();
            syncLinkedState(level);
            sync();
            return;
        }
        EssenceCharge available = source.charge();
        if (available.isEmpty() || !charge.compatibleWith(available.essence(), available.form())) {
            return;
        }
        int want = Math.min(Config.RELAY_TRANSFER_AMOUNT.get(),
                Math.min(available.amount(), capacity() - charge.amount()));
        int taken = source.extract(want, false);
        if (taken > 0) {
            charge = charge.grow(available.essence(), available.form(), taken);
            setChanged();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonancePillarBlockEntity pillar) {
        long time = level.getGameTime();

        if (pillar.link.dueForRevalidate(time) && pillar.link.revalidate(level)) {
            pillar.syncLinkedState(level);
            pillar.sync();
        }
        if (pillar.link.dueForTransfer(time)) {
            pillar.pullEssence(level);
        }

        boolean itemScan = pillar.link.dueForItemScan(time);
        if (itemScan) {
            pillar.validateLogistics(level);
        }
        if (pillar.hasJob()) {
            pillar.tickJob(level);
        } else if (itemScan) {
            pillar.runLogistics(level);
        }
    }

    private void validateLogistics(Level level) {
        if (logisticsPickup == null || !level.hasChunkAt(logisticsPickup)) {
            return;
        }
        boolean validPickup = logisticsWorldPickup ? !level.getBlockState(logisticsPickup).isAir()
                : level.getBlockEntity(logisticsPickup) instanceof Container;
        boolean pickupClear = logisticsWorldPickup ? LineOfSight.clearToTop(level, worldPosition, logisticsPickup)
                : LineOfSight.clear(level, worldPosition, logisticsPickup);
        if (!validPickup || !pickupClear) {
            logisticsPickup = null;
            logisticsWorldPickup = false;
            logisticsOutputs.clear();
            sync();
            return;
        }
        boolean changed = logisticsOutputs.removeIf(target -> level.hasChunkAt(target)
                && (!hasInsertableInventory(level, target)
                        || !(logisticsWorldPickup ? LineOfSight.clearFromTop(level, logisticsPickup, target)
                                : LineOfSight.clear(level, logisticsPickup, target))));
        if (changed) {
            sync();
        }
    }

    private void runLogistics(Level level) {
        if (charge.isEmpty() || charge.essence() != Essence.MOTUS || charge.amount() < Config.LOGISTICS_COST.get()
                || logisticsPickup == null || !level.hasChunkAt(logisticsPickup)
                || !(logisticsWorldPickup ? LineOfSight.clearToTop(level, worldPosition, logisticsPickup)
                        : LineOfSight.clear(level, worldPosition, logisticsPickup))) {
            return;
        }

        if (logisticsWorldPickup) {
            runWorldPickup(level);
            return;
        }
        if (!(level.getBlockEntity(logisticsPickup) instanceof Container pickup)) {
            return;
        }

        for (int slot = 0; slot < pickup.getContainerSize(); slot++) {
            ItemStack stack = pickup.getItem(slot);
            if (stack.isEmpty() || pickup instanceof ItemPedestalBlockEntity pedestal && !pedestal.hasCompletedOutput()) {
                continue;
            }
            for (BlockPos target : logisticsOutputs) {
                if (!level.hasChunkAt(target) || !(logisticsWorldPickup
                        ? LineOfSight.clearFromTop(level, logisticsPickup, target)
                        : LineOfSight.clear(level, logisticsPickup, target))) {
                    continue;
                }
                if (insertOne(level, target, stack)) {
                    pickup.setChanged();
                    if (pickup instanceof ItemPedestalBlockEntity pedestal) {
                        pedestal.setChangedAndSync();
                    }
                    extract(Config.LOGISTICS_COST.get(), false);
                    return;
                }
            }
        }
    }

    private void runWorldPickup(Level level) {
        double radius = Config.LOGISTICS_PICKUP_RADIUS.get();
        double centerX = logisticsPickup.getX() + 0.5D;
        double centerY = logisticsPickup.getY() + 1.0D;
        double centerZ = logisticsPickup.getZ() + 0.5D;
        AABB bounds = new AABB(centerX - radius, centerY - 0.25D, centerZ - radius,
                centerX + radius, centerY + 1.5D, centerZ + radius);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, bounds,
                item -> item.isAlive() && !item.getItem().isEmpty()
                        && item.distanceToSqr(centerX, item.getY(), centerZ) <= radius * radius)) {
            ItemStack stack = entity.getItem();
            for (BlockPos target : logisticsOutputs) {
                if (!level.hasChunkAt(target) || !LineOfSight.clearFromTop(level, logisticsPickup, target)) {
                    continue;
                }
                if (insertOne(level, target, stack)) {
                    if (stack.isEmpty()) {
                        entity.discard();
                    }
                    extract(Config.LOGISTICS_COST.get(), false);
                    return;
                }
            }
        }
    }

    private static boolean insertOne(Level level, BlockPos target, ItemStack source) {
        ResourceHandler<ItemResource> handler = findItemHandler(level, target);
        if (handler != null) {
            try (Transaction transaction = Transaction.openRoot()) {
                if (handler.insert(ItemResource.of(source), 1, transaction) == 1) {
                    source.shrink(1);
                    transaction.commit();
                    return true;
                }
            }
        }
        if (!(level.getBlockEntity(target) instanceof Container inventory)) {
            return false;
        }
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (existing.isEmpty() && inventory.canPlaceItem(slot, source)) {
                inventory.setItem(slot, source.split(1));
                inventory.setChanged();
                return true;
            }
            if (ItemStack.isSameItemSameComponents(existing, source) && inventory.canPlaceItem(slot, source)
                    && existing.getCount() < Math.min(existing.getMaxStackSize(), inventory.getMaxStackSize(existing))) {
                source.shrink(1);
                existing.grow(1);
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }

    private static boolean hasInsertableInventory(Level level, BlockPos target) {
        return level.getBlockEntity(target) instanceof Container || findItemHandler(level, target) != null;
    }

    private static @Nullable ResourceHandler<ItemResource> findItemHandler(Level level, BlockPos target) {
        for (Direction side : Direction.values()) {
            ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, target, side);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ResonancePillarBlockEntity pillar) {
        pillar.clientAdvanceJob();
    }

    private void syncLinkedState(Level level) {
        BlockState current = getBlockState();
        if (current.getValue(ResonancePillarBlock.LINKED) != link.valid()) {
            level.setBlock(worldPosition, current.setValue(ResonancePillarBlock.LINKED, link.valid()), Block.UPDATE_ALL);
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        link.save(output);
        output.store("charge", EssenceCharge.CODEC, charge);
        output.storeNullable("logistics_pickup", BlockPos.CODEC, logisticsPickup);
        output.putBoolean("logistics_world_pickup", logisticsWorldPickup);
        output.store("logistics_outputs", BlockPos.CODEC.listOf(), logisticsOutputs);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        link.load(input);
        charge = input.read("charge", EssenceCharge.CODEC).orElse(EssenceCharge.EMPTY);
        logisticsPickup = input.read("logistics_pickup", BlockPos.CODEC).orElse(null);
        logisticsWorldPickup = input.getBooleanOr("logistics_world_pickup", false);
        logisticsOutputs.clear();
        logisticsOutputs.addAll(input.read("logistics_outputs", BlockPos.CODEC.listOf()).orElse(List.of()));
    }
}
