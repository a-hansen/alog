package com.comfortanalytics.alog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Asynchronously logs records to a file.  When the file exceeds a certain size, it'll be zipped
 * to a backup, and excess backups will be deleted.
 *
 * @author Aaron Hansen
 */
public class FileLogHandler extends AsyncLogHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static Map<String, FileLogHandler> allHandlers = new HashMap<String, FileLogHandler>();
    private int backupThreshold = DEFAULT_BACKUP_THRESHOLD;
    private StringBuilder builder;
    private Calendar calendar;
    private File file;
    private FileOutputStream fileOut;
    private long length;
    private int maxBackups = DEFAULT_MAX_BACKUPS;
    private PrintStream out;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public FileLogHandler() {
        configure();
        start();
    }

    /**
     * Will start by appending to an existing file.
     */
    private FileLogHandler(File file) {
        configure();
        setFile(file);
        start();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Closes the output stream.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void close() {
        super.close();
        //the above waits until the queue is empty
        try {
            if (fileOut != null) {
                fileOut.close();
                fileOut = null;
                out = null;
            }
        } catch (IOException x) {
            AlogException.throwRuntime(x);
        }
        synchronized (FileLogHandler.class) {
            allHandlers.remove(file.getAbsolutePath());
        }
    }

    @Override
    public void flush() {
        if (out != null) {
            out.flush();
        }
    }

    /**
     * The size after which a log file will be backed up and cleared.
     */
    public int getBackupThreshold() {
        return backupThreshold;
    }

    /**
     * Will return an existing handler for the given file, or create a new one.
     */
    public static synchronized FileLogHandler getHandler(File file) {
        String path = file.getAbsolutePath();
        FileLogHandler handler = allHandlers.get(path);
        if (handler == null) {
            handler = new FileLogHandler(file);
            allHandlers.put(path, handler);
        }
        return handler;
    }

    /**
     * The number of backup files to retain.
     */
    public int getMaxBackups() {
        return maxBackups;
    }

    /**
     * The file size threshold after which a log file will be backed up and cleared.
     */
    public FileLogHandler setBackupThreshold(int arg) {
        backupThreshold = arg;
        return this;
    }

    /**
     * The default is 10.
     */
    public FileLogHandler setMaxBackups(int arg) {
        maxBackups = arg;
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Load configuration from LogManager.
     */
    @Override
    protected void configure() {
        super.configure();
        LogManager manager = LogManager.getLogManager();
        String prop = manager.getProperty(PROPERTY_BASE + ".backupThreshold");
        setBackupThreshold(optInt(prop, DEFAULT_BACKUP_THRESHOLD));
        try {
            prop = manager.getProperty(PROPERTY_BASE + ".encoding");
            setEncoding(optString(prop, "UTF-8"));
        } catch (Exception x) {
            try {
                setEncoding(null);
            } catch (Exception ignore) {
            }
        }
        prop = manager.getProperty(PROPERTY_BASE + ".maxBackups");
        setMaxBackups(optInt(prop, DEFAULT_MAX_BACKUPS));
        prop = manager.getProperty(PROPERTY_BASE + ".filename");
        if (prop != null) {
            File f = makeFile(prop);
            allHandlers.put(f.getAbsolutePath(), this);
            setFile(f);
        }
    }

    @Override
    protected String getThreadName() {
        return file.getName();
    }

    @Override
    protected void houseKeeping() {
        if (length > backupThreshold) {
            flush();
            makeBackup();
            trimBackups();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    protected void write(LogRecord record) {
        Formatter formatter = getFormatter();
        if (formatter != null) {
            out.println(formatter.format(record));
            return;
        }
        if (builder == null) {
            builder = new StringBuilder();
            calendar = Calendar.getInstance();
        }
        Utils.write(record, out, builder, calendar);
    }

    /**
     * Backup files for this log, found in the same directory as the active log.
     */
    File[] getBackups() {
        File dir = file.getAbsoluteFile().getParentFile();
        File[] backups = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".zip") && name.startsWith(file.getName());
            }
        });
        if (backups == null) {
            return new File[0];
        }
        Arrays.sort(backups);
        return backups;
    }

    File getFile() {
        return file;
    }

    /**
     * Zips the current log file, deletes the unzipped version, then starts a new one.
     */
    private void makeBackup() {
        if (getMaxBackups() > 0) {
            try {
                if (out != null) {
                    out.close();
                    out = null;
                    fileOut.close();
                    fileOut = null;
                }
            } catch (Exception x) {
                Alog.rootLogger().log(Level.WARNING, "Closing streams", x);
            }
            ZipOutputStream zip = null;
            FileOutputStream fout = null;
            FileInputStream in = null;
            try {
                File back = uniqueFile();
                fout = new FileOutputStream(back);
                zip = new ZipOutputStream(fout);
                zip.putNextEntry(new ZipEntry(file.getName()));
                in = new FileInputStream(file);
                byte[] bytes = new byte[4096];
                int len = in.read(bytes);
                while (len > 0) {
                    zip.write(bytes, 0, len);
                    len = in.read(bytes);
                }
            } catch (Exception x) {
                Alog.rootLogger().log(Level.SEVERE, "Log backup error", x);
            }
            if (zip != null) {
                try {
                    zip.close();
                } catch (Exception x) {
                    Alog.rootLogger().log(Level.FINEST, "Log backup error", x);
                }
            }
            if (fout != null) {
                try {
                    zip.close();
                } catch (Exception x) {
                    Alog.rootLogger().log(Level.FINEST, "Log backup error", x);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception x) {
                    Alog.rootLogger().log(Level.FINEST, "Log backup error", x);
                }
            }
        }
        try {
            file.delete();
            setFile(file);
        } catch (Exception e) {
            AlogException.throwRuntime(e);
        }
    }

    static File makeFile(String pattern) {
        File file = null;
        StringBuilder buf = new StringBuilder();
        for (int i = 0, len = pattern.length(); i < len; ) {
            char ch = pattern.charAt(i);
            i++;
            char ch2 = 0;
            if (i < len) {
                ch2 = Character.toLowerCase(pattern.charAt(i));
            }
            if (ch == '/') {
                if (file == null) {
                    file = new File(buf.toString());
                } else {
                    file = new File(file, buf.toString());
                }
                buf.setLength(0);
                continue;
            } else if (ch == '%') {
                if (ch2 == 't') {
                    String tmpDir = System.getProperty("java.io.tmpdir");
                    if (tmpDir == null) {
                        tmpDir = System.getProperty("user.home");
                    }
                    file = new File(tmpDir);
                    i++;
                    buf.setLength(0);
                    continue;
                } else if (ch2 == 'h') {
                    file = new File(System.getProperty("user.home"));
                    i++;
                    buf.setLength(0);
                    continue;
                } else if (ch2 == '%') {
                    buf.append('%');
                    i++;
                    continue;
                }
            }
            buf.append(ch);
        }
        if (buf.length() > 0) {
            if (file == null) {
                file = new File(buf.toString());
            } else {
                file = new File(file, buf.toString());
            }
        }
        return file;
    }

    private void setFile(File file) {
        try {
            this.file = file;
            this.length = file.length();
            if (out != null) {
                out.close();
                out = null;
                fileOut.close();
                fileOut = null;
            }
            fileOut = new FileOutputStream(file, true);
            OutputStream tmp = new MeterStream(new BufferedOutputStream(fileOut));
            out = new PrintStream(tmp, false, getEncoding());
        } catch (Exception x) {
            AlogException.throwRuntime(x);
        }
    }

    /**
     * Calls sync on the file descriptor of the log file.
     */
    void sync() {
        FileOutputStream out = fileOut;
        if (out == null) {
            return;
        }
        try {
            out.getFD().sync();
        } catch (Exception x) {
            Alog.rootLogger().log(Level.WARNING, file.getName(), x);
        }
    }

    /**
     * Deletes old zipped up logs.
     */
    private void trimBackups() {
        File[] backups = getBackups();
        if (backups.length <= maxBackups) {
            return;
        }
        for (int i = 0, len = backups.length - maxBackups; i < len; i++) {
            backups[i].delete();
        }
    }

    /**
     * Needed mainly because of testing.
     */
    private File uniqueFile() {
        Calendar cal = Utils.getCalendar(System.currentTimeMillis());
        File parent = file.getAbsoluteFile().getParentFile();
        StringBuilder buf = new StringBuilder();
        try {
            buf.append(file.getName()).append('.');
            Utils.encodeForFiles(cal, false, false, buf);
            buf.append(".zip");
            File f = new File(parent, buf.toString());
            if (!f.exists()) {
                return f;
            }
            buf.setLength(0);
            buf.append(file.getName()).append('.');
            Utils.encodeForFiles(cal, true, false, buf);
            buf.append(".zip");
            f = new File(parent, buf.toString());
            if (!f.exists()) {
                return f;
            }
            buf.setLength(0);
            buf.append(file.getName()).append('.');
            Utils.encodeForFiles(cal, true, true, buf);
            buf.append(".zip");
            f = new File(parent, buf.toString());
            if (!f.exists()) {
                return f;
            }
            buf.setLength(0);
            buf.append(file.getName()).append('.');
            Utils.encodeForFiles(cal, true, true, buf);
            buf.append('.');
            String base = buf.toString();
            for (int i = 0; ; i++) {
                f = new File(parent, base + i + ".zip");
                if (!f.exists()) {
                    return f;
                }
            }
        } finally {
            Utils.recycle(cal);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class MeterStream extends OutputStream {

        private OutputStream out;

        MeterStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public void close() throws IOException {
            out.close();
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void write(byte[] buf, int off, int len) throws IOException {
            out.write(buf, off, len);
            length += len;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            length++;
        }

        @Override
        public void write(byte[] buf) throws IOException {
            out.write(buf);
            length += buf.length;
        }
    }

}
