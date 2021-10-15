package org.redlamp.participants;

import java.io.IOException;
import java.io.Serializable;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.redlamp.interfaces.ISO;
import org.redlamp.logger.IsoLogger;

public class ServiceResponder implements TransactionParticipant, AbortParticipant {

	@Override
	public int prepare(long l, Serializable serializable) {
		Context ctx = (Context) serializable;
		ISOMsg respMsg = (ISOMsg) ctx.get(ISO.RESPONSE_KEY);
		if (respMsg.getString(39) == null) {
			respMsg.set(39, ISO.SYSTEM_ERROR);
			ctx.put(ISO.RESPONSE_KEY, respMsg);
		}
		return PREPARED;
	}

	@Override
	public void commit(long l, Serializable serializable) {
		sendMessage((Context) serializable);
	}

	@Override
	public void abort(long l, Serializable serializable) {
		sendMessage((Context) serializable);
	}

	private void sendMessage(Context context) {
		try {
			ISOSource source = (ISOSource) context.get(ISO.RESOURCE_KEY);
			ISOMsg msgResp = (ISOMsg) context.get(ISO.RESPONSE_KEY);
			source.send(msgResp);
		} catch (IOException | ISOException ex) {
			IsoLogger.getLogger().error(ex);
		}
	}

}
