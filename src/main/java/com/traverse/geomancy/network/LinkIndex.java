package com.traverse.geomancy.network;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

// Server-side, per-level, rebuilt from BlockEntity lifecycle rather than persisted: chunk ->
// positions of every relay with a link segment passing through that chunk. A block
// place/break only needs to ask "which relays might care about this chunk?" - it never
// raycasts itself, so revalidation stays cheap even with many relays loaded at once.
public final class LinkIndex {
    private final Long2ObjectMap<LongSet> byChunk = new Long2ObjectOpenHashMap<>();

    public void register(BlockPos downstream, BlockPos upstream) {
        forEachChunk(downstream, upstream, chunkKey ->
                byChunk.computeIfAbsent(chunkKey, key -> new LongOpenHashSet()).add(downstream.asLong()));
    }

    public void unregister(BlockPos downstream, BlockPos upstream) {
        forEachChunk(downstream, upstream, chunkKey -> {
            LongSet relays = byChunk.get(chunkKey);
            if (relays == null) {
                return;
            }
            relays.remove(downstream.asLong());
            if (relays.isEmpty()) {
                byChunk.remove(chunkKey);
            }
        });
    }

    public void invalidate(Level level, BlockPos changed) {
        LongSet relays = byChunk.get(ChunkPos.pack(changed));
        if (relays == null) {
            return;
        }
        for (long packed : relays) {
            if (level.getBlockEntity(BlockPos.of(packed)) instanceof EssenceRelay relay) {
                relay.markLinkDirty();
            }
        }
    }

    private interface ChunkVisitor {
        void visit(long chunkKey);
    }

    private static void forEachChunk(BlockPos a, BlockPos b, ChunkVisitor visitor) {
        int minChunkX = Math.min(SectionPos.blockToSectionCoord(a.getX()), SectionPos.blockToSectionCoord(b.getX()));
        int maxChunkX = Math.max(SectionPos.blockToSectionCoord(a.getX()), SectionPos.blockToSectionCoord(b.getX()));
        int minChunkZ = Math.min(SectionPos.blockToSectionCoord(a.getZ()), SectionPos.blockToSectionCoord(b.getZ()));
        int maxChunkZ = Math.max(SectionPos.blockToSectionCoord(a.getZ()), SectionPos.blockToSectionCoord(b.getZ()));

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                visitor.visit(ChunkPos.pack(chunkX, chunkZ));
            }
        }
    }
}
