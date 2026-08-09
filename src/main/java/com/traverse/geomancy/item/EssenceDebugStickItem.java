package com.traverse.geomancy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;

public class EssenceDebugStickItem extends Item {
    public EssenceDebugStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TectonicNodeBlockEntity node)) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            var essence = context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
                    ? node.clearDebugEssence(context.getLevel())
                    : node.cycleDebugEssence(context.getLevel());
            if (context.getPlayer() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("item.geomancy.essence_debug_stick.changed",
                        essence.displayName()), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
