package com.traverse.geomancy.compat.curios;

import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

// Every Curios type stays inside CuriosVessels, which the JVM only links once one of
// these guarded calls actually runs. Geomancy loads fine without Curios installed.
public final class CuriosCompat {
    private static final boolean LOADED = ModList.get().isLoaded("curios");

    private CuriosCompat() {
    }

    public static boolean isLoaded() {
        return LOADED;
    }

    public static List<ItemStack> vessels(Player player) {
        return LOADED ? CuriosVessels.vessels(player) : List.of();
    }

    public static void markDirty(Player player, ItemStack stack) {
        if (LOADED) {
            CuriosVessels.markDirty(player, stack);
        }
    }
}
