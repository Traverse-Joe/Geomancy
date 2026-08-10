package com.traverse.geomancy.prospecting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

// Locates naturally generated amethyst geodes by their Budding Amethyst signature.
// The search volume is a cylinder rather than a sphere because vanilla geodes generate
// far below the surface, and a sphere centred on a surface player would barely reach them.
public final class GeodeSurvey {
    public static final int DEFAULT_RADIUS = 64;
    public static final int DEFAULT_VERTICAL_REACH = 128;

    // Budding Amethyst has no vanilla crafting path, so a hit is a real geode rather than
    // player-placed decoration.
    private static final Predicate<BlockState> SIGNATURE = state -> state.is(Blocks.BUDDING_AMETHYST);

    // GlobalPalette#maybeHas always returns true, so bound the full-section scans.
    private static final int MAX_SECTION_SCANS = 48;

    private static final int SECTION_SIZE = 16;

    private GeodeSurvey() {
    }

    public static Optional<BlockPos> findNearest(ServerLevel level, Vec3 origin) {
        return findNearest(level, origin, DEFAULT_RADIUS, DEFAULT_VERTICAL_REACH);
    }

    public static Optional<BlockPos> findNearest(ServerLevel level, Vec3 origin, int radius, int verticalReach) {
        double radiusSq = (double) radius * radius;
        List<Candidate> candidates = gather(level, origin, radius, verticalReach, radiusSq);
        candidates.sort(Comparator.comparingDouble(Candidate::nearDistSq));

        Best best = new Best(radiusSq);
        int scans = 0;
        for (Candidate candidate : candidates) {
            // Candidates are ordered by their nearest possible distance, so once that
            // exceeds the best hit found, every remaining section is strictly farther.
            if (candidate.nearDistSq() >= best.distSq || scans >= MAX_SECTION_SCANS) {
                break;
            }
            scans++;
            scanSection(candidate, origin, best);
        }
        return Optional.ofNullable(best.pos);
    }

    // Close-range sweep used by the tuning fork's reveal. Unlike findNearest this returns
    // every signature block so the client can outline the whole geode, bounded by limit so
    // a dense cluster cannot bloat the reveal packet.
    public static List<BlockPos> findNearby(ServerLevel level, Vec3 origin, int radius, int limit) {
        double radiusSq = (double) radius * radius;
        List<BlockPos> found = new ArrayList<>();
        for (Candidate candidate : gather(level, origin, radius, radius, radiusSq)) {
            collectSection(candidate, origin, radiusSq, limit, found);
            if (found.size() >= limit) {
                break;
            }
        }
        return found;
    }

    private static void collectSection(Candidate candidate, Vec3 origin, double radiusSq, int limit,
            List<BlockPos> found) {
        for (int y = 0; y < SECTION_SIZE; y++) {
            for (int z = 0; z < SECTION_SIZE; z++) {
                for (int x = 0; x < SECTION_SIZE; x++) {
                    if (found.size() >= limit) {
                        return;
                    }
                    if (!SIGNATURE.test(candidate.section().getBlockState(x, y, z))) {
                        continue;
                    }
                    int worldX = candidate.baseX() + x;
                    int worldY = candidate.baseY() + y;
                    int worldZ = candidate.baseZ() + z;
                    if (origin.distanceToSqr(worldX + 0.5, worldY + 0.5, worldZ + 0.5) <= radiusSq) {
                        found.add(new BlockPos(worldX, worldY, worldZ));
                    }
                }
            }
        }
    }

    // Chunk-wide census used by the tuning fork: it answers "is there amethyst under this
    // chunk" without exposing a bearing to any single block.
    public static ChunkSignal surveyChunk(ServerLevel level, ChunkPos chunkPos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z());
        if (chunk == null) {
            return ChunkSignal.EMPTY;
        }

