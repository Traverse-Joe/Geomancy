package com.traverse.geomancy.compat.jei;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

import com.traverse.geomancy.resonance.ResonanceCost;
import com.traverse.geomancy.resonance.ResonanceType;
import com.traverse.geomancy.resonance.ResonanceTypes;

final class JeiText {
    private JeiText() {
    }

    // Draws the cost line, tinted and named by the recipe's required type so a typed recipe
    // reads as "500 Infernal Resonance" rather than a bare number an untyped battery
    // would silently fail to satisfy.
    static int drawCost(GuiGraphicsExtractor guiGraphics, ResonanceCost cost, int x, int y, int defaultColor) {
        Optional<Holder<ResonanceType>> type = cost.type();
        Component line = type
                .map(holder -> Component.translatable("geomancy.jei.resonance_cost_typed",
                        cost.amount(), ResonanceTypes.displayName(holder)))
                .orElseGet(() -> Component.translatable("geomancy.jei.resonance_cost", cost.amount()));
        int color = type.map(holder -> holder.value().color()).orElse(defaultColor);
        guiGraphics.text(Minecraft.getInstance().font, line, x, y, color);
        return y + 12;
    }
}
