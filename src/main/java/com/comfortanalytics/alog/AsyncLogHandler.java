package com.comfortanalytics.alog;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * Enqueues log records which are then processed by separate thread.
 *
 * @author Aaron Hansen
 */
public abstract class AsyncLogHandler extends Handler {

    //////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final String PROPERTY_BASE = "com.comfortanalytics.alog";
    /**
     * The approximate file size after which a file will be zipped into a backup and a new
     * log file will be started; 10 mb by default.
     */
    static int DEFAULT_BACKUP_THRESHOLD = 10 * 1000 * 1000;
    /**
     * The default number of backups to retain; 10 by default.
     */
    static int DEFAULT_MAX_BACKUPS = 10;
    /**
     * Max async queue size after which records will be ignored; 25K by default.
     */
    static int DEFAULT_MAX_QUEUE = 25000;
    /**
     * Percentage (0-100) of the max queue after which log records less than
     * INFO are ignored; 90 by default.
     */
    static int DEFAULT_THROTTLE = 90;
    static int EMPTY_QUEUE_TIMEOUT = 15000;

    //////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean inferCaller = false;
    private LogHandlerThread logHandlerThread;
    private int maxQueueSize = DEFAULT_MAX_QUEUE;
    private boolean open = false;
    private final LinkedList<LogRecord> queue = new LinkedList<LogRecord>();
    private int throttle = DEFAULT_THROTTLE;
    private int throttleThreshold = (int) (DEFAULT_MAX_QUEUE * .90);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
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
     * Does not return until the queue is drained, or 15 seconds have elapsed.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void close() {
        synchronized (this) {
            if (!open) {
                return;
            }
            open = false;
        }
        waitForEmptyQueue(false);
        flush();
    }

    public boolean getInferCaller() {
        return inferCaller;
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
     * Enqueues the record for the write thread.
     */
    @Override
    public void publish(LogRecord record) {
        synchronized (this) {
            if (!open) {
                return;
            }
        }
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
        if (inferCaller) {
            record.getSourceClassName();
            record.getSourceMethodName();
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
                    } else if (param instanceof Number) {
                        if (param instanceof Integer) {
                            continue;
                        } else if (param instanceof Double) {
                            continue;
                        } else if (param instanceof Float) {
                            continue;
                        } else if (param instanceof Long) {
                            continue;
                        } else if (param instanceof Short) {
                            continue;
                        } else if (param instanceof Byte) {
                            continue;
                        }
                        Formatter formatter = getFormatter();
                        if (formatter != null) {
                            record.setMessage(formatter.formatMessage(record));
                        } else {
                            record.setMessage(String.format(msg, params));
                        }
                        record.setParameters(null);
                        break;
                    } else if (param instanceof Boolean) {
                        continue;
                    } else if (param instanceof Character) {
                        continue;
                    } else if (param instanceof Date) {
                        continue;
                    } else if (param instanceof Enum) {
                        continue;
                    } else if (param instanceof Calendar) {
                        params[i] = ((Calendar) param).clone();
                    } else {
                        params[i] = param.toString();
                    }
                }
            }
        }
        synchronized (queue) {
            if (open) {
                queue.addLast(record);
            }
            queue.notifyAll();
        }
    }

    public AsyncLogHandler setInferCaller(boolean fill) {
        inferCaller = fill;
        return this;
    }

    /**
     * The maximum number of records allowed in the queue, after which log records will be dropped.
     * Set to zero or less for an unbounded queue.
     */
    public AsyncLogHandler setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
        this.throttleThreshold = (int) (maxQueueSize * (throttle / 100d));
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
        this.throttleThreshold = (int) (maxQueueSize * (throttle / 100d));
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Load configuration from LogManager.
     */
    protected void configure() {
        LogManager manager = LogManager.getLogManager();
        String prop = manager.getProperty(PROPERTY_BASE + ".filter");
        Filter filter = optFilter(prop, null);
        if (filter != null) {
            setFilter(filter);
        }
        prop = manager.getProperty(PROPERTY_BASE + ".formatter");
        Formatter formatter = optFormatter(prop, null);
        if (formatter != null) {
            setFormatter(formatter);
        }
        prop = manager.getProperty(PROPERTY_BASE + ".inferCaller");
        setInferCaller(optBoolean(prop, false));
        prop = manager.getProperty(PROPERTY_BASE + ".level");
        setLevel(optLevel(prop, Level.INFO));
        prop = manager.getProperty(PROPERTY_BASE + ".maxQueue");
        setMaxQueueSize(optInt(prop, DEFAULT_MAX_QUEUE));
        prop = manager.getProperty(PROPERTY_BASE + ".throttle");
        setThrottle(optInt(prop, DEFAULT_THROTTLE));
    }

    /**
     * Used to name the thread that processes log records.
     */
    protected abstract String getThreadName();

    /**
     * Subclass hook for activities such as rolling files and cleaning up old garbage. Called
     * after every record is written. Does nothing by default.
     */
    protected void houseKeeping() {
    }

    /**
     * This must be called for the handler to actually do anything. Starts the write
     * thread if there isn't already an active write thread.
     */
    protected void start() {
        synchronized (this) {
            if (open) {
                return;
            }
            open = true;
            logHandlerThread = new LogHandlerThread();
            logHandlerThread.start();
        }
    }

    /**
     * Format and write the log record the underlying stream.
     */
    protected abstract void write(LogRecord record);

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    static boolean optBoolean(String val, boolean defaultValue) {
        if (val != null) {
            try {
                return Boolean.parseBoolean(val);
            } catch (Exception ignore) {
            }
        }
        return defaultValue;
    }

    static Filter optFilter(String val, Filter defaultValue) {
        if (val != null) {
            try {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Filter) clz.newInstance();
            } catch (Exception ignore) {
            }
        }
        return defaultValue;
    }

    static Formatter optFormatter(String val, Formatter defaultValue) {
        try {
            if (val != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Formatter) clz.newInstance();
            }
        } catch (Exception ignore) {
        }
        return defaultValue;
    }

    static int optInt(String val, int defaultValue) {
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (Exception ignore) {
            }
        }
        return defaultValue;
    }

    static Level optLevel(String val, Level defaultValue) {
        if (val == null) {
            return defaultValue;
        }
        Level l = Level.parse(val.trim());
        return l != null ? l : defaultValue;
    }

    static String optString(String val, String defaultValue) {
        if (val == null) {
            return defaultValue;
        }
        return val;
    }

    void waitForEmptyQueue(boolean throwException) {
        long start = System.currentTimeMillis();
        synchronized (queue) {
            while (!queue.isEmpty()) {
                if ((System.currentTimeMillis() - start) > EMPTY_QUEUE_TIMEOUT) {
                    if (throwException) {
                        throw new IllegalStateException("Timed out waiting for empty queue");
                    }
                    return;
                }
                queue.notifyAll();
                try {
                    queue.wait(100);
                } catch (Exception ignore) {
                }
            }
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
            LogRecord record = null;
            while (true) {
                synchronized (queue) {
                    if (queue.isEmpty()) {
                        queue.notifyAll();
                        if (open) {
                            try {
                                queue.wait(1000);
                            } catch (Exception ignore) {
                            }
                            queue.notifyAll();
                        } else {
                            logHandlerThread = null;
                            return;
                        }
                    } else {
                        record = queue.removeFirst();
                    }
                }
                if (record != null) {
                    write(record);
                    record = null;
                    houseKeeping();
                }
            }
        }
    }

}
