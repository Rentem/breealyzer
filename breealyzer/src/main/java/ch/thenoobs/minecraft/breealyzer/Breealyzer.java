package ch.thenoobs.minecraft.breealyzer;

import ch.thenoobs.minecraft.breealyzer.blocks.ModBlocks;
import ch.thenoobs.minecraft.breealyzer.items.ModItems;
import ch.thenoobs.minecraft.breealyzer.proxies.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Breealyzer.MOD_ID, version = Breealyzer.VERSION)
public class Breealyzer
{
    public static final String MOD_ID = "breealyzer";
    public static final String NAME = "Breealyzer";
    public static final String VERSION = "0.1";
        
    
    @SidedProxy(serverSide = "ch.thenoobs.minecraft.breealyzer.proxies.CommonProxy", clientSide = "ch.thenoobs.minecraft.breealyzer.proxies.ClientProxy")
    public static CommonProxy proxy;
    

    public static final BreealyzerTab BREEALYZER_TAB = new BreealyzerTab();
    
    @Mod.Instance(MOD_ID)
	public static Breealyzer instance;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		System.out.println(NAME + " is loading!");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

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
