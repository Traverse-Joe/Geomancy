package com.traverse.geomancy.resonance;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.registry.ModRegistries;

// A pool that carries one resonance type at a time. Empty accepts anything, a filled pool
// only accepts more of what it already holds, and draining it forgets the type - so a
// vessel is never permanently stained by whatever happened to reach it first.
//
// Untyped is a type for this purpose: a pool holding generator output rejects a typed
// pulse and vice versa, which is what makes "this jar is Ignis" a readable statement.
public final class TypedResonancePool {
    private @Nullable ResourceKey<ResonanceType> type;
    private int amount;

    // Render-side only: resolving a Holder out of the registry every frame for every
    // visible crystal is not worth it when the answer only changes when the type does.
    private @Nullable ResourceKey<ResonanceType> cachedColorType;
    private boolean colorResolved;
    private int cachedColor = ResonanceTypes.UNTYPED_COLOR;

    public @Nullable ResourceKey<ResonanceType> type() {
        return amount > 0 ? type : null;
    }

    public int amount() {
        return amount;
    }

    public boolean accepts(@Nullable ResourceKey<ResonanceType> incoming) {
        return amount <= 0 || Objects.equals(type, incoming);
    }

    public int insert(@Nullable ResourceKey<ResonanceType> incoming, int requested, int capacity, boolean simulate) {
        if (requested <= 0 || !accepts(incoming)) {
            return 0;
        }
        int accepted = Math.min(requested, capacity - amount);
        if (accepted <= 0) {
            return 0;
        }
        if (!simulate) {
            type = incoming;
            amount += accepted;
        }
        return accepted;
    }

    public int extract(int requested, boolean simulate) {
        int extracted = Math.min(Math.max(requested, 0), amount);
        if (extracted > 0 && !simulate) {
            amount -= extracted;
            if (amount == 0) {
                type = null;
            }
        }
        return extracted;
    }

    public void set(@Nullable ResourceKey<ResonanceType> newType, int newAmount) {
        amount = Math.max(newAmount, 0);
        type = amount > 0 ? newType : null;
    }

    public int color(@Nullable LevelReader level) {
        ResourceKey<ResonanceType> current = type();
        // The retry on a null level matters: a block entity can be asked for its colour
        // after load but before it has a level, and that answer must not stick.
        if (!colorResolved || !Objects.equals(current, cachedColorType)) {
            cachedColor = ResonanceTypes.color(level, current);
            cachedColorType = current;
            colorResolved = level != null;
        }
        return cachedColor;
    }

    public void save(ValueOutput output, String key) {
        output.putInt(key, amount);
        output.storeNullable(key + "_type", ResourceKey.codec(ModRegistries.RESONANCE_TYPE), type);
    }

    public void load(ValueInput input, String key, int capacity) {
        amount = Math.min(Math.max(input.getIntOr(key, 0), 0), capacity);
        type = amount > 0
                ? input.read(key + "_type", ResourceKey.codec(ModRegistries.RESONANCE_TYPE)).orElse(null)
                : null;
    }
}
