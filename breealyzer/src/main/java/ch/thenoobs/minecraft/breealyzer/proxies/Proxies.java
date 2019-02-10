package ch.thenoobs.minecraft.breealyzer.proxies;

import net.minecraftforge.fml.common.SidedProxy;

public class Proxies {
	@SidedProxy(serverSide = "ch.thenoobs.minecraft.breealyzer.proxies.CommonProxy", clientSide = "ch.thenoobs.minecraft.breealyzer.proxies.ClientProxy")
	public static CommonProxy common;
}
