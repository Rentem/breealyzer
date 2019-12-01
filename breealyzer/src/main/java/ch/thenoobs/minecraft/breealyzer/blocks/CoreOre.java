package ch.thenoobs.minecraft.breealyzer.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class CoreOre extends CoreBlock {

	public CoreOre(String name) {
		super(Material.ROCK, name);

		setHardness(3f);
		setResistance(5f);
	}

	@Override
	public CoreOre setCreativeTab(CreativeTabs tab) {
		super.setCreativeTab(tab);
		return this;
	}
}
