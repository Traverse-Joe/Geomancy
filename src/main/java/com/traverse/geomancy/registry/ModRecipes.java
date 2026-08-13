package com.traverse.geomancy.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.recipe.TransmutationRecipe;
import com.traverse.geomancy.recipe.HearthSynthesisRecipe;
import com.traverse.geomancy.recipe.LithicShatteringRecipe;
import com.traverse.geomancy.recipe.PedestalSynthesisRecipe;

public final class ModRecipes {
    private ModRecipes() {
    }

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Geomancy.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Geomancy.MODID);
    public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES =
            DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, Geomancy.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<TransmutationRecipe>> TRANSMUTATION_TYPE =
            RECIPE_TYPES.register("transmutation", id -> RecipeType.simple(id));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TransmutationRecipe>> TRANSMUTATION_SERIALIZER =
            RECIPE_SERIALIZERS.register("transmutation",
                    () -> new RecipeSerializer<>(TransmutationRecipe.MAP_CODEC, TransmutationRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> TRANSMUTATION_CATEGORY =
            RECIPE_BOOK_CATEGORIES.register("transmutation", () -> new RecipeBookCategory());

    public static final DeferredHolder<RecipeType<?>, RecipeType<HearthSynthesisRecipe>> HEARTH_SYNTHESIS_TYPE =
            RECIPE_TYPES.register("hearth_synthesis", RecipeType::simple);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<HearthSynthesisRecipe>> HEARTH_SYNTHESIS_SERIALIZER =
            RECIPE_SERIALIZERS.register("hearth_synthesis",
                    () -> new RecipeSerializer<>(HearthSynthesisRecipe.MAP_CODEC, HearthSynthesisRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> HEARTH_SYNTHESIS_CATEGORY =
            RECIPE_BOOK_CATEGORIES.register("hearth_synthesis", RecipeBookCategory::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<LithicShatteringRecipe>> LITHIC_SHATTERING_TYPE =
            RECIPE_TYPES.register("lithic_shattering", RecipeType::simple);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<LithicShatteringRecipe>> LITHIC_SHATTERING_SERIALIZER =
            RECIPE_SERIALIZERS.register("lithic_shattering",
                    () -> new RecipeSerializer<>(LithicShatteringRecipe.MAP_CODEC, LithicShatteringRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> LITHIC_SHATTERING_CATEGORY =
            RECIPE_BOOK_CATEGORIES.register("lithic_shattering", RecipeBookCategory::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<PedestalSynthesisRecipe>> PEDESTAL_SYNTHESIS_TYPE =
            RECIPE_TYPES.register("pedestal_synthesis", RecipeType::simple);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PedestalSynthesisRecipe>> PEDESTAL_SYNTHESIS_SERIALIZER =
            RECIPE_SERIALIZERS.register("pedestal_synthesis",
                    () -> new RecipeSerializer<>(PedestalSynthesisRecipe.MAP_CODEC, PedestalSynthesisRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> PEDESTAL_SYNTHESIS_CATEGORY =
            RECIPE_BOOK_CATEGORIES.register("pedestal_synthesis", RecipeBookCategory::new);

    public static void bootstrap() {
    }
}
