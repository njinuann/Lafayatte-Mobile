package org.redlamp.util;

public class RefUtils {

	static long current = System.nanoTime();

	static public synchronized String get() {
		return String.valueOf(current++);
	}

}
