/* Copyright 2017 by Aaron Hansen.
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

package com.ca.alog;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class AlogTests {

    static Logger log;

    static {
        Alog.replaceRootHandler();
        log = Alog.getLogger("test", new File("test.log"));
        log.setLevel(Level.ALL);
    }

    private void aMethod() {
        log.entering("AlogTests", "aMethod");
        log.exiting("AlogTests", "aMethod");
    }

    @Test
    public void test1() throws Exception {
        Logger.getLogger("").info("My first log");
        log.info("My second log");
        aMethod();
    }

    @Test
    public void test2() throws Exception {
        FileLogHandler handler = (FileLogHandler) log.getHandlers()[0];
        handler.flush();
        handler.setMaxBackups(2);
        handler.makeBackup();
        for (File f : handler.getBackups()) {
            f.delete();
        }
        for (int i = 0; i < 4; i++) {
            log.log(Level.SEVERE, "backup" + i, new Exception());
            Thread.sleep(1100);
            handler.flush();
            handler.makeBackup();
        }
        handler.trimBackups();
        File[] backups = handler.getBackups();
        Assert.assertTrue(backups.length == 2);
    }

    @Test
    public void test3() throws Exception {
        Logger another = Alog.getLogger("another", new File("test.log"));
        Assert.assertTrue(log.getHandlers()[0] == another.getHandlers()[0]);
    }


}
