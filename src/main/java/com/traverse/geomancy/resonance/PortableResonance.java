package com.traverse.geomancy.resonance;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.traverse.geomancy.item.ResonanceVesselItem;

public final class PortableResonance {
    private PortableResonance() {
    }

    public static int available(Player player) {
        return vessels(player).stream().mapToInt(ResonanceVesselItem::stored).sum();
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
                remaining -= taken;
            }
            if (remaining == 0) {
                return true;
            }
        }
        throw new IllegalStateException("Portable resonance changed during atomic extraction");
    }

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
        return vessels;
    }
}
