package com.traverse.geomancy.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.recipe.LithicShatteringRecipe;
import com.traverse.geomancy.registry.ModRecipes;
import com.traverse.geomancy.resonance.PortableResonance;

@EventBusSubscriber(modid = Geomancy.MODID)
public final class LithicMiningEvents {
    private static final int BUDDING_PRESERVATION_COST = 200;

    private LithicMiningEvents() {
    }

    @SubscribeEvent
    static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player) || player.isShiftKeyDown()
                || !(event.getTool().getItem() instanceof LithicPickaxeItem)) {
            return;
        }
        if (event.getState().is(Blocks.BUDDING_AMETHYST)) {
            if (PortableResonance.consume(player, BUDDING_PRESERVATION_COST)) {
                event.getDrops().add(new ItemEntity(event.getLevel(), event.getPos().getX() + 0.5,
                        event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5,
                        new ItemStack(Blocks.BUDDING_AMETHYST)));
            }
            return;
        }

        List<Conversion> conversions = new ArrayList<>();
        int totalCost = 0;
        for (ItemEntity drop : event.getDrops()) {
            ItemStack raw = drop.getItem();
            RecipeHolder<LithicShatteringRecipe> holder = event.getLevel().recipeAccess()
                    .getRecipeFor(ModRecipes.LITHIC_SHATTERING_TYPE.get(), new SingleRecipeInput(raw), event.getLevel())
                    .orElse(null);
            if (holder != null) {
                totalCost += holder.value().resonanceCost() * raw.getCount();
                conversions.add(new Conversion(drop, holder.value()));
            }
        }
        if (conversions.isEmpty() || !PortableResonance.consume(player, totalCost)) {
            return;
        }
        for (Conversion conversion : conversions) {
            ItemStack result = conversion.recipe.result().create();
            result.setCount(result.getCount() * conversion.drop.getItem().getCount());
            conversion.drop.setItem(result);
        }
    }

    private record Conversion(ItemEntity drop, LithicShatteringRecipe recipe) {
    }
}
