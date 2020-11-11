package org.redlamp.extras;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redlamp.interfaces.ISO;
import org.redlamp.interfaces.SQL;
import org.redlamp.io.StringUtils;
import org.redlamp.logger.ApiLogger;
import org.redlamp.model.AccountMap;
import org.redlamp.model.AlertRequest;
import org.redlamp.model.BillRequest;
import org.redlamp.model.ChargeMap;
import org.redlamp.model.ReversalRequest;
import org.redlamp.model.TransferRequest;
import org.redlamp.model.XapiTran;
import org.redlamp.util.RefUtils;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

import NIPClient.Channel.NibssClient;

public class TxnHandler implements AutoCloseable, ISO, SQL {

	private Connection conn;
	private StringBuilder builder;
	private AlertRequest alertRequest;

	public TxnHandler() {
		try {
			setBuilder(new StringBuilder());
			setAlertRequest(new AlertRequest());
			conn = XapiPool.getConnection();
		} catch (SQLException ex) {
			ApiLogger.getLogger().error(ex);
		}
	}

	@Override
	public void close() {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			ApiLogger.getLogger().error(e);
		}
	}

	public int isValidTxn(XapiTran transaction) {
		try (CallableStatement validateTxn = conn.prepareCall(SQL.VALIDATE_TXN)) {
			validateTxn.registerOutParameter(1, Types.INTEGER);
			validateTxn.setString(2,
					transaction.isBillable() ? transaction.getPnReferenceNo() : "C" + transaction.getPnReferenceNo());
			validateTxn.setString(3, transaction.getPnOrigReferenceNo());
			validateTxn.setString(4, transaction.getPsReversal());
			validateTxn.setString(5, transaction.getPsCardNo());
			validateTxn.setInt(6, transaction.getPnTranCode());
			validateTxn.setString(7, transaction.getPsISOCurrency());
			validateTxn.setString(8, transaction.getPsAcctType1());
			validateTxn.setString(9, transaction.getPsAcctNo1());
			validateTxn.setString(10, transaction.getPsAcctType2());
			validateTxn.setString(11, transaction.getPsAcctNo2());
			validateTxn.setDouble(12, transaction.getPnAmt1());
			validateTxn.setDouble(13, transaction.getPnAmt2());
			validateTxn.setString(14, transaction.getChargeFlag());
			validateTxn.setInt(15, 0);
			validateTxn.execute();
			return validateTxn.getInt(1);
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return 96;
	}

	public boolean postTxn(XapiTran xapiTran) {
		int validTxn = isValidTxn(xapiTran);
		if (validTxn != 0) {
			xapiTran.setXapiCode(validTxn);
			return false;
		}
		return processTxn(xapiTran);
	}

	private boolean processTxn(XapiTran transaction) {

		try (CallableStatement main2Statement = conn.prepareCall(SQL.MOB_CALLABLE)) {

			getBuilder(true).append(44).append(", ");
			main2Statement.registerOutParameter(1, Types.INTEGER);

			getBuilder().append("'").append(transaction.getPdtTranDt()).append("', ");
			main2Statement.setDate(2, transaction.getPdtTranDt());

			getBuilder().append("'").append(transaction.getPdtEffectiveDt()).append("', ");
			main2Statement.setDate(3, transaction.getPdtEffectiveDt());

			getBuilder().append("'").append(transaction.getPsTerminalID()).append("', ");
			main2Statement.setString(4, transaction.getPsTerminalID());

			getBuilder().append("'").append(transaction.getPsProprietaryATM()).append("',");
			main2Statement.setString(5, transaction.getPsProprietaryATM());

			getBuilder().append("'").append(transaction.getPsATMSwitchID()).append("', ");
			main2Statement.setString(6, transaction.getPsATMSwitchID());

			getBuilder().append("'").append(transaction.getPsMannedDevice()).append("',");
			main2Statement.setString(7, transaction.getPsMannedDevice());

			if (transaction.isBillable()) {
				getBuilder().append("'").append(transaction.getPnReferenceNo()).append("', ");
				main2Statement.setString(8, transaction.getPnReferenceNo());
			} else {
				getBuilder().append("'").append(transaction.getPnReferenceNo()).append("', ");
				main2Statement.setString(8, "C" + transaction.getPnReferenceNo());
			}

			getBuilder().append("'").append(transaction.getPnOrigReferenceNo()).append("', ");
			main2Statement.setString(9, transaction.getPnOrigReferenceNo());

			getBuilder().append("'").append(transaction.getPsOffline()).append("', ");
			main2Statement.setString(10, transaction.getPsOffline());

			getBuilder().append("'").append(transaction.getPsReversal()).append("', ");
			main2Statement.setString(11, transaction.getPsReversal());

			getBuilder().append("'").append(transaction.getPsCardNo()).append("', ");
			main2Statement.setString(12, transaction.getPsCardNo());

			getBuilder().append("null, ");
			main2Statement.setString(13, null);

			getBuilder().append("'").append(transaction.getPsCardNo()).append("', ");
			main2Statement.setString(14, transaction.getPsCardNo());

			getBuilder().append(transaction.getPnTranCode()).append(", ");
			main2Statement.setInt(15, transaction.getPnTranCode());

			getBuilder().append("'").append(transaction.getPsISOCurrency()).append("', ");
			main2Statement.setString(16, transaction.getPsISOCurrency());

			getBuilder().append("0, ");
			main2Statement.setInt(17, 0);

			getBuilder().append("'").append(transaction.getPsApplType1()).append("', ");
			main2Statement.setString(18, transaction.getPsApplType1());

			getBuilder().append("'").append(transaction.getPsAcctType1()).append("', ");
			main2Statement.setString(19, transaction.getPsAcctType1());

			getBuilder().append("'").append(transaction.getPsAcctNo1()).append("', ");
			main2Statement.setString(20, transaction.getPsAcctNo1());

			getBuilder().append("'").append(transaction.getPsApplType2()).append("', ");
			main2Statement.setString(21, transaction.getPsApplType2());

			getBuilder().append("'").append(transaction.getPsAcctType2()).append("', ");
			main2Statement.setString(22, transaction.getPsAcctType2());

			getBuilder().append("'").append(transaction.getPsAcctNo2()).append("', ");
			main2Statement.setString(23, transaction.getPsAcctNo2());

			getBuilder().append(transaction.getPnAmt1()).append(", ");
			main2Statement.setDouble(24, transaction.getPnAmt1());

			getBuilder().append(transaction.getPnAmt2()).append(", ");
			main2Statement.setDouble(25, transaction.getPnAmt2());

			getBuilder().append(transaction.getPnCheckNo1()).append(", ");
			main2Statement.setDouble(26, transaction.getPnCheckNo1());

			getBuilder().append(transaction.getPnCheckNo2()).append(", ");
			main2Statement.setDouble(27, transaction.getPnCheckNo2());

			getBuilder().append("null, ");
			main2Statement.setString(28, null);

			getBuilder().append("null, ");
			main2Statement.setString(29, null);

			getBuilder().append("0, ");
			main2Statement.setInt(30, 0);

			getBuilder().append("'").append(transaction.getPsRegEDescription()).append("', ");
			main2Statement.setString(31, transaction.getPsRegEDescription());

			getBuilder().append("'").append(transaction.getPsDescription()).append("', ");
			main2Statement.setString(32, transaction.getPsDescription());

			main2Statement.setString(33, transaction.isBillable() ? transaction.getChargeFlag() : null);
			getBuilder().append("'").append(transaction.isBillable() ? transaction.getChargeFlag() : null)
					.append("', ");

			getBuilder().append("1,");
			main2Statement.setInt(34, 1);

			getBuilder().append("1,");
			main2Statement.setInt(35, 1);

			getBuilder().append("'2.0' ,");
			main2Statement.setString(36, "2.0");

			getBuilder().append("0,");
			main2Statement.setInt(37, 0);

			getBuilder().append("'DP' ,");
			main2Statement.setString(38, "DP");

			getBuilder().append("0,");
			main2Statement.setInt(39, 0);

			getBuilder().append("'SMS' ,");
			main2Statement.setString(40, "SMS");

			getBuilder().append("0");
			main2Statement.setInt(41, 0);

			if (transaction.getPnTranCode() != 200 && "N".equalsIgnoreCase(transaction.getPsReversal())) {
				try (ResultSet resultset = main2Statement.executeQuery()) {
					if (resultset.next()) {
						ResultSetMetaData metaData = resultset.getMetaData();
						if (metaData.getColumnCount() >= 2) {
							transaction.setCurrentBal(resultset.getDouble(1));
							transaction.setAvailableBal(resultset.getDouble(2));
						}
					}
				} catch (Exception ex) {
					ApiLogger.getLogger().error(ex);
				}
				transaction.setXapiCode(main2Statement.getInt(1));
			} else {
				main2Statement.execute();
			}
			transaction.setXapiCode(main2Statement.getInt(1));
			return true;
		} catch (SQLException ex) {
			ApiLogger.getLogger().error(ex);
		} finally {
			ApiLogger.getLogger().info(getBuilder().toString().replaceAll("'null'", "null"));
			getBuilder(true);
		}
		return false;
	}

	public Map<String, Object> getAccountBalance(String phone, String acct_no, String currency) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			XapiTran tran = new XapiTran();
			AccountMap fromAccount = accountName(acct_no);
			if (fromAccount.getNubanAccount() == null) {
				response.put("responseCode", "58");
				response.put("responseTxt", "Unable to locate nuban entry for account " + acct_no);
				return response;
			}
			tran.setPsAcctNo1(fromAccount.getLocalAccount());

			tran.setProcCode("31");
			tran.setPsISOCurrency(currency);
			tran.setPsCardNo(phone);
			tran.setPnTranCode(100);
			tran.setPsDescription("Mobile Balance Enquiry");
			tran.setBillable(true);
			tran.setPnReferenceNo(RefUtils.get());
			if (processTxn(tran)) {
				response.put("available_bal", tran.getAvailableBal());
				response.put("current_bal", tran.getCurrentBal());
			}
			response.put("reference", tran.getPnReferenceNo());
			response.put("responseCode", tran.getResponseCode());
		} catch (Exception ex) {
			ApiLogger.getLogger().error(ex);
		}
		return response;
	}

	public String nibssResponse(String responseCode) {
		try (Statement stmt = conn.createStatement();
				ResultSet rset = stmt
						.executeQuery(getBuilder(true).append("select Description from ").append(XapiPool.CLEARING_DB)
								.append("..Response where Code = '").append(responseCode).append("'").toString())) {
			if (rset.next()) {
				return rset.getString(1);
			}
		} catch (SQLException ex) {
			ApiLogger.getLogger().error(ex);
		}
		return "Your request failed with a response code " + responseCode;
	}

	public AccountMap accountName(String accountNo) {
		AccountMap accountMap = new AccountMap();
		boolean nuban = !accountNo.contains("-");
		try (Statement stmt = conn.createStatement()) {

			try (ResultSet rset = stmt
					.executeQuery(getBuilder(true).append("select a.value, b.title_1, b.acct_no from ")
							.append(XapiCodes.coreschema).append("..gb_user_defined a, ").append(XapiCodes.coreschema)
							.append("..dp_acct b where a.field_id=45 and ")
							.append(nuban ? "a.value = '" : "b.acct_no = '").append(accountNo)
							.append("' and b.acct_no = a.acct_no_key").toString())) {
				if (rset.next()) {
					accountMap.setAccountTitle(rset.getString(2));
					accountMap.setLocalAccount(rset.getString(3));
					accountMap.setNubanAccount(rset.getString(1));
				}
			}
		} catch (Exception e) {
			ApiLogger.getLogger().error(e);
		}
		return accountMap;
	}

	public Map<String, Object> transferFunds(TransferRequest transfer) {
		return "20".equalsIgnoreCase(transfer.getTran_code()) ? interbankFundsTransfer(transfer)
				: internalFundsTransfer(transfer);
	}

	public Map<String, Object> interbankFundsTransfer(TransferRequest transfer) {
		Map<String, Object> response = new HashMap<String, Object>();

		try {

			XapiTran tran = new XapiTran();
			tran.setProcCode("40");
			tran.setPnAmt1(transfer.getTran_amount());
			tran.setPsTerminalID(transfer.getRecipient_bank_code());

			// nibss transfer. check the recipient account name and number
			if (transfer.getRecipient_account_no() == null
					|| "".equalsIgnoreCase(transfer.getRecipient_account_no().trim())) {
				response.put("responseCode", "21");
				response.put("responseTxt", "Target account required for NIBSS transfer");
				return response;
			}
			tran.setPsAcctNo2(transfer.getRecipient_account_no());

			AccountMap fromAccount = accountName(transfer.getAccount_no());
			if (fromAccount.getNubanAccount() == null) {
				// no nuban account equivalent
				response.put("responseCode", "58");
				response.put("responseTxt",
						"Unable to locate corresponding source nuban account for " + transfer.getAccount_no());
				return response;
			}
			tran.setPsAcctNo1(fromAccount.getLocalAccount());

			tran.setPsDescription(String.format("FROM %s TO %s/%s", fromAccount.getAccountTitle(),
					transfer.getRecipient_account_name(), transfer.getDescription()));

			String returnCode = new NibssClient().postNIPTransfer(tran.getAcctName(),
					StringUtils.stripDashes(tran.getPsAcctNo1()), tran.getTranLocation(), tran.getPsDescription(),
					StringUtils.stripDashes(tran.getPsAcctNo2()), tran.getPsTerminalID(), tran.getPnAmt1(),
					tran.getPnReferenceNo()).trim();

			if (returnCode != null) {
				response.put("responseCode", returnCode);
				response.put("responseTxt", nibssResponse(returnCode));
			} else {
				response.put("responseCode", "96");
				response.put("responseTxt", XapiCodes.getErrorDesc(96));
			}
			response.put("reference", tran.getPnReferenceNo());
		} catch (Exception ex) {
			ApiLogger.getLogger().error(ex);
		}
		return response;
	}

	public Map<String, Object> internalFundsTransfer(TransferRequest transfer) {
		Map<String, Object> response = new HashMap<String, Object>();

		try {

			XapiTran tran = new XapiTran();
			tran.setProcCode("40");
			tran.setPnAmt1(transfer.getTran_amount());
			tran.setPsTerminalID(transfer.getRecipient_bank_code());
			AccountMap toAccount = null, fromAccount = null;

			// transform the account from nuban to ordinary account
			fromAccount = accountName(transfer.getAccount_no());
			if (fromAccount.getNubanAccount() == null) {
				// no nuban account equivalent
				response.put("responseCode", "58");
				response.put("responseTxt", "Cannot locate nuban account for " + transfer.getAccount_no());
				return response;
			}
			tran.setPsAcctNo1(StringUtils.appendDash(fromAccount.getLocalAccount().trim()));

			if ("10".equalsIgnoreCase(transfer.getTran_code())) {
				if (transfer.getRecipient_account_no() == null
						|| "".equalsIgnoreCase(transfer.getRecipient_account_no().trim())) {
					response.put("responseCode", "62");
					response.put("responseTxt", "Missing Recipient Account Number");
					return response;
				}
				if (transfer.getAccount_no().equalsIgnoreCase(transfer.getRecipient_account_no())) {
					response.put("responseCode", "63");
					response.put("responseTxt", "Cannot transfer to same account. Please review and adjust");
					return response;
				}

				// translate the to nuban account to local account
				toAccount = accountName(transfer.getRecipient_account_no());
				if (toAccount.getNubanAccount() == null) {
					// no nuban account equivalent
					response.put("responseCode", "58");
					response.put("responseTxt",
							"Cannot locate nuban account for " + transfer.getRecipient_account_no());
					return response;
				}
				tran.setPsAcctNo2(StringUtils.appendDash(toAccount.getLocalAccount().trim()));
			}

			tran.setPsISOCurrency(transfer.getCurrency());
			tran.setPsCardNo(transfer.getPhone_no());
			tran.setPsAcctType1(transfer.getAccount_type());

			if ("10".equalsIgnoreCase(transfer.getTran_code())) {
				tran.setChargeFlag(XapiCodes.internal_transfer);
				tran.setPnTranCode(850);// default to local transfer
			} else {
				tran.setPnTranCode(750); // QT teller xfer
				tran.setChargeFlag(XapiCodes.quick_teller);
			}

			if (transfer.getRecipient_account_name() != null && !transfer.getRecipient_account_name().isEmpty()
					&& (transfer.getDescription() != null && !transfer.getDescription().isEmpty()))
				tran.setPsDescription(String.format("FROM %s TO %s/%s", fromAccount.getAccountTitle(),
						transfer.getRecipient_account_name(), transfer.getDescription()));
			else if (transfer.getRecipient_account_name() == null || transfer.getRecipient_account_name().isEmpty()
					&& (transfer.getDescription() != null && !transfer.getDescription().isEmpty()))
				tran.setPsDescription(
						String.format("FROM %s/%s", fromAccount.getAccountTitle(), transfer.getDescription()));
			else if (transfer.getRecipient_account_name() != null && !transfer.getRecipient_account_name().isEmpty()
					&& (transfer.getDescription() == null || transfer.getDescription().isEmpty()))
				tran.setPsDescription(String.format("FROM %s/%s", fromAccount.getAccountTitle(),
						transfer.getRecipient_account_name()));
			else
				tran.setPsDescription(String.format("FROM %s", fromAccount.getAccountTitle()));

			tran.setBillable(true);
			tran.setPnReferenceNo(RefUtils.get());

			if (processTxn(tran)) {
				response.put("available_bal", tran.getAvailableBal());
				response.put("current_bal", tran.getCurrentBal());
			}

			response.put("from_acct", fromAccount.getNubanAccount());
			if (toAccount != null) {
				response.put("to_acct", toAccount.getNubanAccount());
			}

			response.put("responseCode", tran.getResponseCode());
			response.put("responseTxt", tran.getResponseTxt());
			response.put("reference", tran.getPnReferenceNo());

		} catch (Exception ex) {
			ApiLogger.getLogger().error(ex);
		}
		return response;
	}

	XapiTran buildUtility(BillRequest billRequest) {
		XapiTran tran = new XapiTran();
		tran.setProcCode("50");

		tran.setPsCardNo(billRequest.getPhone_no());
		tran.setPsISOCurrency(billRequest.getCurrency());
		tran.setPnAmt1(billRequest.getTran_amount());
		tran.setPnTranCode(810);
		return tran;
	}

	ChargeMap billingInfo(String billerCode) {
		ChargeMap chargeMap = new ChargeMap();
		try (Statement stmt = conn.createStatement()) {
			try (ResultSet rset = stmt.executeQuery(getBuilder(true)
					.append("select txnType, tranDesc from eb_charges where serviceID = ").append(MOBILE_SERVICE)
					.append(" and biller_code = '").append(billerCode).append("' and status = 'Active'").toString())) {
				if (rset != null && rset.next()) {
					chargeMap.setChargeDesc(rset.getString("tranDesc"));
					chargeMap.setChargeFlag(rset.getString("txnType"));
				}
			}
		} catch (Exception e) {
			ApiLogger.getLogger().error(e);
		}
		return chargeMap;
	}

	public Map<String, Object> payUtility(BillRequest billRequest) {
		Map<String, Object> response = new HashMap<String, Object>();

		try (Statement stmt = conn.createStatement()) {

			XapiTran tran = buildUtility(billRequest);
			AccountMap fromAccount = accountName(billRequest.getAccount_no());
			if (fromAccount.getNubanAccount() == null) {
				response.put("responseCode", "71");
				response.put("responseTxt", "Unable to locate nuban entry for account " + billRequest.getAccount_no());
				return response;
			}
			tran.setPsAcctNo1(fromAccount.getLocalAccount());

			ChargeMap billingInfo = billingInfo(billRequest.getBiller_code());
			if (billingInfo.getChargeFlag() != null) {
				tran.setChargeFlag(billingInfo.getChargeFlag());
				tran.setPsDescription(billingInfo.getChargeDesc());
			}
			if (tran.getPsDescription() == null && billRequest.getDescription() != null) {
				tran.setPsDescription(billRequest.getDescription());
			} else if (tran.getPsDescription() == null) {
				tran.setPsDescription("Mobile Utility Payment");
			}

			tran.setBillable(true);
			tran.setPnReferenceNo(RefUtils.get());

			if (processTxn(tran)) {
				response.put("available_bal", tran.getAvailableBal());
				response.put("current_bal", tran.getCurrentBal());
			}

			response.put("currency", tran.getPsISOCurrency());
			response.put("responseCode", tran.getResponseCode());
			response.put("reference", tran.getPnReferenceNo());

		} catch (Exception ex) {
			ApiLogger.getLogger().error("payUtility", ex);
		} finally {
			// schedule the alert manager to send this alert to venus
		}
		return response;
	}

	public Map<String, Object> reverseTransaction(ReversalRequest regInput) {
		Map<String, Object> response = new HashMap<String, Object>();

		XapiTran originalTxn = retrieveOriginalTxn(regInput);
		if (originalTxn == null) {
			response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
			response.put("responseTxt", XapiCodes.getErrorDesc(UNABLE_TO_LOCATE_RECORD));
			return response;
		}
		try (Statement stmt = conn.createStatement()) {
			// attempt to pick up the original biller code
			try (ResultSet rset = stmt
					.executeQuery(getBuilder(true).append("select txnType from eb_charges where serviceID = ")
							.append(MOBILE_SERVICE).append(" and biller_code = '").append(regInput.getBiller_code())
							.append("' and status = 'Active'").toString())) {
				originalTxn.setChargeFlag(rset.next() ? rset.getString("txnType") : "P");
			} catch (Exception e) {
				originalTxn.setChargeFlag("P");
				ApiLogger.getLogger().error(e);
			}
			originalTxn.setBillable(true);
			originalTxn.setPnReferenceNo(RefUtils.get());
			if (processTxn(originalTxn)) {
				response.put("available_bal", originalTxn.getAvailableBal());
				response.put("current_bal", originalTxn.getCurrentBal());
				response.put("currency", originalTxn.getPsISOCurrency());
			}
			response.put("responseCode", originalTxn.getResponseCode());
		} catch (Exception ex) {
			ApiLogger.getLogger().error(ex);
			response.put("responseCode", SYSTEM_ERROR);
		} finally {
			response.put("reference", originalTxn.getPnReferenceNo());
			response.put("responseTxt", originalTxn.getResponseTxt());
		}
		return response;
	}

	private XapiTran retrieveOriginalTxn(ReversalRequest request) {
		try (Statement stmt = conn.createStatement();
				ResultSet tranCursor = stmt
						.executeQuery(getBuilder(true).append("select * from ").append(XapiCodes.coreschema)
								.append(XapiPool.isInOfflineMode() ? "..offline_tran_log " : "..atm_tran_log ")
								.append(" where acct_no = '").append(StringUtils.appendDash(request.getAccount_no()))
								.append("' and amt = ").append(request.getTran_amount()).append(" and iso_currency = '")
								.append(request.getCurrency()).append("' and reference_no= '")
								.append(request.getReference()).append("' and reversal not in ('Y')").toString())) {
			if (tranCursor.next()) {
				XapiTran originalTxn = new XapiTran();
				originalTxn.setBillable(true);
				originalTxn.setPnTranCode(tranCursor.getInt("tran_code"));
				originalTxn.setPsISOCurrency(tranCursor.getString("iso_currency"));
				originalTxn.setPsAcctNo2(tranCursor.getString("tfr_acct_no"));
				originalTxn.setPsRegEDescription(tranCursor.getString("reg_e_desc"));
				originalTxn.setPsDescription("Rev~" + tranCursor.getString("description"));
				originalTxn.setPsAcctNo1(tranCursor.getString("acct_no"));
				originalTxn.setProcCode("XX");
				originalTxn.setPnAmt1(tranCursor.getDouble("amt"));
				originalTxn.setPnOrigReferenceNo(request.getReference());
				originalTxn.setPsReversal("Y");
				return originalTxn;
			}
		} catch (SQLException ex) {
			ApiLogger.getLogger().error(ex);
		}
		return null;
	}

	Map<String, Object> asMap(ResultSet resultSet) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			if (resultSet != null && resultSet.isBeforeFirst()) {
				ResultSetMetaData metaData = resultSet.getMetaData();
				if (resultSet.next()) {
					for (int i = 1; i <= metaData.getColumnCount(); i++)
						map.put(metaData.getColumnName(i), resultSet.getObject(i));
				}
			}
		} catch (SQLException e) {
			ApiLogger.getLogger().error(e);
		}
		return map;
	}

	List<Map<String, Object>> asListMap(ResultSet resultSet) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			if (resultSet != null && resultSet.isBeforeFirst()) {
				ResultSetMetaData metaData = resultSet.getMetaData();
				while (resultSet.next()) {
					Map<String, Object> map = new HashMap<String, Object>();
					for (int i = 1; i <= metaData.getColumnCount(); i++)
						map.put(metaData.getColumnName(i), resultSet.getObject(i));
					list.add(map);
				}
			}
		} catch (SQLException e) {
			ApiLogger.getLogger().error(e);
		}
		return list;
	}

	public boolean smsCharge(String acct) {
		try (Statement stmt = conn.createStatement();
				CallableStatement alertChargeStmt = conn
						.prepareCall("{? = call " + XapiCodes.coreschema + "..Nep_ApplySms_CC(?, ?)}")) {
			String acctType = null;
			try (ResultSet rset = stmt
					.executeQuery(getBuilder(true).append("select acct_type from ").append(XapiCodes.coreschema)
							.append("..dp_acct where acct_no =  '").append(acct).append("'").toString())) {
				if (!rset.next()) {
					return false;
				}
				acctType = rset.getString(1);
			}
			alertChargeStmt.registerOutParameter(1, Types.INTEGER);
			alertChargeStmt.setString(2, acct);
			alertChargeStmt.setString(3, acctType);
			alertChargeStmt.execute();
		} catch (Exception e) {
			ApiLogger.getLogger().error(e);
		}
		return false;
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

	public AlertRequest getAlertRequest() {
		return alertRequest;
	}

	public void setAlertRequest(AlertRequest alertRequest) {
		this.alertRequest = alertRequest;
	}

}
