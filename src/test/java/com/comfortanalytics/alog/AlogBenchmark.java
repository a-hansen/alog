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

import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

/**
 * @author Aaron Hansen
 */
public class AlogBenchmark {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    public static int ITERATIONS = 50000;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    public static PrintStream out;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    private void printField(Object obj, int len) {
        String str = obj.toString();
        for (int i = len - str.length(); --i >= 0; ) {
            out.print(' ');
        }
        out.print(str);
    }

    private void printResults(BenchmarkInterface target,
                              long exception,
                              long param,
                              long simple,
                              long total) {
        printField(target, 10);
        out.print(", Exception:");
        printField(exception + "ms", 7);
        out.print(", Param:");
        printField(param + "ms", 7);
        out.print(", Simple:");
        printField(simple + "ms", 7);
        out.print(", TOTAL:");
        printField(total + "ms", 8);
        out.println();
    }

    @Test
    public void run() {
        out = System.out;
        out.println();
        out.println("Configuring benchmark");
        out.println("Replacing System.out with a null stream.");
        System.setOut(new PrintStream(new NullOutputStream()));
        BenchmarkInterface alog = new AlogInterface();
        BenchmarkInterface log4j2 = new Log4j2Interface();
        BenchmarkInterface slf4j = new Slf4jInterface();
        out.println();
        out.println("Warming up benchmark");
        run(alog, false);
        printField(alog,10);
        out.println(" complete.");
        run(log4j2, false);
        printField(log4j2,10);
        out.println(" complete.");
        run(slf4j, false);
        printField(slf4j,10);
        out.println(" complete.");
        out.println("Begin benchmark");
        run(alog, true);
        run(log4j2, true);
        run(slf4j, true);
        out.println("End benchmark");
        System.setOut(out);
    }

    public void run(BenchmarkInterface logger, boolean print) {
        long start = System.currentTimeMillis();
        long exception = runException(logger, ITERATIONS);
        long param = runParam(logger, ITERATIONS);
        long simple = runSimple(logger, ITERATIONS);
        long total = System.currentTimeMillis() - start;
        if (print) {
            printResults(logger, exception, param, simple, total);
        }
    }

    public static long runException(BenchmarkInterface logger, int count) {
        long start = System.currentTimeMillis();
        Exception x = new Exception();
        for (int i = 0; i < count; i++) {
            logger.log("Exception " + i, x);
        }
        return System.currentTimeMillis() - start;
    }

    public static long runParam(BenchmarkInterface logger, int count) {
        long start = System.currentTimeMillis();
        Exception x = new Exception();
        for (int i = 0; i < count; i++) {
            logger.log("Param %d", i);
        }
        return System.currentTimeMillis() - start;
    }

    public static long runSimple(BenchmarkInterface logger, int count) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            logger.log("Simple " + i);
        }
        return System.currentTimeMillis() - start;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    static class AlogInterface implements BenchmarkInterface {

        private java.util.logging.Logger log;

        public AlogInterface() {
            Alog.replaceRootHandler();
            log = java.util.logging.Logger.getLogger("Alog");
            log.setLevel(java.util.logging.Level.ALL);
            AlogBenchmark.out.println("Alog class: " + log.getClass().getName());
        }

        public void log(String message) {
            log.info(message);
        }

        public void log(String message, Throwable ex) {
            log.log(java.util.logging.Level.INFO, message, ex);
        }

        public void log(String message, Object param) {
            log.log(java.util.logging.Level.INFO, message, param);
        }

        public String toString() {
            return "Alog";
        }

    }

    public interface BenchmarkInterface {

        public void log(String message);

        public void log(String message, Throwable ex);

        public void log(String message, Object param);

    }

    static class Log4j2Interface implements BenchmarkInterface {

        private Logger log;

        public Log4j2Interface() {
            System.setProperty(
                    "Log4jContextSelector",
                    "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
            LoggerContext context = LoggerContext.getContext();
            log = context.getLogger("Log4j2");
            log.setAdditive(false);
            log.setLevel(Level.ALL);
            AlogBenchmark.out.println("Log4j2 class: " + log.getClass().getName());
        }

        public void log(String message) {
            log.info(message);
        }

        public void log(String message, Throwable ex) {
            log.log(Level.INFO, message, ex);
        }

        public void log(String message, Object param) {
            log.log(Level.INFO, message, param);
        }

        public String toString() {
            return "Log4j2";
        }

    }

    static class NullOutputStream extends OutputStream {

        public void write(byte[] b) {
        }

        public void write(byte[] b, int off, int len) {
        }

        public void write(int b) {
        }
    }

    static class Slf4jInterface implements BenchmarkInterface {

        private ch.qos.logback.classic.Logger log;

        public Slf4jInterface() {
            log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("Slf4j");
            log.detachAndStopAllAppenders();
            AsyncAppender async = new AsyncAppender();
            async.addAppender(new ConsoleAppender<ILoggingEvent>());
            async.start();
            log.addAppender(async);
            log.setAdditive(false);
            AlogBenchmark.out.println("Slf4j class: " + log.getClass().getName());
        }

        public void log(String message) {
            log.info(message);
        }

        public void log(String message, Throwable ex) {
            log.info(message, ex);
        }

        public void log(String message, Object param) {
            log.info(message, param);
        }

        public String toString() {
            return "Slf4j";
        }

    }

    // /////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
