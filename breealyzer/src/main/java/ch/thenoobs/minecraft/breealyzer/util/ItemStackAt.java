package ch.thenoobs.minecraft.breealyzer.util;

import net.minecraft.item.ItemStack;

public class ItemStackAt {
	private ItemStack stack;
	private int slot;
	private InventoryHandler inventory;
	
	public ItemStackAt(ItemStack stack, int slot, InventoryHandler inventory) {
		this.stack = stack;
		this.slot = slot;
		this.inventory = inventory;
	}
	
	public ItemStack getStack() {
		return stack;
	}
	
	public void setStack(ItemStack stack) {
		this.stack = stack;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}

	public InventoryHandler getInventory() {
		return inventory;
	}

	public void setInventory(InventoryHandler inventory) {
		this.inventory = inventory;
	}	
}
