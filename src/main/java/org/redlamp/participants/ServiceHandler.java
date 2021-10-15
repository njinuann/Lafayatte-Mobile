package org.redlamp.participants;

import java.io.Serializable;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.redlamp.helpers.MobileService;
import org.redlamp.interfaces.ISO;
import org.redlamp.logger.IsoLogger;

public class ServiceHandler implements TransactionParticipant, Configurable {

	@Override
	public int prepare(long arg0, Serializable serializable) {
		Context ctx = (Context) serializable;
		ISOMsg reqMsg = (ISOMsg) ctx.get(ISO.REQUEST_KEY);
		ISOMsg respMsg = (ISOMsg) ctx.get(ISO.RESPONSE_KEY);
		try (MobileService handler = new MobileService(reqMsg, respMsg)) {
			ctx.put(ISO.RESPONSE_KEY, handler.postTxn());
			return PREPARED;
		} catch (Exception e) {
			IsoLogger.getLogger().error(e);
		}
		return ABORTED;
	}

	@Override
	public void commit(long id, Serializable context) {
	}

	@Override
	public void abort(long id, Serializable context) {
	}

	@Override
	public void setConfiguration(Configuration arg0) throws ConfigurationException {

	}

}
