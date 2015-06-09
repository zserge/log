package trikita.log;

import org.junit.*;
import static org.junit.Assert.*;

public class AndroidLogTest {
	@Test
	public void testAndroidLog() {
		Log.useLog(true);
		Log.d("hello");
		assertEquals("D/AndroidLogTest: hello", android.util.Log.getLastMessage());
	}

	@Test
	public void testAndroidLogMutes() {
		Log.useLog(true);
		Log.d("foo");
		assertEquals("D/AndroidLogTest: foo", android.util.Log.getLastMessage());
		Log.useLog(false);
		Log.d("bar");
		assertEquals("D/AndroidLogTest: foo", android.util.Log.getLastMessage());
	}
}
