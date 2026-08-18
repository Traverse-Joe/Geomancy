package com.traverse.geomancy.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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

import com.traverse.geomancy.recipe.PedestalSynthesisRecipe;
import com.traverse.geomancy.registry.ModBlocks;
import com.traverse.geomancy.registry.ModRecipes;

public class PedestalSynthesisCategory implements IRecipeCategory<RecipeHolder<PedestalSynthesisRecipe>> {
    public static final IRecipeType<RecipeHolder<PedestalSynthesisRecipe>> RECIPE_TYPE =
            IRecipeType.create(ModRecipes.PEDESTAL_SYNTHESIS_TYPE.get());

    private static final int INPUT_X = 4;
    private static final int INPUT_Y = 24;
    private static final int OUTPUT_X = 60;
    private static final int OUTPUT_Y = 24;
    private static final int WIDTH = 90;
    private static final int HEIGHT = 54;
    private static final int TEXT_COLOR = 0x404040;

    private final IDrawable icon;

    public PedestalSynthesisCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.RESONANCE_PEDESTAL_ITEM.get());
    }

    @Override
    public IRecipeType<RecipeHolder<PedestalSynthesisRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.geomancy.resonance_pedestal");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<PedestalSynthesisRecipe> holder, IFocusGroup focuses) {
        PedestalSynthesisRecipe recipe = holder.value();
        builder.addInputSlot(INPUT_X, INPUT_Y).setStandardSlotBackground().add(recipe.input());
        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).setOutputSlotBackground().add(recipe.result());
    }

    @Override
    public void draw(RecipeHolder<PedestalSynthesisRecipe> holder, IRecipeSlotsView recipeSlotsView,
            GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        PedestalSynthesisRecipe recipe = holder.value();
        JeiText.drawCost(guiGraphics, recipe.cost(), 4, 40, TEXT_COLOR);
    }
}
