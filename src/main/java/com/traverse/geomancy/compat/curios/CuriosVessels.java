package com.traverse.geomancy.compat.curios;

import java.util.List;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import com.traverse.geomancy.item.ResonanceVesselItem;
import com.traverse.geomancy.registry.ModItems;

final class CuriosVessels {
    private CuriosVessels() {
    }

    static void registerCurioItem() {
        CuriosApi.registerCurio(ModItems.RESONANCE_VESSEL.get(), new ICurioItem() {
            @Override
            public ICurio.SoundInfo getEquipSound(SlotContext context, ItemStack stack) {
                return new ICurio.SoundInfo(SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F, 1.4F);
            }
        });
        CuriosApi.registerCurio(ModItems.VIBRANIC_RING.get(), new ICurioItem() {
            @Override
            public ICurio.SoundInfo getEquipSound(SlotContext context, ItemStack stack) {
                return new ICurio.SoundInfo(SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F, 1.6F);
            }
        });
    }

    static boolean hasVibranicRing(Player player) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventoryOrNull(player);
        return handler != null && handler.isEquipped(ModItems.VIBRANIC_RING.get());
    }

    static List<ItemStack> vessels(Player player) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventoryOrNull(player);
        if (handler == null) {
            return List.of();
        }
        return handler.findCurios(stack -> stack.getItem() instanceof ResonanceVesselItem).stream()
                .map(SlotResult::stack)
                .toList();
    }

    // Component edits made in place do not reliably trip Curios' change detection, so
    // the slot is re-set to force a resync to the wearer's client.
    static void markDirty(Player player, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventoryOrNull(player);
        if (handler == null) {
            return;
        }
        for (SlotResult result : handler.findCurios(held -> held == stack)) {
            handler.setEquippedCurio(result.slotContext().identifier(), result.slotContext().index(), stack);
        }
    }
}
