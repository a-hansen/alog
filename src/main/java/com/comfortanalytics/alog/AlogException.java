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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An runtime exception that forwards most calls to the inner exception.
 * This is to exclude itself from reporting and expose the real issue as soon as
 * possible.
 *
 * @author Aaron Hansen
 */
class AlogException extends RuntimeException {

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public AlogException(Exception inner) {
        this.inner = inner;
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Public and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public Throwable getCause() {
        return null;
    }

    @Override
    public String getMessage() {
        return inner.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return inner.getLocalizedMessage();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return inner.getStackTrace();
    }

    @Override
    public void printStackTrace() {
        inner.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream out) {
        inner.printStackTrace(out);
    }

    @Override
    public void printStackTrace(PrintWriter out) {
        inner.printStackTrace(out);
    }

    @Override
    public Throwable initCause(Throwable cause) {
        return this;
    }

    /**
     * If the given exception is already a runtime exception, it is rethrown,
     * otherwise it will be thrown wrapped by an instance of this class.
     */
    public static void throwRuntime(Exception x) {
        if (x instanceof RuntimeException)
            throw (RuntimeException) x;
        throw new AlogException(x);
    }

    public String toString() {
        return inner.toString();
    }


    /////////////////////////////////////////////////////////////////
    // Methods - Protected and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods - Package and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods - Private and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Inner Classes - in alphabetical order by class name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Constants - in alphabetical order by field name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Attributes - in alphabetical order by field name.
    /////////////////////////////////////////////////////////////////

    private Exception inner;


    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}
