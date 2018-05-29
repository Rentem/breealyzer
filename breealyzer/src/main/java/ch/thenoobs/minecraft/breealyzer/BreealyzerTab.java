package ch.thenoobs.minecraft.breealyzer;

import ch.thenoobs.minecraft.breealyzer.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class BreealyzerTab extends CreativeTabs {

	public BreealyzerTab() {
//		super("TESTEST");
		super(Breealyzer.MOD_ID);
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(ModItems.ingotCopper);
	} 

}
