package com.comfortanalytics.alog;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class AlogTest {

    private File file = new File("test.log");

    @Before
    public void setup() {
        LogManager.getLogManager().reset();
    }

    @After
    public void teardown() {
        LogManager.getLogManager().reset();
        file.delete();
    }

    @Test
    public void test() {
        Logger log = Alog.getLogger(getClass(), file);
        FileLogHandler handler = FileLogHandler.getHandler(file);
        Assert.assertEquals(log.getHandlers()[0], handler);
        Alog.replaceRootHandler(file);
        log = Alog.rootLogger();
        Assert.assertEquals(log.getHandlers()[0], handler);
        Alog.replaceRootHandler();
        Assert.assertTrue(log.getHandlers()[0] instanceof PrintStreamLogHandler);

        log = Alog.getLogger(getClass());
        Alog.getHandler(log).setInferCaller(true);
        log.log(Level.INFO, "Just for viewing the format", new Exception());
        try {
            Thread.sleep(50);
        } catch (Exception x) {
        }
    }

}
