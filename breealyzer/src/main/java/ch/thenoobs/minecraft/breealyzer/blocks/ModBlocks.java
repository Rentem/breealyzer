package ch.thenoobs.minecraft.breealyzer.blocks;

import ch.thenoobs.minecraft.breealyzer.blocks.tileentities.AutomaticMoverBlock;
import ch.thenoobs.minecraft.breealyzer.blocks.tileentities.AutomaticMoverTE;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {
	public static CoreOre oreCopper = new CoreOre("ore_copper");
	public static AutomaticMoverBlock mover = new AutomaticMoverBlock();

	public static void register(IForgeRegistry<Block> registry) {
		registry.registerAll(
				oreCopper,
				mover
		);
		GameRegistry.registerTileEntity(mover.getTileEntityClass(), mover.getRegistryName().toString());
	}
	
	public static void registerItemBlocks(IForgeRegistry<Item> registry) {
		registry.registerAll(
				oreCopper.createItemBlock(),
				mover.createItemBlock()
		);
	}
	
	public static void registerModels() {
		oreCopper.registerItemModel(Item.getItemFromBlock(oreCopper));
		mover.registerItemModel(Item.getItemFromBlock(mover));
	}
}
