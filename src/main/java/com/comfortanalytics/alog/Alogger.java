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

import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.util.logging.Logger;

/**
 * Logging convenience that enables shorter statements using ternary expressions for
 * efficiency.
 * <p>
 * Without this interface:
 * <p>
 * <code>if (log.isLoggable(FINEST)) log.finest(complexString());</code>
 * <p>Or,<p>
 * <code>log.finest(log.isLoggable(FINEST) ? complexString() : null);</code>
 * <p>
 * With this interface:
 * <p>
 * <code>finest(finest() ? complexString() : null);</code>
 *
 * <p>
 * If using Java 8, see the alternate interface Alogger.java8
 *
 * @author Aaron Hansen
 */
public abstract class Alogger {

    /**
     * True if level is loggable.
     */
    public boolean config() {
        return getLogger().isLoggable(CONFIG);
    }

    public void config(Object msg) {
        getLogger().log(CONFIG, str(msg));
    }

    public void config(Object msg, Throwable x) {
        getLogger().log(CONFIG, str(msg), x);
    }

    /**
     * True if level is loggable.
     */
    public boolean fine() {
        return getLogger().isLoggable(FINE);
    }

    public void fine(Object msg) {
        getLogger().log(FINE, str(msg));
    }

    public void fine(Object msg, Throwable x) {
        getLogger().log(FINE, str(msg), x);
    }

    /**
     * True if level is loggable.
     */
    public boolean finer() {
        return getLogger().isLoggable(FINER);
    }

    public void finer(Object msg) {
        getLogger().log(FINER, str(msg));
    }

    public void finer(Object msg, Throwable x) {
        getLogger().log(FINER, str(msg), x);
    }

    /**
     * True if level is loggable.
     */
    public boolean finest() {
        return getLogger().isLoggable(FINEST);
    }

    public void finest(Object msg) {
        getLogger().log(FINEST, str(msg));
    }

    public void finest(Object msg, Throwable x) {
        getLogger().log(FINEST, str(msg), x);
    }

    public abstract Logger getLogger();

    /**
     * True if level is loggable.
     */
    public boolean info() {
        return getLogger().isLoggable(INFO);
    }

    public void info(Object msg) {
        getLogger().log(INFO, str(msg));
    }

    public void info(Object msg, Throwable x) {
        getLogger().log(INFO, str(msg), x);
    }

    /**
     * True if level is loggable.
     */
    public boolean severe() {
        return getLogger().isLoggable(SEVERE);
    }

    public void severe(Object msg) {
        getLogger().log(SEVERE, str(msg));
    }

    public void severe(Object msg, Throwable x) {
        getLogger().log(SEVERE, str(msg), x);
    }

    /**
     * True if level is loggable.
     */
    public boolean warn() {
        return getLogger().isLoggable(WARNING);
    }

    public void warn(Object msg) {
        getLogger().log(WARNING, str(msg));
    }

    public void warn(Object msg, Throwable x) {
        getLogger().log(WARNING, str(msg), x);
    }

    private static String str(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

}
