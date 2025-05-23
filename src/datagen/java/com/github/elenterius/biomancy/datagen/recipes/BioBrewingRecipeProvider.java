package com.github.elenterius.biomancy.datagen.recipes;

import com.github.elenterius.biomancy.datagen.recipes.builder.BioBrewingRecipeBuilder;
import com.github.elenterius.biomancy.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class BioBrewingRecipeProvider extends RecipeProvider {

	protected BioBrewingRecipeProvider(PackOutput output) {
		super(output);
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		buildCompoundRecipes(consumer);
		buildAdditiveRecipes(consumer);
		buildSerumRecipes(consumer);
	}

	private void buildCompoundRecipes(Consumer<FinishedRecipe> consumer) {
		BioBrewingRecipeBuilder.create(ModItems.ORGANIC_COMPOUND.get())
				.addIngredient(ModItems.BILE.get())
				.addIngredient(ModItems.BILE.get())
				.addIngredient(ModItems.ORGANIC_MATTER.get())
				.addIngredient(ModItems.NUTRIENTS.get())
				.setCraftingTime(2 * 20)
				.unlockedBy(ModItems.BILE.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.EXOTIC_COMPOUND.get())
				.addIngredient(ModItems.EXOTIC_DUST.get())
				.addIngredient(ModItems.EXOTIC_DUST.get())
				.addIngredient(ModItems.MINERAL_FRAGMENT.get())
				.setReactant(ModItems.ORGANIC_COMPOUND.get())
				.setCraftingTime(4 * 20)
				.unlockedBy(ModItems.ORGANIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.GENETIC_COMPOUND.get())
				.addIngredient(ModItems.HORMONE_SECRETION.get())
				.addIngredient(ModItems.NUTRIENT_PASTE.get())
				.setReactant(ModItems.ORGANIC_COMPOUND.get())
				.setCraftingTime(4 * 20)
				.unlockedBy(ModItems.ORGANIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.UNSTABLE_COMPOUND.get())
				.addIngredient(ModItems.VOLATILE_FLUID.get())
				.setReactant(ModItems.ORGANIC_COMPOUND.get())
				.setCraftingTime(4 * 20)
				.unlockedBy(ModItems.ORGANIC_COMPOUND.get()).save(consumer);
	}

	private void buildAdditiveRecipes(Consumer<FinishedRecipe> consumer) {
		BioBrewingRecipeBuilder.create(ModItems.HEALING_ADDITIVE.get())
				.addIngredient(ModItems.REGENERATIVE_FLUID.get())
				.addIngredient(ModItems.REGENERATIVE_FLUID.get())
				.setReactant(ModItems.ORGANIC_COMPOUND.get())
				.setCraftingTime(2 * 20)
				.unlockedBy(ModItems.ORGANIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.DECAYING_ADDITIVE.get())
				.addIngredient(ModItems.WITHERING_OOZE.get())
				.addIngredient(ModItems.WITHERING_OOZE.get())
				.setReactant(ModItems.ORGANIC_COMPOUND.get())
				.setCraftingTime(2 * 20)
				.unlockedBy(ModItems.ORGANIC_COMPOUND.get()).save(consumer);
	}

	private void buildSerumRecipes(Consumer<FinishedRecipe> consumer) {
		BioBrewingRecipeBuilder.create(ModItems.INSOMNIA_CURE.get())
				.addIngredient(Items.SUGAR)
				.addIngredient(ModItems.BILE.get())
				.setReactant(ModItems.EXOTIC_COMPOUND.get())
				.setCraftingTime(8 * 20)
				.unlockedBy(ModItems.EXOTIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.ABSORPTION_BOOST.get())
				.addIngredient(ModItems.HEALING_ADDITIVE.get())
				.addIngredient(ModItems.EXOTIC_DUST.get())
				.addIngredient(ModItems.MINERAL_FRAGMENT.get())
				.setReactant(ModItems.EXOTIC_COMPOUND.get())
				.setCraftingTime(8 * 20)
				.unlockedBy(ModItems.EXOTIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.CLEANSING_SERUM.get())
				.addIngredient(ModItems.DECAYING_ADDITIVE.get())
				.addIngredient(ModItems.HEALING_ADDITIVE.get())
				.setReactant(ModItems.EXOTIC_COMPOUND.get())
				.setCraftingTime(8 * 20)
				.unlockedBy(ModItems.EXOTIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.FRENZY_SERUM.get())
				.addIngredient(ModItems.VOLATILE_FLUID.get())
				.addIngredient(ModItems.HORMONE_SECRETION.get())
				.setReactant(ModItems.EXOTIC_COMPOUND.get())
				.setCraftingTime(8 * 20)
				.unlockedBy(ModItems.EXOTIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.BREEDING_STIMULANT.get())
				.addIngredient(ModItems.NUTRIENT_PASTE.get())
				.addIngredient(ItemTags.FLOWERS)
				.addIngredient(ModItems.HORMONE_SECRETION.get())
				.addIngredient(Items.COCOA_BEANS)
				.setReactant(ModItems.GENETIC_COMPOUND.get())
				.setCraftingTime(6 * 20)
				.unlockedBy(ModItems.GENETIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.REJUVENATION_SERUM.get())
				.addIngredient(ModItems.HEALING_ADDITIVE.get())
				.addIngredient(ModItems.NUTRIENT_PASTE.get())
				.addIngredient(ModItems.DECAYING_ADDITIVE.get())
				.setReactant(ModItems.GENETIC_COMPOUND.get())
				.setCraftingTime(8 * 20)
				.unlockedBy(ModItems.GENETIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.AGEING_SERUM.get())
				.addIngredient(ModItems.NUTRIENT_PASTE.get())
				.addIngredient(ModItems.MINERAL_FRAGMENT.get())
				.addIngredient(ModItems.DECAYING_ADDITIVE.get())
				.setReactant(ModItems.GENETIC_COMPOUND.get())
				.setCraftingTime(6 * 20)
				.unlockedBy(ModItems.GENETIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.ENLARGEMENT_SERUM.get())
				.addIngredient(ModItems.NUTRIENT_PASTE.get())
				.addIngredient(ModItems.HORMONE_SECRETION.get())
				.addIngredient(ModItems.HEALING_ADDITIVE.get())
				.addIngredient(ModItems.MINERAL_FRAGMENT.get())
				.setReactant(ModItems.GENETIC_COMPOUND.get())
				.setCraftingTime(6 * 20)
				.unlockedBy(ModItems.GENETIC_COMPOUND.get()).save(consumer);

		BioBrewingRecipeBuilder.create(ModItems.SHRINKING_SERUM.get())
				.addIngredient(ModItems.EXOTIC_DUST.get())
				.addIngredient(ModItems.HEALING_ADDITIVE.get())
				.addIngredient(ModItems.DECAYING_ADDITIVE.get())
				.addIngredient(ModItems.DECAYING_ADDITIVE.get())
				.setReactant(ModItems.GENETIC_COMPOUND.get())
				.setCraftingTime(8 * 20)
				.unlockedBy(ModItems.GENETIC_COMPOUND.get()).save(consumer);
	}

}
