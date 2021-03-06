package ch.thenoobs.minecraft.breealyzer.blocks;

import ch.thenoobs.minecraft.breealyzer.Breealyzer;
import ch.thenoobs.minecraft.breealyzer.proxies.Proxies;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class CoreBlock extends Block {

	protected String name;

	public CoreBlock(Material material, String name) {
		super(material);
	
		this.name = name;
	
		setUnlocalizedName(name);
		setRegistryName(name);
		setCreativeTab(Breealyzer.BREEALYZER_TAB);
	}
	
	public void registerItemModel(Item itemBlock) {
		Proxies.common.registerItemRenderer(itemBlock, 0, name);
	}
	
	public Item createItemBlock() {
		return new ItemBlock(this).setRegistryName(getRegistryName());
	}
	
	@Override
	public CoreBlock setCreativeTab(CreativeTabs tab) {
		super.setCreativeTab(tab);
		return this;
	}

}
