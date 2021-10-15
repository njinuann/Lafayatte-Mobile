package org.redlamp.participants;

import java.io.Serializable;

import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextRecovery;
import org.jpos.transaction.TransactionParticipant;
import org.redlamp.interfaces.ISO;

public class FieldHandler implements TransactionParticipant, ContextRecovery {

	@Override
	public int prepare(long arg0, Serializable serializable) {
		Context ctx = (Context) serializable;
		ISOMsg reqMsg = (ISOMsg) ctx.get(ISO.REQUEST_KEY);
		ISOMsg respMsg = (ISOMsg) ctx.get(ISO.RESPONSE_KEY);

		if (!reqMsg.hasField(3)) {
			respMsg.set(39, ISO.INVALID_PROCESSING_CODE);
			ctx.put(ISO.RESPONSE_KEY, respMsg);
			return ABORTED;
		} else if (!reqMsg.hasField(4)) {
			respMsg.set(39, ISO.MISSING_TXN_AMOUNT);
			ctx.put(ISO.RESPONSE_KEY, respMsg);
			return ABORTED;
		} else if (!reqMsg.hasField(7)) {
			respMsg.set(39, ISO.MISSING_TRANSMISSION_TIME);
			ctx.put(ISO.RESPONSE_KEY, respMsg);
			return ABORTED;
		} else if (!reqMsg.hasField(11)) {
			respMsg.set(39, ISO.MISSING_TRACE_AUDIT_NUMBER);
			ctx.put(ISO.RESPONSE_KEY, respMsg);
			return ABORTED;
		} else if (!reqMsg.hasField(41)) {
			respMsg.set(39, ISO.MISSING_TERMINAL_ID);
			ctx.put(ISO.RESPONSE_KEY, respMsg);
			return ABORTED;
		} else if (!reqMsg.hasField(102)) {
			respMsg.set(39, ISO.MISSING_ACCT_NUMBER);
			ctx.put(ISO.RESPONSE_KEY, respMsg);
			return ABORTED;
		}
		return PREPARED;
	}

	@Override
	public Serializable recover(long arg0, Serializable arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}

}
