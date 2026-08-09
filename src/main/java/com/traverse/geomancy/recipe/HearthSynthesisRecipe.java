package com.traverse.geomancy.recipe;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.traverse.geomancy.registry.ModRecipes;

public record HearthSynthesisRecipe(List<HearthIngredient> ingredients, boolean requiresFocus, int resonanceCost, int duration,
                                    ItemStackTemplate result) implements Recipe<HearthSynthesisInput> {
    public static final MapCodec<HearthSynthesisRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HearthIngredient.CODEC.listOf(1, 16).fieldOf("ingredients").forGetter(HearthSynthesisRecipe::ingredients),
            Codec.BOOL.optionalFieldOf("requires_focus", true).forGetter(HearthSynthesisRecipe::requiresFocus),
            ExtraCodecs.POSITIVE_INT.fieldOf("resonance_cost").forGetter(HearthSynthesisRecipe::resonanceCost),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("duration", 20).forGetter(HearthSynthesisRecipe::duration),
            ItemStackTemplate.MAP_CODEC.codec().fieldOf("result").forGetter(HearthSynthesisRecipe::result)
    ).apply(instance, HearthSynthesisRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HearthSynthesisRecipe> STREAM_CODEC = StreamCodec.of(
            (buffer, recipe) -> {
                ByteBufCodecs.VAR_INT.encode(buffer, recipe.ingredients.size());
                recipe.ingredients.forEach(ingredient -> HearthIngredient.STREAM_CODEC.encode(buffer, ingredient));
                ByteBufCodecs.BOOL.encode(buffer, recipe.requiresFocus);
                ByteBufCodecs.VAR_INT.encode(buffer, recipe.resonanceCost);
                ByteBufCodecs.VAR_INT.encode(buffer, recipe.duration);
                ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
            },
            buffer -> {
                int size = ByteBufCodecs.VAR_INT.decode(buffer);
                List<HearthIngredient> ingredients = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    ingredients.add(HearthIngredient.STREAM_CODEC.decode(buffer));
                }
                return new HearthSynthesisRecipe(ingredients, ByteBufCodecs.BOOL.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer),
                        ByteBufCodecs.VAR_INT.decode(buffer), ItemStackTemplate.STREAM_CODEC.decode(buffer));
            });

    @Override
    public boolean matches(HearthSynthesisInput input, Level level) {
        if (requiresFocus && !input.hasFocus() || input.size() != ingredients.size()) {
            return false;
        }
        return match(input, 0, new boolean[input.size()]);
    }

    private boolean match(HearthSynthesisInput input, int ingredientIndex, boolean[] used) {
        if (ingredientIndex == ingredients.size()) {
            return true;
        }
        HearthIngredient required = ingredients.get(ingredientIndex);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!used[i] && stack.getCount() == required.count() && required.ingredient().test(stack)) {
                used[i] = true;
                if (match(input, ingredientIndex + 1, used)) {
                    return true;
                }
                used[i] = false;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(HearthSynthesisInput input) {
        return result.create();
    }

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
    public RecipeSerializer<? extends Recipe<HearthSynthesisInput>> getSerializer() {
        return ModRecipes.HEARTH_SYNTHESIS_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<HearthSynthesisInput>> getType() {
        return ModRecipes.HEARTH_SYNTHESIS_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.HEARTH_SYNTHESIS_CATEGORY.get();
    }
}
