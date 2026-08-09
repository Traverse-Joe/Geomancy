package com.traverse.geomancy.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.traverse.geomancy.essence.EssenceForm;
import com.traverse.geomancy.registry.ModRecipes;

public record TransmutationRecipe(TransmutationSubject input, EssenceFilter essence, EssenceForm form, int cost,
                                   TransmutationResult result, int duration) implements Recipe<TransmutationInput> {

    public static final MapCodec<TransmutationRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            TransmutationSubject.CODEC.fieldOf("input").forGetter(TransmutationRecipe::input),
            EssenceFilter.CODEC.optionalFieldOf("essence", EssenceFilter.ANY).forGetter(TransmutationRecipe::essence),
            EssenceForm.CODEC.optionalFieldOf("form", EssenceForm.RAW).forGetter(TransmutationRecipe::form),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("cost", 0).forGetter(TransmutationRecipe::cost),
            TransmutationResult.CODEC.fieldOf("result").forGetter(TransmutationRecipe::result),
            ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(TransmutationRecipe::duration)
    ).apply(i, TransmutationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransmutationRecipe> STREAM_CODEC = StreamCodec.composite(
            TransmutationSubject.STREAM_CODEC, TransmutationRecipe::input,
            EssenceFilter.STREAM_CODEC, TransmutationRecipe::essence,
            EssenceForm.STREAM_CODEC, TransmutationRecipe::form,
            ByteBufCodecs.VAR_INT, TransmutationRecipe::cost,
            TransmutationResult.STREAM_CODEC, TransmutationRecipe::result,
            ByteBufCodecs.VAR_INT, TransmutationRecipe::duration,
            TransmutationRecipe::new);

    @Override
    public boolean matches(TransmutationInput input, Level level) {
        return this.input.matches(input.target()) && essence.accepts(input.essence()) && form == input.form();
    }

    @Override
    public ItemStack assemble(TransmutationInput input) {
        return result instanceof TransmutationResult.AsItem(var template) ? template.create() : ItemStack.EMPTY;
    }

    // Keeps RecipeManager from warning that a recipe with no item ingredients
    // "can't be placed"; there is nothing here for the recipe book to place.
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<? extends Recipe<TransmutationInput>> getSerializer() {
        return ModRecipes.TRANSMUTATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<TransmutationInput>> getType() {
        return ModRecipes.TRANSMUTATION_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.TRANSMUTATION_CATEGORY.get();
    }
}
