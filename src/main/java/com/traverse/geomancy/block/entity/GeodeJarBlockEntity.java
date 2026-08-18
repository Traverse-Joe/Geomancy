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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.registry.ModDataComponents;
import com.traverse.geomancy.resonance.BindResult;
import com.traverse.geomancy.resonance.ResonanceStorage;
import com.traverse.geomancy.resonance.ResonanceTypes;
import com.traverse.geomancy.resonance.ResonanceType;
import com.traverse.geomancy.resonance.TypedResonancePool;

// A jar bound to a Tectonic Node siphons that node's output and takes on its type, which
// is the only route by which stored resonance is anything other than plain generator
// output. Draw is limited by what the node has actually regenerated, so a jar drains a
// node rather than minting from it.
public class GeodeJarBlockEntity extends BlockEntity implements ResonanceStorage {
    public static final int CAPACITY = 10_000;

    private static final int DRAW_INTERVAL = 10;
    private static final int DRAW_AMOUNT = 25;
    private static final int MAX_NODE_DISTANCE = 16;

    // Spread across the draw cycle so a field of jars does not all siphon on one tick.
    private final int stagger;
    private @Nullable BlockPos node;

    private final TypedResonancePool pool = new TypedResonancePool();

    public GeodeJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEODE_JAR.get(), pos, state);
        this.stagger = Math.floorMod(pos.hashCode(), DRAW_INTERVAL);
    }

    public @Nullable BlockPos node() {
        return node;
    }

    public BindResult bindNode(Level level, BlockPos nodePos) {
        if (!(level.getBlockEntity(nodePos) instanceof TectonicNodeBlockEntity)) {
            return BindResult.SOURCE_GONE;
        }
        if (nodePos.distSqr(worldPosition) > (double) MAX_NODE_DISTANCE * MAX_NODE_DISTANCE) {
            return BindResult.TOO_FAR;
        }
        node = nodePos.immutable();
        sync();
        return BindResult.OK;
    }

    public void clearNode() {
        node = null;
        sync();
    }

    // Siphons on a fixed cadence. A missing, unloaded, or replaced node simply produces
    // nothing this tick; the binding survives so the jar resumes when the chunk returns.
    public static void serverTick(Level level, BlockPos pos, BlockState state, GeodeJarBlockEntity jar) {
        BlockPos nodePos = jar.node;
        if (nodePos == null || (level.getGameTime() + jar.stagger) % DRAW_INTERVAL != 0) {
            return;
        }
        if (!level.isLoaded(nodePos) || !(level.getBlockEntity(nodePos) instanceof TectonicNodeBlockEntity source)) {
            return;
        }
        ResourceKey<ResonanceType> type = ResonanceTypes.of(source.essence());
        int room = jar.insertResonance(type, DRAW_AMOUNT, true);
        if (room <= 0) {
            return;
        }
        int drawn = source.extract(room, false);
        if (drawn > 0) {
            jar.insertResonance(type, drawn, false);
        }
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
        return CAPACITY;
    }

    @Override
    public int insertResonance(@Nullable ResourceKey<ResonanceType> type, int amount, boolean simulate) {
        int accepted = pool.insert(type, amount, CAPACITY, simulate);
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

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        pool.set(components.get(ModDataComponents.STORED_RESONANCE_TYPE.get()),
                Math.min(components.getOrDefault(ModDataComponents.STORED_RESONANCE.get(), 0), CAPACITY));
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
        output.storeNullable("node", BlockPos.CODEC, node);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        pool.load(input, "resonance", CAPACITY);
        node = input.read("node", BlockPos.CODEC).orElse(null);
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
