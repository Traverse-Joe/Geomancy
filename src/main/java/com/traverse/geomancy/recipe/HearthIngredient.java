package com.traverse.geomancy.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;

public record HearthIngredient(Ingredient ingredient, int count) {
    public static final Codec<HearthIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(HearthIngredient::ingredient),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(HearthIngredient::count)
    ).apply(instance, HearthIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HearthIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, HearthIngredient::ingredient,
            ByteBufCodecs.VAR_INT, HearthIngredient::count,
            HearthIngredient::new);
}
