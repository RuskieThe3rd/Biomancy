package com.github.elenterius.biomancy.crafting.recipe;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class SimpleRecipeType<T extends Recipe<?>> implements RecipeType<T> {

	private final String identifier;

	public SimpleRecipeType(String identifier) {
		this.identifier = identifier;
	}

	public String getId() {
		return identifier;
	}

	@Override
	public String toString() {
		return identifier;
	}

	public static class ItemStackRecipeType<R extends Recipe<Container>> extends SimpleRecipeType<R> {

		public ItemStackRecipeType(String identifier) {
			super(identifier);
		}

		public Optional<R> getRecipeById(Level level, ResourceLocation id) {
			RecipeManager recipeManager = level.getRecipeManager();
			return Optional.ofNullable(castRecipe(recipeManager.byType(this).get(id)));
		}

		public Optional<R> getRecipeFromContainer(Level level, Container inputInventory) {
			RecipeManager recipeManager = level.getRecipeManager();
			return recipeManager.getRecipeFor(this, inputInventory, level);
		}

		private @Nullable R castRecipe(@Nullable Recipe<Container> recipe) {
			//noinspection unchecked
			return (R) recipe;
		}

		private boolean matches(R recipe, ItemStack stack) {
			for (Ingredient ingredient : recipe.getIngredients()) {
				if (ingredient.test(stack)) return true;
			}
			return false;
		}

		public Optional<R> getRecipeForIngredient(Level level, ItemStack stack) {
			RecipeManager recipeManager = level.getRecipeManager();
			return recipeManager.byType(this).values().stream()
					.filter(recipe -> matches(recipe, stack))
					.findFirst().map(this::castRecipe);
		}

		public Optional<Pair<ResourceLocation, R>> getRecipeForIngredient(Level level, ItemStack stack, @Nullable ResourceLocation lastRecipeId) {
			RecipeManager recipeManager = level.getRecipeManager();

			Map<ResourceLocation, R> map = recipeManager.byType(this);
			if (lastRecipeId != null) {
				R recipe = map.get(lastRecipeId);
				if (recipe != null && matches(recipe, stack)) {
					return Optional.of(Pair.of(lastRecipeId, recipe));
				}
			}

			return map.entrySet().stream()
					.filter(entry -> matches(entry.getValue(), stack))
					.findFirst()
					.map(entry -> Pair.of(entry.getKey(), entry.getValue()));
		}
	}

}
