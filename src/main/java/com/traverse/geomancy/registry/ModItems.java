package com.traverse.geomancy.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.item.GeomancerBellItem;
import com.traverse.geomancy.item.GeomancerTuningHammerItem;

public final class ModItems {
    private ModItems() {
    }

    public static final DeferredItem<Item> RESONANT_AMETHYST_SHARD = Geomancy.ITEMS.registerSimpleItem("resonant_amethyst_shard");

    public static final DeferredItem<GeomancerBellItem> GEOMANCER_BELL =
            Geomancy.ITEMS.registerItem("geomancer_bell", GeomancerBellItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<GeomancerTuningHammerItem> GEOMANCER_TUNING_HAMMER =
            Geomancy.ITEMS.registerItem("geomancer_tuning_hammer", GeomancerTuningHammerItem::new, p -> p.stacksTo(1));

    public static void bootstrap() {
    }
}
