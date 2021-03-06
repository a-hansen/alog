package com.comfortanalytics.alog;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class AlogTest {

    private File file = new File("test.log");

    @BeforeClass
    public void setup() {
        LogManager.getLogManager().reset();
    }

    @AfterClass
    public void teardown() {
        LogManager.getLogManager().reset();
        file.delete();
    }

    @Test
    public void test() {
        Logger log = Alog.getLogger(getClass(), file);
        FileLogHandler handler = FileLogHandler.getHandler(file);
        Assert.assertEquals(log.getHandlers()[0], handler);
        log = Alog.rootLogger();
        Alog.replaceRootHandler();
        Assert.assertTrue(log.getHandlers()[0] instanceof PrintStreamLogHandler);
        Alog.addToRootLogger(file);
        Assert.assertEquals(log.getHandlers()[1], handler);

        log = Alog.getLogger(getClass());
        log.log(Level.INFO, "Just for viewing the format", new Exception());
        try {
            Thread.sleep(50);
        } catch (Exception x) {
        }
    }

}
