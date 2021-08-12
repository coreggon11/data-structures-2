package application.utils;

public class Timer {

	private static long started;

	public static void start() {
		started = System.currentTimeMillis();
	}

	public static long stop() {
		return System.currentTimeMillis() - started;
	}

}
