package com.traverse.geomancy.resonance;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.essence.Essence;
import com.traverse.geomancy.registry.ModRegistries;

// Resolution helpers for the datapack type registry. Storage keeps a ResourceKey rather
// than a Holder so it can be saved without registry ops and survives a datapack dropping
// the type - an unresolvable key simply behaves as untyped.
public final class ResonanceTypes {
    public static final int UNTYPED_COLOR = 0xFF804CFF;

    private ResonanceTypes() {
    }

    // Node essences and resonance types share their serialized names, so a node's essence
    // names its type directly. This is the seam the Essence enum is eventually deleted at.
    public static ResourceKey<ResonanceType> of(Essence essence) {
        return ResourceKey.create(ModRegistries.RESONANCE_TYPE,
                Identifier.fromNamespaceAndPath(Geomancy.MODID, essence.getSerializedName()));
    }

    public static Optional<Holder<ResonanceType>> resolve(@Nullable LevelReader level,
            @Nullable ResourceKey<ResonanceType> key) {
        if (level == null || key == null) {
            return Optional.empty();
        }
        return level.registryAccess()
                .lookup(ModRegistries.RESONANCE_TYPE)
                .flatMap(registry -> registry.get(key))
                .map(holder -> (Holder<ResonanceType>) holder);
    }

    public static Optional<Holder<ResonanceType>> resolve(HolderLookup.Provider registries,
            @Nullable ResourceKey<ResonanceType> key) {
        if (key == null) {
            return Optional.empty();
        }
        return registries.lookup(ModRegistries.RESONANCE_TYPE)
                .flatMap(registry -> registry.get(key))
                .map(holder -> (Holder<ResonanceType>) holder);
    }

    public static int color(@Nullable LevelReader level, @Nullable ResourceKey<ResonanceType> key) {
        return resolve(level, key).map(holder -> 0xFF000000 | holder.value().color()).orElse(UNTYPED_COLOR);
    }

    public static float pitch(@Nullable LevelReader level, @Nullable ResourceKey<ResonanceType> key) {
        return resolve(level, key).map(holder -> holder.value().pitch()).orElse(1.0F);
    }

    public static Component displayName(@Nullable ResourceKey<ResonanceType> key) {
        if (key == null) {
            return Component.translatable("geomancy.resonance_type.untyped");
        }
        Identifier id = key.identifier();
        return Component.translatable("geomancy.resonance_type." + id.getNamespace() + "." + id.getPath());
    }

    // The single place a type is turned into readable text, so the item tooltip, the Jade
    // line, and anything later all tint from the same registry colour the crystal renders.
    public static Component coloredName(HolderLookup.@Nullable Provider registries,
            @Nullable ResourceKey<ResonanceType> key) {
        if (key == null) {
            return Component.translatable("geomancy.resonance_type.untyped").withStyle(ChatFormatting.GRAY);
        }
        int color = registries == null ? UNTYPED_COLOR : resolve(registries, key)
                .map(holder -> 0xFF000000 | holder.value().color())
                .orElse(UNTYPED_COLOR);
        return displayName(key).copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color & 0x00FFFFFF)));
    }

    public static Component displayName(Holder<ResonanceType> holder) {
        return holder.unwrapKey().map(ResonanceTypes::displayName)
                .orElseGet(() -> Component.translatable("geomancy.resonance_type.untyped"));
    }
}
