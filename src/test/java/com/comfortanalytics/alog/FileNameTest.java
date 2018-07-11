package com.comfortanalytics.alog;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class FileNameTest {

    @Test
    public void test() throws Exception {
        File file = FileLogHandler.makeFile("foobar.log");
        Assert.assertEquals("foobar.log", file.getName());
        //file = FileLogHandler.makeFile("%hfoobar.log");
        file = FileLogHandler.makeFile("foobar%%.log");
        Assert.assertEquals("foobar%.log", file.getName());
        file = FileLogHandler.makeFile("%tfoobar.log");
        String tmpDir = System.getProperty("java.io.tmpdir");
        File tmp = new File(tmpDir);
        tmp = new File(tmp, "foobar.log");
        Assert.assertEquals(tmp.getAbsolutePath(), file.getAbsolutePath());
    }

}
