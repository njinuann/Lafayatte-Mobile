package org.redlamp.logger;

import java.io.IOException;

import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.jpos.util.Log;

import com.google.gson.Gson;

public class IsoLogger extends AbstractNCSARequestLog {

	private static Log logger;
	public static final Gson gson = new Gson();

	public static Log getLogger() {
		return IsoLogger.logger;
	}

	public static void setLogger(Log log) {
		IsoLogger.logger = log;
	}

	@Override
	protected boolean isEnabled() {
		return true;
	}

	@Override
	public void write(String requestEntry) throws IOException {
		getLogger().info(requestEntry);
	}

}
