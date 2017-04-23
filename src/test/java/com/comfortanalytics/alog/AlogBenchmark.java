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

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.SimpleFormatter;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.LoggerFactory;

/**
 * Benchmarks how much time applications spend submitting log records to various async
 * log handlers.
 *
 * @author Aaron Hansen
 */
public class AlogBenchmark {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    static int counter; //used to monitor the Logback queue size
    static Exception exception = new Exception();
    static UUID param = UUID.randomUUID();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void run() throws Exception {
        //Add more iterations, forks, etc, results should be similar or better.  At least they
        //were on my machine.
        Options opt = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                //.mode(Mode.Throughput)
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .threads(10)
                .shouldDoGC(true)
                .jvmArgs("")
                .build();
        new Runner(opt).run();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    @State(Scope.Benchmark)
    public static class Alog {

        private java.util.logging.Logger log;

        @Benchmark
        public void simple() {
            counter++;
            log.severe("Simple Message");
        }

        @Benchmark
        public void exception() {
            counter++;
            log.log(java.util.logging.Level.SEVERE, "Message", exception);
        }

        @Benchmark
        public void parameter() {
            counter++;
            log.log(java.util.logging.Level.SEVERE, "Parameter {0}", param);
        }

        @Setup(Level.Iteration)
        public void start() {
            counter = 0;
            if (log == null) {
                com.comfortanalytics.alog.Alog.DEFAULT_MAX_QUEUE = -1; //ignore no messages
                log = com.comfortanalytics.alog.Alog.getLogger(
                        "Alog", new PrintStream(new NullOutputStream()));
                log.getHandlers()[0].setFormatter(new SimpleFormatter());
                log.setUseParentHandlers(false);
            }
        }

        @TearDown(Level.Iteration)
        public void stop() {
            //We're NOT testing the time it take to process the log record, only how much
            //time an application spends submitting them.
            ((AsyncLogHandler) log.getHandlers()[0]).clearBacklog();
        }
    }

    @State(Scope.Benchmark)
    public static class Log4j2 {

        private org.apache.logging.log4j.core.Logger log;

        @Benchmark
        public void simple() {
            counter++;
            log.error("Simple Message");
        }

        @Benchmark
        public void exception() {
            counter++;
            log.error("Message", exception);
        }

        @Benchmark
        public void parameter() {
            counter++;
            log.error("Parameter {}", param);
        }

        @Setup
        public void start() {
            counter = 0;
            if (log == null) {
                System.setProperty(
                        "Log4jContextSelector",
                        "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
                LoggerContext context = LoggerContext.getContext();
                log = context.getLogger("Log4j2");
                log.setAdditive(false);
                ArrayList<Appender> list = new ArrayList<Appender>();
                list.addAll(log.getAppenders().values());
                for (Appender a : list) {
                    log.removeAppender(a);
                }
            }
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

    @State(Scope.Benchmark)
    public static class Logback {

        private ch.qos.logback.classic.Logger log;
        private int queueSize = 10000000;

        @Benchmark
        public void simple() {
            counter++;
            log.error("Simple Message");
        }

        @Benchmark
        public void exception() {
            counter++;
            log.error("Simple Message", exception);
        }

        @Benchmark
        public void parameter() {
            counter++;
            log.error("Parameter {}", param);
        }

        @Setup(Level.Iteration)
        public void start() {
            if (counter > queueSize) {
                System.out.println("******************* NEED A LARGER QUEUE! ******************");
                queueSize = (int) (counter * 1.1);
            }
            counter = 0;
            log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("Logback");
            log.detachAndStopAllAppenders();
            log.setAdditive(false);
            OutputStreamAppender<ILoggingEvent> out = new OutputStreamAppender<ILoggingEvent>();
            out.setOutputStream(new NullOutputStream());
            out.start();
            AsyncAppender async = new AsyncAppender();
            //The following needs to be larger than the max calls per iteration.
            async.setQueueSize(queueSize);
            async.setNeverBlock(true);
            async.setDiscardingThreshold(0);
            async.addAppender(out);
            async.start();
            log.addAppender(async);
        }

    }

    // /////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
