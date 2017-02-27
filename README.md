Alog
====

* Version: 2.0.1
* JDK 1.5+
* [ISC License](https://en.wikipedia.org/wiki/ISC_license)
* [Javadoc](https://a-hansen.github.io/alog/)


Overview
--------

An asynchronous log handler for JUL (Java Util Logging).  It processes log messages
on separate threads for low latency logging.

* Fast.  Minimum application interference.
* Async handlers for java.io.PrintStream and java.io.File.
* Files will be zipped after they exceed a certain size, and the number of zip backups 
  will be trimmed to a maximum.
* Multiple logs can safely share the same file.
* Extremely permissive [license](https://en.wikipedia.org/wiki/ISC_license).

Usage
-----

Initially acquire logs with Alog.getLogger(). It will add a log handler only if one 
has not already been added.

```java
import com.comfortanalytics.alog.*;

public static void main(String[] args) {
    Logger log = Alog.getLogger("myLog", new File("myLog")));
}
```

You can always create a handler and add it yourself.

```java
import com.comfortanalytics.alog.*;

public static void main(String[] args) {
      FileLogHandler handler = FileLogHandler.getHandler(new File("myLog.log"));
      Logger logger = Logger.getLogger("myLog");
      logger.addHandler(handler);
}
```

Multiple logs can share the same file.  A single FileLogHandler will be maintained for 
each absolute file path.

```java
import com.comfortanalytics.alog.*;

public static void main(String[] args) {
    Logger log = Alog.getLogger("myLog", new File("myLog")));
    Logger another = Alog.getLogger("anotherLog", new File("myLog")));
    if (log.getHandlers()[0] == another.getHandlers()[0]) {
        System.out.println("This will print.");
    }
}
```

The default log handler that prints to System.out can and should be replaced as well.

```java
import com.comfortanalytics.alog.*;

public static void main(String[] args) {
    Alog.replaceRootHandler();
}
```

[Alogger.java.txt](https://github.com/a-hansen/alog/blob/master/src/main/java/com.comfortanalytics/alog/Alogger.java.txt) 
is a Java 8 interface that could be used for efficiency and convenience.

Performance
----------

Alog is faster than Log4j2 and SLF4J in terms of application latency.  In other 
words application threads will spend less time logging with Alog.  

Tests include a benchmark for comparing Alog with async versions of Log4j2 and 
SLF4J (with Logback).  I can't explain why Log4j2 is so slow, I could be doing something
 wrong, but that api is impossible.

Run the benchmark with the gradle wrapper:

```
gradlew benchmark
```

Don't run the benchmark task from within your IDE, it'll probably double print the 
output.  Just run all tests, or AlogBenchmark specifically.


History
-------
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
