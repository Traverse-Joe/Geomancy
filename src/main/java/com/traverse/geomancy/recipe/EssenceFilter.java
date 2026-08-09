package com.traverse.geomancy.recipe;

import java.util.Locale;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.traverse.geomancy.essence.Essence;

public record EssenceFilter(Optional<Essence> required) {
    public static final EssenceFilter ANY = new EssenceFilter(Optional.empty());

    private static final String ANY_KEY = "any";

    public static final Codec<EssenceFilter> CODEC = Codec.STRING.comapFlatMap(
            EssenceFilter::parse,
            EssenceFilter::serialize);

    public static final StreamCodec<RegistryFriendlyByteBuf, EssenceFilter> STREAM_CODEC = StreamCodec.of(
            (buf, filter) -> ByteBufCodecs.STRING_UTF8.encode(buf, filter.serialize()),
            buf -> parse(ByteBufCodecs.STRING_UTF8.decode(buf)).getOrThrow(IllegalStateException::new));

    private static com.mojang.serialization.DataResult<EssenceFilter> parse(String raw) {
        String key = raw.toLowerCase(Locale.ROOT);
        if (key.equals(ANY_KEY)) {
            return com.mojang.serialization.DataResult.success(ANY);
        }
        return Essence.byName(key)
                .map(essence -> com.mojang.serialization.DataResult.success(new EssenceFilter(Optional.of(essence))))
                .orElseGet(() -> com.mojang.serialization.DataResult.error(() -> "Unknown essence '" + raw + "'"));
    }

    private String serialize() {
        return required.map(Essence::getSerializedName).orElse(ANY_KEY);
    }

    public boolean accepts(Essence essence) {
        return required.isEmpty() || required.get() == essence;
    }
}
