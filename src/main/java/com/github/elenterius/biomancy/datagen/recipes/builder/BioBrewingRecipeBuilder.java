package com.github.elenterius.biomancy.datagen.recipes.builder;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.crafting.recipe.BioBrewingRecipe;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.ConditionalAdvancement;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class BioBrewingRecipeBuilder implements RecipeBuilder<BioBrewingRecipeBuilder> {

	public static final String RECIPE_SUB_FOLDER = ModRecipes.BIO_BREWING_RECIPE_TYPE.getId().getPath();

	private final ResourceLocation recipeId;
	private final List<ICondition> conditions = new ArrayList<>();
	private final ItemData result;
	private final List<IngredientStack> ingredients = new ArrayList<>();
	private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
	private Ingredient reactant = Ingredient.of(ModItems.VIAL.get());
	private int craftingTimeTicks = -1;
	private int craftingCostNutrients = -1;

	private BioBrewingRecipeBuilder(ResourceLocation recipeId, ItemData result) {
		this.recipeId = new ResourceLocation(recipeId.getNamespace(), RECIPE_SUB_FOLDER + "/" + recipeId.getPath());
		this.result = result;
	}

	public static BioBrewingRecipeBuilder create(ResourceLocation recipeId, ItemData result) {
		return new BioBrewingRecipeBuilder(recipeId, result);
	}

	public static BioBrewingRecipeBuilder create(String modId, String outputName, ItemData result) {
		ResourceLocation rl = new ResourceLocation(modId, outputName);
		return new BioBrewingRecipeBuilder(rl, result);
	}

	public static BioBrewingRecipeBuilder create(String outputName, ItemData result) {
		ResourceLocation rl = BiomancyMod.createRL(outputName);
		return new BioBrewingRecipeBuilder(rl, result);
	}

	public static BioBrewingRecipeBuilder create(ItemData result) {
		ResourceLocation rl = BiomancyMod.createRL(result.getItemPath());
		return new BioBrewingRecipeBuilder(rl, result);
	}

	public static BioBrewingRecipeBuilder create(ItemStack stack) {
		return create(new ItemData(stack));
	}

	public static BioBrewingRecipeBuilder create(ItemLike item) {
		return create(new ItemData(item));
	}

	public static BioBrewingRecipeBuilder create(ItemLike item, int count) {
		return create(new ItemData(item, count));
	}

	public BioBrewingRecipeBuilder ifModLoaded(String modId) {
		return withCondition(new ModLoadedCondition(modId));
	}

	public BioBrewingRecipeBuilder ifModMissing(String modId) {
		return withCondition(new NotCondition(new ModLoadedCondition(modId)));
	}

	public BioBrewingRecipeBuilder withCondition(ICondition condition) {
		conditions.add(condition);
		return this;
	}

	public BioBrewingRecipeBuilder setCraftingTime(int timeTicks) {
		if (timeTicks < 0) throw new IllegalArgumentException("Invalid crafting time: " + timeTicks);
		craftingTimeTicks = timeTicks;
		return this;
	}

	public BioBrewingRecipeBuilder setCraftingCost(int costNutrients) {
		if (costNutrients < 0) throw new IllegalArgumentException("Invalid crafting cost: " + costNutrients);
		craftingCostNutrients = costNutrients;
		return this;
	}

	public BioBrewingRecipeBuilder setReactant(ItemLike item) {
		return setReactant(Ingredient.of(item));
	}

	public BioBrewingRecipeBuilder setReactant(TagKey<Item> tag) {
		return setReactant(Ingredient.of(tag));
	}

	public BioBrewingRecipeBuilder setReactant(ItemStack stack) {
		return setReactant(Ingredient.of(stack));
	}

	public BioBrewingRecipeBuilder setReactant(Ingredient ingredient) {
		reactant = ingredient;
		return this;
	}

	public BioBrewingRecipeBuilder addIngredient(TagKey<Item> tag) {
		return addIngredient(Ingredient.of(tag));
	}

	public BioBrewingRecipeBuilder addIngredient(TagKey<Item> tag, int quantity) {
		return addIngredient(Ingredient.of(tag), quantity);
	}

	public BioBrewingRecipeBuilder addIngredient(ItemLike item) {
		return addIngredient(item, 1);
	}

	public BioBrewingRecipeBuilder addIngredient(ItemStack stack) {
		return addIngredient(Ingredient.of(stack), 1);
	}

	public BioBrewingRecipeBuilder addIngredient(Ingredient ingredient) {
		return addIngredient(ingredient, 1);
	}

	public BioBrewingRecipeBuilder addIngredient(ItemLike item, int quantity) {
		addIngredient(Ingredient.of(item), quantity);
		return this;
	}

	public BioBrewingRecipeBuilder addIngredient(Ingredient ingredient, int quantity) {
		ingredients.add(new IngredientStack(ingredient, quantity));
		return this;
	}

	@Override
	public BioBrewingRecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterionTrigger) {
		advancement.addCriterion(name, criterionTrigger);
		return this;
	}

	@Override
	public void save(Consumer<FinishedRecipe> consumer, @Nullable RecipeCategory category) {
		validateCriteria();

		if (craftingTimeTicks < 0) {
			craftingTimeTicks = 4 * 20;
		}

		if (craftingCostNutrients < 0) {
			craftingCostNutrients = RecipeCostUtil.getCost(BioBrewingRecipe.DEFAULT_CRAFTING_COST_NUTRIENTS, craftingTimeTicks);
		}

		advancement.parent(new ResourceLocation("recipes/root"))
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
				.rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);

		String folderName = RecipeBuilder.getRecipeFolderName(category, BiomancyMod.MOD_ID);
		ResourceLocation advancementId = new ResourceLocation(recipeId.getNamespace(), "recipes/%s/%s".formatted(folderName, recipeId.getPath()));

		consumer.accept(new Result(this, advancementId));
	}

	private void validateCriteria() {
		if (advancement.getCriteria().isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe %s because Criteria are empty.".formatted(recipeId));
		}
	}

	public static class Result implements FinishedRecipe {
		private final ResourceLocation id;
		private final List<IngredientStack> ingredients;
		private final Ingredient reactant;
		private final ItemData result;
		private final int craftingTime;
		private final int craftingCost;
		private final List<ICondition> conditions;

		private final Advancement.Builder advancementBuilder;
		private final ResourceLocation advancementId;

		public Result(BioBrewingRecipeBuilder builder, ResourceLocation advancementId) {
			id = builder.recipeId;
			ingredients = builder.ingredients;
			reactant = builder.reactant;
			result = builder.result;
			craftingTime = builder.craftingTimeTicks;
			craftingCost = builder.craftingCostNutrients;
			conditions = builder.conditions;

			advancementBuilder = builder.advancement;
			this.advancementId = advancementId;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			JsonArray jsonArray = new JsonArray();
			for (IngredientStack ingredient : ingredients) {
				jsonArray.add(ingredient.toJson());
			}
			json.add("ingredients", jsonArray);

			if (!reactant.isEmpty()) {
				json.add("reactant", reactant.toJson());
			}

			json.add("result", result.toJson());

			json.addProperty("processingTime", craftingTime);
			json.addProperty("nutrientsCost", craftingCost);

			//serialize conditions
			if (!conditions.isEmpty()) {
				JsonArray array = new JsonArray();
				conditions.forEach(c -> array.add(CraftingHelper.serialize(c)));
				json.add("conditions", array);
			}
		}

		@Override
		public RecipeSerializer<?> getType() {
			return ModRecipes.BIO_BREWING_SERIALIZER.get();
		}

		@Override
		public ResourceLocation getId() {
			return id;
		}

		@Override
		@Nullable
		public JsonObject serializeAdvancement() {
			if (conditions.isEmpty()) return advancementBuilder.serializeToJson();

			ConditionalAdvancement.Builder conditionalBuilder = ConditionalAdvancement.builder();
			conditions.forEach(conditionalBuilder::addCondition);
			conditionalBuilder.addAdvancement(advancementBuilder);
			return conditionalBuilder.write();
		}

		@Override
		@Nullable
		public ResourceLocation getAdvancementId() {
			return advancementId;
		}
	}
}
