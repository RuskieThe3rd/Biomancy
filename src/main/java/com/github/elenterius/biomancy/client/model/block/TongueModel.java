package com.github.elenterius.biomancy.client.model.block;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.world.block.entity.TongueBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TongueModel extends AnimatedGeoModel<TongueBlockEntity> {

	@Override
	public ResourceLocation getModelLocation(TongueBlockEntity blockEntity) {
		return BiomancyMod.createRL("geo/block/tongue.geo.json");
	}

	@Override
	public ResourceLocation getTextureLocation(TongueBlockEntity blockEntity) {
		return BiomancyMod.createRL("textures/block/tongue.png");
	}

	@Override
	public ResourceLocation getAnimationFileLocation(TongueBlockEntity blockEntity) {
		return BiomancyMod.createRL("animations/block/tongue.animation.json");
	}

}
