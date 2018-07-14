/**
 An asynchronous log handler for JUL (Java Util Logging).  It prints log
 messages on separate threads for low latency logging.
 <p>
 There are two ways to use Alog: programmatically and with configuration
 files.

 <p>
 <b>Programmatic Usage</b>
 <p>

 Initially acquire logs with Alog.getLogger(). It will add a log handler
 only if one has not already been added.
 <p>
 <pre>
 public static void main(String[] args) {
    Logger log = Alog.getLogger("myLog", new File("myLog.log")));
    AsyncLogHandler handler = Alog.getHandler(log);
    handler.setThrottle(100);
 }
 </pre>
 <p>

 Multiple logs can share the same file.  A single FileLogHandler will be
 maintained for each absolute file path.  Be aware that once closed,
 none of the other logs can use the same handler.
 <p>
 <pre>
 public static void main(String[] args) {
    Logger log = Alog.getLogger("myLog", new File("myLog.log")));
    Logger another = Alog.getLogger("anotherLog", new File("myLog.log")));
    if (log.getHandlers()[0] == another.getHandlers()[0]) {
        System.out.println("This will print.");
    }
 }
 </pre>
 <p>

 The root log handler that prints to System.out should be replaced as
 well.
 <p>
 <pre>
 public static void main(String[] args) {
    Alog.replaceRootHandler();
 }
 </pre>
 <p>

 <b>Configuration Files</b>
 <p>
 Alog uses configuration as specified by Java Util Logging.  See
 javadoc for java.util.logging.LogManager for details.
 <p>
 There are two handlers:
 <ul>
 <li>com.comfortanalytics.alog.FileLogHandler
 <li>comfortanalytics.alog.PrintStreamLogHandler
 </ul>

 The following keys can be used with both:
 <ul>
 <li>com.comfortanalytics.alog.filter</li> is the name of a Filter class to use
 (defaults to no Filter).
 <li>com.comfortanalytics.alog.formatter</li> is the name of a Formatter class
 to use (defaults to null and uses an optimized Alog format) .
 <li>com.comfortanalytics.alog.inferCaller</li> is a boolean that
 determines whether or not to infer the source class and method name
 before submitting the log record for async processing (expensive, so
 the default is false).
 <li>com.comfortanalytics.alog.level</li> is the default level for the Handler
 (defaults to INFO).
 <li>com.comfortanalytics.alog.maxQueue</li> is the max async queue size above
 which records are ignored (defaults to 2500, use 0 for infinite).
 <li>com.comfortanalytics.alog.throttle</li> is the percentage (0-100) of the
 maxQueue after which log records less than INFO are ignored (defaults to
 90%). A value of 100 effectively disables the throttle.
 </ul>

 The following keys can also be used with the FileLogHandler:
 <ul>
 <li>com.comfortanalytics.alog.backupThreshold</li> is the approximate file
 size in bytes to zip up the log file and store it with a timestamp
 appended to the file name (default is 10000000 bytes).
 <li>com.comfortanalytics.alog.encoding</li> is the charset for encoding log
 files (default is "UTF-8").
 <li>com.comfortanalytics.alog.filename</li> is the pattern for generating the
 output file name. See below for details. (default is "java.log").
 <li>com.comfortanalytics.alog.maxBackups</li> is the number of zip backups to
 maintain (default is 10).
 </ul>
 The filename pattern uses the following tokens:
 <ul>
 <li>"/" represents the local pathname separator.
 <li>"%t" is the system temporary directory.
 <li>"%h" is the value of the "user.home" system property.
 <li>"%%" translates to a single percent sign "%".
 </ul>

 Example configuration for an async file handler:
 <p>
 <pre>
 myLog.handlers=com.comfortanalytics.alog.FileLogHandler
 com.comfortanalytics.alog.level=CONFIG
 com.comfortanalytics.alog.maxQueue=50000
 com.comfortanalytics.alog.backupThreshold=100000000
 com.comfortanalytics.alog.filename=%halog.log
 com.comfortanalytics.alog.maxBackups=5
 </pre>
 <p>
 To replace the root handler that prints to the console with one that
 does it ansynchronously:
 <p>
 <pre>
 handlers=com.comfortanalytics.alog.PrintStreamLogHandler
 com.comfortanalytics.alog.level=FINE
 com.comfortanalytics.alog.maxQueue=5000
 com.comfortanalytics.alog.throttle=95
 </pre>


 @author Aaron Hansen

 */
package com.comfortanalytics.alog;

