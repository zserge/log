# Ultimately minimal (yet very convenient) logger for Android and Java.

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-log-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1958)
[![Build Status](https://travis-ci.org/zserge/log.svg?branch=master)](https://travis-ci.org/zserge/log)

Log is a super-simple drop-in replacement for `android.util.Log` that can also
be used in normal Java projects.

## Features

```java

// API is backwards-compatible with android.util.Log, so you already know it.
Log.d(tag, "X equals " + x);

// Safe logging for multiple comma-separated values
Log.d(tag, "X", x)

// Tag is optional, by default it equals to the class name 
class Foo {
	public void foo() {
		Log.d("Hello"); // prints 'D/Foo: Hello'
	}
}

// If tag or TAG field is found - it overrides the class name
class Foo {
	private final static String tag = "Bar";
	public void foo() {
		Log.d("Hello"); // prints 'D/Bar: Hello'
	}
}

// And it's still compatible with your old code
class Foo {
	private final static String TAG = "Bar";
	public void foo() {
		Log.d(TAG, "Hello"); // prints 'D/Bar: Hello', not 'D/Bar: Bar Hello'
	}
}

// Even if you used different tag field names
class Foo {
	static {
		Log.useTags(new String[]{"tag", "TAG", "MYTAG", "_TAG", "iLoveLongFieldName"});
	}
	private final static String _TAG = "Bar";
	...
}

// Support for Throwables
Exception e = new Exception("foo");
Log.d("Something bad happened", someObject, "error:", e); // A log message and a stack trace will be printed

// Chainable API
Log
	.d("First")
	.d("Second")
	.d("Third line")

// Filters by log level
Log.level(Log.I);
Log.d("foo"); // will be ignored

// Format strings are suported, too
Log.useFormat(true);
Log.d("X equals %d", x); // prints 'X equals 42'
// But if no format is provided - log will be printed as multiple values
Log.d("Value of X", x); // prints 'Value of X 42'

// Long messages are wrapped on newlines
// If the message doesn't contain newlines and is longer than 4000 symbols
// (like compact JSONs or HTML) - it will be wrapped on whitespace or punctuation
Log.d("Hello\nworld"); // prints 'D/SomeTag: Hello' and 'D/SomeTag: world'

// On Android logs are printed via android.util.Log by default.
// On other JVMs logs are printed via System.out.println by default.
// Unless specified otherwise
Log.usePrintln(true).useLog(false).d("hello"); // will be printed via System.out on Android as well

// And it's only a single class of 200 LOC, so don't be afraid that it will
// bloat your APK or slow down your builds.
```

## Installation

build.gradle:

``` gradle
repositories {
	jcenter() // mavenCentral() would work, too
}
dependencies {
	compile 'co.trikita:log:1.1.1'
}
```
## Migration from android.util.Log

If you already have a big project and want to use this logger right now - you
can just change imports using 'sed' (Linux/Mac/Cygwin):

``` bash
$ find -name "*.java" -type f -exec sed -i 's/import android.util.Log/import trikita.log.Log/g' {} \;
```

If you're using Android Studio: in the project pane right-click on the `app/java`
directory (or any other directory containing your java classes). In the popup
menu select "Replace in Path..." option.

'Text to find' should be "android.util.Log".
'Replace with' should be "trikita.log.Log".
Click Find, click All files.

If you know an easier way (or for different IDE) - let me know.

## How is it different from...

Here is a list of other popular loggers for Android: https://android-arsenal.com/tag/57

Most of them are too specific (logging to file or socket, or printing logs on screen).
Some of them are too complicated (Twig, SLF4J and every other logger that has factories).
But some of them are really nice, so that my logger was inspired by them.

These loggers are simple and put class and method names into the log message automatically.
However they only support a single string parameter so you will end up with 
`"X = "+x+", y="+y` in most of your logs.

* https://gist.github.com/shkschneider/4a9849468b80deb172a9 
* https://github.com/MustafaFerhan/DebugLog

This two are very similar to my logger, but the first one is a bit bloated and
the both don't support comma-separated values, only format-like strings:

* https://github.com/noties/Debug
* https://github.com/liaohuqiu/android-CLog

And of course there are Jake Wharton's hugo and timber. Hugo is nice, but I
didn't need to log/profile my methods, I only need to print log messages from time to
time.

Timber was my favourite choice, but I don't use it because of the name. I
understand that it's really clever, but my hands are too much used to start log
lines with the letter "L", not "T". And timber also doesn't support
comma-separated values, only format strings.

## License

Code is distributed under MIT License
