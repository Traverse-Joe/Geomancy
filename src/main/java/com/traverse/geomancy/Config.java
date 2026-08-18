package com.traverse.geomancy;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue NODE_CAPACITY = BUILDER
            .comment("Maximum raw essence a tectonic node can hold")
            .defineInRange("nodeCapacity", 2000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue NODE_REGEN_INTERVAL = BUILDER
            .comment("Ticks between node essence regeneration pulses")
            .defineInRange("nodeRegenInterval", 10, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue NODE_REGEN_AMOUNT = BUILDER
            .comment("Raw essence regenerated per pulse")
            .defineInRange("nodeRegenAmount", 1, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue RELAY_BUFFER_CAPACITY = BUILDER
            .comment("Maximum essence a resonance pillar or jar can buffer")
            .defineInRange("relayBufferCapacity", 200, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue RELAY_TRANSFER_INTERVAL = BUILDER
            .comment("Ticks between relay pull attempts")
            .defineInRange("relayTransferInterval", 10, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue RELAY_TRANSFER_AMOUNT = BUILDER
            .comment("Essence pulled per attempt")
            .defineInRange("relayTransferAmount", 20, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue RELAY_MAX_HOP = BUILDER
            .comment("Maximum link distance between relays, in blocks")
            .defineInRange("relayMaxHop", 16, 1, 64);
    public static final ModConfigSpec.IntValue RELAY_MAX_CHAIN_DEPTH = BUILDER
            .comment("Maximum number of hops from a wild node to a relay")
            .defineInRange("relayMaxChainDepth", 8, 1, 64);
    public static final ModConfigSpec.IntValue RELAY_REVALIDATE_INTERVAL = BUILDER
            .comment("Ticks between forced link revalidation, catching changes place/break events miss")
            .defineInRange("relayRevalidateInterval", 100, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue JAR_RAW_INTAKE = BUILDER
            .comment("Maximum raw essence a resonance jar can hold before refining")
            .defineInRange("jarRawIntake", 100, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue JAR_REFINED_CAPACITY = BUILDER
            .comment("Maximum refined essence a resonance jar can store")
            .defineInRange("jarRefinedCapacity", 500, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue JAR_REFINE_INTERVAL = BUILDER
            .comment("Ticks between refining pulses")
            .defineInRange("jarRefineInterval", 20, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue JAR_REFINE_RATIO = BUILDER
            .comment("Raw essence consumed per unit of refined essence produced")
            .defineInRange("jarRefineRatio", 5, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ITEM_SCAN_INTERVAL = BUILDER
            .comment("Ticks between a Aery pillar's logistics scans")
            .defineInRange("itemScanInterval", 10, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue LOGISTICS_COST = BUILDER
            .comment("Aery essence consumed per item moved")
            .defineInRange("logisticsCost", 5, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue LOGISTICS_RANGE = BUILDER
            .comment("Maximum endpoint distance for Aery logistics")
            .defineInRange("logisticsRange", 8, 1, 32);
    public static final ModConfigSpec.DoubleValue LOGISTICS_PICKUP_RADIUS = BUILDER
            .comment("Radius of an Aery world-item pickup circle")
            .defineInRange("logisticsPickupRadius", 2.5D, 0.5D, 8.0D);
    public static final ModConfigSpec.IntValue PEDESTAL_BUFFER_CAPACITY = BUILDER
            .comment("Maximum essence held by an item pedestal")
            .defineInRange("pedestalBufferCapacity", 100, 1, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}
