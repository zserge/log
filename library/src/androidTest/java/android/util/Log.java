package android.util;

public class Log {

	private static String mLastMessage = "";

	public static void d(String tag, String msg) {
		mLastMessage = "D/" + tag + ": " + msg;
	}
	public static void i(String tag, String msg) {
		mLastMessage = "I/" + tag + ": " + msg;
	}
	public static void v(String tag, String msg) {
		mLastMessage = "V/" + tag + ": " + msg;
	}
	public static void w(String tag, String msg) {
		mLastMessage = "W/" + tag + ": " + msg;
	}
	public static void e(String tag, String msg) {
		mLastMessage = "E/" + tag + ": " + msg;
	}

	public static String getLastMessage() {
		return mLastMessage;
	}
}
