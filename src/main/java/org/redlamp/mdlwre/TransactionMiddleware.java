package org.redlamp.mdlwre;

import java.util.HashMap;
import java.util.Map;

import org.redlamp.extras.TxnHandler;
import org.redlamp.interfaces.ISO;
import org.redlamp.model.BillRequest;
import org.redlamp.model.ReversalRequest;
import org.redlamp.model.TransferRequest;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

public class TransactionMiddleware implements AutoCloseable, ISO {

	private StringBuilder builder;

	public TransactionMiddleware() {
		setBuilder(new StringBuilder());
	}

	@Override
	public void close() {
		getBuilder().setLength(0);
	}

	public Map<String, Object> getAccountBalance(String phone, String acct_no, String currency) {
		Map<String, Object> local = new HashMap<String, Object>();
		try (TxnHandler handler = new TxnHandler()) {
			local = handler.getAccountBalance(phone, acct_no, currency);
		} finally {
			local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
		}
		return local;
	}

	public Map<String, Object> transferFunds(TransferRequest transfer) {
		Map<String, Object> local = new HashMap<String, Object>();
		if (transfer.getTran_code() == null || transfer.getTran_code().isEmpty()) {
			local.put("responseCode", "58");
			local.put("responseTxt", "missing tran code");
			return local;
		}
		if (transfer.getTran_code().equals("20") && !XapiPool.nibss_allowed) {
			local.put("responseCode", "58");
			local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
			return local;
		}
		try (TxnHandler handler = new TxnHandler()) {
			local = handler.transferFunds(transfer);
		} finally {
			local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
		}
		return local;
	}

	public Map<String, Object> payUtility(BillRequest billRequest) {
		Map<String, Object> local = new HashMap<String, Object>();
		try (TxnHandler handler = new TxnHandler()) {
			local = handler.payUtility(billRequest);
		} finally {
			local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
		}
		return local;
	}

	public Map<String, Object> reverseTransaction(ReversalRequest regInput) {
		Map<String, Object> local = new HashMap<String, Object>();
		try (TxnHandler handler = new TxnHandler()) {
			local = handler.reverseTransaction(regInput);
		} finally {
			local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
		}
		return local;
	}

	public String mapCode(int returnCode) {
		String RC;
		switch (returnCode) {
		case 0:
			RC = "00";
			break;
		case 10:
			RC = "53";
			break;
		case 11:
			RC = "52";
			break;
		case 28:
			RC = "52";
			break;
		case 24:
			RC = "14";
			break;
		case 30:
			RC = "13";
			break;
		case 39:
			RC = "51";
			break;
		case 43:
			RC = "25";
			break;
		case 51:
			RC = "45";
			break;
		case 58:
		case -50040:
			RC = "58";
			break;
		case 60:
			RC = "13";
			break;
		case 70:
			RC = "26";
			break;
		case 79:
			RC = "13";
			break;
		case 111:
			RC = "57";
			break;
		case -50007:
			RC = "40";
			break;
		default:
			RC = "96";
			break;
		}
		return RC;
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
