package skelemarket.core;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////
// Severity enum
////////////////////////////////////////////////////////////////////////////////////
enum LoggerSeverity
{
    Trace,
    Info,
    Warning,
    Error,
}

public class Logger {
    ////////////////////////////////////////////////////////////////////////////////////
    // Static methods
    ////////////////////////////////////////////////////////////////////////////////////
    public static void trace(String message) { log(LoggerSeverity.Trace, message); }
    public static void info(String message) { log(LoggerSeverity.Info, message); }
    public static void warn(String message) { log(LoggerSeverity.Warning, message); }
    public static void error(String message) { log(LoggerSeverity.Error, message); }

    ////////////////////////////////////////////////////////////////////////////////////
    // Private static methods
    ////////////////////////////////////////////////////////////////////////////////////
    private static String severityToColour(LoggerSeverity severity)
    {
        switch (severity)
        {
            case LoggerSeverity.Trace:    return "\033[37m";
            case LoggerSeverity.Info:     return "\033[32m";
            case LoggerSeverity.Warning:  return "\033[33m";
            case LoggerSeverity.Error:    return "\033[31m";

            default:
                throw new RuntimeException("Unreachable logger colour code.");
        }
    }

    private static String severityToTag(LoggerSeverity severity)
    {
        switch (severity)
        {
            case LoggerSeverity.Trace:    return "TRACE";
            case LoggerSeverity.Info:     return "INFO";
            case LoggerSeverity.Warning:  return "WARN";
            case LoggerSeverity.Error:    return "ERROR";

            default:
                throw new RuntimeException("Unreachable logger tag code.");
        }
    }

    private static void log(LoggerSeverity severity, String message)
    {
        Format timeFormat = new SimpleDateFormat("HH:mm:ss");
        String timeStr = timeFormat.format(new Date());

        System.out.printf("%s[%s] [%s]: %s\n", severityToColour(severity), timeStr, severityToTag(severity), message);
        System.out.print("\033[0m");
    }
}
