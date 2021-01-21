package com.github.elenterius.blightlings.init;

import com.github.elenterius.blightlings.BlightlingsMod;
import com.github.elenterius.blightlings.recipe.DecomposingRecipe;
import com.github.elenterius.blightlings.recipe.MasonBeetleRecipe;
import com.github.elenterius.blightlings.recipe.PotionBeetleRecipe;
import com.github.elenterius.blightlings.recipe.SewingKitRepairRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModRecipes {
	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, BlightlingsMod.MOD_ID);

	public static final RegistryObject<SpecialRecipeSerializer<PotionBeetleRecipe>> CRAFTING_SPECIAL_POTION_BEETLE = RECIPE_SERIALIZERS.register("crafting_special_potion_beetle", () -> new SpecialRecipeSerializer<>(PotionBeetleRecipe::new));
	public static final RegistryObject<SpecialRecipeSerializer<MasonBeetleRecipe>> CRAFTING_SPECIAL_MASON_BEETLE = RECIPE_SERIALIZERS.register("crafting_special_mason_beetle", () -> new SpecialRecipeSerializer<>(MasonBeetleRecipe::new));
	public static final RegistryObject<SpecialRecipeSerializer<SewingKitRepairRecipe>> REPAIR_SPECIAL_SEWING_KIT = RECIPE_SERIALIZERS.register("repair_special_sewing_kit", () -> new SpecialRecipeSerializer<>(SewingKitRepairRecipe::new));

	public static final RegistryObject<IRecipeSerializer<DecomposingRecipe>> DECOMPOSING_SERIALIZER = RECIPE_SERIALIZERS.register("decomposing", DecomposingRecipe.Serializer::new);

	public static final IRecipeType<DecomposingRecipe> DECOMPOSING_RECIPE_TYPE = createRecipeType("decomposing");

	public static final ItemPredicate ANY_MEATLESS_FOOD_ITEM_PREDICATE = new ItemPredicate() {
		@Override
		public boolean test(ItemStack stack) {
			Item item = stack.getItem();
			return item.isFood() && item.getFood() != null && !item.getFood().isMeat();
		}

		@Override
		public JsonElement serialize() {
			return JsonNull.INSTANCE;
		}
	};

	private ModRecipes() {}

	public static void registerCustomItemPredicates() {
		ItemPredicate.register(BlightlingsMod.createRL("any_meatless_food"), jsonObject -> ANY_MEATLESS_FOOD_ITEM_PREDICATE);
	}

	public static void registerRecipeTypes() {
		Registry.register(Registry.RECIPE_TYPE, BlightlingsMod.createRL("decomposing"), DECOMPOSING_RECIPE_TYPE);
	}

	private static <T extends IRecipe<?>> IRecipeType<T> createRecipeType(String name) {
		return new IRecipeType<T>() {
			public String toString() {
				return name;
			}
		};
	}

}
