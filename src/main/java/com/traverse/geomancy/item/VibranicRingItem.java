package com.traverse.geomancy.item;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import com.traverse.geomancy.compat.curios.CuriosCompat;

// Fully passive: the extra-jump behavior lives in VibranicRingEvents, driven by whether the
// player has this item on them, not by any interaction on the item itself.
public class VibranicRingItem extends Item {
    public VibranicRingItem(Properties properties) {
        super(properties);
    }

    // Curios is an optional dependency, so a Curios-slot-only check would make the ring
    // silently inert for anyone without Curios installed. Carrying it works too, mirroring
    // how PortableResonance already accepts Vessels from inventory, offhand, or Curios.
    public static boolean isWorn(Player player) {
        if (CuriosCompat.hasVibranicRing(player)) {
            return true;
        }
        if (player.getOffhandItem().getItem() instanceof VibranicRingItem) {
            return true;
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() instanceof VibranicRingItem) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("item.geomancy.vibranic_ring.tooltip"));
    }
}
