Alog
====

* Date: Jan 29, 2017
* Version: 1.1.0
* Author: Aaron hansen
* [Javadocs](https://a-hansen.github.io/alog/)


Overview
--------

A simple asynchronous log handler for JUL (Java Util Logging).  It uses separate threads 
to process log messages for improved application performance.

The following are notable features:

* Fast, minimal application interference.
* Async handlers for java.io.PrintStream and java.io.File.
* Files will be zipped after they exceed a certain size, and the number of zip backups 
  will be trimmed to a maximum.
* Multiple logs can safely share the same file.

Requirements
------------

Java 1.5 or higher is required.

Usage
-----

Initially acquire logs with Alog.getLogger(). It will add a log handler only if one 
has not already been added.

```java
import com.ca.alog.*;

public static void main(String[] args) {
    Logger log = Alog.getLogger("myLog", new File("myLog")));
}
```

Multiple logs can share the same file.  A single FileLogHandler will be maintained for 
each absolute file path.

```java
import com.ca.alog.*;

public static void main(String[] args) {
    Logger log = Alog.getLogger("myLog", new File("myLog")));
    Logger another = Alog.getLogger("anotherLog", new File("myLog")));
    if (log.getHandlers()[0] == another.getHandlers()[0]) {
        System.out.println("This will print.");
    }
}
```

The default log handler that prints to System.out can be replaced as well.

```java
import com.ca.alog.*;

public static void main(String[] args) {
    Alog.replaceRootHandler();
}
```

[Alogger.java.txt](https://github.com/a-hansen/alog/blob/master/src/main/java/com/ca/alog/Alogger.java.txt) 
is an example Java 8 interface that can be used as an efficiency and convenience.

Benchmarks
----------

Unit testing includes benchmarks for comparing async versions of JUL, Log4j and 
SLF4J (with Logback).


History
-------
_1.1.0 - 2017-1-29_
  - Formatting fixes / changes.
  - Added a max queue size.
  - If the queue is 75% full, records less than INFO will be discarded.
  - True jdk 1.5 compatibility.
  - Changed the async thread sleep/wait interval.
  - Added other logging framework benchmarks to the unit tests for comparison.

_1.0.0 - 2017-1-15_
  - Hello World.
