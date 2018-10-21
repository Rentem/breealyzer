package ch.thenoobs.minecraft.breealyzer.util;

import net.minecraft.item.ItemStack;

public class ItemStackAtSlot {
	ItemStack stack;
	int slot;
	
	public ItemStackAtSlot(ItemStack stack, int slot) {
		this.stack = stack;
		this.slot = slot;
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
}
