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
    public static void Trace(String message) { Log(LoggerSeverity.Trace, message); }
    public static void Info(String message) { Log(LoggerSeverity.Info, message); }
    public static void Warning(String message) { Log(LoggerSeverity.Warning, message); }
    public static void Warn(String message) { Warning(message); }
    public static void Error(String message) { Log(LoggerSeverity.Error, message); }

    ////////////////////////////////////////////////////////////////////////////////////
    // Private static methods
    ////////////////////////////////////////////////////////////////////////////////////
    private static String SeverityToColour(LoggerSeverity severity)
    {
        switch (severity)
        {
            case LoggerSeverity.Trace:    return "\033[37m";
            case LoggerSeverity.Info:     return "\033[32m";
            case LoggerSeverity.Warning:  return "\033[33m";
            case LoggerSeverity.Error:    return "\033[31m";

            default:
                // TODO: Break
                break;
        }

        return "\033[0m";
    }

    private static String SeverityToTag(LoggerSeverity severity)
    {
        switch (severity)
        {
            case LoggerSeverity.Trace:    return "TRACE";
            case LoggerSeverity.Info:     return "INFO";
            case LoggerSeverity.Warning:  return "WARN";
            case LoggerSeverity.Error:    return "ERROR";

            default:
                // TODO: Break
                break;
        }

        return "<UNKNOWN>";
    }

    private static void Log(LoggerSeverity severity, String message)
    {
        Format timeFormat = new SimpleDateFormat("HH:mm:ss");
        String timeStr = timeFormat.format(new Date());

        System.out.printf("%s[%s] [%s]: %s\n", SeverityToColour(severity), timeStr, SeverityToTag(severity), message);
        System.out.print("\033[0m");
    }
}
