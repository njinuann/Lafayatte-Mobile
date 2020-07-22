package org.redlamp.ws;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.redlamp.logger.ApiLogger;
import org.redlamp.logger.IsoLogger;
import org.redlamp.mdlwre.AccountMiddleware;
import org.redlamp.mdlwre.CustomerMiddleware;
import org.redlamp.mdlwre.TransactionMiddleware;
import org.redlamp.model.BillRequest;
import org.redlamp.model.NameInquiryRequest;
import org.redlamp.model.ReversalRequest;
import org.redlamp.model.TransferRequest;
import org.redlamp.util.XapiPool;
import org.redlamp.util.XapiUtils;

/**
 *
 * @author Olupot.D
 */
@Path("/v1.0")
public class LegacyAPI {

	@POST
	@Path("/nameLookup")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> nameLookup(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, NameInquiryRequest regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Invalid request object");
		} else if (regInput.getAccount_no() == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing account number {account_no}");
		} else if (regInput.getBank_code() == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing bank code {bank_code}");
		} else if (authcode == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (AccountMiddleware helper = new AccountMiddleware()) {
				outcome = helper.processNameLookup(regInput);
			} finally {
				ApiLogger.debug("\trequest>>>" + IsoLogger.gson.toJson(regInput) + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/getDepositAccounts")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> getDepositAccounts(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final Map<String, String> regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.get("account_no") == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing account number {account_no}");
		} else if (authcode == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (AccountMiddleware helper = new AccountMiddleware()) {
				outcome = helper.getDepAcctList(regInput.get("account_no"));
			} finally {
				ApiLogger.debug(regInput + " <:> " + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/getLoanAccounts")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> getLoanAccounts(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final Map<String, Integer> regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.get("cust_no") == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing customer number {cust_no}");
		} else if (authcode == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (AccountMiddleware helper = new AccountMiddleware()) {
				outcome = helper.getLoanAcctList(regInput.get("cust_no"));
			} finally {
				ApiLogger.debug("\trequest>>>" + regInput + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/getLoanSchedule")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> getLoanBillSchedule(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final Map<String, String> regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.get("account_no") == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing loan account number {account_no}");
		} else if (authcode == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (AccountMiddleware helper = new AccountMiddleware()) {
				outcome = helper.getLoanBillSchedule(regInput.get("account_no"));
			} finally {
				ApiLogger.debug("\trequest>>>" + regInput + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/serviceRegistration")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> serviceRegistration(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final Map<String, String> regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.get("account_no") == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing customer account number {account_no}");
		} else if (regInput.get("phone_no") == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing customer phone number {phone_no}");
		} else if (authcode == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (CustomerMiddleware helper = new CustomerMiddleware()) {
				outcome = helper.serviceRegistration(regInput.get("account_no"), regInput.get("phone_no"));
			} finally {
				ApiLogger.debug("\trequest>>>" + regInput + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/getBankList")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> getBankList(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final Map<String, String> regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (authcode == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (AccountMiddleware helper = new AccountMiddleware()) {
				outcome = helper.getBankList();
			} finally {
				ApiLogger.debug("\trequest>>>" + regInput + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/sendAlert")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> sendAlert(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final Map<String, String> regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.get("account_no") == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing billing account number {account_no}");
		} else if (regInput.get("recipient_phone") == null || regInput.get("recipient_phone").trim().length() <= 6) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing or invalid recipient phone number {recipient_phone}");
		} else if (regInput.get("txt_message") == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing alert message text {txt_message}");
		} else if (regInput.get("txt_message").trim().length() > XapiPool.MAX_MESSAGE_LENGTH) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "The field {txt_message} has an overflow of expected character length ("
					+ XapiPool.MAX_MESSAGE_LENGTH + ")");
		} else if (authcode == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (CustomerMiddleware helper = new CustomerMiddleware()) {
				outcome = helper.processAlert(regInput.get("account_no"), regInput.get("txt_message"),
						regInput.get("recipient_phone"), regInput.getOrDefault("chargeable", "Y"),
						regInput.getOrDefault("referenceNo", XapiUtils.generateReference()));
			} finally {
				ApiLogger.debug("\trequest>>>" + regInput + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/getAccountBalance")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> getAccountBalance(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final Map<String, String> regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.get("phone_no") == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing account phone number {phone_no}");
		} else if (regInput.get("account_no") == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing account number {account_no}");
		} else if (regInput.get("currency") == null) {
			outcome.put("responseCode", 16);
			outcome.put("responseTxt", "Missing account currency {currency}");
		} else if (authcode == null) {
			outcome.put("responseCode", 17);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 18);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (TransactionMiddleware helper = new TransactionMiddleware()) {
				outcome = helper.getAccountBalance(regInput.get("phone_no"), regInput.get("account_no"),
						regInput.get("currency"));
			} finally {
				ApiLogger.debug("\trequest>>>" + regInput + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/loanAccountInquiry")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> loanAccountInquiry(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final Map<String, String> regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.get("account_no") == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing account number {account_no}");
		} else if (regInput.get("acct_type") == null) {
			outcome.put("responseCode", 16);
			outcome.put("responseTxt", "Missing account type {acct_type}");
		} else if (authcode == null) {
			outcome.put("responseCode", 17);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 18);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (AccountMiddleware helper = new AccountMiddleware()) {
				outcome = helper.loanAccountInquiry(regInput.get("account_no"), regInput.get("acct_type"));
			} finally {
				ApiLogger.debug("\trequest>>>" + regInput + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/getAccountHistory")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> getAccountHistory(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final Map<String, String> regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.get("account_no") == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing account number {account_no}");
		} else if (authcode == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 13);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (AccountMiddleware helper = new AccountMiddleware()) {
				outcome = helper.getAccountStatement(regInput.get("account_no"));
			} finally {
				ApiLogger.debug("\trequest>>>" + regInput + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/transferFunds")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> transferFunds(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final TransferRequest regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.getPhone_no() == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing account phone number {phone_no}");
		} else if (regInput.getAccount_type() == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing source account type {account_type}");
		} else if (regInput.getAccount_no() == null) {
			outcome.put("responseCode", 16);
			outcome.put("responseTxt", "Missing source account number {account_no}");
		} else if (regInput.getTran_amount() <= 0) {
			outcome.put("responseCode", 17);
			outcome.put("responseTxt", "Invalid transaction amount");
		} else if (regInput.getCurrency() == null) {
			outcome.put("responseCode", 18);
			outcome.put("responseTxt", "Missing transaction currency {currency}");
		} else if (regInput.getRecipient_bank_code() == null) {
			outcome.put("responseCode", 19);
			outcome.put("responseTxt", "Missing target bank code {recipient_bank_code}");
		} else if (regInput.getTran_code() == null) {
			outcome.put("responseCode", 20);
			outcome.put("responseTxt", "Missing transaction type code {tran_code}");
		} else if (authcode == null) {
			outcome.put("responseCode", 22);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 21);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (TransactionMiddleware helper = new TransactionMiddleware()) {
				outcome = helper.transferFunds(regInput);
			} finally {
				ApiLogger.debug("\trequest>>>" + IsoLogger.gson.toJson(regInput) + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/billPayment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> billPayment(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final BillRequest regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.getPhone_no() == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing account phone number {phone_no}");
		} else if (regInput.getAccount_no() == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing account number {account_no}");
		} else if (authcode == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else if (regInput.getTran_amount() <= 0) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing transaction amount {tran_amount}");
		} else if (regInput.getBiller_code() == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing parameter biller_code {biller_code}");
		} else if (regInput.getCurrency() == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing account currency code {currency}");
		} else if (regInput.getAccount_type() == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing account type code {account_type}");
		} else {
			try (TransactionMiddleware helper = new TransactionMiddleware()) {
				outcome = helper.payUtility(regInput);
			} finally {
				ApiLogger.debug("\trequest>>>" + IsoLogger.gson.toJson(regInput) + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

	@POST
	@Path("/reversePayment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> reversePayment(@HeaderParam("authcode") String authcode,
			@HeaderParam("authpass") String authpass, final ReversalRequest regInput) {

		Map<String, Object> outcome = new HashMap<>();
		if (regInput.getAccount_no() == null) {
			outcome.put("responseCode", 15);
			outcome.put("responseTxt", "Missing billing account number {account_no}");
		} else if (regInput.getTran_amount() <= 0) {
			outcome.put("responseCode", 16);
			outcome.put("responseTxt", "Missing tran_amount {tran_amount}");
		} else if (regInput.getReference() == null) {
			outcome.put("responseCode", 17);
			outcome.put("responseTxt", "Missing original reference {reference}");
		} else if (regInput.getBiller_code() == null) {
			outcome.put("responseCode", 18);
			outcome.put("responseTxt", "Missing biller_code {biller_code}");
		} else if (regInput.getCurrency() == null) {
			outcome.put("responseCode", 19);
			outcome.put("responseTxt", "Missing currency {currency}");
		} else if (authcode == null) {
			outcome.put("responseCode", 12);
			outcome.put("responseTxt", "Missing authorization header code {authcode}");
		} else if (authpass == null) {
			outcome.put("responseCode", 13);
			outcome.put("responseTxt", "Missing authorization header password {authpass}");
		} else {
			try (TransactionMiddleware helper = new TransactionMiddleware()) {
				outcome = helper.reverseTransaction(regInput);
			} finally {
				ApiLogger.debug("\trequest>>>" + IsoLogger.gson.toJson(regInput) + "\n\tresponse>>>" + outcome);
			}
		}
		return outcome;
	}

}
