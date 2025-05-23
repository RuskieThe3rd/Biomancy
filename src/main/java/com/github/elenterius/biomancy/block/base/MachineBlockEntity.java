package com.github.elenterius.biomancy.block.base;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.api.nutrients.FuelHandler;
import com.github.elenterius.biomancy.crafting.recipe.ProcessingRecipe;
import com.github.elenterius.biomancy.crafting.state.CraftingState;
import com.github.elenterius.biomancy.crafting.state.RecipeCraftingStateData;
import com.github.elenterius.biomancy.init.ModBlockProperties;
import com.github.elenterius.biomancy.inventory.InventoryHandler;
import com.github.elenterius.biomancy.util.PlayerInteractionPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class MachineBlockEntity<R extends ProcessingRecipe, S extends RecipeCraftingStateData<R>> extends BlockEntity implements Nameable, PlayerInteractionPredicate {

	protected final int tickOffset = BiomancyMod.GLOBAL_RANDOM.nextInt(20);
	protected int ticks = tickOffset;

	protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static <R extends ProcessingRecipe, S extends RecipeCraftingStateData<R>> void serverTick(Level level, BlockPos pos, BlockState state, MachineBlockEntity<R, S> entity) {
		entity.serverTick((ServerLevel) level);
	}

	@Override
	public boolean canPlayerInteract(Player player) {
		if (isRemoved()) return false;
		if (level == null || level.getBlockEntity(worldPosition) != this) return false;
		return player.distanceToSqr(Vec3.atCenterOf(worldPosition)) < 8d * 8d;
	}

	protected void onInventoryChanged() {
		if (level != null && !level.isClientSide) setChanged();
	}

	public int getTicks() {
		return ticks - tickOffset;
	}

	protected abstract S getStateData();

	protected abstract InventoryHandler getInputInventory();

	protected abstract FuelHandler getFuelHandler();

	public int getFuelCost(R recipeToCraft) {
		return getFuelHandler().getFuelCost(recipeToCraft.getCraftingCostNutrients(getInputInventory().getRecipeWrapper()));
	}

	public abstract ItemStack getStackInFuelSlot();

	public abstract void setStackInFuelSlot(ItemStack stack);

	protected abstract boolean doesRecipeResultFitIntoOutputInv(R craftingGoal, ItemStack stackToCraft);

	protected abstract boolean craftRecipe(R recipeToCraft, Level level);

	@Nullable
	protected abstract R resolveRecipeFromInput(Level level);

	protected abstract boolean doesRecipeMatchInput(R recipeToTest, Level level);

	public abstract void dropAllInvContents(Level level, BlockPos pos);

	public boolean hasEnoughFuel(R recipeToCraft) {
		return getFuelHandler().getFuelAmount() >= getFuelCost(recipeToCraft);
	}

	public void refuel() {
		if (getFuelHandler().getFuelAmount() < getFuelHandler().getMaxFuelAmount()) {
			ItemStack stack = getStackInFuelSlot();
			if (getFuelHandler().isValidFuel(stack)) {
				ItemStack remainder = getFuelHandler().addFuel(stack);
				if (remainder.getCount() != stack.getCount()) {
					setStackInFuelSlot(remainder);
					setChanged();
				}
			}
		}
	}

	protected void serverTick(final ServerLevel level) {
		ticks++;

		if (ticks % 8 == 0) {
			refuel();
		}

		S state = getStateData();
		R craftingGoal = state
				.getCraftingGoalRecipe(level) //try to use the current/previous crafting goal first
				.filter(r -> doesRecipeMatchInput(r, level)) //check if it's still matches with the ingredients in the input, if yes use it
				.orElseGet(() -> resolveRecipeFromInput(level)); //else try to find new crafting goal

		boolean emitRedstoneSignal = false;
		if (craftingGoal == null) {
			state.cancelCrafting();
		}
		else {
			ItemStack itemToCraft = getItemToCraft(level, craftingGoal);
			if (itemToCraft.isEmpty()) {
				state.cancelCrafting();
			}
			else {
				if (doesRecipeResultFitIntoOutputInv(craftingGoal, itemToCraft)) {
					if (state.getCraftingState() == CraftingState.NONE) { // nothing is being crafted, try to start crafting
						state.setCraftingGoalRecipe(craftingGoal, getInputInventory().getRecipeWrapper());
						if (hasEnoughFuel(craftingGoal)) {
							state.setCraftingState(CraftingState.IN_PROGRESS);
						}
					}
					else if (!state.isCraftingCanceled()) { // something is being crafted, check that the crafting goals match
						R prevCraftingGoal = state.getCraftingGoalRecipe(level).orElse(null);
						if (prevCraftingGoal == null || !craftingGoal.isRecipeEqual(prevCraftingGoal)) {
							state.cancelCrafting();
						}
					}
				}
				else if (state.getCraftingState() != CraftingState.COMPLETED) {
					state.cancelCrafting();
				}
			}

			//change crafting progress
			if (state.getCraftingState() == CraftingState.IN_PROGRESS) {
				if (hasEnoughFuel(craftingGoal)) state.timeElapsed += 1;
				else state.timeElapsed -= 2;

				if (state.timeElapsed < 0) state.timeElapsed = 0;
			}

			//craft the recipe output
			if (state.getCraftingState() == CraftingState.IN_PROGRESS || state.getCraftingState() == CraftingState.COMPLETED) {
				if (state.timeElapsed >= state.timeForCompletion) {
					state.setCraftingState(CraftingState.COMPLETED);
					if (craftRecipe(craftingGoal, level)) {
						getFuelHandler().addFuelAmount(-getFuelCost(craftingGoal));
						emitRedstoneSignal = true;
						state.setCraftingState(CraftingState.NONE);
					}
				}
			}
		}

		//clean-up states
		if (state.isCraftingCanceled()) {
			state.setCraftingState(CraftingState.NONE);
		}

		updateBlockState(level, state, emitRedstoneSignal);
	}

	private ItemStack getItemToCraft(ServerLevel level, R craftingGoal) {
		if (craftingGoal.isSpecial()) {
			return craftingGoal.assemble(getInputInventory().getRecipeWrapper(), level.registryAccess());
		}
		return craftingGoal.getResultItem(level.registryAccess());
	}

	protected BooleanProperty getIsCraftingBlockStateProperty() {
		return ModBlockProperties.IS_CRAFTING;
	}

	protected void updateBlockState(Level world, S tileState, boolean redstoneSignal) {
		BlockState oldBlockState = world.getBlockState(worldPosition);
		BlockState newBlockState = oldBlockState.setValue(getIsCraftingBlockStateProperty(), tileState.getCraftingState() == CraftingState.IN_PROGRESS);
		if (!newBlockState.equals(oldBlockState)) {
			if (redstoneSignal) {
				if (newBlockState.getBlock() instanceof MachineBlock machine) {
					machine.powerBlock(world, worldPosition, newBlockState);
				}
			}
			else {
				world.setBlock(worldPosition, newBlockState, Block.UPDATE_CLIENTS);
			}
			setChanged();
		}
		else if (redstoneSignal && newBlockState.getBlock() instanceof MachineBlock machine) {
			machine.powerBlock(world, worldPosition, oldBlockState);
		}
	}

}
