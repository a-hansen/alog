package com.comfortanalytics.alog;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A runtime exception wrapper that forwards most calls to the inner exception.
 *
 * @author Aaron Hansen
 */
class AlogException extends RuntimeException {

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    private Exception inner;

    /////////////////////////////////////////////////////////////////
    // Methods - Public and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    public AlogException(Exception inner) {
        this.inner = inner;
    }

    @Override
    public Throwable getCause() {
        return null;
    }

    @Override
    public String getLocalizedMessage() {
        return inner.getLocalizedMessage();
    }

    @Override
    public String getMessage() {
        return inner.getMessage();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return inner.getStackTrace();
    }

    @Override
    public Throwable initCause(Throwable cause) {
        return this;
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

    /**
     * If the given exception is already a runtime exception, it is rethrown,
     * otherwise it will be thrown wrapped by an instance of this class.
     */
    public static void throwRuntime(Exception x) {
        if (x instanceof RuntimeException) {
            throw (RuntimeException) x;
        }
        throw new AlogException(x);
    }

    public String toString() {
        return inner.toString();
    }


}
