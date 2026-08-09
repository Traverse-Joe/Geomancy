package com.traverse.geomancy.network;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.Config;
import com.traverse.geomancy.registry.ModAttachments;

// Upstream-link bookkeeping shared by every EssenceRelay (pillars, jars): validation,
// the LinkIndex registration that lets a block place/break invalidate the right relays,
// and the staggered revalidate/transfer schedule. Owned by, not implemented on, the
// BlockEntity, since a relay also has its own buffer and (for pillars) job state that
// this class has no business knowing about.
public final class RelayLink {
    private final BlockPos owner;
    private final int stagger;

    private @Nullable BlockPos upstream;
    private boolean valid;
    private boolean dirty;

    public RelayLink(BlockPos owner) {
        this.owner = owner;
        this.stagger = Math.floorMod(owner.hashCode(), Config.RELAY_REVALIDATE_INTERVAL.get());
    }

    public @Nullable BlockPos upstream() {
        return upstream;
    }

    public boolean valid() {
        return valid;
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean dueForRevalidate(long time) {
        return dirty || (time + stagger) % Config.RELAY_REVALIDATE_INTERVAL.get() == 0;
    }

    public boolean dueForTransfer(long time) {
        return (time + stagger) % Config.RELAY_TRANSFER_INTERVAL.get() == 0;
    }

    public boolean dueForItemScan(long time) {
        return (time + stagger) % Config.ITEM_SCAN_INTERVAL.get() == 0;
    }

    public LinkResult set(Level level, @Nullable BlockPos pos) {
        if (pos == null) {
            clear(level);
            return LinkResult.OK;
        }
        if (!(level.getBlockEntity(pos) instanceof EssenceProvider)) {
            return LinkResult.NO_SOURCE;
        }
        int maxHop = Config.RELAY_MAX_HOP.get();
        if (pos.distSqr(owner) > (double) maxHop * maxHop) {
            return LinkResult.TOO_FAR;
        }
        if (!LineOfSight.clear(level, owner, pos)) {
            return LinkResult.OBSTRUCTED;
        }
        LinkResult chain = checkChain(level, pos);
        if (chain != LinkResult.OK) {
            return chain;
        }

        clear(level);
        upstream = pos.immutable();
        valid = true;
        dirty = false;
        level.getData(ModAttachments.LINK_INDEX).register(owner, upstream);
        return LinkResult.OK;
    }

    // Walks the proposed upstream's own chain to catch both a cycle back to this relay
    // and a chain that never bottoms out at a plain source within the hop budget.
    private LinkResult checkChain(Level level, BlockPos from) {
        BlockPos cursor = from;
        for (int depth = 0; depth < Config.RELAY_MAX_CHAIN_DEPTH.get(); depth++) {
            if (cursor.equals(owner)) {
                return LinkResult.CYCLE;
            }
            if (!(level.getBlockEntity(cursor) instanceof EssenceRelay relay)) {
                return LinkResult.OK;
            }
            BlockPos next = relay.upstream();
            if (next == null) {
                return LinkResult.OK;
            }
            cursor = next;
        }
        return LinkResult.TOO_DEEP;
    }

    public void clear(Level level) {
        if (upstream != null) {
            level.getData(ModAttachments.LINK_INDEX).unregister(owner, upstream);
        }
        upstream = null;
        valid = false;
        dirty = false;
    }

    // Returns true if validity changed, so the caller knows whether a blockstate/sync
    // update is warranted.
    public boolean revalidate(Level level) {
        dirty = false;
        boolean was = valid;
        valid = upstream != null && LineOfSight.clear(level, owner, upstream);
        return valid != was;
    }

    public void invalidate() {
        valid = false;
    }

    public void onLoad(Level level) {
        if (upstream != null) {
            level.getData(ModAttachments.LINK_INDEX).register(owner, upstream);
            dirty = true;
        }
    }

    public void onUnload(Level level) {
        if (upstream != null) {
            level.getData(ModAttachments.LINK_INDEX).unregister(owner, upstream);
        }
    }

    public void save(ValueOutput output) {
        output.storeNullable("upstream", BlockPos.CODEC, upstream);
    }

    public void load(ValueInput input) {
        upstream = input.read("upstream", BlockPos.CODEC).orElse(null);
    }
}
