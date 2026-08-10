package com.traverse.geomancy;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.traverse.geomancy.registry.ModAttachments;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.registry.ModBlocks;
import com.traverse.geomancy.registry.ModDataComponents;
import com.traverse.geomancy.registry.ModFeatures;
import com.traverse.geomancy.registry.ModItems;
import com.traverse.geomancy.registry.ModRecipes;

@Mod(Geomancy.MODID)
public class Geomancy {
    public static final String MODID = "geomancy";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GEOMANCY_TAB = CREATIVE_MODE_TABS.register("geomancy_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.geomancy"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.RESONANT_AMETHYST_SHARD.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.RESONANT_AMETHYST_SHARD.get());
                output.accept(ModItems.RESONANT_AMETHYST_FOCUS.get());
                output.accept(ModItems.IRON_TUNING_FORK.get());
                output.accept(ModItems.GEOMANCER_BELL.get());
                output.accept(ModItems.GEOMANCER_TUNING_HAMMER.get());
                output.accept(ModBlocks.RESONANCE_PILLAR_ITEM);
                output.accept(ModBlocks.ITEM_PEDESTAL_ITEM);
                output.accept(ModItems.RESONANCE_JAR.get());
                output.accept(ModBlocks.TECTONIC_NODE_ITEM);
                output.accept(ModItems.ESSENCE_DEBUG_STICK.get());
                output.accept(ModItems.GEOMANCER_ROUTING_ROD.get());
                output.accept(ModBlocks.PIEZO_ANVIL_ITEM);
                output.accept(ModItems.GEODE_JAR.get());
                output.accept(ModBlocks.RESONANT_HEARTH_ITEM);
                output.accept(ModBlocks.RESONANT_BRAZIER_ITEM);
                output.accept(ModItems.QUARTZ_DUST.get());
                output.accept(ModItems.AMETHYST_DUST.get());
                output.accept(ModItems.IRON_DUST.get());
                output.accept(ModItems.COPPER_DUST.get());
                output.accept(ModItems.GOLD_DUST.get());
                output.accept(ModItems.SUBSTRATE_SLATE.get());
                output.accept(ModItems.ATTUNED_LENS_BLANK.get());
                output.accept(ModItems.RESONANT_CRYSTAL_SEED.get());
                output.accept(ModItems.RESONANCE_VESSEL.get());
                output.accept(ModItems.LITHIC_PICKAXE.get());
                output.accept(ModBlocks.HEARTH_CRYSTAL_ITEM);
                output.accept(ModItems.RESONANT_WAYFINDER.get());
            }).build());

    public Geomancy(IEventBus modEventBus, ModContainer modContainer) {
        // Forces ModBlocks/ModBlockEntities static init before the register calls below.
        ModBlocks.bootstrap();
        ModBlockEntities.bootstrap();
        ModDataComponents.bootstrap();
        ModItems.bootstrap();
        ModRecipes.bootstrap();
        ModFeatures.bootstrap();
        ModAttachments.bootstrap();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModRecipes.RECIPE_BOOK_CATEGORIES.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
