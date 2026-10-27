package skelemarket.core;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////
// Severity enum
////////////////////////////////////////////////////////////////////////////////////
enum LoggerSeverity {
	Trace,
	Info,
	Warning,
	Error,
}

public class Logger {
	////////////////////////////////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////////////////////////////////
	private static final String WHITE = "\033[37m";
	private static final String GREEN = "\033[32m";
	private static final String YELLOW = "\033[33m";
	private static final String RED = "\033[31m";

	////////////////////////////////////////////////////////////////////////////////////
	// Static methods
	////////////////////////////////////////////////////////////////////////////////////
	public static void trace(String message, Object... args) {
		log(LoggerSeverity.Trace, message, args);
	}

	public static void info(String message, Object... args) {
		log(LoggerSeverity.Info, message, args);
	}

	public static void warn(String message, Object... args) {
		log(LoggerSeverity.Warning, message, args);
	}

	public static void error(String message, Object... args) {
		log(LoggerSeverity.Error, message, args);
	}

	////////////////////////////////////////////////////////////////////////////////////
	// Private static methods
	////////////////////////////////////////////////////////////////////////////////////
	private static String severityToColour(LoggerSeverity severity) {
		switch (severity) {
			case LoggerSeverity.Trace:
				return Logger.WHITE;
			case LoggerSeverity.Info:
				return Logger.GREEN;
			case LoggerSeverity.Warning:
				return Logger.YELLOW;
			case LoggerSeverity.Error:
				return Logger.RED;

			default:
				throw new RuntimeException("Unreachable logger colour code.");
		}
	}

	private static String severityToTag(LoggerSeverity severity) {
		switch (severity) {
			case LoggerSeverity.Trace:
				return "TRACE";
			case LoggerSeverity.Info:
				return "INFO";
			case LoggerSeverity.Warning:
				return "WARN";
			case LoggerSeverity.Error:
				return "ERROR";

			default:
				throw new RuntimeException("Unreachable logger tag code.");
		}
	}

	private static void log(LoggerSeverity severity, String message, Object... args) {
		Format timeFormat = new SimpleDateFormat("HH:mm:ss");
		String timeStr = timeFormat.format(new Date());

		System.out.printf("%s[%s] [%s]: %s\n", severityToColour(severity), timeStr, severityToTag(severity),
				String.format(message, args));
		System.out.print("\033[0m");
	}
}
