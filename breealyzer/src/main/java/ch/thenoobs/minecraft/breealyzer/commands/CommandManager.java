package ch.thenoobs.minecraft.breealyzer.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.thenoobs.minecraft.breealyzer.blocks.tileentities.BreealyzerTE;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import scala.actors.threadpool.Arrays;

public class CommandManager {
	private static Map<String, BreealyzerTE> registeredBreeAlyzers = new HashMap<>();

	private static List<String> generalCommands;
	private static List<String> breeAlyzerCommands;
	

	public static void registerBreeAlyzer(BreealyzerTE breealyzer) {
		if (registeredBreeAlyzers.containsKey(breealyzer.ensureAndGetUUID())) {
		} else {
			registeredBreeAlyzers.put(breealyzer.ensureAndGetUUID(), breealyzer);
		}
	}

	public static void forwardCommand(String[] command, MinecraftServer server, ICommandSender sender) {
		if (command.length < 1 || command[0] == null || command[0].isEmpty()) {
			return;
		}
		String idString = command[0];
		if (idString.equalsIgnoreCase("listBreeAlyzers")) {
			listRegisteredBreealyzers(sender);
			return;
		}
		if (idString.equalsIgnoreCase("findBreeAlyzer")) {
			findBreealayzer(command[1], sender);
			return;
		}
		if (idString.equalsIgnoreCase("clearRegister")) {
			registeredBreeAlyzers.clear();
			return;
		}
		if (command.length < 2) {
			TextComponentString text = new TextComponentString("Unknown general command: " + Arrays.toString(command));
			sender.sendMessage(text);
			return;
		}
		if (!isCommandValid(command)) {
			TextComponentString text = new TextComponentString("Command not recognised: " + Arrays.toString(command));
			sender.sendMessage(text);
			return;
		}

		BreealyzerTE alyzer = registeredBreeAlyzers.get(idString);
		if (alyzer == null) {
			TextComponentString text = new TextComponentString("No breealyzer with id " + idString + " found");
			sender.sendMessage(text);
			return;
		}
		alyzer.sendCommand((String[]) Arrays.copyOfRange(command, 1, command.length), sender);

	}

	public static List<String> getGlobalCommands() {
		if (generalCommands != null) {
			return generalCommands;
		}
		generalCommands = new ArrayList<>();
		generalCommands.add("listBreeAlyzers");
		generalCommands.add("findBreeAlyzer");
		generalCommands.add("clearRegister");
		return generalCommands;
	}
	
	public static List<String> getBreeAlyzerCommands() {
		if (breeAlyzerCommands != null) {
			return breeAlyzerCommands;
		}
		breeAlyzerCommands = new ArrayList<>();
		breeAlyzerCommands.add("listBreeAlyzers");
		breeAlyzerCommands.add("findBreeAlyzer");
		breeAlyzerCommands.add("clearRegister");
		return breeAlyzerCommands;
	}
	
	// TODO better way? switch?
	private static boolean isCommandValid(String[] command) {
		return breeAlyzerCommands.contains(command[1]);
	}

	private static void findBreealayzer(String alyzerId, ICommandSender sender) {
		BreealyzerTE alyzer = registeredBreeAlyzers.get(alyzerId);
		if (alyzer == null) {
			return;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("Position of: ");
		builder.append(alyzerId);
		builder.append("\nx: ");
		builder.append(alyzer.getPos().getX());
		builder.append("\ny: ");
		builder.append(alyzer.getPos().getY());
		builder.append("\nz: ");
		builder.append(alyzer.getPos().getZ());
		TextComponentString text = new TextComponentString(builder.toString());
		sender.sendMessage(text);
	}

	private static void listRegisteredBreealyzers(ICommandSender sender) {
		for (String key : getRegisteredBreeAlyzers()) {
			TextComponentString text = new TextComponentString(key);
			sender.sendMessage(text);
		}
	}

	public static Set<String> getRegisteredBreeAlyzers() {
		return registeredBreeAlyzers.keySet();
	}

}
