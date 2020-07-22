package org.redlamp.iso;

import java.io.Serializable;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.GroupSelector;
import org.redlamp.interfaces.ISO;
import org.redlamp.logger.IsoLogger;

public class ServiceRouter implements GroupSelector, Configurable {

	private Configuration configuration;

	@Override
	public void setConfiguration(Configuration configuration) throws ConfigurationException {
		this.configuration = configuration;
	}

	@Override
	public String select(long l, Serializable serializable) {
		try {
			ISOMsg resIsoMsg = (ISOMsg) ((Context) serializable).get(ISO.REQUEST_KEY);
			return configuration.get(resIsoMsg.getMTI(), null);
		} catch (ISOException ex) {
			IsoLogger.getLogger().error(ex);
		}
		return null;
	}

	@Override
	public int prepare(long l, Serializable serializable) {
		return PREPARED | ABORTED | NO_JOIN;
	}

	@Override
	public void commit(long l, Serializable serializable) {
	}

	@Override
	public void abort(long l, Serializable serializable) {
	}
}
