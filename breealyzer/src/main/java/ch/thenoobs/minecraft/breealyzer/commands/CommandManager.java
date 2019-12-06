package ch.thenoobs.minecraft.breealyzer.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ch.thenoobs.minecraft.breealyzer.blocks.tileentities.BreealyzerTE;
import ch.thenoobs.minecraft.breealyzer.util.InventoryHandler;
import ch.thenoobs.minecraft.breealyzer.util.InventoryUtil;
import ch.thenoobs.minecraft.breealyzer.util.ItemStackAt;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeScore;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeSelector;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeWrapper;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringResult;
import jline.internal.Log;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import scala.actors.threadpool.Arrays;

public class CommandManager {
	private static Map<String, BreealyzerTE> registeredBreeAlyzers = new HashMap<>();

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
		if (idString.equalsIgnoreCase("list")) {
			listRegisteredBreealyzers(sender);
			return;
		}
		if (idString.equalsIgnoreCase("find")) {
			findBreealayzer(command[1], sender);
			return;
		}
		if (idString.equalsIgnoreCase("clearRegister")) {
			registeredBreeAlyzers.clear();
			return;
		}
		if (command.length < 2) {
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
		alyzer.setNewCommand((String[]) Arrays.copyOfRange(command, 1, command.length));

	}

	// TODO better way? switch?
	private static boolean isCommandValid(String[] command) {
		if (command[1].equalsIgnoreCase("purify") && command[2] != null && !command[2].isEmpty()) {
			return true;
		}
		if (command[1].equalsIgnoreCase("toggleDeleteTrashBees")) {
			return true;
		}
		if (command[1].equalsIgnoreCase("listKnownSpecies")) {
			return true;
		}
		return false;
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
		for (String key : registeredBreeAlyzers.keySet()) {
			TextComponentString text = new TextComponentString(key);
			sender.sendMessage(text);
		}
	}

}
