package ch.thenoobs.minecraft.breealyzer.proxies;

import java.io.File;

import ch.thenoobs.minecraft.breealyzer.util.Log;
import net.minecraft.item.Item;

public class CommonProxy {
	public void registerItemRenderer(Item item, int meta, String id) {
	}
	
	public File getRootFolder() {
		File file = new File(".");
		File parentFile = file.getParentFile();
		
		Log.info("Got File {0}", file.getAbsolutePath());
		Log.info("Got Parent File {0}", parentFile.getAbsolutePath());
		
		if (!parentFile.exists()) {
			parentFile.mkdir();
		}
		
		return parentFile;
	}
}
