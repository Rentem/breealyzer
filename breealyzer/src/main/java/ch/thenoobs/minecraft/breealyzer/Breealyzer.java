package ch.thenoobs.minecraft.breealyzer;

import java.io.File;

import ch.thenoobs.minecraft.breealyzer.blocks.ModBlocks;
import ch.thenoobs.minecraft.breealyzer.items.ModItems;
import ch.thenoobs.minecraft.breealyzer.proxies.CommonProxy;
import ch.thenoobs.minecraft.breealyzer.util.InventoryFactory;
import ch.thenoobs.minecraft.breealyzer.util.Log;
import ch.thenoobs.minecraft.breealyzer.util.LogToFile;
import ch.thenoobs.minecraft.breealyzer.util.inventory.AnalyzerInventoryHandler;
import ch.thenoobs.minecraft.breealyzer.util.inventory.ApiaryInventoryHandler;
import forestry.apiculture.tiles.TileApiary;
import forestry.core.config.Constants;
import forestry.core.tiles.TileAnalyzer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Breealyzer.MOD_ID, version = Breealyzer.VERSION, dependencies = "required-after:forestry")
public class Breealyzer {
	public static final String MOD_ID = "breealyzer";
	public static final String NAME = "Breealyzer";
	public static final String VERSION = "0.1";
	
	public static final BreealyzerTab BREEALYZER_TAB = new BreealyzerTab();

	@Mod.Instance(MOD_ID)
	public static Breealyzer instance;
	
	private File configFolder;

	@Mod.EventHandler
	public void construction(FMLConstructionEvent event) {

	}
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Log.info("Loading {}", NAME);
		
		configFolder = new File(event.getModConfigurationDirectory(), Constants.MOD_ID);
		LogToFile.info("Test");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		Log.info("Initializing {}", NAME);

		InventoryFactory.registerInventoryHander(TileApiary.class.getName(), ApiaryInventoryHandler.class);
		InventoryFactory.registerInventoryHander(TileAnalyzer.class.getName(), AnalyzerInventoryHandler.class);

		Log.info("InventoryFactory has {} registered handlers.", InventoryFactory.getRegisteredHandlers().size());
	}
	
	public File getConfigFolder()
	{
		return this.configFolder;
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Log.info("Finished loading.");
	}

	@Mod.EventBusSubscriber
	public static class RegistrationHandler {
		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event) {
			ModItems.register(event.getRegistry());
			ModBlocks.registerItemBlocks(event.getRegistry());
		}

		@SubscribeEvent
		public static void registerItems(ModelRegistryEvent event) {
			ModItems.registerModels();
			ModBlocks.registerModels();
		}

		@SubscribeEvent
		public static void registerBlocks(RegistryEvent.Register<Block> event) {
			ModBlocks.register(event.getRegistry());
		}
	}
}
