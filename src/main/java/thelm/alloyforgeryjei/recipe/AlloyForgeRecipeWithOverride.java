package thelm.alloyforgeryjei.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

public record AlloyForgeRecipeWithOverride(AlloyForgeRecipe recipe, int overrideIndex) {

	public static Stream<AlloyForgeRecipeWithOverride> fromRecipe(AlloyForgeRecipe recipe) {
		return IntStream.rangeClosed(0, recipe.getTierOverrides().size()).
				mapToObj(i -> new AlloyForgeRecipeWithOverride(recipe, i));
	}

	public List<List<ItemStack>> getInputs() {
		return recipe.getIngredientsMap().entrySet().stream().
				map(entry -> Arrays.stream(entry.getKey().getItems()).
						map(s -> s.copyWithCount(entry.getValue())).
						toList()).
				toList();
	}

	public ItemStack getOutput() {
		if(overrideIndex > 0) {
			List<ItemStack> overrideOutputs = recipe.getTierOverrides().values().asList();
			if(overrideIndex <= overrideOutputs.size()) {
				return overrideOutputs.get(overrideIndex - 1);
			}
		}
		return recipe.getOutput();
	}

	public Component getTierComponent() {
		Object tierArg = recipe.getMinForgeTier();
		if(overrideIndex > 0) {
			List<AlloyForgeRecipe.OverrideRange> overrideRanges = recipe.getTierOverrides().keySet().asList();
			if(overrideIndex <= overrideRanges.size()) {
				tierArg = overrideRanges.get(overrideIndex - 1);
			}
		}
		return Component.translatable("container.alloy_forgery.rei.min_tier", tierArg);
	}

	public Component getFuelComponent() {
		return Component.translatable("container.alloy_forgery.rei.fuel_per_tick", recipe.getFuelPerTick());
	}
}
