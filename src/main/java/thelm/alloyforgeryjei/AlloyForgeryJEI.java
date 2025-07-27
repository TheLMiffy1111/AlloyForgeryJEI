package thelm.alloyforgeryjei;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import thelm.alloyforgeryjei.recipe.AlloyForgeRecipeWithOverride;
import thelm.alloyforgeryjei.recipe.category.AlloyForgeCategory;
import wraith.alloyforgery.AlloyForgeScreenHandler;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.block.ForgeControllerBlock;
import wraith.alloyforgery.client.AlloyForgeScreen;
import wraith.alloyforgery.forges.ForgeDefinition;
import wraith.alloyforgery.forges.ForgeRegistry;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

public class AlloyForgeryJEI implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation("alloyforgeryjei:alloy_forgery");
	public static final Logger LOGGER = LogManager.getLogger();

	public static IJeiHelpers jeiHelpers;
	public static IJeiRuntime jeiRuntime;

	public static final RecipeType<AlloyForgeRecipeWithOverride> ALLOY_FORGE = new RecipeType<>(AlloyForgery.id("alloy_forge"), AlloyForgeRecipeWithOverride.class);

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		jeiHelpers = registration.getJeiHelpers();

		if(checkDisabled()) {
			return;
		}

		registration.addRecipeCategories(new AlloyForgeCategory());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if(checkDisabled()) {
			return;
		}

		RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
		registration.addRecipes(ALLOY_FORGE, recipeManager.getAllRecipesFor(AlloyForgeRecipe.Type.INSTANCE).stream().
				flatMap(AlloyForgeRecipeWithOverride::fromRecipe).toList());
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		if(checkDisabled()) {
			return;
		}

		registration.addRecipeTransferHandler(AlloyForgeScreenHandler.class, ALLOY_FORGE, 2, 10, 12, 36);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		if(checkDisabled()) {
			return;
		}

		ForgeRegistry.controllerBlocksView().stream().
		map(ForgeControllerBlock.class::cast).
		sorted(this::compareForgeControllers).
		map(ItemStack::new).
		forEach(stack->registration.addRecipeCatalyst(stack, ALLOY_FORGE));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if(checkDisabled()) {
			return;
		}

		registration.addRecipeClickArea(AlloyForgeScreen.class, 142, 20, 21, 24, ALLOY_FORGE);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		AlloyForgeryJEI.jeiRuntime = jeiRuntime;
	}

	public int compareForgeControllers(ForgeControllerBlock a, ForgeControllerBlock b) {
		ForgeDefinition ad = a.forgeDefinition;
		ForgeDefinition bd = b.forgeDefinition;
		int res = Integer.compare(ad.forgeTier(), bd.forgeTier());
		if(res != 0) {
			return res;
		}
		res = Float.compare(ad.speedMultiplier(), bd.speedMultiplier());
		if(res != 0) {
			return res;
		}
		res = Integer.compare(ad.fuelCapacity(), bd.fuelCapacity());
		if(res != 0) {
			return res;
		}
		res = Integer.compare(ad.maxSmeltTime(), bd.maxSmeltTime());
		if(res != 0) {
			return res;
		}
		return Registry.BLOCK.getKey(a).compareTo(Registry.BLOCK.getKey(b));
	}

	public boolean checkDisabled() {
		if(FabricLoader.getInstance().isModLoaded("rei_plugin_compatibilities")) {
			LOGGER.warn("AlloyForgeryJEI is disabled with REIPC as Alloy Forgery has native REI support");
			return true;
		}
		return false;
	}
}
