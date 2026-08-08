package com.traverse.geomancy.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.block.Block;

import com.traverse.geomancy.registry.ModRecipes;

public record TransmutationRecipe(HolderSet<Block> input, EssenceFilter essence, Holder<Block> result, int duration)
        implements Recipe<TransmutationInput> {

    public static final MapCodec<TransmutationRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("input").forGetter(TransmutationRecipe::input),
            EssenceFilter.CODEC.optionalFieldOf("essence", EssenceFilter.ANY).forGetter(TransmutationRecipe::essence),
            BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("result").forGetter(TransmutationRecipe::result),
            ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(TransmutationRecipe::duration)
    ).apply(i, TransmutationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransmutationRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.BLOCK), TransmutationRecipe::input,
            EssenceFilter.STREAM_CODEC, TransmutationRecipe::essence,
            ByteBufCodecs.holderRegistry(Registries.BLOCK), TransmutationRecipe::result,
            ByteBufCodecs.VAR_INT, TransmutationRecipe::duration,
            TransmutationRecipe::new);

    @Override
    public boolean matches(TransmutationInput input, Level level) {
        return this.input.contains(BuiltInRegistries.BLOCK.wrapAsHolder(input.state().getBlock()))
                && essence.accepts(input.essence());
    }

    @Override
    public ItemStack assemble(TransmutationInput input) {
        return ItemStack.EMPTY;
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
