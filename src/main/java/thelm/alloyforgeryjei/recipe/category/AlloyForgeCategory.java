package thelm.alloyforgeryjei.recipe.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import thelm.alloyforgeryjei.AlloyForgeryJEI;
import thelm.jeidrawables.JEIDrawables;
import thelm.jeidrawables.gui.render.BlankDrawable;
import thelm.jeidrawables.gui.render.ResourceDrawable;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

public class AlloyForgeCategory implements IRecipeCategory<RecipeHolder<AlloyForgeRecipe>> {

	public static final Component TITLE = Component.translatable("container.alloy_forgery.rei.title");
	public static final Component BUTTON = Component.translatable("container.alloy_forgery.rei.button");

	public static final ResourceLocation BACKGROUND = AlloyForgery.id("textures/gui/forge_controller.png");
	public static final ResourceDrawable INPUT_SLOT = new ResourceDrawable(BACKGROUND, 208, 0, 18, 18);
	public static final ResourceDrawable INPUT_SLOTS = new ResourceDrawable(BACKGROUND, 42, 41, 92, 38);
	public static final ResourceDrawable FAUCET = new ResourceDrawable(BACKGROUND, 208, 30, 10, 10);
	public static final ResourceDrawable INGOT = new ResourceDrawable(BACKGROUND, 176, 0, 16, 20);
	public static final ResourceDrawable BUTTON_ACTIVE = new ResourceDrawable(BACKGROUND, 176, 68, 12, 12);
	public static final ResourceDrawable BUTTON_HOVERED = new ResourceDrawable(BACKGROUND, 176, 80, 12, 12);

	public final IDrawable background;

	public AlloyForgeCategory() {
		background = new BlankDrawable(getWidth(), getHeight());
	}

	@Override
	public RecipeType<RecipeHolder<AlloyForgeRecipe>> getRecipeType() {
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
		return 132;
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
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AlloyForgeRecipe> recipeHolder, IFocusGroup focuses) {
		AlloyForgeRecipe recipe = recipeHolder.value();
		List<List<ItemStack>> inputs = recipe.getIngredientsMap().entrySet().stream().
				map(entry -> Arrays.stream(entry.getKey().getItems()).
						map(s -> s.copyWithCount(entry.getValue())).
						toList()).
				toList();
		for(int i = 0; i < inputs.size(); ++i) {
			int x = 2 + i % 5 * 18;
			int y = 22 + i / 5 * 18;
			builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStacks(inputs.get(i)).setBackground(INPUT_SLOT, -1, -1);
		}
		builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 29).addItemStack(recipe.getBaseResult()).setBackground(JEIDrawables.OUTPUT_SLOT, -5, -5);
		builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStacks(recipe.getTierOverrides().values().asList());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<AlloyForgeRecipe> recipeHolder, IFocusGroup focuses) {
		AlloyForgeRecipe recipe = recipeHolder.value();
		builder.addDrawable(FAUCET, 101, 0);
		builder.addDrawable(INGOT, 105, 3);
		if(!recipe.getTierOverrides().isEmpty()) {
			IRecipeSlotDrawable outputSlot = builder.getRecipeSlots().getSlots(RecipeIngredientRole.OUTPUT).get(0);
			IStackHelper stackHelper = AlloyForgeryJEI.jeiHelpers.getStackHelper();
			List<Object> outputUids = new ArrayList<>(recipe.getTierOverrides().size() + 1);
			outputUids.add(stackHelper.getUidForStack(recipe.getBaseResult(), UidContext.Recipe));
			for(ItemStack output : recipe.getTierOverrides().values()) {
				outputUids.add(stackHelper.getUidForStack(output, UidContext.Recipe));
			}
			int index = focuses.getItemStackFocuses(RecipeIngredientRole.OUTPUT).
					map(f -> stackHelper.getUidForStack(f.getTypedValue().getIngredient(), UidContext.Recipe)).
					mapToInt(outputUids::indexOf).filter(i -> i >= 0).min().orElse(0);
			StateHandler stateHandler = new StateHandler(recipe, outputSlot, index);
			builder.addSlottedWidget(stateHandler, List.of(outputSlot));
			builder.addGuiEventListener(new StateButton(new ScreenRectangle(120, 0, 12, 12), stateHandler));
		}
	}

	@Override
	public void draw(RecipeHolder<AlloyForgeRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		AlloyForgeRecipe recipe = recipeHolder.value();
		INPUT_SLOTS.draw(guiGraphics, 0, 20);
		Font font = Minecraft.getInstance().font;
		Component fuelComponent = Component.translatable("container.alloy_forgery.rei.fuel_per_tick", recipe.getFuelPerTick());
		guiGraphics.drawString(font, fuelComponent, 2, 10, 0x404040, false);
		if(!recipe.getTierOverrides().isEmpty()) {
			if(mouseX >= 120 && mouseX < 132 && mouseY >= 0 && mouseY < 12) {
				BUTTON_HOVERED.draw(guiGraphics, 120, 0);
			}
			else {
				BUTTON_ACTIVE.draw(guiGraphics, 120, 0);
			}
		}
		else {
			Component tierComponent = Component.translatable("container.alloy_forgery.rei.min_tier", recipe.getMinForgeTier());
			guiGraphics.drawString(font, tierComponent, 2, 0, 0x404040, false);
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<AlloyForgeRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		AlloyForgeRecipe recipe = recipeHolder.value();
		if(!recipe.getTierOverrides().isEmpty() &&
				mouseX >= 120 && mouseX < 132 && mouseY >= 0 && mouseY < 12) {
			tooltip.add(BUTTON);
		}
	}

	public class StateHandler implements ISlottedRecipeWidget {

		static final ScreenPosition ZERO = new ScreenPosition(0, 0);
		final AlloyForgeRecipe recipe;
		final IRecipeSlotDrawable slot;
		int index = 1;

		public StateHandler(AlloyForgeRecipe recipe, IRecipeSlotDrawable slot, int index) {
			this.recipe = recipe;
			this.slot = slot;
			this.index = index;
			updateSlot();
		}

		@Override
		public ScreenPosition getPosition() {
			return ZERO;
		}

		public void cycle() {
			index = (index + 1) % (recipe.getTierOverrides().size() + 1);
			updateSlot();
		}

		public void updateSlot() {
			slot.clearDisplayOverrides();
			if(index > 0) {
				slot.createDisplayOverrides().addItemStack(recipe.getTierOverrides().values().asList().get(index - 1));
			}
		}

		@Override
		public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			slot.draw(guiGraphics);
			Font font = Minecraft.getInstance().font;
			Component tierComponent = getTierComponent();
			guiGraphics.drawString(font, tierComponent, 2, 0, 0x404040, false);
		}

		public Component getTierComponent() {
			Object tierArg = recipe.getMinForgeTier();
			if(index > 0) {
				tierArg = recipe.getTierOverrides().keySet().asList().get(index - 1);
			}
			return Component.translatable("container.alloy_forgery.rei.min_tier", tierArg);
		}

		@Override
		public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
			return slot.isMouseOver(mouseX, mouseY) ? Optional.of(new RecipeSlotUnderMouse(slot, ZERO)) : Optional.empty();
		}
	}

	public class StateButton implements IJeiGuiEventListener {

		final ScreenRectangle area;
		final StateHandler handler;

		public StateButton(ScreenRectangle area, StateHandler handler) {
			this.area = area;
			this.handler = handler;
		}

		@Override
		public ScreenRectangle getArea() {
			return area;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			handler.cycle();
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			return true;
		}
	}
}
