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
import com.traverse.geomancy.resonance.ResonanceCost;

// One item + a resonance cost = an output, driven automatically by the Resonance Pedestal's
// tick loop rather than a Tuning-Fork strike. Same shape as LithicShatteringRecipe, but embeds
// ResonanceCost per the project's convention for recipes that spend resonance.
public record PedestalSynthesisRecipe(Ingredient input, ResonanceCost cost, int duration, ItemStackTemplate result)
        implements Recipe<SingleRecipeInput> {
    public static final MapCodec<PedestalSynthesisRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(PedestalSynthesisRecipe::input),
            ResonanceCost.CODEC.fieldOf("resonance_cost").forGetter(PedestalSynthesisRecipe::cost),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("duration", 40).forGetter(PedestalSynthesisRecipe::duration),
            ItemStackTemplate.MAP_CODEC.codec().fieldOf("result").forGetter(PedestalSynthesisRecipe::result)
    ).apply(instance, PedestalSynthesisRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PedestalSynthesisRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, PedestalSynthesisRecipe::input,
            ResonanceCost.STREAM_CODEC, PedestalSynthesisRecipe::cost,
            ByteBufCodecs.VAR_INT, PedestalSynthesisRecipe::duration,
            ItemStackTemplate.STREAM_CODEC, PedestalSynthesisRecipe::result,
            PedestalSynthesisRecipe::new);

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
    @Override public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() { return ModRecipes.PEDESTAL_SYNTHESIS_SERIALIZER.get(); }
    @Override public RecipeType<? extends Recipe<SingleRecipeInput>> getType() { return ModRecipes.PEDESTAL_SYNTHESIS_TYPE.get(); }
    @Override public RecipeBookCategory recipeBookCategory() { return ModRecipes.PEDESTAL_SYNTHESIS_CATEGORY.get(); }
}
