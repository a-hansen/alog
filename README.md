Alog
====

* Version: 4.0.0
* JDK 1.5+
* [ISC License](https://en.wikipedia.org/wiki/ISC_license)
* [Javadoc](https://jitpack.io/com/github/a-hansen/alog/v4.0.0/javadoc/)
* For adding the dependency, visit [![](https://jitpack.io/v/a-hansen/alog.svg)](https://jitpack.io/#a-hansen/alog/+)

Overview
--------

An asynchronous log handler for JUL (Java Util Logging).  It prints log
messages on separate threads for low latency logging.

* Minimized application interference.
* Small code / jar size.
* Files will be zipped after they reach a configurable size, and the
number of zip backups will be trimmed to a configurable maximum.
* Multiple logs can safely share the same file.

There are two ways to use Alog: programmatically and with configuration
files.

Programmatic Usage
------------------

Initially acquire logs with Alog.getLogger(). It will add a log handler
only if one has not already been added.

```java
import com.comfortanalytics.alog.*;

public static void main(String[] args) {
    Logger log = Alog.getLogger("myLog", new File("myLog.log")));
    AsyncLogHandler handler = Alog.getHandler(log);
    handler.setThrottle(100);
}
```

Multiple logs can share the same file.  A single FileLogHandler will be
maintained for each absolute file path.  Be aware that once closed,
none of the other logs should use the same handler.

```java
import com.comfortanalytics.alog.*;

public static void main(String[] args) {
    Logger log = Alog.getLogger("myLog", new File("myLog.log")));
    Logger another = Alog.getLogger("anotherLog", new File("myLog.log")));
    if (log.getHandlers()[0] == another.getHandlers()[0]) {
        System.out.println("This will print.");
    }
}
```

The default log handler that prints to System.out should be replaced as
well.

```java
import com.comfortanalytics.alog.*;

public static void main(String[] args) {
    Alog.replaceRootHandler();
}
```

Configuration Files
-------------------

Alog uses configuration as specified by Java Util Logging.  See
javadoc for java.util.logging.LogManager for details.

There are two handlers:

* com.comfortanalytics.alog.FileLogHandler
* com.comfortanalytics.alog.PrintStreamLogHandler

The following keys can be used with both:

* _com.comfortanalytics.alog.filter_ is the name of a Filter class to use
(defaults to no Filter).
* _com.comfortanalytics.alog.formatter_ is the name of a Formatter class
to use (defaults to null and uses an optimized Alog format) .
* _com.comfortanalytics.alog.inferCaller_ is a boolean that
determines whether or not to infer the source class and method name
before submitting the log record for async processing (expensive, so
the default is false).
* _com.comfortanalytics.alog.level_ is the default level for the Handler
(defaults to INFO).
* _com.comfortanalytics.alog.maxQueue_ is the max async queue size above
which records are ignored (defaults to 2500, use 0 for infinite).
* _com.comfortanalytics.alog.throttle_ is the percentage (0-100) of the
maxQueue after which log records less than INFO are ignored (defaults to
90%). A value of 100 effectively disables the throttle.

The following keys can also be used with the FileLogHandler:

* _com.comfortanalytics.alog.backupThreshold_ is the approximate file
size in bytes to zip up the log file and store it with a timestamp
appended to the file name (default is 10000000 bytes).
* _com.comfortanalytics.alog.encoding_ is the charset for encoding log
files (default is "UTF-8").
* _com.comfortanalytics.alog.filename_ is the pattern for generating the
output file name. See below for details. (default is "java.log").
* _com.comfortanalytics.alog.maxBackups_ is the number of zip backups to
maintain (default is 10).

The filename pattern uses the following tokens:

* "/" represents the local pathname separator.
* "%t" is the system temporary directory.
* "%h" is the value of the "user.home" system property.
* "%%" translates to a single percent sign "%".

Example configuration for an async file handler:

```
myLog.handlers=com.comfortanalytics.alog.FileLogHandler
com.comfortanalytics.alog.level=CONFIG
com.comfortanalytics.alog.maxQueue=50000
com.comfortanalytics.alog.backupThreshold=100000000
com.comfortanalytics.alog.filename=%halog.log
com.comfortanalytics.alog.maxBackups=5
```

To replace the root handler that prints to the console with one that
does it ansynchronously:

```
handlers=com.comfortanalytics.alog.PrintStreamLogHandler
com.comfortanalytics.alog.level=FINE
com.comfortanalytics.alog.maxQueue=5000
com.comfortanalytics.alog.throttle=95
```

History
-------
_4.0.0_
  - Major refactor.
  - Proper configuration file support.
  - The close methods will not return until the associate queue is
    drained.
  - Better tests.

_3.0.0_
  - Made default settings configurable through LogManager properties.
  - Made the throttle configurable and changed the default to 90%.
  - Changed the default max queue to 25K.
  - Ensure messages with potentially mutable parameters are formatted synchronously.
  - Now uses JMH for the benchmark.
  
_2.0.2_
  - Removed idea and findbugs from the gradle script.
  
_2.0.1_
  - Benchmark rework.
  - Added the gradle wrapper.
  
_2.0.0_
  - Package change.
  
_1.1.0_
  - Formatting fixes / changes.
  - Added a max queue size.
  - If the queue is 75% full, records less than INFO will be discarded.
  - True jdk 1.5 compatibility.
  - Changed the async thread sleep/wait interval.
  - Added other logging framework benchmarks to the unit tests for comparison.

_1.0.0_
  - Hello World.
