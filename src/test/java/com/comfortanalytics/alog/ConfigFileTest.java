package com.comfortanalytics.alog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class ConfigFileTest {

    String config = "testLogger.handlers=com.comfortanalytics.alog.FileLogHandler\n" +
            "com.comfortanalytics.alog.level=CONFIG\n" +
            "com.comfortanalytics.alog.maxQueue=50000\n" +
            "com.comfortanalytics.alog.formatter=java.util.logging.SimpleFormatter\n" +
            "com.comfortanalytics.alog.throttle=85\n" +
            "com.comfortanalytics.alog.filename=foobar.log\n" +
            "com.comfortanalytics.alog.maxBackups=5\n" +
            "com.comfortanalytics.alog.inferCaller=true\n" +
            "com.comfortanalytics.alog.backupThreshold=100\n";

    @Before
    public void setup() {
        LogManager.getLogManager().reset();
    }

    @After
    public void teardown() {
        LogManager.getLogManager().reset();
    }

    @Test
    public void test() throws Exception {
        LogManager mgr = LogManager.getLogManager();
        mgr.readConfiguration(new ByteArrayInputStream(config.getBytes("UTF-8")));
        Logger log = Logger.getLogger("testLogger");
        FileLogHandler handler = FileLogHandler.getHandler(new File("foobar.log"));
        Assert.assertEquals(handler, log.getHandlers()[0]);
        Assert.assertTrue(handler.getLevel() == Level.CONFIG);
        Assert.assertTrue(handler.getMaxQueueSize() == 50000);
        Assert.assertTrue(handler.getFormatter() instanceof SimpleFormatter);
        Assert.assertTrue(handler.getThrottle() == 85);
        Assert.assertTrue(handler.getMaxBackups() == 5);
        Assert.assertTrue(handler.getInferCaller());
        Assert.assertTrue(handler.getBackupThreshold() == 100);
    }

}
