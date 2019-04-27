package com.comfortanalytics.alog;

import java.io.File;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class SameFileTest {

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
        Logger log = Alog.getLogger("same", file);
        Logger another = Alog.getLogger("anothersame", file);
        Assert.assertEquals(log.getHandlers()[0], another.getHandlers()[0]);
    }

}
