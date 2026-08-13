package com.traverse.geomancy.data;

import java.util.Optional;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.essence.Essence;
import com.traverse.geomancy.essence.EssenceForm;
import com.traverse.geomancy.recipe.EssenceFilter;
import com.traverse.geomancy.recipe.HearthIngredient;
import com.traverse.geomancy.recipe.HearthSynthesisRecipe;
import com.traverse.geomancy.recipe.LithicShatteringRecipe;
import com.traverse.geomancy.recipe.PedestalSynthesisRecipe;
import com.traverse.geomancy.recipe.TransmutationRecipe;
import com.traverse.geomancy.recipe.TransmutationResult;
import com.traverse.geomancy.recipe.TransmutationSubject;
import com.traverse.geomancy.registry.ModBlocks;
import com.traverse.geomancy.registry.ModItems;
import com.traverse.geomancy.resonance.ResonanceCost;

public class GeomancyRecipeProvider extends RecipeProvider {
    protected GeomancyRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        // Unlocked by iron, not amethyst: this is the tool that finds the first amethyst.
        shaped(RecipeCategory.TOOLS, ModItems.IRON_TUNING_FORK.get())
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .pattern("I I")
                .pattern(" I ")
                .pattern(" S ")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

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

        shaped(RecipeCategory.TOOLS, ModItems.RESONANT_TUNING_FORK.get())
                .define('C', Items.COPPER_INGOT)
                .define('R', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('S', Items.STICK)
                .pattern("C C")
                .pattern(" R ")
                .pattern(" S ")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.TOOLS, ModItems.GEOMANCER_ROUTING_ROD.get())
                .define('C', Items.COPPER_INGOT)
                .define('R', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('S', Items.STICK)
                .pattern("R C")
                .pattern(" S ")
                .pattern(" S ")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.RESONANCE_PILLAR_ITEM.get(), 2)
                .define('C', Blocks.CHISELED_STONE_BRICKS)
                .define('R', ModItems.RESONANT_AMETHYST_SHARD.get())
                .pattern("C")
                .pattern("R")
                .pattern("C")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.MISC, ModItems.RESONANCE_JAR.get())
                .define('G', Items.GLASS)
                .define('R', ModItems.RESONANT_AMETHYST_SHARD.get())
                .pattern("GRG")
                .pattern("G G")
                .pattern("GGG")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.ITEM_PEDESTAL_ITEM.get())
                .define('S', Blocks.SMOOTH_STONE_SLAB)
                .define('R', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('C', Blocks.CHISELED_STONE_BRICKS)
                .pattern(" S ")
                .pattern(" R ")
                .pattern(" C ")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.PIEZO_ANVIL_ITEM.get())
                .define('D', Blocks.DEEPSLATE_BRICKS)
                .define('C', Items.COPPER_INGOT)
                .define('A', Items.AMETHYST_SHARD)
                .pattern("DDD")
                .pattern("CAC")
                .pattern("DDD")
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output);

        shaped(RecipeCategory.MISC, ModItems.GEODE_JAR.get())
                .define('G', Items.GLASS)
                .define('A', Items.AMETHYST_SHARD)
                .define('C', Items.COPPER_INGOT)
                .pattern("GAG")
                .pattern("G G")
                .pattern("GCG")
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.SMALL_BATTERY_CRYSTAL.get())
                .define('A', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('C', Items.COPPER_INGOT)
                .pattern("ACA")
                .pattern("C C")
                .pattern("ACA")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.MEDIUM_BATTERY_CRYSTAL.get())
                .define('A', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('B', ModBlocks.SMALL_BATTERY_CRYSTAL.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.LARGE_BATTERY_CRYSTAL.get())
                .define('A', Blocks.AMETHYST_BLOCK)
                .define('B', ModBlocks.MEDIUM_BATTERY_CRYSTAL.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.RESONANCE_EMITTER.get())
                .define('C', Items.COPPER_INGOT)
                .define('R', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('D', Items.REDSTONE)
                .pattern("CDC")
                .pattern(" R ")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.RESONANCE_RECEIVER.get())
                .define('C', Items.COPPER_INGOT)
                .define('R', ModItems.RESONANT_AMETHYST_SHARD.get())
                .pattern("CCC")
                .pattern(" R ")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.RESONANCE_PEDESTAL_ITEM.get())
                .define('S', Blocks.SMOOTH_STONE_SLAB)
                .define('R', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('B', Blocks.CHISELED_STONE_BRICKS)
                .define('C', Items.COPPER_INGOT)
                .pattern(" S ")
                .pattern(" R ")
                .pattern("CBC")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.RESONANT_HEARTH_ITEM.get())
                .define('D', Blocks.POLISHED_DEEPSLATE)
                .define('C', Items.COPPER_INGOT)
                .pattern("D D")
                .pattern("DCD")
                .pattern("DDD")
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.RESONANT_BRAZIER_ITEM.get())
                .define('D', Blocks.POLISHED_DEEPSLATE)
                .define('C', Items.COPPER_INGOT)
                .define('I', Items.IRON_BARS)
                .pattern("I I")
                .pattern("CDC")
                .pattern("DDD")
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output);

        shaped(RecipeCategory.MISC, ModBlocks.HEARTH_CRYSTAL_ITEM.get())
                .define('D', Blocks.POLISHED_DEEPSLATE)
                .define('A', Items.AMETHYST_SHARD)
                .define('C', Items.COPPER_INGOT)
                .pattern("DAD")
                .pattern("ACA")
                .pattern("DAD")
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output);

        shaped(RecipeCategory.TOOLS, ModItems.RESONANT_WAYFINDER.get())
                .define('C', Items.COPPER_INGOT)
                .define('A', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('S', Items.STICK)
                .pattern(" A ")
                .pattern("CAC")
                .pattern(" S ")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.COMBAT, ModItems.RESONANT_CRYSTAL_BLADE.get())
                .define('I', Items.IRON_INGOT)
                .define('A', ModItems.RESONANT_AMETHYST_SHARD.get())
                .define('S', Items.STICK)
                .pattern("I I")
                .pattern(" A ")
                .pattern(" S ")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        shaped(RecipeCategory.COMBAT, ModItems.VIBRANIC_RING.get())
                .define('C', Items.COPPER_INGOT)
                .define('A', ModItems.RESONANT_AMETHYST_SHARD.get())
                .pattern(" C ")
                .pattern("C C")
                .pattern(" A ")
                .unlockedBy("has_resonant_amethyst_shard", has(ModItems.RESONANT_AMETHYST_SHARD.get()))
                .save(output);

        output.accept(ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(Geomancy.MODID, "hearth_synthesis/resonant_amethyst_focus")),
                new HearthSynthesisRecipe(List.of(
                        new HearthIngredient(Ingredient.of(Items.AMETHYST_SHARD), 1),
                        new HearthIngredient(Ingredient.of(Items.QUARTZ), 1),
                        new HearthIngredient(Ingredient.of(Items.COPPER_INGOT), 1)),
                        false, ResonanceCost.any(500), 20, new ItemStackTemplate(ModItems.RESONANT_AMETHYST_FOCUS.get())), null);

        hearth("quartz_dust", List.of(new HearthIngredient(Ingredient.of(Items.QUARTZ), 1)),
                40, new ItemStackTemplate(ModItems.QUARTZ_DUST.get(), 2));
        hearth("amethyst_dust", List.of(new HearthIngredient(Ingredient.of(Items.AMETHYST_SHARD), 1)),
                50, new ItemStackTemplate(ModItems.AMETHYST_DUST.get(), 2));
        hearth("substrate_slate", List.of(
                        new HearthIngredient(Ingredient.of(Blocks.SMOOTH_STONE), 1),
                        new HearthIngredient(Ingredient.of(ModItems.AMETHYST_DUST.get()), 1)),
                150, new ItemStackTemplate(ModItems.SUBSTRATE_SLATE.get()));
        hearth("resonance_vessel", List.of(
                        new HearthIngredient(Ingredient.of(Items.GLASS_BOTTLE), 1),
                        new HearthIngredient(Ingredient.of(Items.AMETHYST_SHARD), 1),
                        new HearthIngredient(Ingredient.of(Items.COPPER_INGOT), 1),
                        new HearthIngredient(Ingredient.of(ModItems.QUARTZ_DUST.get()), 1)),
                300, new ItemStackTemplate(ModItems.RESONANCE_VESSEL.get()));
        hearth("lithic_pickaxe", List.of(
                        new HearthIngredient(Ingredient.of(Items.IRON_PICKAXE), 1),
                        new HearthIngredient(Ingredient.of(Items.COPPER_INGOT), 1),
                        new HearthIngredient(Ingredient.of(Items.COPPER_INGOT), 1),
                        new HearthIngredient(Ingredient.of(Items.AMETHYST_SHARD), 1),
                        new HearthIngredient(Ingredient.of(Items.AMETHYST_SHARD), 1),
                        new HearthIngredient(Ingredient.of(ModItems.SUBSTRATE_SLATE.get()), 1)),
                750, new ItemStackTemplate(ModItems.LITHIC_PICKAXE.get()));

        hearth("gravel", List.of(new HearthIngredient(Ingredient.of(Blocks.COBBLESTONE), 1)),
                20, new ItemStackTemplate(Blocks.GRAVEL.asItem()));
        hearth("sand", List.of(new HearthIngredient(Ingredient.of(Blocks.GRAVEL), 1)),
                20, new ItemStackTemplate(Blocks.SAND.asItem()));

        hearth("calcite", List.of(
                        new HearthIngredient(Ingredient.of(Blocks.SAND), 4),
                        new HearthIngredient(Ingredient.of(ModItems.QUARTZ_DUST.get()), 1)),
                100, new ItemStackTemplate(Blocks.CALCITE.asItem()));
        hearth("tuff", List.of(
                        new HearthIngredient(Ingredient.of(Items.FLINT), 4),
                        new HearthIngredient(Ingredient.of(ModItems.AMETHYST_DUST.get()), 1)),
                150, new ItemStackTemplate(Blocks.TUFF.asItem()));
        hearth("clay_block", List.of(
                        new HearthIngredient(Ingredient.of(Items.CLAY_BALL), 4),
                        new HearthIngredient(Ingredient.of(ModItems.QUARTZ_DUST.get()), 1)),
                100, new ItemStackTemplate(Blocks.CLAY.asItem()));

        hearth("iron_dust_purification", List.of(
                        new HearthIngredient(Ingredient.of(Items.RAW_IRON), 1),
                        new HearthIngredient(Ingredient.of(ModItems.QUARTZ_DUST.get()), 1)),
                200, new ItemStackTemplate(ModItems.IRON_DUST.get(), 2));
        hearth("copper_dust_purification", List.of(
                        new HearthIngredient(Ingredient.of(Items.RAW_COPPER), 1),
                        new HearthIngredient(Ingredient.of(ModItems.QUARTZ_DUST.get()), 1)),
                150, new ItemStackTemplate(ModItems.COPPER_DUST.get(), 2));
        hearth("gold_dust_purification", List.of(
                        new HearthIngredient(Ingredient.of(Items.RAW_GOLD), 1),
                        new HearthIngredient(Ingredient.of(ModItems.AMETHYST_DUST.get()), 1)),
                300, new ItemStackTemplate(ModItems.GOLD_DUST.get(), 2));

        hearth("attuned_lens_blank", List.of(
                        new HearthIngredient(Ingredient.of(Blocks.GLASS_PANE), 1),
                        new HearthIngredient(Ingredient.of(ModItems.QUARTZ_DUST.get()), 1),
                        new HearthIngredient(Ingredient.of(ModItems.AMETHYST_DUST.get()), 1)),
                250, new ItemStackTemplate(ModItems.ATTUNED_LENS_BLANK.get()));
        hearth("resonant_crystal_seed", List.of(
                        new HearthIngredient(Ingredient.of(Blocks.AMETHYST_BLOCK), 1),
                        new HearthIngredient(Ingredient.of(ModItems.QUARTZ_DUST.get()), 1)),
                400, new ItemStackTemplate(ModItems.RESONANT_CRYSTAL_SEED.get()));

        // Template only - deliberately just one pedestal_synthesis recipe for now, more to
        // follow once the block is confirmed working in-game.
        pedestal("gravel", Ingredient.of(Blocks.COBBLESTONE), 15, 40, new ItemStackTemplate(Blocks.GRAVEL.asItem()));

        shattering("raw_iron", Items.RAW_IRON, ModItems.IRON_DUST.get());
        shattering("raw_copper", Items.RAW_COPPER, ModItems.COPPER_DUST.get());
        shattering("raw_gold", Items.RAW_GOLD, ModItems.GOLD_DUST.get());

        smeltingDust(ModItems.IRON_DUST.get(), Items.IRON_INGOT, "iron_dust");
        smeltingDust(ModItems.COPPER_DUST.get(), Items.COPPER_INGOT, "copper_dust");
        smeltingDust(ModItems.GOLD_DUST.get(), Items.GOLD_INGOT, "gold_dust");

        transmutation(Blocks.STONE, Blocks.IRON_ORE, new EssenceFilter(Optional.of(Essence.METALLUM)),
                EssenceForm.RAW, 40, 100)
                .save(output, Identifier.fromNamespaceAndPath(Geomancy.MODID, "transmutation/stone_to_iron_ore"));
        transmutation(Blocks.STONE, Blocks.IRON_ORE, new EssenceFilter(Optional.of(Essence.METALLUM)),
                EssenceForm.REFINED, 40, 100)
                .save(output, Identifier.fromNamespaceAndPath(Geomancy.MODID, "transmutation/stone_to_iron_ore_refined"));

        transmutationLoot(Blocks.IRON_ORE, Essence.TERRA, EssenceForm.RAW, 20, 80)
                .save(output, Identifier.fromNamespaceAndPath(Geomancy.MODID, "transmutation/iron_ore_excavation"));
        transmutationLoot(Blocks.IRON_ORE, Essence.TERRA, EssenceForm.REFINED, 20, 80)
                .save(output, Identifier.fromNamespaceAndPath(Geomancy.MODID, "transmutation/iron_ore_excavation_refined"));

        transmutationItem(Items.RAW_IRON, Items.IRON_INGOT, Essence.IGNIS, EssenceForm.RAW, 15, 60)
                .save(output, Identifier.fromNamespaceAndPath(Geomancy.MODID, "transmutation/raw_iron_to_iron_ingot"));
        transmutationItem(Items.RAW_IRON, Items.IRON_INGOT, Essence.IGNIS, EssenceForm.REFINED, 15, 60)
                .save(output, Identifier.fromNamespaceAndPath(Geomancy.MODID, "transmutation/raw_iron_to_iron_ingot_refined"));

        transmutation(Blocks.DEEPSLATE, Blocks.DEEPSLATE_GOLD_ORE, new EssenceFilter(Optional.of(Essence.METALLUM)),
                EssenceForm.REFINED, 20, 200)
                .save(output, Identifier.fromNamespaceAndPath(Geomancy.MODID, "transmutation/deepslate_to_deepslate_gold_ore"));
    }

    private TransmutationRecipeBuilder transmutation(Block input, Block result, int cost, int duration) {
        return transmutation(input, result, EssenceFilter.ANY, EssenceForm.RAW, cost, duration);
    }

    private void hearth(String name, List<HearthIngredient> ingredients, int cost, ItemStackTemplate result) {
        output.accept(ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(Geomancy.MODID, "hearth_synthesis/" + name)),
                new HearthSynthesisRecipe(ingredients, true, ResonanceCost.any(cost), 20, result), null);
    }

    private void pedestal(String name, Ingredient input, int cost, int duration, ItemStackTemplate result) {
        output.accept(ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(Geomancy.MODID, "pedestal_synthesis/" + name)),
                new PedestalSynthesisRecipe(input, ResonanceCost.any(cost), duration, result), null);
    }

    private void shattering(String name, ItemLike input, ItemLike result) {
        output.accept(ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(Geomancy.MODID, "lithic_shattering/" + name)),
                new LithicShatteringRecipe(Ingredient.of(input), new ItemStackTemplate(result.asItem(), 2), 50), null);
    }

    private void smeltingDust(ItemLike input, ItemLike result, String name) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, CookingBookCategory.MISC,
                        result, 0.7F, 200)
                .unlockedBy("has_" + name, has(input))
                .save(output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(Geomancy.MODID, name + "_smelting")));
    }

    private TransmutationRecipeBuilder transmutation(Block input, Block result, EssenceFilter essence,
            EssenceForm form, int cost, int duration) {
        return new TransmutationRecipeBuilder(
                new TransmutationSubject.OfBlock(HolderSet.direct(BuiltInRegistries.BLOCK.wrapAsHolder(input))),
                essence, form, cost,
                new TransmutationResult.AsBlock(BuiltInRegistries.BLOCK.wrapAsHolder(result)),
                duration);
    }

    private TransmutationRecipeBuilder transmutationItem(ItemLike input, ItemLike result, int cost, int duration) {
        return transmutationItem(input, result, null, EssenceForm.RAW, cost, duration);
    }

    private TransmutationRecipeBuilder transmutationItem(ItemLike input, ItemLike result, Essence essence,
            EssenceForm form, int cost, int duration) {
        return new TransmutationRecipeBuilder(
                new TransmutationSubject.OfItem(Ingredient.of(input)),
                essence == null ? EssenceFilter.ANY : new EssenceFilter(Optional.of(essence)), form, cost,
                new TransmutationResult.AsItem(new ItemStackTemplate(result.asItem())),
                duration);
    }

    private TransmutationRecipeBuilder transmutationLoot(Block input, Essence essence, EssenceForm form,
            int cost, int duration) {
        return new TransmutationRecipeBuilder(
                new TransmutationSubject.OfBlock(HolderSet.direct(BuiltInRegistries.BLOCK.wrapAsHolder(input))),
                new EssenceFilter(Optional.of(essence)), form, cost,
                new TransmutationResult.AsLoot(), duration);
    }

    private record TransmutationRecipeBuilder(TransmutationSubject input, EssenceFilter essence, EssenceForm form,
                                              int cost, TransmutationResult result, int duration) {
        void save(RecipeOutput output, Identifier id) {
            output.accept(ResourceKey.create(Registries.RECIPE, id),
                    new TransmutationRecipe(input, essence, form, cost, result, duration), null);
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
