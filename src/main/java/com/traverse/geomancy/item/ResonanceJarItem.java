package com.traverse.geomancy.item;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import com.traverse.geomancy.essence.EssenceCharge;
import com.traverse.geomancy.registry.ModDataComponents;

public class ResonanceJarItem extends BlockItem {
    public ResonanceJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        EssenceCharge charge = stack.getOrDefault(ModDataComponents.STORED_ESSENCE.get(), EssenceCharge.EMPTY);
        if (charge.isEmpty()) {
            tooltip.accept(Component.translatable("geomancy.tooltip.stored_essence.empty"));
            return;
        }
        tooltip.accept(Component.translatable("geomancy.tooltip.stored_essence",
                        charge.amount(), charge.essence().displayName())
                .withColor(charge.essence().color() & 0xFFFFFF));
    }
}
