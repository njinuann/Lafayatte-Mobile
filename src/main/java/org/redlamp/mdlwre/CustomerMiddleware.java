package org.redlamp.mdlwre;

import java.util.HashMap;
import java.util.Map;

import org.redlamp.extras.CustomerHandler;
import org.redlamp.extras.HttpHandler;
import org.redlamp.extras.TxnHandler;
import org.redlamp.interfaces.ISO;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

public class CustomerMiddleware implements AutoCloseable, ISO {

	private StringBuilder builder;

	public CustomerMiddleware() {
		setBuilder(new StringBuilder());
	}

	@Override
	public void close() {
		getBuilder().setLength(0);
	}

	public Map<String, Object> processAlert(String acct_no, String txt_message, String recipient_phone,
			String chargeable, String referenceNo) {
		Map<String, Object> response = new HashMap<>();
		try (TxnHandler sqlHandler = new TxnHandler()) {
			HttpHandler handler = new HttpHandler();
			if (handler.sendAlert(acct_no, txt_message, recipient_phone, chargeable, referenceNo)) {
				response.put("responseCode", XAPI_APPROVED);
				sqlHandler.smsCharge(acct_no);
			} else
				response.put("responseCode", NO_ACTION_TAKEN);
		} finally {
			response.put("responseTxt", XapiCodes.getErrorDesc(String.valueOf(response.get("responseCode"))));
		}
		return response;
	}

	public Map<String, Object> serviceRegistration(String acct_no, String phone_1) {
		Map<String, Object> local = new HashMap<String, Object>();
		if (XapiPool.isInOfflineMode()) {
			local.put("responseCode", OFFLINE_ACTIVATED);
			local.put("responseTxt", XapiCodes.getErrorDesc(OFFLINE_ACTIVATED));
			return local;
		}
		try (CustomerHandler handler = new CustomerHandler()) {
			local = handler.register(acct_no, phone_1);
		} finally {
			local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
		}
		return local;
	}

	public StringBuilder getBuilder(boolean reset) {
		if (reset)
			builder.setLength(0);
		return builder;
	}

	public StringBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(StringBuilder builder) {
		this.builder = builder;
	}

}
