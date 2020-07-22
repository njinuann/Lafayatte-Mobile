package org.redlamp.io;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {

	public static synchronized Map<String, Object> buildMap(Map<String, Object> map) {
		if (map == null) {
			map = new HashMap<>();
		}
		return map;
	}

}
