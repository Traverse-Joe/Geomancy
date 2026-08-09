package com.traverse.geomancy.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import com.traverse.geomancy.registry.ModRecipes;

public record LithicShatteringRecipe(Ingredient input, ItemStackTemplate result, int resonanceCost)
        implements Recipe<SingleRecipeInput> {
    public static final MapCodec<LithicShatteringRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(LithicShatteringRecipe::input),
            ItemStackTemplate.MAP_CODEC.codec().fieldOf("result").forGetter(LithicShatteringRecipe::result),
            ExtraCodecs.POSITIVE_INT.fieldOf("resonance_cost").forGetter(LithicShatteringRecipe::resonanceCost)
    ).apply(instance, LithicShatteringRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LithicShatteringRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, LithicShatteringRecipe::input,
            ItemStackTemplate.STREAM_CODEC, LithicShatteringRecipe::result,
            ByteBufCodecs.VAR_INT, LithicShatteringRecipe::resonanceCost,
            LithicShatteringRecipe::new);

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }

    @Override public boolean isSpecial() { return true; }
    @Override public boolean showNotification() { return false; }
    @Override public String group() { return ""; }
    @Override public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() { return ModRecipes.LITHIC_SHATTERING_SERIALIZER.get(); }
    @Override public RecipeType<? extends Recipe<SingleRecipeInput>> getType() { return ModRecipes.LITHIC_SHATTERING_TYPE.get(); }
    @Override public RecipeBookCategory recipeBookCategory() { return ModRecipes.LITHIC_SHATTERING_CATEGORY.get(); }
}
