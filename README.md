Alog
====

* Date: Jan 15, 2017
* Version: 1.0.0
* Author: Aaron hansen


Overview
--------

An async log handler for Java Util Logging.  It uses separate threads to print
log messages for improved application performance.

The following are notable features:

* Async handlers for java.io.PrintStream and java.io.File.
* Multiple logs can safely share the same file.
* Files will be zipped up after they exceed a certain size, and the number
  of backups will to trimmed to a maximum.

Requirements
------------

Java 1.5 or higher is required.

Usage
-----

Acquire logs with Alog.getLogger(). It will add a log handler only if one hasn't already
 been added.

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
}
```

The default log handler that prints to System.out can be replaced as well.

```java
import com.ca.alog.*;

public static void main(String[] args) {
    Alog.replaceRootHandler();
    Logger log = Alog.getLogger("myLog", new File("myLog")));
}
```

History
-------
_1.0.0 - 2017-1-15_
  - Hello World.
