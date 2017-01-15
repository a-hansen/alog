/* Copyright 2017 by Aaron Hansen.
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

package com.ca.alog;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Time utilities.
 *
 * @author Aaron Hansen
 */
class Time {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    public static final long NANOS_IN_MS = 1000000;
    public static final long NANOS_IN_SEC = 1000 * NANOS_IN_MS;
    public static final long NANOS_IN_MIN = 60 * NANOS_IN_SEC;

    public static final int MS_IN_MIN = 60 * 1000;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static Calendar calendarCache1;
    private static Calendar calendarCache2;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Do not allow instantiation.
     */
    private Time() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Converts a Java Calendar into a number safe for file names: YYMMDD-HHMMSS.
     * If seconds align to 00, then they will be omitted.
     *
     * @param calendar The calendar representing the timestamp to encode.
     * @param buf      The buffer to append the encoded timestamp and return,
     *                 can be null.
     * @return The buf argument, or if that was null, a new StringBuilder.
     */
    public static StringBuilder encodeForFiles(Calendar calendar, StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        int tmp = calendar.get(Calendar.YEAR) % 100;
        if (tmp < 10) buf.append('0');
        buf.append(tmp);
        //month
        tmp = calendar.get(Calendar.MONTH) + 1;
        if (tmp < 10) buf.append('0');
        buf.append(tmp);
        //date
        tmp = calendar.get(Calendar.DAY_OF_MONTH);
        if (tmp < 10) buf.append('0');
        buf.append(tmp).append('-');
        //hour
        tmp = calendar.get(Calendar.HOUR_OF_DAY);
        if (tmp < 10) buf.append('0');
        buf.append(tmp);
        //minute
        tmp = calendar.get(Calendar.MINUTE);
        if (tmp < 10) buf.append('0');
        buf.append(tmp);
        //second
        tmp = calendar.get(Calendar.SECOND);
        if (tmp > 0) {
            if (tmp < 10) buf.append('0');
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
    public static StringBuilder encodeForLogs(Calendar calendar, StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        int tmp = calendar.get(Calendar.YEAR);
        buf.append(tmp).append('-');
        //month
        tmp = calendar.get(Calendar.MONTH) + 1;
        if (tmp < 10) buf.append('0');
        buf.append(tmp).append('-');
        //date
        tmp = calendar.get(Calendar.DAY_OF_MONTH);
        if (tmp < 10) buf.append('0');
        buf.append(tmp).append(' ');
        //hour
        tmp = calendar.get(Calendar.HOUR_OF_DAY);
        if (tmp < 10) buf.append('0');
        buf.append(tmp).append(':');
        //minute
        tmp = calendar.get(Calendar.MINUTE);
        if (tmp < 10) buf.append('0');
        buf.append(tmp).append(':');
        //second
        tmp = calendar.get(Calendar.SECOND);
        if (tmp < 10) buf.append('0');
        buf.append(tmp);
        return buf;
    }

    /**
     * Attempts to reuse a calendar instance, the timezone will be set to
     * TimeZone.getDefault().
     */
    public static Calendar getCalendar() {
        Calendar cal = null;
        synchronized (Time.class) {
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
    public static Calendar getCalendar(long timestamp) {
        Calendar cal = getCalendar();
        cal.setTimeInMillis(timestamp);
        return cal;
    }

    /**
     * Return a calendar instance for reuse.
     */
    public static void recycle(Calendar cal) {
        synchronized (Time.class) {
            if (calendarCache1 == null) {
                calendarCache1 = cal;
            } else {
                calendarCache2 = cal;
            }
        }
    }

}
