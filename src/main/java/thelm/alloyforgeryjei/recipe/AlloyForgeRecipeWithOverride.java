package thelm.alloyforgeryjei.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

public record AlloyForgeRecipeWithOverride(RecipeHolder<AlloyForgeRecipe> recipeHolder, int overrideIndex) {

	public static Stream<AlloyForgeRecipeWithOverride> fromRecipeHolder(RecipeHolder<AlloyForgeRecipe> recipe) {
		return IntStream.rangeClosed(0, recipe.value().getTierOverrides().size()).
				mapToObj(i -> new AlloyForgeRecipeWithOverride(recipe, i));
	}

	public List<List<ItemStack>> getInputs() {
		return recipeHolder.value().getIngredientsMap().entrySet().stream().
				map(entry -> Arrays.stream(entry.getKey().getItems()).
						map(s -> s.copyWithCount(entry.getValue())).
						toList()).
				toList();
	}

	public ItemStack getOutput() {
		if(overrideIndex > 0) {
			List<ItemStack> overrideOutputs = recipeHolder.value().getTierOverrides().values().asList();
			if(overrideIndex <= overrideOutputs.size()) {
				return overrideOutputs.get(overrideIndex - 1);
			}
		}
		return recipeHolder.value().getBaseResult();
	}

	public Component getTierComponent() {
		Object tierArg = recipeHolder.value().getMinForgeTier();
		if(overrideIndex > 0) {
			List<AlloyForgeRecipe.OverrideRange> overrideRanges = recipeHolder.value().getTierOverrides().keySet().asList();
			if(overrideIndex <= overrideRanges.size()) {
				tierArg = overrideRanges.get(overrideIndex - 1);
			}
		}
		return Component.translatable("container.alloy_forgery.rei.min_tier", tierArg);
	}

	public Component getFuelComponent() {
		return Component.translatable("container.alloy_forgery.rei.fuel_per_tick", recipeHolder.value().getFuelPerTick());
	}
}
