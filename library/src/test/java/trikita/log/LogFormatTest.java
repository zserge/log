package trikita.log;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.ArrayDeque;
import java.io.PrintStream;
import java.io.OutputStream;

public class LogFormatTest {

	public static class QueuePrintStream extends PrintStream {
		private ArrayDeque<String> mLogs = new ArrayDeque<>();

		public QueuePrintStream(OutputStream out) {
			super(out);
		}

		public void println(String s) {
			mLogs.add(s);
		}

		public String pop() {
			return mLogs.poll();
		}
	}

	static QueuePrintStream mLogQueue = new QueuePrintStream(System.out);

	static {
		System.setOut(mLogQueue);
	}

	@Before
	public void setup() {
		Log.usePrintln(true);
	}

	@Test
	public void testSimple() {
		// Check that log queue wrapper works
		Log.d("hello");
		assertEquals("D/LogFormatTest: hello", mLogQueue.pop());
	}

	@Test
	public void testCommaSeparated() {
		// Check that comma-separated values become tab-separated when printed
		Log.d("a", "b", 1, true, null);
		assertEquals("D/LogFormatTest: a\tb\t1\ttrue\tnull", mLogQueue.pop());

		Log.d(1);
		assertEquals("D/LogFormatTest: 1", mLogQueue.pop());
	}

	@Test
	public void testNull() {
		// Check that null can be passed as an argument(s) and null objects are handled correctly
		Object a = null;
		Object b = null;
		Log.d(null);
		assertEquals("D/LogFormatTest: null", mLogQueue.pop());
		Log.d(a);
		assertEquals("D/LogFormatTest: null", mLogQueue.pop());
		Log.d(a, b);
		assertEquals("D/LogFormatTest: null\tnull", mLogQueue.pop());
		Log.d(a, null); // XXX in normal code it should be Log.d(null, (Object) null);
		assertEquals("D/LogFormatTest: null\tnull", mLogQueue.pop());
		Log.d(null, null); // XXX same as above
		assertEquals("D/LogFormatTest: null\tnull", mLogQueue.pop());
		Log.d(null, null, null);
		assertEquals("D/LogFormatTest: null\tnull\tnull", mLogQueue.pop());
		Log.d(null, a);
		assertEquals("D/LogFormatTest: null\tnull", mLogQueue.pop());
	}

	@Test
	public void testLogLevels() {
		// Check that level is added to the printed message
		Log.v("a");
		assertEquals("V/LogFormatTest: a", mLogQueue.pop());
		Log.d("b");
		assertEquals("D/LogFormatTest: b", mLogQueue.pop());
		Log.i("c");
		assertEquals("I/LogFormatTest: c", mLogQueue.pop());
		Log.w("d");
		assertEquals("W/LogFormatTest: d", mLogQueue.pop());
		Log.e("e");
		assertEquals("E/LogFormatTest: e", mLogQueue.pop());

		Log.level(Log.I);

		// Check that logs below the given level are ignored
		Log.v("a");
		Log.d("b");
		Log.i("c");
		assertEquals("I/LogFormatTest: c", mLogQueue.pop());
		Log.w("d");
		assertEquals("W/LogFormatTest: d", mLogQueue.pop());
		Log.e("e");
		assertEquals("E/LogFormatTest: e", mLogQueue.pop());

		Log.level(Log.V);

		// Check that log level can be restored and logs will be printed normally then
		Log.v("a");
		assertEquals("V/LogFormatTest: a", mLogQueue.pop());
	}

	@Test
	public void testFormat() {
		Log.useFormat(true);
		// Check that format strings are autodetected
		Log.d("number %d", 42);
		assertEquals("D/LogFormatTest: number 42", mLogQueue.pop());

		Log.d("just %%");
		assertEquals("D/LogFormatTest: just %", mLogQueue.pop());

		// Check that comma-separated values still work if no format string is detected
		Log.d("a", "b", "c");
		assertEquals("D/LogFormatTest: a\tb\tc", mLogQueue.pop());
		Log.useFormat(false);

		// Check that format is ignored when it is explicitly disabled
		Log.d("number %d", 42);
		assertEquals("D/LogFormatTest: number %d\t42", mLogQueue.pop());
	}

	@Test
	public void testMute() {
		Log.usePrintln(false);
		// Check that this message will be skipped
		Log.d("a");
		Log.usePrintln(true);
		Log.d("b");
		assertEquals("D/LogFormatTest: b", mLogQueue.pop());
	}

	public static class LogWithoutTag {
		public void print(String msg) {
			Log.d(msg);
		}
	}

	public static class LogWithTag {
		private final static String tag = "TestTag";
		public void print(String msg) {
			Log.d(msg);
		}

		public void printWithTag(String msg) {
			Log.d(tag, msg);
		}

		public void printWithTag(String msg, String arg) {
			Log.d(tag, msg, arg);
		}
	}

	public static class LogWithUnknownTag {
		private final static String myTag = "MyTag";
		public void print(String msg) {
			Log.d(msg);
		}
	}

	public static class LogWithCustomTag {
		private final static String myTag = "MyTag";
		public void print(String msg) {
			Log.d(msg);
		}
	}

	@Test
	public void testTags() {
		// Check inner class
		LogWithoutTag a = new LogWithoutTag();
		a.print("foo");
		assertEquals("D/LogFormatTest$LogWithoutTag: foo", mLogQueue.pop());

		// Check tag field
		LogWithTag b = new LogWithTag();
		b.print("bar");
		assertEquals("D/TestTag: bar", mLogQueue.pop());
		// Check tag when it's passed explicitly
		b.printWithTag("bar");
		assertEquals("D/TestTag: bar", mLogQueue.pop());
		b.printWithTag("a", "b");
		assertEquals("D/TestTag: a\tb", mLogQueue.pop());

		// Check unknown tag field
		new LogWithUnknownTag().print("baz");
		assertEquals("D/LogFormatTest$LogWithUnknownTag: baz", mLogQueue.pop());

		// Check custom (but known) tag field
		Log.useTags(new String[]{"tag", "TAG", "myTag"});
		new LogWithCustomTag().print("qux");
		assertEquals("D/MyTag: qux", mLogQueue.pop());
	}

	@Test
	public void testThrowables() {
		Log.d(new Exception("foo"));
		assertEquals("D/LogFormatTest: java.lang.Exception: foo", mLogQueue.pop());

		Log.d("a", "b", new Exception("bar"));
		assertTrue(mLogQueue.pop().startsWith("D/LogFormatTest: a\tb\njava.lang.Exception: bar\n"));
	}
}
