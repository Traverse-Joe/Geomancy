package com.traverse.geomancy.data;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.block.TectonicNodeBlock;
import com.traverse.geomancy.registry.ModBlocks;
import com.traverse.geomancy.registry.ModItems;

public class GeomancyModelProvider extends ModelProvider {
    private static final ModelTemplate NODE_CORE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Geomancy.MODID, "block/tectonic_node_core")),
            Optional.empty(),
            TextureSlot.ALL);

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

        itemModels.generateFlatItem(ModItems.RESONANT_AMETHYST_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GEOMANCER_BELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GEOMANCER_TUNING_HAMMER.get(), ModelTemplates.FLAT_ITEM);
    }

    private static TextureMapping textureOf(String path) {
        return TextureMapping.singleSlot(TextureSlot.ALL,
                new Material(Identifier.fromNamespaceAndPath(Geomancy.MODID, path), false));
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of(BuiltInRegistries.BLOCK.wrapAsHolder(ModBlocks.TECTONIC_NODE.get()));
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of(
                BuiltInRegistries.ITEM.wrapAsHolder(ModBlocks.TECTONIC_NODE_ITEM.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.RESONANT_AMETHYST_SHARD.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.GEOMANCER_BELL.get()),
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.GEOMANCER_TUNING_HAMMER.get()));
    }
}
