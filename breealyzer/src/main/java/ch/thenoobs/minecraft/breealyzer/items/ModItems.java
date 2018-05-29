package ch.thenoobs.minecraft.breealyzer.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems {
	public static CoreItem ingotCopper = new CoreItem("ingot_copper");

	public static void register(IForgeRegistry<Item> registry) {
		registry.registerAll(
				ingotCopper
				);
	}

	public static void registerModels() {
		ingotCopper.registerItemModel();
	}
}
