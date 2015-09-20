package net.devwiki.playmode;

import android.util.Log;

/**
 * 包装后的Log输出,可控制显示哪些级别的LOG
 * @author Administrator
 *
 */
public class DLog {
	private static String className;			//所在的类名
	private static String methodName;			//所在的方法名
	private static int lineNumber;				//所在行号
	
	private DLog() {
	}

	public static boolean isDebuggable() {
		return BuildConfig.DEBUG;
	}

	private static String createLog(String log) {

		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		buffer.append(methodName);
		buffer.append(":");
		buffer.append(lineNumber);
		buffer.append("]");
		buffer.append(log);

		return buffer.toString();
	}

	private static void getMethodNames(StackTraceElement[] sElements) {
		className = sElements[1].getFileName();
		methodName = sElements[1].getMethodName();
		lineNumber = sElements[1].getLineNumber();
	}
	
	public static void v(String message) {
		if (!isDebuggable()) {
			return;
		}

		getMethodNames(new Throwable().getStackTrace());
		Log.v(className, createLog(message));
	}

	public static void d(String message) {
		if (!isDebuggable()) {
			return;
		}

		getMethodNames(new Throwable().getStackTrace());
		Log.d(className, createLog(message));
	}

	public static void i(String message) {
		if (!isDebuggable()) {
			return;
		}

		getMethodNames(new Throwable().getStackTrace());
		Log.i(className, createLog(message));
	}

	public static void w(String message) {
		if (!isDebuggable()) {
			return;
		}

		getMethodNames(new Throwable().getStackTrace());
		Log.w(className, createLog(message));
	}
	
	public static void e(String message) {
		if (!isDebuggable()) {
			return;
		}

		getMethodNames(new Throwable().getStackTrace());
		Log.e(className, createLog(message));
	}
}
