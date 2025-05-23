package com.github.elenterius.biomancy.entity.mob.ai.goal;

import com.github.elenterius.biomancy.block.cradle.PrimordialCradleBlock;
import com.github.elenterius.biomancy.block.cradle.PrimordialCradleBlockEntity;
import com.github.elenterius.biomancy.entity.mob.PrimordialCradleUser;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.util.LevelUtil;
import com.github.elenterius.biomancy.util.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class UsePrimordialCradleGoal<T extends PathfinderMob & PrimordialCradleUser> extends MoveToBlockGoal {

	public static final int SEARCH_RANGE = 32;

	private final T cradleUser;
	private boolean wantsToUseCradle;
	private boolean canUseCradle;
	private final double maxUseDistance;

	public UsePrimordialCradleGoal(T mob) {
		this(mob, 1.5d);
	}

	public UsePrimordialCradleGoal(T mob, double maxUseDistance) {
		super(mob, 1f, SEARCH_RANGE, SEARCH_RANGE);
		cradleUser = mob;
		this.maxUseDistance = maxUseDistance;
	}

	@Override
	public boolean canUse() {
		if (nextStartTick <= 0) {
			canUseCradle = false;
			wantsToUseCradle = cradleUser.hasTributeForCradle();
		}
		return super.canUse();
	}

	@Override
	public boolean canContinueToUse() {
		return canUseCradle && super.canContinueToUse();
	}

	@Override
	public void tick() {
		super.tick();

		if (!isReachedTarget()) return;

		cradleUser.getLookControl().setLookAt(blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, 20f, cradleUser.getMaxHeadXRot());

		if (canUseCradle) {
			Level level = cradleUser.level();
			BlockPos pos = blockPos;
			BlockState state = level.getBlockState(pos);

			if (state.getBlock() instanceof PrimordialCradleBlock && level.getBlockEntity(pos) instanceof PrimordialCradleBlockEntity cradle) {
				if (sacrificeItem(level, pos, cradle, cradleUser.getTributeItemForCradle()) && !cradle.isFull()) {
					sacrificeItem(level, pos, cradle, new ItemStack(ModItems.CREATOR_MIX.get(), 5)); //force the cradle to become full
				}
			}
		}

		canUseCradle = false;
		nextStartTick = 10;
	}

	@Override
	public double acceptedDistance() {
		return maxUseDistance;
	}

	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof PrimordialCradleBlock && wantsToUseCradle && !canUseCradle && level.getBlockEntity(pos) instanceof PrimordialCradleBlockEntity cradle && !cradle.isFull()) {
			canUseCradle = true;
			return true;
		}
		return false;
	}

	@Override
	protected boolean findNearestBlock() {
		PrimordialCradleBlockEntity nearestBlockEntity = LevelUtil.findNearestBlockEntity((ServerLevel) mob.level(), mob.blockPosition(), SEARCH_RANGE, PrimordialCradleBlockEntity.class);
		if (nearestBlockEntity == null) return false;

		BlockPos nearestPos = nearestBlockEntity.getBlockPos();
		if (mob.isWithinRestriction(nearestPos) && isValidTarget(mob.level(), nearestPos)) {
			this.blockPos = nearestPos;
			return true;
		}

		return false;
	}

	private boolean sacrificeItem(Level level, BlockPos pos, PrimordialCradleBlockEntity cradle, ItemStack stack) {
		if (!cradle.isFull() && cradle.insertItem(stack)) {
			SoundEvent soundEvent = cradle.isFull() ? ModSoundEvents.CRADLE_BECAME_FULL.get() : ModSoundEvents.CRADLE_EAT.get();
			SoundUtil.broadcastBlockSound((ServerLevel) level, pos, soundEvent);
			return true;
		}
		return false;
	}


}
