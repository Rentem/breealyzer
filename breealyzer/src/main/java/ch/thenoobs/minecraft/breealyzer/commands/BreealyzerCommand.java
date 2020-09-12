package ch.thenoobs.minecraft.breealyzer.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import ch.thenoobs.minecraft.breealyzer.util.Log;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class BreealyzerCommand extends CommandBase {

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {

		Log.info("execute called");

		if (params != null) {
			CommandManager.forwardCommand(params, server, sender);
		} else {
			TextComponentString text = new TextComponentString("Invalid command format");
			text.getStyle().setColor(TextFormatting.RED);
			sender.sendMessage(text);
		}
	}

	@Override
	public String getName() {
		return "breealyzer";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "command.breealyzer.usage";
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length < 1) {
			List<String> commands = new ArrayList<>(CommandManager.getBreeAlyzerCommands());
			commands.addAll(CommandManager.getGlobalCommands());
			return commands;
		}
		if (args.length == 1) {
			Stream<String> breeAlyzerStream = CommandManager.getBreeAlyzerCommands().stream();
			Stream<String> globalStream = CommandManager.getGlobalCommands().stream();

			return Stream.concat(breeAlyzerStream, globalStream)
					.filter(command -> command.startsWith(args[0]))
					.collect(Collectors.toList());
		}

		if (CommandManager.getBreeAlyzerCommands().contains(args[0])) {
			return new ArrayList<>(CommandManager.getRegisteredBreeAlyzers());
		}
		return Collections.<String>emptyList();
	}

}
