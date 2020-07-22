package org.redlamp.logger;

import java.io.File;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.redlamp.core.MainClass;
import org.redlamp.util.XapiPool;

import ug.ac.mak.java.logger.DailyLogListener;
import ug.ac.mak.java.logger.Log;
import ug.ac.mak.java.logger.Logger;
import ug.ac.mak.java.logger.SimpleLogListener;

public class ApiLogger implements RequestLog {

	private static Log log;

	static {
		configure();
	}

	public static Log getLogger() {
		return log;
	}

	@Override
	public void log(Request requestEntry, Response arg1) {
		getLogger().info(requestEntry);
		MainClass.serviceUI.updateMobileUI(false, "IN");
		MainClass.serviceUI.updateMobileUI(true, "OUT");
	}

	public static void configure() {
		new File("logs/").mkdirs();
		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener());
		DailyLogListener dailyLogger = new DailyLogListener();
		dailyLogger.setConfiguration("logs/mbank_events", "gzip");
		logger.addListener(dailyLogger);
		log = new Log(logger, "mbank");
	}

	public static void debug(Object data) {
		if (XapiPool.DEBUG_ENABLED)
			ApiLogger.getLogger().info(data);
	}

	public static void debug(Object request, Object response) {
		if (XapiPool.DEBUG_ENABLED)
			ApiLogger.getLogger().debug(request + " <:> " + response);
	}

}