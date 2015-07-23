package trikita.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;

public final class Log {

	private static Map<String, String> tags = new HashMap<>();

	public final static int V = 0;
	public final static int D = 1;
	public final static int I = 2;
	public final static int W = 3;
	public final static int E = 4;

	private Log() {}

	public interface Printer {
		public void print(int level, String tag, String msg);
	}

	private static class SystemOutPrinter implements Printer {
		public final static String[] levels = {"V", "D", "I", "W", "E"};
		public void print(int level, String tag, String msg) {
			System.out.println(levels[level] + "/" + tag + ": " + msg);
		}
	}

	private static class AndroidPrinter implements Printer {
		private final Class<?> mLogClass;
		private final Method[] mLogMethods;

		private final boolean loaded;

		public AndroidPrinter() {
			try {
				mLogClass = Class.forName("android.util.Log");
				String[] names = new String[]{"v", "d", "i", "w", "e"};
				mLogMethods = new Method[names.length];
				for (int i = 0; i < names.length; i++) {
					mLogMethods[i] = mLogClass.getMethod(names[i], String.class, String.class);
				}
			} catch (NoSuchMethodException|ClassNotFoundException e) {
				loaded = false;
				return;
			}
			loaded = true;
		}

		public void print(int level, String tag, String msg) {
			try {
				if (loaded) {
					mLogMethods[level].invoke(null, tag, msg);
				}
			} catch (InvocationTargetException|IllegalAccessException e) {
				// Ignore
			}
		}
	}

	public final static SystemOutPrinter SYSTEM = new SystemOutPrinter();
	public final static AndroidPrinter ANDROID = new AndroidPrinter();

	private static String[] mUseTags = new String[]{"tag", "TAG"};
	private static boolean mUseFormat = false;
	private static int mMinLevel = V;

	private static Set<Printer> mPrinters = new HashSet<>();

	static {
		if (ANDROID.loaded) {
			usePrinter(ANDROID, true);
		} else {
			usePrinter(SYSTEM, true);
		}
	}

	public static Log useTags(String[] tags) {
		mUseTags = tags;
		return null;
	}

	public static Log level(int minLevel) {
		mMinLevel = minLevel;
		return null;
	}

	public static Log useFormat(boolean yes) {
		mUseFormat = yes;
		return null;
	}

	public static Log usePrinter(Printer p, boolean on) {
		if (on) {
			mPrinters.add(p);
		} else {
			mPrinters.remove(p);
		}
		return null;
	}

	public static Log v(Object msg, Object... args) {
		log(V, mUseFormat, msg, args);
		return null;
	}
	public static Log d(Object msg, Object... args) {
		log(D, mUseFormat, msg, args);
		return null;
	}
	public static Log i(Object msg, Object... args) {
		log(I, mUseFormat, msg, args);
		return null;
	}
	public static Log w(Object msg, Object... args) {
		log(W, mUseFormat, msg, args);
		return null;
	}
	public static Log e(Object msg, Object... args) {
		log(E, mUseFormat, msg, args);
		return null;
	}

	private static void log(int level, boolean fmt, Object msg, Object... args) {
		if (level < mMinLevel) {
			return;
		}
		String tag = tag();
		if (mUseTags.length > 0 && tag.equals(msg)) {
			if (args.length > 1) {
				print(level, tag, format(fmt, args[0], Arrays.copyOfRange(args, 1, args.length)));
			} else {
				print(level, tag, format(fmt, (args.length > 0 ? args[0] : "")));
			}
		} else {
			print(level, tag, format(fmt, msg, args));
		}
	}

	private static String format(boolean fmt, Object msg, Object... args) {
		Throwable t = null;
		if (args == null) {
			// Null array is not supposed to be passed into this method, so it must
			// be a single null argument
			args = new Object[]{null};
		}
		if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
			t = (Throwable) args[args.length - 1];
			args = Arrays.copyOfRange(args, 0, args.length - 1);
		}
		if (fmt && msg instanceof String) {
			String head = (String) msg;
			if (head.indexOf('%') != -1) {
				return String.format(head, args);
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append(msg == null ? "null" : msg.toString());
		for (Object arg : args) {
			sb.append("\t");
			sb.append(arg == null ? "null" : arg.toString());
		}
		if (t != null) {
			sb.append("\n");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			sb.append(sw.toString());
		}
		return sb.toString();
	}

	public final static int MAX_LOG_LINE_LENGTH = 4000;

	private static void print(int level, String tag, String msg) {
		for (String line : msg.split("\\n")) {
			do {
				int splitPos = Math.min(MAX_LOG_LINE_LENGTH, line.length());
				for (int i = splitPos-1; line.length() > MAX_LOG_LINE_LENGTH && i >= 0; i--) {
					if (" \t,.;:?!{}()[]/\\".indexOf(line.charAt(i)) != -1) {
						splitPos = i;
						break;
					}
				}
				splitPos = Math.min(splitPos + 1, line.length());
				String part = line.substring(0, splitPos);
				line = line.substring(splitPos);

				for (Printer p : mPrinters) {
					p.print(level, tag, part);
				}
			} while (line.length() > 0);
		}
	}

	private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
	public final static int STACK_DEPTH = 4;
	private static String tag() {
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		if (stackTrace.length < STACK_DEPTH) {
			throw new IllegalStateException
				("Synthetic stacktrace didn't have enough elements: are you using proguard?");
		}
		String className = stackTrace[STACK_DEPTH-1].getClassName();
		if (tags.get(className) != null) {
			return tags.get(className);
		}

		try {
			Class<?> c = Class.forName(className);
			for (String f : mUseTags) {
				try {
					Field field = c.getDeclaredField(f);
					if (field != null) {
						field.setAccessible(true);
						Object value = field.get(null);
						if (value instanceof String) {
							tags.put(className, (String) value);
							return (String) value;
						}
					}
				} catch (NoSuchFieldException|IllegalAccessException|
						IllegalStateException|NullPointerException e) {
					 //Ignore 
				}
			}
		} catch (ClassNotFoundException e) { /* Ignore */ }

		// Check class field useTag, if exists - return it, otherwise - return the generated tag
		Matcher m = ANONYMOUS_CLASS.matcher(className);
		if (m.find()) {
			className = m.replaceAll("");
		}
		return className.substring(className.lastIndexOf('.') + 1);
	}
}
