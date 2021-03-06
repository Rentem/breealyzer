package ch.thenoobs.minecraft.breealyzer.items;

import ch.thenoobs.minecraft.breealyzer.Breealyzer;
import ch.thenoobs.minecraft.breealyzer.proxies.Proxies;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CoreItem extends Item {

	protected String itemName;

	public CoreItem(String name) {
		setCreativeTab(Breealyzer.BREEALYZER_TAB);
		this.itemName = name;
		setUnlocalizedName(name);
		setRegistryName(name);
	}
	
	public void registerItemModel() {
		Proxies.common.registerItemRenderer(this, 0, itemName);
	}
	
	@Override
	public CoreItem setCreativeTab(CreativeTabs tab) {
		super.setCreativeTab(tab);
		return this;
	}
}