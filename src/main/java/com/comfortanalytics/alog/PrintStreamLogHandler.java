package com.comfortanalytics.alog;

import java.io.PrintStream;

/**
 * Async log handler for writing to streams such as System.out.
 *
 * @author Aaron Hansen
 */
public class PrintStreamLogHandler extends AsyncLogHandler {

    private String name;

    public PrintStreamLogHandler() {
        this.name = "Async Log Handler";
        configure();
        setOut(System.out);
        start();
    }

    public PrintStreamLogHandler(String name, PrintStream out) {
        this.name = name;
        configure();
        setOut(out);
        start();
    }

    @Override
    protected String getThreadName() {
        return name;
    }

}
