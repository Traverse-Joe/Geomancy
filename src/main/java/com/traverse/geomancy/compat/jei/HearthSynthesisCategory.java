package com.traverse.geomancy.compat.jei;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

import com.traverse.geomancy.recipe.HearthIngredient;
import com.traverse.geomancy.recipe.HearthSynthesisRecipe;
import com.traverse.geomancy.registry.ModBlocks;
import com.traverse.geomancy.registry.ModRecipes;

public class HearthSynthesisCategory implements IRecipeCategory<RecipeHolder<HearthSynthesisRecipe>> {
    public static final IRecipeType<RecipeHolder<HearthSynthesisRecipe>> RECIPE_TYPE =
            IRecipeType.create(ModRecipes.HEARTH_SYNTHESIS_TYPE.get());

    private static final int SLOT_SPACING = 20;
    private static final int GRID_COLUMNS = 3;
    private static final int GRID_ORIGIN_X = 4;
    private static final int GRID_ORIGIN_Y = 4;
    private static final int OUTPUT_X = 96;
    private static final int OUTPUT_Y = 24;
    private static final int WIDTH = 130;
    private static final int HEIGHT = 94;
    private static final int TEXT_COLOR = 0x404040;

    private final IDrawable icon;

    public HearthSynthesisCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.RESONANT_HEARTH_ITEM.get());
    }

    @Override
    public IRecipeType<RecipeHolder<HearthSynthesisRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.geomancy.resonant_hearth");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<HearthSynthesisRecipe> holder, IFocusGroup focuses) {
        HearthSynthesisRecipe recipe = holder.value();
        List<HearthIngredient> ingredients = recipe.ingredients();
        for (int i = 0; i < ingredients.size(); i++) {
            HearthIngredient hearthIngredient = ingredients.get(i);
            int x = GRID_ORIGIN_X + (i % GRID_COLUMNS) * SLOT_SPACING;
            int y = GRID_ORIGIN_Y + (i / GRID_COLUMNS) * SLOT_SPACING;
            IRecipeSlotBuilder slot = builder.addInputSlot(x, y)
                    .setStandardSlotBackground()
                    .add(hearthIngredient.ingredient());
            if (hearthIngredient.count() > 1) {
                slot.addRichTooltipCallback((slotView, tooltip) ->
                        tooltip.add(Component.translatable("geomancy.jei.ingredient_count", hearthIngredient.count())));
            }
        }

        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y)
                .setOutputSlotBackground()
                .add(recipe.result());
    }

    @Override
    public void draw(RecipeHolder<HearthSynthesisRecipe> holder, IRecipeSlotsView recipeSlotsView,
            GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        HearthSynthesisRecipe recipe = holder.value();
        guiGraphics.text(Minecraft.getInstance().font,
                Component.translatable("geomancy.jei.resonance_cost", recipe.cost().amount()),
                4, 70, TEXT_COLOR);
        if (recipe.requiresFocus()) {
            guiGraphics.text(Minecraft.getInstance().font,
                    Component.translatable("geomancy.jei.requires_focus"),
                    4, 82, TEXT_COLOR);
        }
    }
}
