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
public class BackupTest {

    private File file = new File("test.log").getAbsoluteFile();
    private FileLogHandler handler;

    @Before
    public void setup() {
        LogManager.getLogManager().reset();
        file.delete();
        handler = FileLogHandler.getHandler(file);
    }

    @After
    public void teardown() {
        LogManager.getLogManager().reset();
        for (File f : handler.getBackups()) {
            f.delete();
        }
        file.delete();
    }

    @Test
    public void test() {
        Logger log = Alog.getLogger(getClass(), file);
        handler.setBackupThreshold(10);
        handler.setMaxBackups(2);
        for (int i = 0; i < 4; i++) {
            log.log(Level.SEVERE, "backup" + i, new Exception());
            try {
                Thread.sleep(10);
            } catch (Exception x) {
            }
            handler.waitForEmptyQueue(true);
            Assert.assertTrue(handler.backlog() == 0);
        }
        try {
            Thread.sleep(500);
        } catch (Exception x) {
        }
        File[] backups = handler.getBackups();
        Assert.assertEquals(2, backups.length);
    }

}
