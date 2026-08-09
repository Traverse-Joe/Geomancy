package com.traverse.geomancy.essence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

// A typed quantity of essence. An empty charge is compatible with anything, so a drained
// buffer can accept the first delivery of any essence/form without needing a separate
// "unset" state.
public record EssenceCharge(Essence essence, EssenceForm form, int amount) {
    public static final EssenceCharge EMPTY = new EssenceCharge(Essence.TERRA, EssenceForm.RAW, 0);

    public static final Codec<EssenceCharge> CODEC = RecordCodecBuilder.create(i -> i.group(
            Essence.CODEC.fieldOf("essence").forGetter(EssenceCharge::essence),
            EssenceForm.CODEC.optionalFieldOf("form", EssenceForm.RAW).forGetter(EssenceCharge::form),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("amount").forGetter(EssenceCharge::amount)
    ).apply(i, EssenceCharge::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EssenceCharge> STREAM_CODEC = StreamCodec.composite(
            Essence.STREAM_CODEC, EssenceCharge::essence,
            EssenceForm.STREAM_CODEC, EssenceCharge::form,
            ByteBufCodecs.VAR_INT, EssenceCharge::amount,
            EssenceCharge::new);

    public boolean isEmpty() {
        return amount <= 0;
    }

    public boolean compatibleWith(Essence otherEssence, EssenceForm otherForm) {
        return isEmpty() || (essence == otherEssence && form == otherForm);
    }

    public EssenceCharge with(int newAmount) {
        return newAmount <= 0 ? EMPTY : new EssenceCharge(essence, form, newAmount);
    }

    public EssenceCharge grow(Essence incomingEssence, EssenceForm incomingForm, int incomingAmount) {
        if (isEmpty()) {
            return new EssenceCharge(incomingEssence, incomingForm, incomingAmount);
        }
        return new EssenceCharge(essence, form, amount + incomingAmount);
    }
}
