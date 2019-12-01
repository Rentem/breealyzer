package ch.thenoobs.minecraft.breealyzer.commands;

import ch.thenoobs.minecraft.breealyzer.util.Log;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
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
		
}
