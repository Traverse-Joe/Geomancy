package com.traverse.geomancy.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.item.BatteryCrystalItem;
import com.traverse.geomancy.item.GeomancerBellItem;
import com.traverse.geomancy.item.EssenceDebugStickItem;
import com.traverse.geomancy.item.GeomancerRoutingRodItem;
import com.traverse.geomancy.item.ResonanceJarItem;
import com.traverse.geomancy.item.ResonantTuningForkItem;
import com.traverse.geomancy.item.GeodeJarItem;
import com.traverse.geomancy.item.IronTuningForkItem;
import com.traverse.geomancy.item.LithicPickaxeItem;
import com.traverse.geomancy.item.ResonanceVesselItem;
import com.traverse.geomancy.item.ResonantWayfinderItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;

public final class ModItems {
    private ModItems() {
    }

    public static final DeferredItem<Item> RESONANT_AMETHYST_SHARD = Geomancy.ITEMS.registerSimpleItem("resonant_amethyst_shard");

    public static final DeferredItem<Item> RESONANT_AMETHYST_FOCUS =
            Geomancy.ITEMS.registerSimpleItem("resonant_amethyst_focus", properties -> properties.stacksTo(1));

    public static final DeferredItem<Item> QUARTZ_DUST = Geomancy.ITEMS.registerSimpleItem("quartz_dust");
    public static final DeferredItem<Item> AMETHYST_DUST = Geomancy.ITEMS.registerSimpleItem("amethyst_dust");
    public static final DeferredItem<Item> IRON_DUST = Geomancy.ITEMS.registerSimpleItem("iron_dust");
    public static final DeferredItem<Item> COPPER_DUST = Geomancy.ITEMS.registerSimpleItem("copper_dust");
    public static final DeferredItem<Item> GOLD_DUST = Geomancy.ITEMS.registerSimpleItem("gold_dust");
    public static final DeferredItem<Item> SUBSTRATE_SLATE = Geomancy.ITEMS.registerSimpleItem("substrate_slate");
    public static final DeferredItem<Item> ATTUNED_LENS_BLANK = Geomancy.ITEMS.registerSimpleItem("attuned_lens_blank");
    public static final DeferredItem<Item> RESONANT_CRYSTAL_SEED = Geomancy.ITEMS.registerSimpleItem("resonant_crystal_seed");

    public static final DeferredItem<ResonanceVesselItem> RESONANCE_VESSEL = Geomancy.ITEMS.registerItem(
            "resonance_vessel", ResonanceVesselItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<LithicPickaxeItem> LITHIC_PICKAXE = Geomancy.ITEMS.registerItem(
            "lithic_pickaxe", LithicPickaxeItem::new, properties -> properties
                    .pickaxe(ToolMaterial.IRON, 1.0F, -2.8F)
                    .durability(512)
                    .repairable(Items.AMETHYST_SHARD)
                    .enchantable(18));

    // Pre-amethyst prospecting tool: the only Geomancy item craftable before finding a geode.
    public static final DeferredItem<IronTuningForkItem> IRON_TUNING_FORK = Geomancy.ITEMS.registerItem(
            "iron_tuning_fork", IronTuningForkItem::new, properties -> properties
                    .durability(64)
                    .repairable(Items.IRON_INGOT));

    public static final DeferredItem<GeomancerBellItem> GEOMANCER_BELL =
            Geomancy.ITEMS.registerItem("geomancer_bell", GeomancerBellItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<ResonantTuningForkItem> RESONANT_TUNING_FORK =
            Geomancy.ITEMS.registerItem("resonant_tuning_fork", ResonantTuningForkItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<ResonanceJarItem> RESONANCE_JAR = Geomancy.ITEMS.registerItem(
            "resonance_jar", p -> new ResonanceJarItem(ModBlocks.RESONANCE_JAR.get(), p), p -> p.stacksTo(1));

    public static final DeferredItem<GeodeJarItem> GEODE_JAR = Geomancy.ITEMS.registerItem(
            "geode_jar", properties -> new GeodeJarItem(ModBlocks.GEODE_JAR.get(), properties),
            properties -> properties.stacksTo(1));

    public static final DeferredItem<BatteryCrystalItem> SMALL_BATTERY_CRYSTAL = Geomancy.ITEMS.registerItem(
            "small_battery_crystal", p -> new BatteryCrystalItem(ModBlocks.SMALL_BATTERY_CRYSTAL.get(), p), p -> p.stacksTo(1));

    public static final DeferredItem<BatteryCrystalItem> MEDIUM_BATTERY_CRYSTAL = Geomancy.ITEMS.registerItem(
            "medium_battery_crystal", p -> new BatteryCrystalItem(ModBlocks.MEDIUM_BATTERY_CRYSTAL.get(), p), p -> p.stacksTo(1));

    public static final DeferredItem<BatteryCrystalItem> LARGE_BATTERY_CRYSTAL = Geomancy.ITEMS.registerItem(
            "large_battery_crystal", p -> new BatteryCrystalItem(ModBlocks.LARGE_BATTERY_CRYSTAL.get(), p), p -> p.stacksTo(1));

    public static final DeferredItem<EssenceDebugStickItem> ESSENCE_DEBUG_STICK = Geomancy.ITEMS.registerItem(
            "essence_debug_stick", EssenceDebugStickItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<GeomancerRoutingRodItem> GEOMANCER_ROUTING_ROD = Geomancy.ITEMS.registerItem(
            "geomancer_routing_rod", GeomancerRoutingRodItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<ResonantWayfinderItem> RESONANT_WAYFINDER = Geomancy.ITEMS.registerItem(
            "resonant_wayfinder", ResonantWayfinderItem::new, p -> p.stacksTo(1));

    public static void bootstrap() {
    }
}
