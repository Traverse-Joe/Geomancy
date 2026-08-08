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

    public static void bootstrap() {
    }
}
