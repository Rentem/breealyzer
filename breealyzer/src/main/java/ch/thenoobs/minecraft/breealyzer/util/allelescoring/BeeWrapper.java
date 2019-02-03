package ch.thenoobs.minecraft.breealyzer.util.allelescoring;

import ch.thenoobs.minecraft.breealyzer.util.ItemStackAt;
import forestry.api.apiculture.IBee;

public class BeeWrapper {
	private IBee bee;
	private ItemStackAt itemStackAt;
	
	public BeeWrapper(IBee bee, ItemStackAt itemStackAt) {
		this.bee = bee;
		this.itemStackAt = itemStackAt;
	}
	
	public IBee getBee() {
		return bee;
	}
	public void setBee(IBee bee) {
		this.bee = bee;
	}

	public ItemStackAt getItemStackAt() {
		return itemStackAt;
	}

	public void setItemStackAt(ItemStackAt itemStackAt) {
		this.itemStackAt = itemStackAt;
	}

}
