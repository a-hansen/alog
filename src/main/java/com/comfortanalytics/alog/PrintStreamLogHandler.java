package com.comfortanalytics.alog;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Async log handler for writing to streams such as System.out.
 *
 * @author Aaron Hansen
 */
public class PrintStreamLogHandler extends AsyncLogHandler {

    private StringBuilder builder;
    private Calendar calendar;
    private String name;
    private PrintStream out;

    public PrintStreamLogHandler() {
        this.name = "Async Log Handler";
        configure();
        this.out = out;
        start();
    }

    public PrintStreamLogHandler(String name, PrintStream out) {
        this.name = name;
        configure();
        this.out = out;
        start();
    }

    @Override
    public void close() {
        super.close();
        out = null;
    }

    @Override
    public void flush() {
        if (out != null) {
            out.flush();
        }
    }

    @Override
    protected String getThreadName() {
        return name;
    }

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

}