        int count = 0;
        long ySum = 0;
        for (int index = 0; index < chunk.getSections().length; index++) {
            LevelChunkSection section = chunk.getSection(index);
            if (section.hasOnlyAir() || !section.maybeHas(SIGNATURE)) {
                continue;
            }
            int baseY = chunk.getSectionYFromSectionIndex(index) * SECTION_SIZE;
            for (int y = 0; y < SECTION_SIZE; y++) {
                for (int z = 0; z < SECTION_SIZE; z++) {
                    for (int x = 0; x < SECTION_SIZE; x++) {
                        if (SIGNATURE.test(section.getBlockState(x, y, z))) {
                            count++;
                            ySum += baseY + y;
                        }
                    }
                }
            }
        }
        return count == 0 ? ChunkSignal.EMPTY : new ChunkSignal(count, (int) (ySum / count));
    }

    public record ChunkSignal(int count, int centroidY) {
        public static final ChunkSignal EMPTY = new ChunkSignal(0, 0);

        public boolean isEmpty() {
            return count <= 0;
        }
    }

    private static List<Candidate> gather(ServerLevel level, Vec3 origin, int radius, int verticalReach,
            double radiusSq) {
        List<Candidate> candidates = new ArrayList<>();
        int minChunkX = SectionPos.blockToSectionCoord(origin.x - radius);
        int maxChunkX = SectionPos.blockToSectionCoord(origin.x + radius);
        int minChunkZ = SectionPos.blockToSectionCoord(origin.z - radius);
        int maxChunkZ = SectionPos.blockToSectionCoord(origin.z + radius);
        int minSectionY = Math.max(level.getMinSectionY(), SectionPos.blockToSectionCoord(origin.y - verticalReach));
        int maxSectionY = Math.min(level.getMaxSectionY(), SectionPos.blockToSectionCoord(origin.y + verticalReach));

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                // getChunkNow returns null for anything not already loaded; a survey must
                // never force-load chunks.
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null || horizontalDistSq(chunkX, chunkZ, origin) > radiusSq) {
                    continue;
                }
                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    int index = chunk.getSectionIndexFromSectionY(sectionY);
                    if (index < 0 || index >= chunk.getSections().length) {
                        continue;
                    }
                    LevelChunkSection section = chunk.getSection(index);
                    double nearDistSq = sectionDistSq(chunkX, sectionY, chunkZ, origin);
                    if (section.hasOnlyAir() || nearDistSq > radiusSq || !section.maybeHas(SIGNATURE)) {
                        continue;
                    }
                    candidates.add(new Candidate(section, chunkX * SECTION_SIZE, sectionY * SECTION_SIZE,
                            chunkZ * SECTION_SIZE, nearDistSq));
                }
            }
        }
        return candidates;
    }

    private static void scanSection(Candidate candidate, Vec3 origin, Best best) {
        for (int y = 0; y < SECTION_SIZE; y++) {
            for (int z = 0; z < SECTION_SIZE; z++) {
                for (int x = 0; x < SECTION_SIZE; x++) {
                    if (!SIGNATURE.test(candidate.section().getBlockState(x, y, z))) {
                        continue;
                    }
                    int worldX = candidate.baseX() + x;
                    int worldY = candidate.baseY() + y;
                    int worldZ = candidate.baseZ() + z;
                    double distSq = origin.distanceToSqr(worldX + 0.5, worldY + 0.5, worldZ + 0.5);
                    if (distSq < best.distSq) {
                        best.distSq = distSq;
                        best.pos = new BlockPos(worldX, worldY, worldZ);
                    }
                }
            }
        }
    }

    private static double horizontalDistSq(int sectionX, int sectionZ, Vec3 origin) {
        double dx = axisDistance(origin.x, sectionX * SECTION_SIZE);
        double dz = axisDistance(origin.z, sectionZ * SECTION_SIZE);
        return dx * dx + dz * dz;
    }

    private static double sectionDistSq(int sectionX, int sectionY, int sectionZ, Vec3 origin) {
        double dx = axisDistance(origin.x, sectionX * SECTION_SIZE);
        double dy = axisDistance(origin.y, sectionY * SECTION_SIZE);
        double dz = axisDistance(origin.z, sectionZ * SECTION_SIZE);
        return dx * dx + dy * dy + dz * dz;
    }

    // Distance from a coordinate to the nearest edge of the 16-block span starting at base.
    private static double axisDistance(double coordinate, int base) {
        return Math.max(0.0, Math.max(base - coordinate, coordinate - (base + SECTION_SIZE)));
    }

    private record Candidate(LevelChunkSection section, int baseX, int baseY, int baseZ, double nearDistSq) {
    }

    private static final class Best {
        private BlockPos pos;
        private double distSq;

        private Best(double distSq) {
            this.distSq = distSq;
        }
    }
}
