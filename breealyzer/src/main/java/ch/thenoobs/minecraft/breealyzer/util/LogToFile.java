package ch.thenoobs.minecraft.breealyzer.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.FormattedMessageFactory;
import org.apache.logging.log4j.message.Message;
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
		Message logMessage = messageFactory.newMessage(message, params);
		
		Log.info("Log Directory: {}", getLogDirectory().getAbsolutePath());
		
		try {
			FileWriter fileWriter = new FileWriter(getLogFile(), true);
			
			fileWriter.write(logMessage.getFormattedMessage());
			
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static File getLogFile() {
		return new File(getLogDirectory(), (new SimpleDateFormat("yyyy-MM-dd")).format(new Date()) + "-Breealyzer.log");
	}
	
	private static File getLogDirectory()
	{
		File logDirectory = new File(Proxies.common.getRootFolder(), "Logs"); 
		
		if (!logDirectory.exists()) {
			logDirectory.mkdirs();
		}
		
		return logDirectory;
	}
}
