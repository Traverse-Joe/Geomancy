package com.traverse.geomancy.block.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.Config;
import com.traverse.geomancy.block.TectonicNodeBlock;
import com.traverse.geomancy.essence.BiomeEssence;
import com.traverse.geomancy.essence.Essence;
import com.traverse.geomancy.essence.EssenceCharge;
import com.traverse.geomancy.essence.EssenceForm;
import com.traverse.geomancy.network.EssenceProvider;
import com.traverse.geomancy.registry.ModBlockEntities;

// A wild node is a power SOURCE first; it can also run jobs directly (§7 Tier 1's early
// path, before a player has any pillars) but at half the range and double the essence
// cost of a proper Resonance Pillar, so pillars are always the better choice once built.
public class TectonicNodeBlockEntity extends EssenceWorkerBlockEntity implements EssenceProvider {
    public static final int REVEAL_DURATION_TICKS = 200;
    private static final int COST_MULTIPLIER = 2;
    private static final int WORK_RANGE = 8;

    private long revealedUntil;

    // Null until seeded from the biome on the node's first server tick, then fixed for
    // the node's lifetime - essence already produced should not retroactively change
    // type if something later edits the biome underneath it.
    private @Nullable Essence storedEssence;
    private int essenceAmount;

    public TectonicNodeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TECTONIC_NODE.get(), pos, state);
    }

    public Essence essence() {
        if (storedEssence != null) {
            return storedEssence;
        }
        return level == null ? Essence.TERRA : BiomeEssence.at(level, worldPosition);
    }

    public Essence cycleDebugEssence(Level level) {
        Essence[] values = Essence.values();
        storedEssence = values[(essence().ordinal() + 1) % values.length];
        reveal(level);
        sync();
        return storedEssence;
    }

    public Essence clearDebugEssence(Level level) {
        storedEssence = BiomeEssence.at(level, worldPosition);
        reveal(level);
        sync();
        return storedEssence;
    }

    @Override
    protected Essence workEssence() {
        return essence();
    }

    @Override
    protected EssenceForm workForm() {
        return EssenceForm.RAW;
    }

    @Override
    protected int costMultiplier() {
        return COST_MULTIPLIER;
    }

    @Override
    public int workRange() {
        return WORK_RANGE;
    }

    @Override
    protected int consume(int amount, boolean simulate) {
        return extract(amount, simulate);
    }

    @Override
    protected void onJobStarted(Level level) {
        reveal(level);
    }

    @Override
    public EssenceCharge charge() {
        return new EssenceCharge(essence(), EssenceForm.RAW, essenceAmount);
    }

    @Override
    public int capacity() {
        return Config.NODE_CAPACITY.get();
    }

    @Override
    public int extract(int amount, boolean simulate) {
        int taken = Math.min(amount, essenceAmount);
        if (!simulate && taken > 0) {
            essenceAmount -= taken;
            setChanged();
        }
        return taken;
    }

    public void reveal(Level level) {
        revealedUntil = level.getGameTime() + REVEAL_DURATION_TICKS;
        if (!level.getBlockState(worldPosition).getValue(TectonicNodeBlock.REVEALED)) {
            level.setBlock(worldPosition, getBlockState().setValue(TectonicNodeBlock.REVEALED, true), Block.UPDATE_ALL);
        }
    }

    // revealedUntil is intentionally not persisted, so a node reloaded mid-reveal
    // expires on its first tick rather than staying visible forever.
    public static void serverTick(Level level, BlockPos pos, BlockState state, TectonicNodeBlockEntity node) {
        if (node.storedEssence == null) {
            node.storedEssence = BiomeEssence.at(level, pos);
        }

        int capacity = node.capacity();
        if (node.essenceAmount < capacity && level.getGameTime() % Config.NODE_REGEN_INTERVAL.get() == 0) {
            node.essenceAmount = Math.min(capacity, node.essenceAmount + Config.NODE_REGEN_AMOUNT.get());
            node.setChanged();
        }

        if (node.hasJob()) {
            node.tickJob(level);
        }

        if (!node.hasJob() && level.getGameTime() >= node.revealedUntil
                && state.getValue(TectonicNodeBlock.REVEALED)) {
            level.setBlock(pos, state.setValue(TectonicNodeBlock.REVEALED, false), Block.UPDATE_ALL);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TectonicNodeBlockEntity node) {
        node.clientAdvanceJob();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("essence", Essence.CODEC, storedEssence);
        output.putInt("essence_amount", essenceAmount);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedEssence = input.read("essence", Essence.CODEC).orElse(null);
        essenceAmount = input.getIntOr("essence_amount", 0);
    }
}
