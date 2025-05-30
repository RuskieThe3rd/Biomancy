package com.github.elenterius.biomancy.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VariableOutput {

	private final Item item;
	private final @Nullable
	CompoundTag tag;
	private final ItemCountRange countRange;

	public VariableOutput(ItemStack stack) {
		this(stack, stack.getCount());
	}

	public VariableOutput(ItemStack stack, int count) {
		this(stack.getItem(), stack.getTag(), new ItemCountRange.ConstantValue(count));
	}

	public VariableOutput(ItemStack stack, int min, int max) {
		this(stack.getItem(), stack.getTag(), new ItemCountRange.UniformRange(min, max));
	}

	public VariableOutput(ItemStack stack, int n, float p) {
		this(stack.getItem(), stack.getTag(), new ItemCountRange.BinomialRange(n, p));
	}

	public VariableOutput(ItemLike item) {
		this(item, 1);
	}

	public VariableOutput(ItemLike item, int count) {
		this(item, new ItemCountRange.ConstantValue(count));
	}

	public VariableOutput(ItemLike item, int min, int max) {
		this(item, new ItemCountRange.UniformRange(min, max));
	}

	public VariableOutput(ItemLike item, int n, float p) {
		this(item, new ItemCountRange.BinomialRange(n, p));
	}

	public VariableOutput(ItemStack stack, ItemCountRange countRange) {
		this(stack.getItem(), stack.getTag(), countRange);
	}

	public VariableOutput(ItemLike item, ItemCountRange countRange) {
		this(item, null, countRange);
	}

	public VariableOutput(ItemLike item, @Nullable CompoundTag tag, ItemCountRange countRange) {
		this.item = item.asItem();
		this.tag = tag;
		this.countRange = countRange;
	}

	public Item getItem() {
		return item;
	}

	public ItemStack getItemStack() {
		ItemStack stack = new ItemStack(item);
		if (tag != null && !tag.isEmpty()) {
			stack.setTag(tag.copy());
		}
		return stack;
	}

	public ItemStack getItemStack(RandomSource rng) {
		int count = getCount(rng);
		if (count < 1) return ItemStack.EMPTY;

		ItemStack stack = new ItemStack(item);
		if (tag != null && !tag.isEmpty()) {
			stack.setTag(tag.copy());
		}
		stack.setCount(count);

		return stack;
	}

	public int getCount(RandomSource rng) {
		return countRange.getCount(rng);
	}

	public ItemCountRange getCountRange() {return countRange;}

	public JsonObject serialize() {
		JsonObject result = new JsonObject();
		result.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString());

		JsonObject obj = new JsonObject();
		ItemCountRange.toJson(obj, countRange);
		result.add("countRange", obj);

		if (tag != null && !tag.isEmpty()) {
			result.addProperty("nbt", tag.getAsString());
		}

		return result;
	}

	public static VariableOutput deserialize(JsonObject jsonObject) {
		ItemStack stack = ShapedRecipe.itemStackFromJson(jsonObject);
		ItemCountRange countRange = ItemCountRange.fromJson(GsonHelper.getAsJsonObject(jsonObject, "countRange"));
		if (stack.isEmpty()) throw new JsonParseException("Result can't be Empty");
		return new VariableOutput(stack, countRange);
	}

	public static VariableOutput fromNetwork(FriendlyByteBuf buffer) {
		ItemStack stack = buffer.readItem();
		ItemCountRange range = ItemCountRange.fromNetwork(buffer);
		return new VariableOutput(stack, range);
	}

	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeItem(getItemStack());
		ItemCountRange.toNetwork(buffer, countRange);
	}

}
