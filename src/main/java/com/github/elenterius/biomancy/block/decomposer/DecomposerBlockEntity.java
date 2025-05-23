package com.github.elenterius.biomancy.block.decomposer;

import com.github.elenterius.biomancy.api.nutrients.FuelHandler;
import com.github.elenterius.biomancy.api.nutrients.FuelHandlerImpl;
import com.github.elenterius.biomancy.block.base.MachineBlock;
import com.github.elenterius.biomancy.block.base.MachineBlockEntity;
import com.github.elenterius.biomancy.client.util.ClientLoopingSoundHelper;
import com.github.elenterius.biomancy.crafting.VariableOutput;
import com.github.elenterius.biomancy.crafting.recipe.DecomposingRecipe;
import com.github.elenterius.biomancy.crafting.recipe.SimpleRecipeType;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.init.ModCapabilities;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.inventory.InventoryHandler;
import com.github.elenterius.biomancy.inventory.InventoryHandlers;
import com.github.elenterius.biomancy.inventory.ItemHandlerUtil;
import com.github.elenterius.biomancy.menu.DecomposerMenu;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.util.ILoopingSoundHelper;
import com.github.elenterius.biomancy.util.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class DecomposerBlockEntity extends MachineBlockEntity<DecomposingRecipe, DecomposerStateData> implements MenuProvider, GeoBlockEntity {

	public static final int FUEL_SLOTS = 1;
	public static final int INPUT_SLOTS = DecomposingRecipe.MAX_INGREDIENTS;
	public static final int OUTPUT_SLOTS = DecomposingRecipe.MAX_OUTPUTS;

	public static final int MAX_FUEL = 1_000;

	public static final RegistryObject<SimpleRecipeType.ItemStackRecipeType<DecomposingRecipe>> RECIPE_TYPE = ModRecipes.DECOMPOSING_RECIPE_TYPE;

	protected static final RawAnimation WORKING_ANIM = RawAnimation.begin().thenLoop("decomposer.working");
	protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("decomposer.idle");

	private final DecomposerStateData stateData;
	private final FuelHandlerImpl fuelHandler;
	private final InventoryHandler<?> fuelInventory;
	private final InventoryHandler<?> inputInventory;
	private final InventoryHandler<?> outputInventory;

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private ILoopingSoundHelper loopingSoundHelper = ILoopingSoundHelper.NULL;

	private @Nullable DecomposerRecipeResult computedRecipeResult;
	private LazyOptional<IFluidHandler> optionalFluidConsumer;

	public DecomposerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.DECOMPOSER.get(), pos, state);

		inputInventory = InventoryHandlers.standard(INPUT_SLOTS, this::onInventoryChanged);
		outputInventory = InventoryHandlers.denyInput(OUTPUT_SLOTS, this::onInventoryChanged);

		fuelInventory = InventoryHandlers.filterFuel(FUEL_SLOTS, this::onInventoryChanged);
		fuelHandler = FuelHandlerImpl.createNutrientFuelHandler(MAX_FUEL, this::onInventoryChanged);

		stateData = new DecomposerStateData(fuelHandler);
		optionalFluidConsumer = LazyOptional.of(fuelHandler::getFluidConsumer);
	}

	@Override
	public void onLoad() {
		if (level != null && level.isClientSide) {
			loopingSoundHelper = new ClientLoopingSoundHelper();
		}
	}

	@Override
	public Component getDisplayName() {
		return getName();
	}

	@Override
	public Component getName() {
		return TextComponentUtil.getTranslationText("container", "decomposer");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return DecomposerMenu.createServerMenu(containerId, playerInventory, this);
	}

	@Override
	public DecomposerStateData getStateData() {
		return stateData;
	}

	@Override
	public InventoryHandler<?> getInputInventory() {
		return inputInventory;
	}

	public InventoryHandler<?> getFuelInventory() {
		return fuelInventory;
	}

	public InventoryHandler<?> getOutputInventory() {
		return outputInventory;
	}

	@Override
	protected FuelHandler getFuelHandler() {
		return fuelHandler;
	}

	@Override
	public ItemStack getStackInFuelSlot() {
		return fuelInventory.getStackInSlot(0);
	}

	@Override
	public void setStackInFuelSlot(ItemStack stack) {
		fuelInventory.setStackInSlot(0, stack);
	}

	@Override
	protected boolean doesRecipeResultFitIntoOutputInv(DecomposingRecipe craftingGoal, ItemStack ignored) {
		DecomposerRecipeResult precomputedResult = getComputedRecipeResult(craftingGoal);
		return ItemHandlerUtil.doAllItemsFit(outputInventory.getRaw(), precomputedResult.items);
	}

	DecomposerRecipeResult getComputedRecipeResult(DecomposingRecipe craftingGoal) {
		if (computedRecipeResult == null || !computedRecipeResult.recipeId.equals(craftingGoal.getId())) {
			return DecomposerRecipeResult.computeRecipeResult(craftingGoal, level.random.nextInt());
		}

		return computedRecipeResult;
	}

	@Override
	protected @Nullable DecomposingRecipe resolveRecipeFromInput(Level level) {
		return RECIPE_TYPE.get().getRecipeFromContainer(level, inputInventory.getRecipeWrapper()).orElse(null);
	}

	@Override
	protected boolean doesRecipeMatchInput(DecomposingRecipe recipeToTest, Level level) {
		return recipeToTest.matches(inputInventory.getRecipeWrapper(), level);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		stateData.serialize(tag);
		if (computedRecipeResult != null) {
			tag.put("ComputedRecipeResult", computedRecipeResult.serialize());
		}
		tag.put("Fuel", fuelHandler.serializeNBT());
		tag.put("FuelSlots", fuelInventory.serializeNBT());
		tag.put("InputSlots", inputInventory.serializeNBT());
		tag.put("OutputSlots", outputInventory.serializeNBT());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		stateData.deserialize(tag);
		if (level != null && tag.contains("ComputedRecipeResult")) {
			computedRecipeResult = DecomposerRecipeResult.deserialize(tag.getCompound("ComputedRecipeResult"), level.getRecipeManager());
		}
		fuelHandler.deserializeNBT(tag.getCompound("Fuel"));
		fuelInventory.deserializeNBT(tag.getCompound("FuelSlots"));
		inputInventory.deserializeNBT(tag.getCompound("InputSlots"));
		outputInventory.deserializeNBT(tag.getCompound("OutputSlots"));
	}

	@Override
	public void dropAllInvContents(Level level, BlockPos pos) {
		ItemHandlerUtil.dropContents(level, pos, fuelInventory);
		ItemHandlerUtil.dropContents(level, pos, inputInventory);
		ItemHandlerUtil.dropContents(level, pos, outputInventory);
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (remove) return super.getCapability(cap, side);

		if (cap == ModCapabilities.ITEM_HANDLER) {
			if (side == null || side == Direction.DOWN) return outputInventory.getLazyOptional().cast();
			if (side == Direction.UP) return inputInventory.getLazyOptional().cast();
			return fuelInventory.getLazyOptional().cast();
		}

		if (cap == ModCapabilities.FLUID_HANDLER) {
			return optionalFluidConsumer.cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		fuelInventory.invalidate();
		inputInventory.invalidate();
		outputInventory.invalidate();
		optionalFluidConsumer.invalidate();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		fuelInventory.revive();
		inputInventory.revive();
		outputInventory.revive();
		optionalFluidConsumer = LazyOptional.of(fuelHandler::getFluidConsumer);
	}

	@Override
	protected boolean craftRecipe(DecomposingRecipe recipeToCraft, Level level) {
		DecomposerRecipeResult precomputedResult = getComputedRecipeResult(recipeToCraft);

		if (!ItemHandlerUtil.doAllItemsFit(outputInventory.getRaw(), precomputedResult.items)) return false;

		inputInventory.extractItem(0, recipeToCraft.getIngredientQuantity().count(), false); //consume input

		for (ItemStack stack : precomputedResult.items) {  //output result
			ItemHandlerUtil.insertItem(outputInventory.getRaw(), stack);
		}

		SoundUtil.broadcastBlockSound((ServerLevel) level, getBlockPos(), ModSoundEvents.DECOMPOSER_CRAFTING_COMPLETED);

		setChanged();
		return true;
	}

	private <T extends DecomposerBlockEntity> void onSoundKeyframe(final SoundKeyframeEvent<T> event) {
		if (event.getKeyframeData().getSound().equals("eat") && level != null && !isRemoved()) {
			SoundUtil.clientPlayBlockSound(level, getBlockPos(), ModSoundEvents.DECOMPOSER_EAT);
		}
	}

	private <T extends DecomposerBlockEntity> PlayState handleAnimationState(final AnimationState<T> event) {
		Boolean isCrafting = event.getAnimatable().getBlockState().getValue(MachineBlock.CRAFTING);

		if (Boolean.TRUE.equals(isCrafting)) {
			event.getController().setAnimation(WORKING_ANIM);
			loopingSoundHelper.startLoop(this, ModSoundEvents.DECOMPOSER_CRAFTING.get(), 0.65f);
		}
		else {
			event.getController().setAnimation(IDLE_ANIM);
			loopingSoundHelper.stopLoop();
		}

		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		AnimationController<DecomposerBlockEntity> controller = new AnimationController<>(this, "controller", 10, this::handleAnimationState);
		controller.setSoundKeyframeHandler(this::onSoundKeyframe);
		controllers.add(controller);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public void setRemoved() {
		if (level != null && level.isClientSide) {
			loopingSoundHelper.clear();
		}
		super.setRemoved();
	}

	record DecomposerRecipeResult(ResourceLocation recipeId, int seed, List<ItemStack> items) {

		@Nullable
		public static DecomposerRecipeResult deserialize(CompoundTag tag, RecipeManager recipeManager) {
			String id = tag.getString("recipeId");
			ResourceLocation recipeId = ResourceLocation.tryParse(id);
			if (recipeId == null) return null;

			Recipe<Container> recipe = recipeManager.byType(RECIPE_TYPE.get()).get(recipeId);
			if (recipe instanceof DecomposingRecipe decomposingRecipe) {
				return computeRecipeResult(decomposingRecipe, tag.getInt("seed"));
			}

			return null;
		}

		public static DecomposerRecipeResult computeRecipeResult(DecomposingRecipe recipe, int seed) {
			RandomSource random = RandomSource.create(seed);

			List<ItemStack> items = new ArrayList<>();
			for (VariableOutput output : recipe.getOutputs()) {
				ItemStack stack = output.getItemStack(random);
				if (!stack.isEmpty()) items.add(stack);
			}

			return new DecomposerRecipeResult(recipe.getId(), seed, items);
		}

		public CompoundTag serialize() {
			CompoundTag tag = new CompoundTag();
			tag.putString("recipeId", recipeId().toString());
			tag.putInt("seed", seed);
			return tag;
		}

	}

}
