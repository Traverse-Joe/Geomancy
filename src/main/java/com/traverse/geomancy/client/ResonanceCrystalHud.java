package com.traverse.geomancy.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.resonance.PortableResonance;

@EventBusSubscriber(modid = Geomancy.MODID, value = Dist.CLIENT)
public final class ResonanceCrystalHud {
    private static final Identifier CLUSTER = Identifier.withDefaultNamespace("textures/block/amethyst_cluster.png");
    private static final int ICON = 20;
    private static final int DIM_TINT = 0xFF3A3348;
    private static final int TEXT_COLOR = 0xFFE9D5FF;

    private ResonanceCrystalHud() {
    }

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                Identifier.fromNamespaceAndPath(Geomancy.MODID, "resonance_crystal"),
                ResonanceCrystalHud::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        PortableResonance.Carried carried = PortableResonance.carried(player);
        if (carried.isEmpty()) {
            return;
        }

        float fill = carried.fill();
        int x = graphics.guiWidth() / 2 + 91 + 6;
        int y = graphics.guiHeight() - ICON - 12;

        graphics.blit(RenderPipelines.GUI_TEXTURED, CLUSTER, x, y, 0.0F, 0.0F, ICON, ICON, ICON, ICON, DIM_TINT);
        int glow = Math.round(fill * 255.0F) << 24 | 0x00FFFFFF;
        graphics.blit(RenderPipelines.GUI_TEXTURED, CLUSTER, x, y, 0.0F, 0.0F, ICON, ICON, ICON, ICON, glow);

        String label = String.valueOf(carried.stored());
        graphics.text(minecraft.font, label, x + (ICON - minecraft.font.width(label)) / 2, y + ICON + 1,
                TEXT_COLOR, true);
    }
}
