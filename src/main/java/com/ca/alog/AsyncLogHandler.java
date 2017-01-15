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

import java.io.PrintStream;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Enqueues log records which are then processed by separate thread.
 *
 * @author Aaron Hansen
 */
public abstract class AsyncLogHandler extends Handler {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private StringBuilder builder = new StringBuilder();
    private Calendar calendar = Calendar.getInstance();
    private boolean open = false;
    private LinkedList<LogRecord> queue = new LinkedList<LogRecord>();
    private PrintStream out;
    private LogHandlerThread logHandlerThread;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Closes the PrintStream, terminates the write thread and performs houseKeeping.
     */
    @Override
    public void close() {
        open = false;
        synchronized (queue) {
            queue.notifyAll();
        }
        houseKeeping();
        out.close();
    }

    @Override
    public void flush() {
        out.flush();
    }

    /**
     * One minute by default, this is a guideline more than anything else.  Housekeeping
     * can be called sooner during low activity periods.
     */
    public long getHouseKeepingIntervalNanos() {
        return Time.NANOS_IN_MIN;
    }

    /**
     * The sink for formatted messages.
     */
    protected PrintStream getOut() {
        return out;
    }

    /**
     * Used to name the thread that processes log records.
     */
    protected abstract String getThreadName();

    /**
     * Subclass hook for activities such as rolling files and cleaning up old garbage.
     * Called during periods of inactivity or after the houseKeepingInterval is
     * exceeded. Does nothing by default and flush will be called just prior to this.
     */
    protected void houseKeeping() {
    }

    /**
     * Enqueues the record for the write thread.
     */
    @Override
    public void publish(LogRecord record) {
        if (open) {
            synchronized (queue) {
                queue.add(record);
                queue.notifyAll();
            }
        }
    }

    /**
     * Sets the sink for formatted messages.
     */
    protected void setOut(PrintStream out) {
        this.out = out;
    }

    /**
     * This must be called for the handler to actually do anything. Starts the write
     * thread if there isn't already an active write thread.
     */
    protected void start() {
        if (logHandlerThread == null) {
            open = true;
            logHandlerThread = new LogHandlerThread();
            logHandlerThread.start();
        }
    }

    /**
     * Formats and writes the log record the underlying stream.
     */
    protected void write(LogRecord record) {
        // severity
        out.print(record.getLevel().getLocalizedName());
        out.print(" [");
        // timestamp
        calendar.setTimeInMillis(record.getMillis());
        out.print(Time.encodeForLogs(calendar, builder).toString());
        builder.setLength(0);
        out.print("][");
        // log name
        out.print(record.getLoggerName());
        out.print("]");
        // message
        Formatter formatter = getFormatter();
        if (formatter == null) {
            formatter = new SimpleFormatter();
            setFormatter(formatter);
        }
        String msg = formatter.formatMessage(record);
        if ((msg != null) && !msg.isEmpty()) {
            out.print(' ');
            out.print(msg);
        }
        out.println();
        // exception
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            thrown.printStackTrace(out);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class LogHandlerThread extends Thread {
        public LogHandlerThread() {
            super(AsyncLogHandler.this.getThreadName());
            setDaemon(true);
        }

        public void run() {
            long lastHouseKeeping = System.nanoTime();
            long now;
            LogRecord record;
            boolean emptyQueue;
            while (open) {
                record = null;
                synchronized (queue) {
                    emptyQueue = queue.isEmpty();
                    if (emptyQueue) {
                        try {
                            queue.wait(5000);
                        } catch (Exception ignore) {
                        }
                        emptyQueue = queue.isEmpty(); //housekeeping opportunity flag
                    } else {
                        record = queue.remove(0);
                    }
                }
                if (open) {
                    if (record != null) {
                        write(record);
                        Thread.yield();
                    } else if (emptyQueue) {
                        //potential housekeeping opportunity
                        now = System.nanoTime();
                        long min = getHouseKeepingIntervalNanos() / 2;
                        if ((now - lastHouseKeeping) > min) {
                            flush();
                            houseKeeping();
                            lastHouseKeeping = System.nanoTime();
                        }
                    }
                    now = System.nanoTime();
                    if ((now - lastHouseKeeping) > getHouseKeepingIntervalNanos()) {
                        flush();
                        houseKeeping();
                        lastHouseKeeping = System.nanoTime();
                    }
                }
            }
            logHandlerThread = null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
