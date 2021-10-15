package org.redlamp.participants;

import java.io.Serializable;

import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.redlamp.interfaces.ISO;

public class NetworkHandler implements TransactionParticipant {

	@Override
	public int prepare(long l, Serializable serializable) {
		Context ctx = (Context) serializable;
		ISOMsg respMsg = (ISOMsg) ctx.get(ISO.RESPONSE_KEY);
		respMsg.set(39, ISO.XAPI_APPROVED);
		ctx.put(ISO.RESPONSE_KEY, respMsg);
		return PREPARED;
	}

	@Override
	public void commit(long l, Serializable serializable) {

	}

	@Override
	public void abort(long l, Serializable serializable) {

	}
}
