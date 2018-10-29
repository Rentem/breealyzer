package ch.thenoobs.minecraft.breealyzer.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {
	public static CoreOre oreCopper = new CoreOre("ore_copper");
	public static AutomaticMoverBlock mover = new AutomaticMoverBlock();
	public static BreealyzerBlock breealyzer = new BreealyzerBlock();

	public static void register(IForgeRegistry<Block> registry) {
		registry.registerAll(
				oreCopper,
				mover,
				breealyzer
		);
		GameRegistry.registerTileEntity(mover.getTileEntityClass(), mover.getRegistryName().toString());
		GameRegistry.registerTileEntity(breealyzer.getTileEntityClass(), breealyzer.getRegistryName().toString());
	}
	
	public static void registerItemBlocks(IForgeRegistry<Item> registry) {
		registry.registerAll(
				oreCopper.createItemBlock(),
				mover.createItemBlock(),
				breealyzer.createItemBlock()
		);
	}
	
	public static void registerModels() {
		oreCopper.registerItemModel(Item.getItemFromBlock(oreCopper));
		mover.registerItemModel(Item.getItemFromBlock(mover));
		breealyzer.registerItemModel(Item.getItemFromBlock(breealyzer));
	}
}
