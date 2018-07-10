/**
 * Async log handling for Java Util Logging.  Prints log messages on seperate threads for
 * reduced application logging latency.
 * <p>
 * The following are notable features:
 * <ul>
 * <li> Async handlers for PrintStreams and Files.
 * <li> Multiple logs can safely share the same file.
 * <li> Files will be zipped up after they exceed a certain size, and the number of
 * backups will to trimmed to a maximum.
 * </ul>
 * <p>
 * <b>Usage</b>
 * <p>
 * Acquire logs with Alog.getLogger(). It will add a log handler only if one hasn't
 * already been added.
 * <p>
 * <pre>
 * import com.comfortanalytics.alog.*;
 *
 * public static void main(String[] args) {
 *     Logger log = Alog.getLogger("myLog", new File("myLog")));
 * }
 * </pre>
 * <p>
 * Multiple logs can share the same file.  A single FileLogHandler will be maintained
 * for each absolute file path.
 * <p>
 * <pre>
 * import com.comfortanalytics.alog.*;
 *
 * public static void main(String[] args) {
 *     Logger log = Alog.getLogger("myLog", new File("myLog")));
 *     Logger another = Alog.getLogger("anotherLog", new File("myLog")));
 * }
 * </pre>
 * <p>
 * The root log handler that prints to System.out can easily be replaced as well.
 * <p>
 * <pre>
 * import com.comfortanalytics.alog.*;
 *
 * public static void main(String[] args) {
 *     Alog.replaceRootHandler();
 * }
 * </pre>
 *
 * @author Aaron Hansen
 */
package com.comfortanalytics.alog;

