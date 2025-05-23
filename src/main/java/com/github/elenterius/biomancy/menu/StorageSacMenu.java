package com.github.elenterius.biomancy.menu;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.storagesac.StorageSacBlockEntity;
import com.github.elenterius.biomancy.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

public class StorageSacMenu extends PlayerContainerMenu {

	private final StorageSacBlockEntity storageSac;

	protected StorageSacMenu(int id, Inventory playerInventory, @Nullable StorageSacBlockEntity storageSac) {
		super(ModMenuTypes.STORAGE_SAC.get(), id, playerInventory, 88, 146);

		this.storageSac = storageSac;

		if (storageSac != null) {
			IItemHandlerModifiable itemHandler = storageSac.getInventory();

			int posX = 44;
			int posY = 17;
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 5; x++) {
					addSlot(new SlotItemHandler(itemHandler, x + 5 * y, posX + x * 18, posY + y * 18));
				}
			}
		}
	}

	public static StorageSacMenu createServerMenu(int screenId, Inventory playerInventory, StorageSacBlockEntity storageSac) {
		return new StorageSacMenu(screenId, playerInventory, storageSac);
	}

	public static StorageSacMenu createClientMenu(int screenId, Inventory playerInventory, FriendlyByteBuf extraData) {
		StorageSacBlockEntity storageSac = playerInventory.player.level().getBlockEntity(extraData.readBlockPos()) instanceof StorageSacBlockEntity be ? be : null;
		return new StorageSacMenu(screenId, playerInventory, storageSac);
	}

	@Override
	public boolean stillValid(Player player) {
		return storageSac != null && storageSac.canPlayerInteract(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stackInSlot = slot.getItem();
		ItemStack copyOfStack = stackInSlot.copy();

		boolean successfulTransfer = switch (SlotZone.getZoneFromIndex(index)) {
			case INVENTORY -> mergeIntoEither(SlotZone.PLAYER_HOTBAR, SlotZone.PLAYER_MAIN_INVENTORY, stackInSlot, true);
			case PLAYER_HOTBAR -> mergeIntoEither(SlotZone.INVENTORY, SlotZone.PLAYER_MAIN_INVENTORY, stackInSlot, false);
			case PLAYER_MAIN_INVENTORY -> mergeIntoEither(SlotZone.INVENTORY, SlotZone.PLAYER_HOTBAR, stackInSlot, false);
		};

		if (!successfulTransfer) return ItemStack.EMPTY;

		if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
		else slot.setChanged();

		if (stackInSlot.getCount() == copyOfStack.getCount()) {
			BiomancyMod.LOGGER.warn(MarkerManager.getMarker("GlandMenu"), "Stack transfer failed in an unexpected way!");
			return ItemStack.EMPTY; //transfer error
		}

		slot.onTake(player, stackInSlot);
		return copyOfStack;
	}

	public enum SlotZone implements ISlotZone {
		PLAYER_HOTBAR(0, 9),
		PLAYER_MAIN_INVENTORY(PLAYER_HOTBAR, 3 * 9),
		INVENTORY(PLAYER_MAIN_INVENTORY, StorageSacBlockEntity.SLOTS);

		public final int firstIndex;
		public final int slotCount;
		public final int lastIndexPlus1;

		SlotZone(SlotZone preSlotZone, int numberOfSlots) {
			this(preSlotZone.lastIndexPlus1, numberOfSlots);
		}

		SlotZone(int firstIndex, int numberOfSlots) {
			this.firstIndex = firstIndex;
			slotCount = numberOfSlots;
			lastIndexPlus1 = firstIndex + numberOfSlots;
		}

		public static SlotZone getZoneFromIndex(int slotIndex) {
			for (SlotZone slotZone : SlotZone.values()) {
				if (slotIndex >= slotZone.firstIndex && slotIndex < slotZone.lastIndexPlus1) return slotZone;
			}
			throw new IndexOutOfBoundsException("Unexpected slotIndex");
		}

		@Override
		public int getFirstIndex() {
			return firstIndex;
		}

		@Override
		public int getLastIndexPlusOne() {
			return lastIndexPlus1;
		}

		@Override
		public int getSlotCount() {
			return slotCount;
		}

	}

}
