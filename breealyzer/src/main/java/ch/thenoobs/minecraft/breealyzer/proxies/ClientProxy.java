package ch.thenoobs.minecraft.breealyzer.proxies;

import java.io.File;

import ch.thenoobs.minecraft.breealyzer.Breealyzer;
import ch.thenoobs.minecraft.breealyzer.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@Override
	public void registerItemRenderer(Item item, int meta, String id) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(Breealyzer.MOD_ID + ":" + id, "inventory"));
	}
	
	@Override
	public File getRootFolder() {
		File rootDirectory = Minecraft.getMinecraft().mcDataDir.getAbsoluteFile().getParentFile();
		
		if (!rootDirectory.exists()) {
			rootDirectory.mkdir();
		}

		return rootDirectory;
	}
}
