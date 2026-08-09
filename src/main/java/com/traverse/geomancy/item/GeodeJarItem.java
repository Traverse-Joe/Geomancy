package com.traverse.geomancy.item;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import com.traverse.geomancy.block.entity.GeodeJarBlockEntity;
import com.traverse.geomancy.registry.ModDataComponents;

public class GeodeJarItem extends BlockItem {
    public GeodeJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("geomancy.tooltip.stored_resonance",
                stack.getOrDefault(ModDataComponents.STORED_RESONANCE.get(), 0), GeodeJarBlockEntity.CAPACITY));
    }
}
