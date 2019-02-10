package ch.thenoobs.minecraft.breealyzer.proxies;

import java.io.File;

import net.minecraft.item.Item;

public class CommonProxy {
	public void registerItemRenderer(Item item, int meta, String id) {
	}
	
	public File getRootFolder() {
		return new File(".");
	}
}
