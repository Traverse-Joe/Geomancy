package com.traverse.geomancy.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.recipe.EssenceFilter;
import com.traverse.geomancy.recipe.TransmutationRecipe;
import com.traverse.geomancy.registry.ModItems;

public class GeomancyRecipeProvider extends RecipeProvider {
    protected GeomancyRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shapeless(RecipeCategory.MISC, ModItems.RESONANT_AMETHYST_SHARD.get())
                .requires(Items.AMETHYST_SHARD)
                .requires(Blocks.CALCITE)
                .requires(Items.REDSTONE)
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output);

        shaped(RecipeCategory.TOOLS, ModItems.GEOMANCER_BELL.get())
                .define('C', Items.COPPER_INGOT)
                .define('R', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('S', Items.STICK)
                .pattern("C")
                .pattern("R")
                .pattern("S")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.TOOLS, ModItems.GEOMANCER_TUNING_HAMMER.get())
                .define('M', Blocks.SMOOTH_STONE)
                .define('R', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('S', Items.STICK)
                .pattern("M")
                .pattern("R")
                .pattern("S")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        transmutation(Blocks.STONE, Blocks.IRON_ORE, 100)
                .save(output, Identifier.fromNamespaceAndPath(Geomancy.MODID, "transmutation/stone_to_iron_ore"));
    }

    private TransmutationRecipeBuilder transmutation(Block input, Block result, int duration) {
        return new TransmutationRecipeBuilder(
                HolderSet.direct(BuiltInRegistries.BLOCK.wrapAsHolder(input)),
                EssenceFilter.ANY,
                BuiltInRegistries.BLOCK.wrapAsHolder(result),
                duration);
    }

    private record TransmutationRecipeBuilder(HolderSet<Block> input, EssenceFilter essence,
                                              Holder<Block> result, int duration) {
        void save(RecipeOutput output, Identifier id) {
            output.accept(ResourceKey.create(Registries.RECIPE, id),
                    new TransmutationRecipe(input, essence, result, duration), null);
        }
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new GeomancyRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Geomancy Recipes";
        }
    }
}
