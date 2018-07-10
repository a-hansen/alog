package com.comfortanalytics.alog;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Use to install async handlers.
 *
 * @author Aaron Hansen
 */
public class Alog {

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private Alog() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds a FileLogHandler to the root logger, if there isn't one for the same file already.
     *
     * @param logFile Where record the log.  Multiple logs can safely share the same file.
     * @return The logger for the given name with an async file handler attached to it.
     */
    public static void addToRootLogger(File logFile) {
        getLogger("", logFile);
    }

    /**
     * Returns the first AsyncLogHandler found for the given log, or null.
     */
    public static AsyncLogHandler getHandler(Logger logger) {
        for (Handler h : logger.getHandlers()) {
            if (h instanceof AsyncLogHandler) {
                return (AsyncLogHandler) h;
            }
        }
        return null;
    }

    /**
     * Adds a FileLogHandler to the logger named for the given class, if there isn't one for the
     * same file already. This can be used repeatedly to acquire the same logger, but doing so
     * would be inefficient.  Use Logger.getLogger after this has installed the handler.
     *
     * @param clazz   The class name will be used as the log name.
     * @param logFile Where record the log.  Multiple logs can safely share the same file.
     * @return The logger for the given name with an async file handler attached to it.
     */
    public static Logger getLogger(Class clazz, File logFile) {
        return getLogger(clazz.getName(), logFile);
    }

    /**
     * Adds a PrintStreamLogHandler to the logger named for the given class, if there isn't one
     * already. This can be used repeatedly to acquire the same logger, but doing so would be
     * inefficient.  Use Logger.getLogger after this has installed the handler.
     *
     * @param clazz The class name will be used as the log name.
     * @param out   Where to print the log.
     * @return The logger for the given name with an async handler attached to it.
     */
    public static Logger getLogger(Class clazz, PrintStream out) {
        return getLogger(clazz.getName(), out);
    }

    /**
     * Adds a FileLogHandler to the named logger, if there isn't one for the same file already.
     * This can be used repeatedly to acquire the same logger, but doing so would be
     * inefficient.  Use Logger.getLogger after this has installed the handler.
     *
     * @param name    Log name.
     * @param logFile Where record the log.  Multiple logs can safely share the same file.
     * @return The logger for the given name with an async file handler attached to it.
     */
    public static Logger getLogger(String name, File logFile) {
        Logger ret = Logger.getLogger(name);
        FileLogHandler fileLogHandler = FileLogHandler.getHandler(logFile);
        for (Handler handler : ret.getHandlers()) {
            if (handler == fileLogHandler) {
                return ret;
            }
        }
        ret.addHandler(fileLogHandler);
        return ret;
    }

    /**
     * Adds a PrintStreamLogHandler to the named logger, if there isn't one already.
     * This can be used repeatedly to acquire the same logger, but doing so would be
     * inefficient.  Use Logger.getLogger after this has installed the handler.
     *
     * @param name Log name.
     * @param out  Where to print the log.
     * @return The logger for the given name with an async handler attached to it.
     */
    public static Logger getLogger(String name, PrintStream out) {
        Logger ret = Logger.getLogger(name);
        for (Handler handler : ret.getHandlers()) {
            if (handler instanceof PrintStreamLogHandler) {
                return ret;
            }
        }
        ret.addHandler(new PrintStreamLogHandler(name, out));
        return ret;
    }

    /**
     * Removes existing handlers from the root logger and installs a
     * PrintStreamLogHandler for System.out.
     */
    public static void replaceRootHandler() {
        Logger root = rootLogger();
        for (Handler handler : root.getHandlers()) {
            if (handler instanceof FileLogHandler) {
                handler.close();
            }
            root.removeHandler(handler);
        }
        root.addHandler(new PrintStreamLogHandler("Async Root Logger", System.out));
    }

    /**
     * Removes existing handlers from the root logger and installs a
     * FileLogHandler.
     */
    public static void replaceRootHandler(File logFile) {
        Logger root = rootLogger();
        for (Handler handler : root.getHandlers()) {
            if (handler instanceof FileLogHandler) {
                handler.close();
            }
            root.removeHandler(handler);
        }
        FileLogHandler fileLogHandler = FileLogHandler.getHandler(logFile);
        root.addHandler(fileLogHandler);
    }

    /**
     * A convenience for Logger.getLogger("").
     */
    public static Logger rootLogger() {
        return Logger.getLogger("");
    }

}
