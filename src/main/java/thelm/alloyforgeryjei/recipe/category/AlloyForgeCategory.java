package thelm.alloyforgeryjei.recipe.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import thelm.alloyforgeryjei.AlloyForgeryJEI;
import thelm.alloyforgeryjei.recipe.AlloyForgeRecipeWithOverride;
import thelm.jeidrawables.JEIDrawables;
import thelm.jeidrawables.gui.render.BlankDrawable;
import thelm.jeidrawables.gui.render.ResourceDrawable;
import wraith.alloyforgery.AlloyForgery;

public class AlloyForgeCategory implements IRecipeCategory<AlloyForgeRecipeWithOverride> {

	public static final Component TITLE = Component.translatable("container.alloy_forgery.rei.title");

	public static final ResourceLocation BACKGROUND = AlloyForgery.id("textures/gui/forge_controller.png");
	public static final ResourceDrawable INPUT_SLOT = new ResourceDrawable(BACKGROUND, 208, 0, 18, 18);
	public static final ResourceDrawable INPUT_SLOTS = new ResourceDrawable(BACKGROUND, 42, 41, 92, 38);
	public static final ResourceDrawable FAUCET = new ResourceDrawable(BACKGROUND, 208, 30, 10, 10);
	public static final ResourceDrawable INGOT = new ResourceDrawable(BACKGROUND, 176, 0, 16, 20);

	public final IDrawable background;

	public AlloyForgeCategory() {
		background = new BlankDrawable(getWidth(), getHeight());
	}

	@Override
	public RecipeType<AlloyForgeRecipeWithOverride> getRecipeType() {
		return AlloyForgeryJEI.ALLOY_FORGE;
	}

	@Override
	public Component getTitle() {
		return TITLE;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public int getWidth() {
		return 124;
	}

	@Override
	public int getHeight() {
		return 58;
	}

	@Override
	public IDrawable getIcon() {
		return null;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, AlloyForgeRecipeWithOverride recipe, IFocusGroup focuses) {
		List<List<ItemStack>> inputs = recipe.getInputs();
		for(int i = 0; i < inputs.size(); ++i) {
			int x = 2 + i % 5 * 18;
			int y = 22 + i / 5 * 18;
			builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStacks(inputs.get(i)).setBackground(INPUT_SLOT, -1, -1);
		}
		builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 29).addItemStack(recipe.getOutput()).setBackground(JEIDrawables.OUTPUT_SLOT, -5, -5);
	}

	@Override
	public void draw(AlloyForgeRecipeWithOverride recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		INPUT_SLOTS.draw(poseStack, 0, 20);
		FAUCET.draw(poseStack, 101, 0);
		INGOT.draw(poseStack, 105, 3);
		Font font = Minecraft.getInstance().font;
		font.draw(poseStack, recipe.getTierComponent(), 2, 0, 0x404040);
		font.draw(poseStack, recipe.getFuelComponent(), 2, 10, 0x404040);
	}

	@Override
	public ResourceLocation getRegistryName(AlloyForgeRecipeWithOverride recipe) {
		return new ResourceLocation("%s/%s".formatted(recipe.recipe().getId(), recipe.overrideIndex()));
	}
}
