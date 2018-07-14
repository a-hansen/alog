package com.comfortanalytics.alog;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Aaron Hansen
 */
class Utils {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static Calendar calendarCache1;
    private static Calendar calendarCache2;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private Utils() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Use by testing.
     */
    static void clearHandlers(String logName) {
        Logger log = Logger.getLogger(logName);
        for (Handler h : log.getHandlers()) {
            if (h instanceof AsyncLogHandler) {
                h.close();
                log.removeHandler(h);
            }
        }
    }

    /**
     * Converts a Java Calendar into a number safe for file names: YYMMDD-HHMMSS.
     *
     * @param calendar The calendar representing the timestamp to encode.
     * @param secs     Whether or not to include the seconds.
     * @param ms       Whether or not to include the millis.
     * @param buf      The buffer to append the encoded timestamp and return,
     *                 can be null.
     * @return The buf argument, or if that was null, a new StringBuilder.
     */
    static StringBuilder encodeForFiles(Calendar calendar,
                                        boolean secs,
                                        boolean ms,
                                        StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        int tmp = calendar.get(Calendar.YEAR) % 100;
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        //month
        tmp = calendar.get(Calendar.MONTH) + 1;
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        //date
        tmp = calendar.get(Calendar.DAY_OF_MONTH);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append('-');
        //hour
        tmp = calendar.get(Calendar.HOUR_OF_DAY);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        //minute
        tmp = calendar.get(Calendar.MINUTE);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        //second
        if (secs) {
            tmp = calendar.get(Calendar.SECOND);
            if (tmp < 10) {
                buf.append('0');
            }
            buf.append(tmp);
        }
        if (ms) {
            tmp = calendar.get(Calendar.MILLISECOND);
            if (tmp < 100) {
                buf.append('0');
            }
            if (tmp < 10) {
                buf.append('0');
            }
            buf.append(tmp);
        }
        return buf;
    }

    /**
     * Converts a Java Calendar into a shorter human readable timestamp for use
     * in log files.
     *
     * @param calendar The calendar representing the timestamp to encode.
     * @param buf      The buffer to append the encoded timestamp and return,
     *                 can be null.
     * @return The buf argument, or if that was null, a new StringBuilder.
     */
    static StringBuilder encodeForLogs(Calendar calendar, StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        int tmp = calendar.get(Calendar.YEAR);
        buf.append(tmp).append('-');
        //month
        tmp = calendar.get(Calendar.MONTH) + 1;
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append('-');
        //date
        tmp = calendar.get(Calendar.DAY_OF_MONTH);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append(' ');
        //hour
        tmp = calendar.get(Calendar.HOUR_OF_DAY);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append(':');
        //minute
        tmp = calendar.get(Calendar.MINUTE);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append(':');
        //second
        tmp = calendar.get(Calendar.SECOND);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        return buf;
    }

    /**
     * Attempts to reuse a calendar instance, the timezone will be set to
     * TimeZone.getDefault().
     */
    static Calendar getCalendar() {
        Calendar cal = null;
        synchronized (Utils.class) {
            if (calendarCache1 != null) {
                cal = calendarCache1;
                calendarCache1 = null;
            } else if (calendarCache2 != null) {
                cal = calendarCache2;
                calendarCache2 = null;
            }
        }
        if (cal == null) {
            cal = Calendar.getInstance();
        } else {
            cal.setTimeZone(TimeZone.getDefault());
        }
        return cal;
    }

    /**
     * Attempts to reuse a calendar instance and sets the time in millis to the argument
     * and the timezone to TimeZone.getDefault().
     */
    static Calendar getCalendar(long timestamp) {
        Calendar cal = getCalendar();
        cal.setTimeInMillis(timestamp);
        return cal;
    }

    /**
     * Return a calendar instance for reuse.
     */
    static void recycle(Calendar cal) {
        synchronized (Utils.class) {
            if (calendarCache1 == null) {
                calendarCache1 = cal;
            } else {
                calendarCache2 = cal;
            }
        }
    }


    /**
     * Formats and writes the log record the underlying stream.
     */
    static void write(LogRecord record, PrintStream out, StringBuilder builder, Calendar calendar) {
        if (builder == null) {
            builder = new StringBuilder();
            calendar = Calendar.getInstance();
        }
        builder.append('[');
        // timestamp
        calendar.setTimeInMillis(record.getMillis());
        Utils.encodeForLogs(calendar, builder);
        builder.append(']');
        builder.append(' ');
        // severity
        builder.append(record.getLevel().getLocalizedName());
        builder.append(" - ");
        // class
        if (record.getSourceClassName() != null) {
            builder.append(record.getSourceClassName());
            builder.append(" - ");
        }
        // method
        if (record.getSourceMethodName() != null) {
            builder.append(record.getSourceMethodName());
            builder.append(" - ");
        }
        // log name
        builder.append(record.getLoggerName());
        // message
        String msg = record.getMessage();
        if ((msg != null) && (msg.length() > 0)) {
            Object[] params = record.getParameters();
            if (params != null) {
                msg = String.format(msg, params);
            }
            builder.append(" - ");
            builder.append(msg);
        }
        out.println(builder);
        builder.setLength(0);
        // exception
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            thrown.printStackTrace(out);
        }
    }


}
