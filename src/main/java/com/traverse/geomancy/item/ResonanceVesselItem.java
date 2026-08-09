package com.traverse.geomancy.item;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

import com.traverse.geomancy.block.entity.GeodeJarBlockEntity;
import com.traverse.geomancy.registry.ModDataComponents;

public class ResonanceVesselItem extends Item {
    public static final int CAPACITY = 2_000;
    private static final int CHARGE_BAR_COLOR = 0xA855F7;

    public ResonanceVesselItem(Properties properties) {
        super(properties);
    }

    public static int stored(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STORED_RESONANCE.get(), 0);
    }

    public static void setStored(ItemStack stack, int amount) {
        stack.set(ModDataComponents.STORED_RESONANCE.get(), Math.clamp(amount, 0, CAPACITY));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * stored(stack) / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return CHARGE_BAR_COLOR;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof GeodeJarBlockEntity jar)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack vessel = context.getItemInHand();
        int moved;
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            moved = jar.insertResonance(stored(vessel), false);
            setStored(vessel, stored(vessel) - moved);
        } else {
            int wanted = CAPACITY - stored(vessel);
            moved = jar.extractResonance(wanted, false);
            setStored(vessel, stored(vessel) + moved);
        }
        if (moved <= 0) {
            return InteractionResult.FAIL;
        }
        context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.8F, 1.4F);
        if (context.getPlayer() instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("item.geomancy.resonance_vessel.transferred", moved), true);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("geomancy.tooltip.stored_resonance", stored(stack), CAPACITY));
    }
}
