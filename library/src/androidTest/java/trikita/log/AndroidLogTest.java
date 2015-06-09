package trikita.log;

import org.junit.*;
import static org.junit.Assert.*;

public class AndroidLogTest {
	@Test
	public void testAndroidLog() {
		Log.d("hello");
		assertEquals("D/AndroidLogTest: hello", android.util.Log.getLastMessage());
	}

	@Test
	public void testAndroidLogMute() {
		Log.d("foo");
		assertEquals("D/AndroidLogTest: foo", android.util.Log.getLastMessage());

		Log.useLog(false);
		Log.d("bar");
		Log.useLog(true);
		assertEquals("D/AndroidLogTest: foo", android.util.Log.getLastMessage());
	}
}
