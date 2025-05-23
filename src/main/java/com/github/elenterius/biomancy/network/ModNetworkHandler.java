package com.github.elenterius.biomancy.network;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.crafting.recipe.BioForgingRecipe;
import com.github.elenterius.biomancy.util.ItemStackFilterList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModNetworkHandler {

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel SIMPLE_NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(BiomancyMod.createRL("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	private ModNetworkHandler() {}

	public static void sendKeyBindPressToServer(InteractionHand hand, byte flag) {
		EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
		sendKeyBindPressToServer(slot, flag);
	}

	public static void sendKeyBindPressToServer(EquipmentSlot slot, byte flag) {
		SIMPLE_NETWORK_CHANNEL.sendToServer(new KeyPressMessage(slot.getFilterFlag(), flag));
	}

	public static void sendKeyBindPressToServer(int slotIndex, byte flag) {
		SIMPLE_NETWORK_CHANNEL.sendToServer(new KeyPressMessage(slotIndex, flag));
	}

	public static void sendBioForgeRecipeToServer(int containerId, BioForgingRecipe recipe) {
		SIMPLE_NETWORK_CHANNEL.sendToServer(new BioForgeRecipeMessage(containerId, recipe.getId()));
	}

	public static void sendBioLabFilterToClient(ServerPlayer player, int containerId, ItemStackFilterList filters) {
		SIMPLE_NETWORK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new BioLabFilterMessage(containerId, filters));
	}

	public static void register() {
		int id = -1;
		SIMPLE_NETWORK_CHANNEL.registerMessage(++id, KeyPressMessage.class, KeyPressMessage::encode, KeyPressMessage::decode, KeyPressMessage::handle);
		SIMPLE_NETWORK_CHANNEL.registerMessage(++id, BioForgeRecipeMessage.class, BioForgeRecipeMessage::encode, BioForgeRecipeMessage::decode, BioForgeRecipeMessage::handle);
		SIMPLE_NETWORK_CHANNEL.registerMessage(++id, BioLabFilterMessage.class, BioLabFilterMessage::encode, BioLabFilterMessage::decode, BioLabFilterMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

}
