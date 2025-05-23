package com.github.elenterius.biomancy.api.tribute;

import com.github.elenterius.biomancy.api.tribute.fluid.FluidTributeConsumerHandler;
import com.github.elenterius.biomancy.inventory.Notify;
import com.github.elenterius.biomancy.util.SaturatedMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Internal
public class SacrificeHandler implements INBTSerializable<CompoundTag> {

	private static final int MAX_BIOMASS_VALUE = 100;
	private static final int REQUIRED_LIFE_ENERGY = 100;

	private byte biomass;
	private int lifeEnergy;
	private int successValue;
	private int diseaseValue;
	private int hostileValue;
	private int anomalyValue;
	private boolean hasModifiers;

	private boolean isDirty = false;

	private final FluidTributeConsumerHandler fluidConsumer;

	public SacrificeHandler(Notify onFluidConsumerChange) {
		reset();
		fluidConsumer = new FluidTributeConsumerHandler(this, onFluidConsumerChange);
	}

	public void reset() {
		biomass = 0;
		lifeEnergy = 0;

		successValue = 0;

		diseaseValue = 0;
		hostileValue = 85;
		anomalyValue = 7;

		hasModifiers = false;
		isDirty = false;
	}

	public boolean isFull() {
		return lifeEnergy >= REQUIRED_LIFE_ENERGY && biomass >= getMaxBiomass();
	}

	public boolean isDirty() {
		return isDirty;
	}

	public boolean isBiomassFull() {
		return biomass >= getMaxBiomass();
	}

	public int getMaxBiomass() {
		return MAX_BIOMASS_VALUE;
	}

	public void setBiomass(int amount) {
		biomass = (byte) Mth.clamp(amount, 0, getMaxBiomass());
		markDirty();
	}

	public boolean addBiomass(int amount) {
		if (amount == 0) return false;

		if (amount < 0 && biomass > 0) {
			setBiomass(biomass + amount);
			return true;
		}

		if (amount > 0 && biomass < getMaxBiomass()) {
			setBiomass(biomass + amount);
			return true;
		}

		return false;
	}

	public int getBiomassAmount() {
		return biomass;
	}

	public float getBiomassPct() {
		return biomass / 100f;
	}

	public void setLifeEnergy(int amount) {
		lifeEnergy = Mth.clamp(amount, 0, Integer.MAX_VALUE);
		markDirty();
	}

	public boolean addLifeEnergy(int amount) {
		if (amount == 0) return false;

		if (amount < 0 && lifeEnergy > 0) {
			lifeEnergy = SaturatedMath.addAndClampToPositiveInteger(lifeEnergy, amount);
			markDirty();
			return true;
		}

		if (amount > 0) {
			lifeEnergy = SaturatedMath.addAndClampToPositiveInteger(lifeEnergy, amount);
			markDirty();
			return true;
		}

		return false;
	}

	public void addSuccess(int amount) {
		if (amount == 0) return;

		successValue = SaturatedMath.add(successValue, amount);
		markDirty();
	}

	public void addHostile(int amount) {
		if (amount == 0) return;

		hostileValue = SaturatedMath.add(hostileValue, amount);
		markDirty();
	}

	public void addAnomaly(int amount) {
		if (amount == 0) return;

		anomalyValue = SaturatedMath.add(anomalyValue, amount);
		markDirty();
	}

	public void addDisease(int amount) {
		if (amount == 0) return;

		diseaseValue = SaturatedMath.add(diseaseValue, amount);
		markDirty();
	}

	public int getLifeEnergyAmount() {
		return lifeEnergy;
	}

	public float getLifeEnergyPct() {
		return lifeEnergy / 100f;
	}

	public float getSuccessChance() {
		return successValue / 100f;
	}

	public float getHostileChance() {
		return hostileValue / 100f;
	}

	public float getAnomalyChance() {
		return anomalyValue / 100f;
	}

	public float getTumorFactor() {
		return diseaseValue / 100f;
	}

	public boolean hasModifiers() {
		return hasModifiers;
	}

	public boolean addItem(ItemStack stack, Consumer<Tribute> onSuccess) {
		if (isFull()) return false;
		if (stack.isEmpty()) return false;

		Tribute tribute = Tributes.getTribute(stack);
		int count = addTribute(tribute, stack.getCount());
		if (count > 0) {
			stack.shrink(count);
			onSuccess.accept(tribute);
			return true;
		}

		return false;
	}

	public boolean addItem(ItemStack stack) {
		if (isFull()) return false;
		if (stack.isEmpty()) return false;

		int count = addTribute(Tributes.getTribute(stack), stack.getCount());
		if (count > 0) {
			stack.shrink(count);
			return true;
		}

		return false;
	}

	private int addTribute(Tribute tribute, int maxCount) {
		int n = maxCount;
		while (n > 0 && addTribute(tribute)) n--;
		return maxCount - n;
	}

	public boolean addTribute(Tribute tribute) {
		if (isFull()) return false;
		if (tribute.isEmpty()) return false;

		boolean addedBiomass = addBiomass(tribute.biomass());
		boolean addedLifeEnergy = addLifeEnergy(tribute.lifeEnergy());

		boolean consumeTribute = addedBiomass || addedLifeEnergy;
		boolean isModifier = tribute.biomass() == 0 && tribute.lifeEnergy() == 0;

		if (consumeTribute || isModifier) {
			addSuccess(tribute.successModifier());
			addDisease(tribute.diseaseModifier());
			addHostile(tribute.hostileModifier());
			addAnomaly(tribute.anomalyModifier());
			return true;
		}

		return false;
	}

	protected void markDirty() {
		hasModifiers = diseaseValue != 0 || hostileValue != 0 || anomalyValue != 0;
		isDirty = true;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putByte("Biomass", biomass);
		tag.putInt("LifeEnergy", lifeEnergy);

		tag.putInt("Success", successValue);

		tag.putInt("Disease", diseaseValue);
		tag.putInt("Hostile", hostileValue);
		tag.putInt("Anomaly", anomalyValue);

		tag.putBoolean("HasModifiers", hasModifiers);

		tag.put("FluidConsumer", fluidConsumer.serializeNBT());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		biomass = tag.getByte("Biomass");
		lifeEnergy = tag.getInt("LifeEnergy");

		successValue = tag.getInt("Success");

		diseaseValue = tag.getInt("Disease");
		hostileValue = tag.getInt("Hostile");
		anomalyValue = tag.getInt("Anomaly");

		hasModifiers = tag.getBoolean("HasModifiers");

		fluidConsumer.deserializeNBT(tag.getCompound("FluidConsumer"));
	}

	public IFluidHandler getFluidConsumer() {
		return fluidConsumer;
	}

	public void setDirty(boolean flag) {
		isDirty = flag;
	}

}
