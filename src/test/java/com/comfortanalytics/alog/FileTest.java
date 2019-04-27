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
public class FileTest {

    private File file = new File("test.log").getAbsoluteFile();

    @BeforeClass
    public void setup() {
        LogManager.getLogManager().reset();
        file.delete();
    }

    @AfterClass
    public void teardown() {
        LogManager.getLogManager().reset();
        file.delete();
    }

    @Test
    public void test() {
        Logger log = Alog.getLogger("filetest", file);
        FileLogHandler handler = FileLogHandler.getHandler(file);
        log.log(Level.SEVERE, "Hello World", new Exception());
        try {
            Thread.sleep(5);
        } catch (Exception x) {
        }
        handler.waitForEmptyQueue(true);
        handler.flush();
        Assert.assertEquals(handler.getFile(), file);
        waitForExists(file);
        Assert.assertTrue(file.exists());
    }

    private void waitForExists(File file) {
        long start = System.currentTimeMillis();
        while (!file.exists()) {
            try {
                Thread.sleep(100);
            } catch (Exception x) {
            }
            if ((System.currentTimeMillis() - start) > 5000) {
                return;
            }
        }
    }

}
