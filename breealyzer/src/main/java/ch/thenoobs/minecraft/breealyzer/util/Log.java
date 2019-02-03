package ch.thenoobs.minecraft.breealyzer.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import ch.thenoobs.minecraft.breealyzer.Breealyzer;

public class Log {

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
		LogManager.getLogger(Breealyzer.MOD_ID).log(logLevel, message, params);
	}
}
