package ch.thenoobs.minecraft.breealyzer.util;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.FormattedMessageFactory;
import org.apache.logging.log4j.message.MessageFactory2;
import ch.thenoobs.minecraft.breealyzer.proxies.Proxies;

public class LogToFile {
	private static MessageFactory2 messageFactory = new FormattedMessageFactory();

	public static void trace(String message, Object... params) {
		log(Level.TRACE, message, params);
	}

	public static void debug(String message, Object... params) {
		log(Level.DEBUG, message, params);
	}

	public static void info(String message, Object... params) {
		log(Level.INFO, message, params);
	}

	public static void warning(String message, Object... params) {
		log(Level.WARN, message, params);
	}

	public static void error(String message, Object... params) {
		log(Level.ERROR, message, params);
	}

	private static void log(Level logLevel, String message, Object... params) {
		//Message logMessage = messageFactory.newMessage(message, params);
		
		Log.info("Log Directory: {}", getLogDirectory().getAbsolutePath());

		//File file1 = new File(new File(getLogDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
	}
	
	private static File getLogDirectory()
	{
		return new File(Proxies.common.getRootFolder(), "Logs");
	}
}
