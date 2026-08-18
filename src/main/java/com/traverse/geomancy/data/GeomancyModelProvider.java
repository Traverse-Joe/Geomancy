package com.traverse.geomancy.data;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.block.ResonancePillarBlock;
import com.traverse.geomancy.block.ResonantBrazierBlock;
import com.traverse.geomancy.block.TectonicNodeBlock;
import com.traverse.geomancy.registry.ModBlocks;
import com.traverse.geomancy.registry.ModDataComponents;
import com.traverse.geomancy.registry.ModItems;

public class GeomancyModelProvider extends ModelProvider {
    private static final ModelTemplate NODE_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/tectonic_node_core")),
            Optional.empty(),
            TextureSlot.ALL);
    private static final ModelTemplate JAR_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/resonance_jar_core")),
            Optional.empty(),
            TextureSlot.ALL);
    private static final ModelTemplate PEDESTAL_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/item_pedestal_core")),
            Optional.empty(), TextureSlot.ALL);
    private static final ModelTemplate RESONANCE_PEDESTAL_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/resonance_pedestal_core")),
            Optional.empty(), TextureSlot.ALL);
    private static final ModelTemplate GEODE_JAR_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/geode_jar_core")),
            Optional.empty(), TextureSlot.ALL);
    private static final ModelTemplate PIEZO_ANVIL_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/piezo_anvil_core")),
            Optional.empty(), TextureSlot.ALL);
    private static final ModelTemplate RESONANT_HEARTH_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/resonant_hearth_core")),
            Optional.empty(), TextureSlot.ALL);
    private static final ModelTemplate RESONANT_BRAZIER_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/resonant_brazier_core")),
            Optional.empty(), TextureSlot.ALL);
    // The Pylon source model paints its obsidian "wings" and its runed-brick totem body as
    // two distinct materials rather than one, so this keeps both instead of collapsing to
    // a single texture like the mod's other placeholder shapes.
    private static final TextureSlot HEARTH_CRYSTAL_PRIMARY = TextureSlot.create("2");
    private static final TextureSlot HEARTH_CRYSTAL_SECONDARY = TextureSlot.create("3");
    private static final ModelTemplate HEARTH_CRYSTAL_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/hearth_crystal_core")),
            Optional.empty(), HEARTH_CRYSTAL_PRIMARY, HEARTH_CRYSTAL_SECONDARY);
    private static final ModelTemplate BATTERY_CRYSTAL_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/battery_crystal_core")),
            Optional.empty(), TextureSlot.ALL);
    private static final ModelTemplate RESONANCE_EMITTER_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/resonance_emitter_core")),
            Optional.empty(), TextureSlot.ALL);
    private static final ModelTemplate RESONANCE_RECEIVER_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/resonance_receiver_core")),
            Optional.empty(), TextureSlot.ALL);

    public GeomancyModelProvider(PackOutput output) {
        super(output, Geomancy.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block node = ModBlocks.TECTONIC_NODE.get();

        Identifier revealed = NODE_CORE.create(node, textureOf("block/tectonic_node"), blockModels.modelOutput);
        Identifier hidden = NODE_CORE.createWithSuffix(node, "_hidden", textureOf("block/tectonic_node_hidden"), blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(node).with(
                BlockModelGenerators.createBooleanModelDispatch(
                        TectonicNodeBlock.REVEALED,
                        BlockModelGenerators.plainVariant(revealed),
                        BlockModelGenerators.plainVariant(hidden))));

        blockModels.registerSimpleItemModel(node, revealed);

        Block pillar = ModBlocks.RESONANCE_PILLAR.get();
        Identifier unlinked = ModelTemplates.CUBE_ALL.create(pillar, textureOf("block/resonance_pillar"), blockModels.modelOutput);
        Identifier linked = ModelTemplates.CUBE_ALL.createWithSuffix(pillar, "_linked",
                textureOf("block/resonance_pillar_linked"), blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(pillar).with(
                BlockModelGenerators.createBooleanModelDispatch(
                        ResonancePillarBlock.LINKED,
                        BlockModelGenerators.plainVariant(linked),
                        BlockModelGenerators.plainVariant(unlinked))));

        blockModels.registerSimpleItemModel(pillar, unlinked);

        Block jar = ModBlocks.RESONANCE_JAR.get();
        Identifier jarModel = JAR_CORE.create(jar, textureOf("block/resonance_jar"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(jar, BlockModelGenerators.plainVariant(jarModel)));
        blockModels.registerSimpleItemModel(jar, jarModel);

        Block pedestal = ModBlocks.ITEM_PEDESTAL.get();
        Identifier pedestalModel = PEDESTAL_CORE.create(pedestal, textureOf("block/resonance_pillar"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(pedestal,
                BlockModelGenerators.plainVariant(pedestalModel)));
        blockModels.registerSimpleItemModel(pedestal, pedestalModel);

        Block geodeJar = ModBlocks.GEODE_JAR.get();
        Identifier geodeJarModel = GEODE_JAR_CORE.create(geodeJar, textureOf("block/geode_jar"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(geodeJar,
                BlockModelGenerators.plainVariant(geodeJarModel)));
        blockModels.registerSimpleItemModel(geodeJar, geodeJarModel);

        Block piezoAnvil = ModBlocks.PIEZO_ANVIL.get();
        Identifier piezoModel = PIEZO_ANVIL_CORE.create(piezoAnvil,
                textureOf("block/resonance_pillar"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(piezoAnvil,
                BlockModelGenerators.plainVariant(piezoModel)));
        blockModels.registerSimpleItemModel(piezoAnvil, piezoModel);

        Block hearth = ModBlocks.RESONANT_HEARTH.get();
        Identifier hearthModel = RESONANT_HEARTH_CORE.create(hearth,
                textureOf("block/resonant_hearth"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(hearth,
                BlockModelGenerators.plainVariant(hearthModel)));
        blockModels.registerSimpleItemModel(hearth, hearthModel);

        Block brazier = ModBlocks.RESONANT_BRAZIER.get();
        Identifier brazierUnlit = RESONANT_BRAZIER_CORE.create(brazier,
                textureOf("block/resonant_brazier"), blockModels.modelOutput);
        Identifier brazierLit = RESONANT_BRAZIER_CORE.createWithSuffix(brazier, "_lit",
                textureOf("block/resonant_brazier_lit"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(brazier).with(
                BlockModelGenerators.createBooleanModelDispatch(ResonantBrazierBlock.LIT,
                        BlockModelGenerators.plainVariant(brazierLit),
                        BlockModelGenerators.plainVariant(brazierUnlit))));
        blockModels.registerSimpleItemModel(brazier, brazierUnlit);

        Block hearthCrystal = ModBlocks.HEARTH_CRYSTAL.get();
        TextureMapping hearthCrystalTextures = new TextureMapping()
                .put(HEARTH_CRYSTAL_PRIMARY, new Material(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/hearth_crystal_primary"), false))
                .put(HEARTH_CRYSTAL_SECONDARY, new Material(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/hearth_crystal_secondary"), false));
        Identifier hearthCrystalModel = HEARTH_CRYSTAL_CORE.create(hearthCrystal,
                hearthCrystalTextures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(hearthCrystal,
                BlockModelGenerators.plainVariant(hearthCrystalModel)).with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blockModels.registerSimpleItemModel(hearthCrystal, hearthCrystalModel);

        Block smallBattery = ModBlocks.SMALL_BATTERY_CRYSTAL.get();
        Identifier smallBatteryModel = BATTERY_CRYSTAL_CORE.create(smallBattery, textureOf("block/raw_resonant_crystal"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(smallBattery,
                BlockModelGenerators.plainVariant(smallBatteryModel)));
        blockModels.registerSimpleItemModel(smallBattery, smallBatteryModel);

        Block mediumBattery = ModBlocks.MEDIUM_BATTERY_CRYSTAL.get();
        Identifier mediumBatteryModel = BATTERY_CRYSTAL_CORE.create(mediumBattery, textureOf("block/raw_resonant_crystal"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(mediumBattery,
                BlockModelGenerators.plainVariant(mediumBatteryModel)));
        blockModels.registerSimpleItemModel(mediumBattery, mediumBatteryModel);

        Block largeBattery = ModBlocks.LARGE_BATTERY_CRYSTAL.get();
        Identifier largeBatteryModel = BATTERY_CRYSTAL_CORE.create(largeBattery, textureOf("block/raw_resonant_crystal"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(largeBattery,
                BlockModelGenerators.plainVariant(largeBatteryModel)));
        blockModels.registerSimpleItemModel(largeBattery, largeBatteryModel);

        Block emitter = ModBlocks.RESONANCE_EMITTER.get();
        Identifier emitterModel = RESONANCE_EMITTER_CORE.create(emitter, textureOf("block/resonance_pillar"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(emitter,
                BlockModelGenerators.plainVariant(emitterModel)).with(BlockModelGenerators.ROTATION_FACING));
        blockModels.registerSimpleItemModel(emitter, emitterModel);

        Block receiver = ModBlocks.RESONANCE_RECEIVER.get();
        Identifier receiverModel = RESONANCE_RECEIVER_CORE.create(receiver, textureOf("block/resonance_pillar_linked"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(receiver,
                BlockModelGenerators.plainVariant(receiverModel)).with(BlockModelGenerators.ROTATION_FACING));
        blockModels.registerSimpleItemModel(receiver, receiverModel);

        Block resonancePedestal = ModBlocks.RESONANCE_PEDESTAL.get();
        Identifier resonancePedestalModel = RESONANCE_PEDESTAL_CORE.create(resonancePedestal,
                textureOf("block/resonance_pillar"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(resonancePedestal,
                BlockModelGenerators.plainVariant(resonancePedestalModel)));
        blockModels.registerSimpleItemModel(resonancePedestal, resonancePedestalModel);

        itemModels.generateFlatItem(ModItems.RESONANT_AMETHYST_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RESONANT_AMETHYST_FOCUS.get(), ModItems.RESONANT_AMETHYST_SHARD.get(),
                ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.IRON_TUNING_FORK.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.GEOMANCER_BELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RESONANT_TUNING_FORK.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.ESSENCE_DEBUG_STICK.get(), ModItems.RESONANT_TUNING_FORK.get(),
                ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GEOMANCER_ROUTING_ROD.get(), ModItems.RESONANT_TUNING_FORK.get(),
                ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.QUARTZ_DUST.get(), ModItems.RESONANT_AMETHYST_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.AMETHYST_DUST.get(), ModItems.RESONANT_AMETHYST_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.IRON_DUST.get(), ModItems.RESONANT_AMETHYST_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.COPPER_DUST.get(), ModItems.RESONANT_AMETHYST_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GOLD_DUST.get(), ModItems.RESONANT_AMETHYST_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SUBSTRATE_SLATE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ATTUNED_LENS_BLANK.get(), ModItems.RESONANT_AMETHYST_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RESONANT_CRYSTAL_SEED.get(), ModItems.RESONANT_AMETHYST_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RESONANCE_VESSEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LITHIC_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.RESONANT_WAYFINDER.get(), ModItems.GEOMANCER_BELL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generateResonantCrystalBlade(itemModels);
        itemModels.generateFlatItem(ModItems.VIBRANIC_RING.get(), ModItems.RESONANT_AMETHYST_SHARD.get(), ModelTemplates.FLAT_ITEM);
    }

    // Two flat models picked by the same component shift-use toggles, so resonance mode shows in
    // the hand and in the inventory without a renderer. An absent component reads as inactive,
    // which is what an uncrafted blade has.
    private static void generateResonantCrystalBlade(ItemModelGenerators itemModels) {
        Item blade = ModItems.RESONANT_CRYSTAL_BLADE.get();
        Identifier chargedTexture = Identifier.fromNamespaceAndPath(Geomancy.MODID,
                "item/resonant_crystal_blade_charged");
        Identifier idleModel = ModelTemplates.FLAT_HANDHELD_ITEM.create(blade,
                TextureMapping.layer0(blade), itemModels.modelOutput);
        Identifier chargedModel = ModelTemplates.FLAT_HANDHELD_ITEM.create(chargedTexture,
                TextureMapping.layer0(new Material(chargedTexture, false)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(blade, ItemModelUtils.select(
                new ComponentContents<>(ModDataComponents.BLADE_ACTIVATED.get()),
                ItemModelUtils.plainModel(idleModel),
                List.of(ItemModelUtils.when(Boolean.TRUE, ItemModelUtils.plainModel(chargedModel)))));
    }

    private static TextureMapping textureOf(String path) {
        return TextureMapping.singleSlot(TextureSlot.ALL,
                new Material(Identifier.fromNamespaceAndPath(Geomancy.MODID, path), false));
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of(
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.TECTONIC_NODE.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.RESONANCE_PILLAR.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.RESONANCE_JAR.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.ITEM_PEDESTAL.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.GEODE_JAR.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.PIEZO_ANVIL.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.RESONANT_HEARTH.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.RESONANT_BRAZIER.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.HEARTH_CRYSTAL.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.SMALL_BATTERY_CRYSTAL.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.MEDIUM_BATTERY_CRYSTAL.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.LARGE_BATTERY_CRYSTAL.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.RESONANCE_EMITTER.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.RESONANCE_RECEIVER.get()),
                BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.RESONANCE_PEDESTAL.get()));
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of(
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.TECTONIC_NODE_ITEM.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.RESONANCE_PILLAR_ITEM.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.RESONANCE_JAR.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.ITEM_PEDESTAL_ITEM.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.RESONANT_AMETHYST_SHARD.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.IRON_TUNING_FORK.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.GEOMANCER_BELL.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.RESONANT_TUNING_FORK.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.ESSENCE_DEBUG_STICK.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.GEOMANCER_ROUTING_ROD.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.RESONANT_AMETHYST_FOCUS.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.GEODE_JAR.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.PIEZO_ANVIL_ITEM.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.RESONANT_HEARTH_ITEM.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.RESONANT_BRAZIER_ITEM.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.QUARTZ_DUST.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.AMETHYST_DUST.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.IRON_DUST.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.COPPER_DUST.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.GOLD_DUST.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.SUBSTRATE_SLATE.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.ATTUNED_LENS_BLANK.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.RESONANT_CRYSTAL_SEED.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.RESONANCE_VESSEL.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.LITHIC_PICKAXE.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.HEARTH_CRYSTAL_ITEM.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.RESONANT_WAYFINDER.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.RESONANT_CRYSTAL_BLADE.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.VIBRANIC_RING.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.SMALL_BATTERY_CRYSTAL.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.MEDIUM_BATTERY_CRYSTAL.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.LARGE_BATTERY_CRYSTAL.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.RESONANCE_EMITTER_ITEM.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.RESONANCE_RECEIVER_ITEM.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.RESONANCE_PEDESTAL_ITEM.get()));
    }
}
