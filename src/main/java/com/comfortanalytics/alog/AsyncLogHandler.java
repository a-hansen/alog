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

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

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
    private int throttle = 90;
    private int throttleThreshold = (int) (Alog.DEFAULT_MAX_QUEUE * .90);

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
     * Clears the queue.
     */
    public void clearBacklog() {
        synchronized (queue) {
            queue.clear();
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
     * When the queue fills to this percent, records finer than INFO are dropped.  Set
     * to 100 to disable this behavior, the default is 90.
     */
    public int getThrottle() {
        return throttle;
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
            if (maxQueueSize > 0) {
                int size = queue.size();
                if (size >= throttleThreshold) {
                    if (size < maxQueueSize) {
                        if (record.getLevel().intValue() < Level.INFO.intValue()) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
            Object[] params = record.getParameters();
            if ((params != null) && (params.length > 0)) {
                String msg = record.getMessage();
                if ((msg != null) && (msg.length() > 0)) {
                    Object param;
                    for (int i = params.length; --i >= 0; ) {
                        param = params[i];
                        if (param instanceof String) {
                            continue;
                        } else if (param instanceof Integer) {
                            continue;
                        } else if (param instanceof Boolean) {
                            continue;
                        } else if (param instanceof Byte) {
                            continue;
                        } else if (param instanceof Character) {
                            continue;
                        } else if (param instanceof Date) {
                            continue;
                        } else if (param instanceof Double) {
                            continue;
                        } else if (param instanceof Enum) {
                            continue;
                        } else if (param instanceof Float) {
                            continue;
                        } else if (param instanceof Long) {
                            continue;
                        } else if (param instanceof Short) {
                            continue;
                        } else if (param instanceof Calendar) {
                            params[i] = ((Calendar) param).clone();
                        } else if (param instanceof Number) {
                            Formatter formatter = getFormatter();
                            if (formatter != null) {
                                record.setMessage(formatter.formatMessage(record));
                            } else {
                                record.setMessage(String.format(msg, params));
                            }
                            record.setParameters(null);
                            break;
                        } else {
                            params[i] = param.toString();
                        }
                    }
                }
            }
            synchronized (queue) {
                queue.addLast(record);
                queue.notify();
            }
        }
    }

    /**
     * The maximum number of records allowed in the queue, after which log records will be dropped.
     * Set to zero or less for an unbounded queue.
     */
    public AsyncLogHandler setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
        this.throttleThreshold = maxQueueSize * (throttle / 100);
        return this;
    }

    /**
     * When the queue fills to this percent, records finer than INFO are dropped.  Set
     * to 100 to disable this behavior, the default is 90.
     *
     * @param percent 0-100 where 100 would disable the throttle.
     */
    public AsyncLogHandler setThrottle(int percent) {
        this.throttle = percent;
        this.throttleThreshold = maxQueueSize * (throttle / 100);
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
            out.println(formatter.format(record));
            return;
        }
        // log name
        builder.append(record.getLoggerName());
        builder.append(" [");
        // timestamp
        calendar.setTimeInMillis(record.getMillis());
        Time.encodeForLogs(calendar, builder);
        builder.append("] ");
        // severity
        builder.append(record.getLevel().getLocalizedName());
        // class
        if (record.getSourceClassName() != null) {
            builder.append(' ');
            builder.append(record.getSourceClassName());
        }
        // method
        if (record.getSourceMethodName() != null) {
            builder.append(' ');
            builder.append(record.getSourceMethodName());
        }
        // message
        String msg = record.getMessage();
        if ((msg != null) && (msg.length() > 0)) {
            Object[] params = record.getParameters();
            if (params != null) {
                msg = String.format(msg, params);
            }
            builder.append(' ');
            builder.append(msg);
        }
        out.println(builder.toString());
        builder.setLength(0);
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
