package com.comfortanalytics.alog;

import java.io.ByteArrayInputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class ConfigRootTest {

    String config = "handlers=com.comfortanalytics.alog.PrintStreamLogHandler\n" +
            "com.comfortanalytics.alog.level=CONFIG\n" +
            "com.comfortanalytics.alog.maxQueue=50000\n" +
            "com.comfortanalytics.alog.formatter=java.util.logging.SimpleFormatter\n" +
            "com.comfortanalytics.alog.inferCaller=true\n" +
            "com.comfortanalytics.alog.throttle=85\n";

    @BeforeClass
    public void setup() {
        LogManager.getLogManager().reset();
        Logger root = Alog.rootLogger();
        for (Handler h : root.getHandlers()) {
            root.removeHandler(h);
        }
    }

    @AfterClass
    public void teardown() {
        LogManager.getLogManager().reset();
    }

    @Test
    public void test() throws Exception {
        LogManager mgr = LogManager.getLogManager();
        mgr.readConfiguration(new ByteArrayInputStream(config.getBytes("UTF-8")));
        Logger log = Alog.rootLogger();
        AsyncLogHandler handler = Alog.getAsyncHandler(log);
        Assert.assertTrue(handler.getLevel() == Level.CONFIG);
        Assert.assertTrue(handler.getMaxQueueSize() == 50000);
        Assert.assertTrue(handler.getFormatter() instanceof SimpleFormatter);
        Assert.assertTrue(handler.getInferCaller());
        Assert.assertTrue(handler.getThrottle() == 85);
    }

}
