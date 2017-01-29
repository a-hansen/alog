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
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.logging.*;

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
    private LogHandlerThread logHandlerThread;
    private int maxQueueSize = Alog.DEFAULT_MAX_QUEUE;
    private boolean open = false;
    private PrintStream out;
    private LinkedList<LogRecord> queue = new LinkedList<LogRecord>();
    private int queueThrottle = (int) (Alog.DEFAULT_MAX_QUEUE * .75);

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The number of items on the queue.
     */
    public int backlog() {
        synchronized (queue) {
            return queue.size();
        }
    }

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
     * Ten seconds by default, this is a guideline more than anything else.  Housekeeping
     * can be called sooner during low activity periods.
     */
    public long getHouseKeepingIntervalNanos() {
        return Time.NANOS_IN_10SEC;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
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
                int size = queue.size();
                if (size < maxQueueSize) {
                    if (size > queueThrottle) {
                        if (record.getLevel().intValue() < Level.INFO.intValue()) {
                            return;
                        }
                    }
                    queue.addLast(record);
                    queue.notify();
                }
            }
        }
    }

    public AsyncLogHandler setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
        this.queueThrottle = (int) (maxQueueSize * .75);
        return this;
    }

    /**
     * Sets the sink for formatted messages.
     */
    protected AsyncLogHandler setOut(PrintStream out) {
        this.out = out;
        return this;
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
        Formatter formatter = getFormatter();
        if (formatter != null) {
            out.println(formatter.formatMessage(record));
            return;
        }
        // log name
        out.print(record.getLoggerName());
        out.print(" [");
        // timestamp
        calendar.setTimeInMillis(record.getMillis());
        out.print(Time.encodeForLogs(calendar, builder).toString());
        builder.setLength(0);
        out.print("] ");
        // severity
        out.print(record.getLevel().getLocalizedName());
        // message
        String msg = record.getMessage();
        if ((msg != null) && (msg.length() > 0)) {
            if (record.getSourceClassName() != null) {
                out.print(' ');
                out.print(record.getSourceClassName());
            }
            if (record.getSourceMethodName() != null) {
                out.print(' ');
                out.print(record.getSourceMethodName());
            }
            Object[] params = record.getParameters();
            if (params != null) {
                msg = String.format(msg, params);
            }
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
                            queue.wait(2000);
                        } catch (Exception ignore) {
                        }
                        emptyQueue = queue.isEmpty(); //housekeeping opportunity flag
                    } else {
                        record = queue.removeFirst();
                    }
                }
                if (open) {
                    if (record != null) {
                        write(record);
                        Thread.yield();
                    }
                    if (emptyQueue) {
                        //housekeeping opportunity
                        now = System.nanoTime();
                        long min = getHouseKeepingIntervalNanos() / 2;
                        if ((now - lastHouseKeeping) > min) {
                            flush();
                            houseKeeping();
                            lastHouseKeeping = System.nanoTime();
                        }
                    } else {
                        now = System.nanoTime();
                        if ((now - lastHouseKeeping) > getHouseKeepingIntervalNanos()) {
                            flush();
                            houseKeeping();
                            lastHouseKeeping = System.nanoTime();
                        }
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
