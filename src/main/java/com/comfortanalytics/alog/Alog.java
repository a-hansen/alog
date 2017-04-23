/* ISC License
 *
 * Copyright 2017 by Comfort Analytics, LLC.
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with
 * or without fee is hereby granted, provided that the above copyright notice and this
 * permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 * PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.comfortanalytics.alog;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Acquire loggers here.  Multiple logs may share the same file, there will be a
 * single file handler per absolute file path and it is thread safe.
 * <p>
 * java.util.logging.LogManager.getProperty(String) will be used for the following default
 * settings:
 * <ul>
 * <li>com.comfortanalytics.alog.backupThreshold - The threshold file size after which the
 * file will be zipped into a backup and a new log file will be started; 10 mb by default.
 * <li>com.comfortanalytics.alog.maxBackups - The default number of backups to retain; 10
 * by default.
 * <li>com.comfortanalytics.alog.maxQueue - Max async queue size after which records will
 * be ignored; 250K by default.
 * <li>com.comfortanalytics.alog.throttle - Percentage (0-100) of the max queue after which
 * log records less than INFO are ignored; 90 by default.
 * </ul>
 *
 * @author Aaron Hansen
 */
public class Alog {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The threshold file size after which the file will be zipped into a backup and a new
     * log file will be started; 10 mb by default.
     */
    public static int DEFAULT_BACKUP_THRESHOLD = 1024 * 10;

    /**
     * The default number of backups to retain; 10 by default.
     */
    public static int DEFAULT_MAX_BACKUPS = 10;

    /**
     * Max async queue size after which records will be ignored; 25K by default.
     */
    public static int DEFAULT_MAX_QUEUE = 25000;

    /**
     * Percentage (0-100) of the max queue after which log records less than
     * INFO are ignored; 90 by default.
     */
    public static int DEFAULT_THROTTLE = 90;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    // Prevent instantiation.
    private Alog() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds a FileLogHandler to the named logger, if there isn't one already.
     * This can be used repeatedly to acquire the same logger, but doing so would be
     * inefficient.  Use Logger.getLogger after this has installed the handler.
     *
     * @param name Log name.
     * @param logFile Where record the log, may be null.  Multiple logs can safely share the same
     * file.
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
     * @param out Where to print the log.
     */
    public static Logger getLogger(String name, PrintStream out) {
        Logger ret = Logger.getLogger(name);
        for (Handler handler : ret.getHandlers()) {
            if (handler instanceof PrintStreamLogHandler) {
                PrintStreamLogHandler pslh = (PrintStreamLogHandler) handler;
                if (pslh.getOut() == out) {
                    return ret;
                }
            }
        }
        ret.addHandler(new PrintStreamLogHandler(name, out));
        return ret;
    }

    /**
     * Loads default settings from the LogManager properties.
     */
    static void loadDefaults() {
        Logger log = Logger.getLogger(Alog.class.getName());
        LogManager logManager = LogManager.getLogManager();
        String str = logManager.getProperty("com.comfortanalytics.alog.backupThreshold");
        if (str != null) {
            try {
                DEFAULT_BACKUP_THRESHOLD = Integer.parseInt(str);
            } catch (Exception x) {
                log.severe("Parse error com.comfortanaltyics.alog.backupThreshold = " + str);
            }
        }
        str = logManager.getProperty("com.comfortanalytics.alog.maxBackups");
        if (str != null) {
            try {
                DEFAULT_MAX_BACKUPS = Integer.parseInt(str);
            } catch (Exception x) {
                log.severe("Parse error com.comfortanaltyics.alog.maxBackups = " + str);
            }
        }
        str = logManager.getProperty("com.comfortanalytics.alog.maxQueue");
        if (str != null) {
            try {
                DEFAULT_MAX_QUEUE = Integer.parseInt(str);
            } catch (Exception x) {
                log.severe("Parse error com.comfortanaltyics.alog.maxQueue = " + str);
            }
        }
        str = logManager.getProperty("com.comfortanalytics.alog.throttle");
        if (str != null) {
            try {
                DEFAULT_THROTTLE = Integer.parseInt(str);
            } catch (Exception x) {
                log.severe("Parse error com.comfortanaltyics.alog.throttle = " + str);
            }
        }
    }

    /**
     * Removes existing handlers from the root logger and installs a
     * PrintStreamLogHandler for System.out.
     */
    public static void replaceRootHandler() {
        Logger global = Logger.getLogger("");
        for (Handler handler : global.getHandlers()) {
            global.removeHandler(handler);
        }
        global.addHandler(new PrintStreamLogHandler("Root Logger", System.out));
    }

    /**
     * Removes existing handlers from the root logger and installs a
     * FileLogHandler.
     */
    public static void replaceRootHandler(File logFile) {
        Logger global = Logger.getLogger("");
        for (Handler handler : global.getHandlers()) {
            global.removeHandler(handler);
        }
        FileLogHandler fileLogHandler = FileLogHandler.getHandler(logFile);
        global.addHandler(fileLogHandler);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        loadDefaults();
    }

} //class
