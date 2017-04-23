Alog
====

* Version: 3.0.0
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

Warning
-------

There are several issues to be aware of before using this.

* Messages still on the queue at shutdown will be lost.
* By default, when the record queue is full, new records are ignored.
* By default, when the record queue is 90% full, records finer than INFO are ignored.

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

Performance
----------

On average, JUL + Alog seems to be a little faster than async Logback and a lot faster than 
async Log4j2 in terms of application latency. In other words application threads will spend less 
time submitting log records with JUL + Alog. Unit tests include a JMH benchmark for comparison.

The Log4j2 performance is suspiciously slow.  No matter what I try, it does not change, but I
only have so much time to dink around with it.  Anyone know how to make a programmatic async 
Log4j2 logger that simply drops records?

Run the benchmark with the gradle wrapper:

```
gradlew benchmark
```

The lower the Score the better.

There are 9 benchmarks and it'll take about a minute to run them all.  I trimmed benchmark 
configuration to finish a quickly as possible for the casual observer.  Adding more iterations, 
threads, forks, or whatever shouldn't really change anything.

Don't run the benchmark gradle task from within your IDE, it'll double print the output.  Run 
AlogBenchmark specifically.


History
-------
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
