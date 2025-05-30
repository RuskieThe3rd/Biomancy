package com.github.elenterius.biomancy.datagen.tags;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import com.github.elenterius.biomancy.block.DirectionalSlabBlock;
import com.github.elenterius.biomancy.block.FleshDoorBlock;
import com.github.elenterius.biomancy.block.FullFleshDoorBlock;
import com.github.elenterius.biomancy.block.membrane.Membrane;
import com.github.elenterius.biomancy.init.ModBlocks;
import com.github.elenterius.biomancy.init.tags.ModBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.*;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.github.elenterius.biomancy.BiomancyMod.MOD_ID;

public class ModBlockTagsProvider extends BlockTagsProvider {

	public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, MOD_ID, existingFileHelper);
	}

	private static TagKey<Block> tagKey(String modId, String path) {
		return BlockTags.create(new ResourceLocation(modId, path));
	}

	@Override
	public String getName() {
		return StringUtils.capitalize(modId) + " " + super.getName();
	}

	protected EnhancedTagAppender<Block> enhancedTag(TagKey<Block> tag) {
		return new EnhancedTagAppender<>(tag(tag), ForgeRegistries.BLOCKS);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		addFleshyBlocksToHoeTag();
		addCreateTags();
		addQuarkTags();

		enhancedTag(ModBlockTags.FLESH_REPLACEABLE)
				.add(Blocks.CLAY).addTag(BlockTags.SAND).addTag(Tags.Blocks.GRAVEL)
				.add(Blocks.ICE, Blocks.FROSTED_ICE)
				.addTag(BlockTags.SNOW)
				.addTag(BlockTags.LEAVES)
				.addTag(BlockTags.OVERWORLD_NATURAL_LOGS)
				.addTag(BlockTags.DIRT)
				.add(Blocks.DIRT_PATH, Blocks.FARMLAND, Blocks.MOSS_BLOCK, Blocks.VINE)
				.add(Blocks.MELON, Blocks.PUMPKIN)
				.add(Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK, Blocks.MUSHROOM_STEM)
				.addTag(BlockTags.FLOWERS)
				.add(
						ACBlockRegistry.BLOCK_OF_CHOCOLATE.get(), ACBlockRegistry.BLOCK_OF_FROSTED_CHOCOLATE.get(), ACBlockRegistry.BLOCK_OF_CHISELED_CHOCOLATE.get(), ACBlockRegistry.BLOCK_OF_POLISHED_CHOCOLATE.get(),
						ACBlockRegistry.CHOCOLATE_ICE_CREAM.get(), ACBlockRegistry.SWEETBERRY_ICE_CREAM.get(), ACBlockRegistry.VANILLA_ICE_CREAM.get(),
						ACBlockRegistry.BLOCK_OF_FROSTING.get(), ACBlockRegistry.BLOCK_OF_CHOCOLATE_FROSTING.get(), ACBlockRegistry.BLOCK_OF_VANILLA_FROSTING.get(),
						ACBlockRegistry.SWEET_PUFF.get(), ACBlockRegistry.CAKE_LAYER.get(), ACBlockRegistry.DOUGH_BLOCK.get(), ACBlockRegistry.COOKIE_BLOCK.get(),
						ACBlockRegistry.WAFER_COOKIE_BLOCK.get(), ACBlockRegistry.WAFER_COOKIE_SLAB.get(), ACBlockRegistry.WAFER_COOKIE_STAIRS.get(), ACBlockRegistry.WAFER_COOKIE_WALL.get(),
						ACBlockRegistry.CANDY_CANE.get(), ACBlockRegistry.CANDY_CANE_BLOCK.get(), ACBlockRegistry.CHISELED_CANDY_CANE_BLOCK.get(), ACBlockRegistry.STRIPPED_CANDY_CANE_BLOCK.get(),
						ACBlockRegistry.CANDY_CANE_POLE.get(), ACBlockRegistry.STRIPPED_CANDY_CANE_POLE.get(),
						ACBlockRegistry.GUMMY_RING_BLUE.get(), ACBlockRegistry.GUMMY_RING_GREEN.get(), ACBlockRegistry.GUMMY_RING_PINK.get(), ACBlockRegistry.GUMMY_RING_RED.get(), ACBlockRegistry.GUMMY_RING_YELLOW.get(),
						ACBlockRegistry.SUNDROP.get(), ACBlockRegistry.SUGAR_GLASS.get(), ACBlockRegistry.SPRINKLES.get(), ACBlockRegistry.LOLLIPOP_BUNCH.get(),
						ACBlockRegistry.FROSTMINT.get(), ACBlockRegistry.SMALL_PEPPERMINT.get(), ACBlockRegistry.LARGE_PEPPERMINT.get(),
						ACBlockRegistry.LICOROOT.get(), ACBlockRegistry.LICOROOT_VINE.get(), ACBlockRegistry.LICOROOT_SPROUT.get(),
						ACBlockRegistry.GINGERBREAD_BLOCK.get(), ACBlockRegistry.GINGERBREAD_STAIRS.get(), ACBlockRegistry.GINGERBREAD_SLAB.get(), ACBlockRegistry.GINGERBREAD_WALL.get(), ACBlockRegistry.GINGERBREAD_DOOR.get(), ACBlockRegistry.GINGERBARREL.get(),
						ACBlockRegistry.FROSTED_GINGERBREAD_BLOCK.get(), ACBlockRegistry.FROSTED_GINGERBREAD_STAIRS.get(), ACBlockRegistry.FROSTED_GINGERBREAD_SLAB.get(), ACBlockRegistry.FROSTED_GINGERBREAD_WALL.get(), ACBlockRegistry.FROSTED_GINGERBREAD_DOOR.get(),
						ACBlockRegistry.GINGERBREAD_BRICKS.get(), ACBlockRegistry.GINGERBREAD_BRICK_STAIRS.get(), ACBlockRegistry.GINGERBREAD_BRICK_SLAB.get(), ACBlockRegistry.GINGERBREAD_BRICK_WALL.get(),
						ACBlockRegistry.FROSTED_GINGERBREAD_BRICKS.get(), ACBlockRegistry.FROSTED_GINGERBREAD_BRICK_STAIRS.get(), ACBlockRegistry.FROSTED_GINGERBREAD_BRICK_SLAB.get(), ACBlockRegistry.FROSTED_GINGERBREAD_BRICK_WALL.get()
				)
				.addTag(ACTagRegistry.ROCK_CANDIES);

		tag(ModBlockTags.ALLOW_VEINS_TO_ATTACH)
				.add(Blocks.DIRT_PATH, Blocks.FARMLAND, Blocks.VINE);

		tag(ModBlockTags.DISALLOW_VEINS_TO_ATTACH).add(
				ModBlocks.PRIMAL_BLOOM.get(),
				ModBlocks.PRIMAL_PERMEABLE_MEMBRANE.get()
		);

		tag(ModBlockTags.ACID_DESTRUCTIBLE)
				.addTag(BlockTags.LEAVES)
				.add(Blocks.MOSS_BLOCK, Blocks.VINE)
				.addTag(BlockTags.FLOWERS);

		tag(ModBlockTags.LAVA_DESTRUCTIBLE).add(
				ModBlocks.MALIGNANT_FLESH_VEINS.get(),
				ModBlocks.MALIGNANT_FLESH_SLAB.get(),
				ModBlocks.MALIGNANT_FLESH_STAIRS.get(),
				ModBlocks.MALIGNANT_FLESH_WALL.get(),
				ModBlocks.PRIMAL_BLOOM.get(),
				ModBlocks.PRIMAL_PERMEABLE_MEMBRANE.get(),
				ModBlocks.PRIMAL_PERMEABLE_MEMBRANE_PANE.get()
		);

		tag(BlockTags.DOORS).add(ModBlocks.FLESH_DOOR.get()).add(ModBlocks.FULL_FLESH_DOOR.get());
		tag(BlockTags.TRAPDOORS).add(ModBlocks.FLESH_IRIS_DOOR.get());

		tag(Tags.Blocks.CHESTS).add(ModBlocks.FLESHKIN_CHEST.get());

		tag(ModBlockTags.FLESHY_FENCES).add(ModBlocks.FLESH_FENCE.get());
		tag(BlockTags.FENCES).addTag(ModBlockTags.FLESHY_FENCES);
		tag(BlockTags.FENCE_GATES).add(ModBlocks.FLESH_FENCE_GATE.get());

		IntrinsicTagAppender<Block> wallsTag = tag(BlockTags.WALLS);
		ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).filter(WallBlock.class::isInstance).forEach(wallsTag::add);

		IntrinsicTagAppender<Block> stairsTag = tag(BlockTags.STAIRS);
		ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).filter(StairBlock.class::isInstance).forEach(stairsTag::add);

		tag(BlockTags.PRESSURE_PLATES).add(ModBlocks.FLESHKIN_PRESSURE_PLATE.get());

		tag(BlockTags.CLIMBABLE).add(ModBlocks.FLESH_LADDER.get());

		IntrinsicTagAppender<Block> slabsTag = tag(BlockTags.SLABS);
		ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).filter(block -> block instanceof DirectionalSlabBlock || block instanceof SlabBlock).forEach(slabsTag::add);

		IntrinsicTagAppender<Block> impermeableTag = tag(BlockTags.IMPERMEABLE);
		ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).filter(Membrane.class::isInstance).forEach(impermeableTag::add);
	}

	private void addFleshyBlocksToHoeTag() {
		IntrinsicTagAppender<Block> tag = tag(BlockTags.MINEABLE_WITH_HOE);
		ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).forEach((block) -> {
			if (block instanceof AbstractCauldronBlock) tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block); //Lazy hack to get around Elen's lazy hack :P
			else tag.add(block);
		});
	}

	/**
	 * <a href="https://github.com/Creators-of-Create/Create/wiki/Useful-Tags">Create wiki: Useful Tags</a>
	 */
	private void addCreateTags() {
		String modId = "create";

		//Blocks which should be able to move on contraptions, but would otherwise be ignored due to their empty collision shape
		//Example: Cobweb
		tag(tagKey(modId, "movable_empty_collider")).add(
				ModBlocks.FLESH_DOOR.get(),
				ModBlocks.FLESH_IRIS_DOOR.get()
		);

		//Blocks which can count toward a functional windmill structure
		//Example: Wool
		IntrinsicTagAppender<Block> windmillSailsTag = tag(tagKey(modId, "windmill_sails"));
		ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).filter(Membrane.class::isInstance).forEach(windmillSailsTag::add);
	}

	/**
	 * <a href="https://github.com/VazkiiMods/Quark/blob/master/src/main/resources/data/quark/tags">Quark Tags</a>
	 */
	private void addQuarkTags() {
		String modId = "quark";

		IntrinsicTagAppender<Block> nonDoubleDoorTag = tag(tagKey(modId, "non_double_door"));
		Predicate<Block> predicate = block -> block instanceof FleshDoorBlock || block instanceof FullFleshDoorBlock;
		ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).filter(predicate).forEach(nonDoubleDoorTag::add);
	}
}
