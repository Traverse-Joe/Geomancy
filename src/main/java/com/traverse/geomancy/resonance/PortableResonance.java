package com.traverse.geomancy.resonance;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.traverse.geomancy.compat.curios.CuriosCompat;
import com.traverse.geomancy.item.ResonanceVesselItem;

public final class PortableResonance {
    private PortableResonance() {
    }

    public static int available(Player player) {
        return vessels(player).stream().mapToInt(ResonanceVesselItem::stored).sum();
    }

    // Single-pass readout for the HUD, which would otherwise rescan the inventory every frame.
    public record Carried(int stored, int capacity) {
        public boolean isEmpty() {
            return capacity <= 0;
        }

        public float fill() {
            return capacity <= 0 ? 0.0F : Math.clamp(stored / (float) capacity, 0.0F, 1.0F);
        }
    }

    public static Carried carried(Player player) {
        List<ItemStack> vessels = vessels(player);
        int stored = 0;
        for (ItemStack vessel : vessels) {
            stored += ResonanceVesselItem.stored(vessel);
        }
        return new Carried(stored, vessels.size() * ResonanceVesselItem.CAPACITY);
    }

    public static boolean consume(Player player, int amount) {
        if (amount < 0 || available(player) < amount) {
            return false;
        }
        int remaining = amount;
        for (ItemStack vessel : vessels(player)) {
            int taken = Math.min(remaining, ResonanceVesselItem.stored(vessel));
            if (taken > 0) {
                ResonanceVesselItem.setStored(vessel, ResonanceVesselItem.stored(vessel) - taken);
                CuriosCompat.markDirty(player, vessel);
                remaining -= taken;
            }
            if (remaining == 0) {
                return true;
            }
        }
        throw new IllegalStateException("Portable resonance changed during atomic extraction");
    }

    // Worn vessels come last so a Curios ring acts as a reserve behind carried ones.
    private static List<ItemStack> vessels(Player player) {
        List<ItemStack> vessels = new ArrayList<>();
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof ResonanceVesselItem) {
            vessels.add(offhand);
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack != offhand && stack.getItem() instanceof ResonanceVesselItem) {
                vessels.add(stack);
            }
        }
        vessels.addAll(CuriosCompat.vessels(player));
        return vessels;
    }
}
