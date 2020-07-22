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

import NIPClient.Channel.EnquireNameResponseReturn;
import NIPClient.Channel.NibssClient;

public class SQLHandler implements AutoCloseable, ISO, SQL {

	private Connection conn;
	private StringBuilder builder;
	private AlertRequest alertRequest;

	public SQLHandler() {
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

	public Map<String, Object> register(String acct_no, String phone_1) {
		Map<String, Object> response = new HashMap<String, Object>();

		try (CallableStatement init = conn.prepareCall(INIT_SERVICE_REGISTRATION);
				CallableStatement complete = conn.prepareCall(COMPLETE_SERVICE_REGISTRATION)) {

			String formatted_acct = StringUtils.appendDash(acct_no);
			init.setString(1, formatted_acct);

			try (ResultSet rset = init.executeQuery()) {

				if (rset != null && rset.isBeforeFirst()) {
					String primary_phone = null;
					List<Map<String, String>> account_list = new ArrayList<>();
					int service_status = 0;
					while (rset.next()) {
						service_status = rset.getInt("service_status");
						if (service_status != 70 && rset.getInt("relation_status") > 0) {
							// has another signatory
							response.put("responseCode", MULTIPLE_SIGNATORIES);
							return response;
						}
						if (service_status == 50) {
							// only savings and or current permitted to register.
							response.put("responseCode", ACCOUNT_NOT_PERMITTED);
							return response;
						}

						String phone = StringUtils.formatPhone(rset.getString("phone_no"));
						String account = rset.getString("acct_no").trim();
						if (formatted_acct.equalsIgnoreCase(account)) {
							primary_phone = phone;
						}

						Map<String, String> data = new HashMap<>();
						data.put("account_number", StringUtils.stripDashes(account));
						data.put("account_title", rset.getString("title"));
						data.put("cust_no", String.valueOf(rset.getInt("rim_no")));
						data.put("account_type", rset.getString("acct_type"));
						data.put("branch_name", rset.getString("branch"));
						data.put("current_bal", rset.getString("CurBal"));
						data.put("email", "N/A");
						data.put("phone_no", phone);
						data.put("available_bal", rset.getString("avail_bal"));
						data.put("currency", rset.getString("iso_code"));
						data.put("status", rset.getString("status"));
						account_list.add(data);
					}

					if (service_status == 70) {
						// already registered. simply give the user their details
						response.put("account_details", account_list);
						response.put("responseCode", XAPI_APPROVED);
					}

					phone_1 = StringUtils.formatPhone(phone_1);
					if (primary_phone != null) {
						// attempt to register the service..
						complete.registerOutParameter(1, Types.INTEGER);
						complete.setInt(2, SQL.MOBILE_SERVICE);
						complete.setString(3, primary_phone);
						complete.setString(4, acct_no);
						complete.setString(5, primary_phone);
						complete.execute();
						int returnout = -99;
						try (ResultSet service = complete.getResultSet()) {
							returnout = service != null && service.isBeforeFirst() && service.next() ? service.getInt(1)
									: complete.getInt(1);
						}
						if (returnout == 70 || returnout == 0) {
							// now they can have details
							response.put("account_details", account_list);
							response.put("responseCode", XAPI_APPROVED);
						} else {
							response.put("responseCode", ErrorHandler.mapCode(returnout));
						}
					} else
						response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
				} else {
					// no service attached
					response.put("responseCode", NO_ACTION_TAKEN);
				}
			} catch (SQLException ex) {
				ApiLogger.getLogger().error(ex);
				response.put("responseCode", SYSTEM_ERROR);
			}
		} catch (SQLException ex) {
			ApiLogger.getLogger().error(ex);
			response.put("responseCode", SYSTEM_ERROR);
		}
		return response;
	}

	public Map<String, Object> depositAccountList(String acct_no) {
		Map<String, Object> response = new HashMap<String, Object>();
		try (CallableStatement callableStatement = conn.prepareCall(DEPOSIT_ACCT_LIST)) {
			callableStatement.setString(1, StringUtils.appendDash(acct_no));
			try (ResultSet rset = callableStatement.executeQuery()) {
				if (rset != null && rset.isBeforeFirst()) {
					List<Map<String, Object>> list = asListMap(rset);
					if (!list.isEmpty()) {
						response.put("responseCode", XAPI_APPROVED);
						response.put("account_list", list);
					} else {
						response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
					}
				}
			}
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return response;
	}

	public Map<String, Object> loanBillSchedule(String acct_no) {
		Map<String, Object> response = new HashMap<String, Object>();

		try (CallableStatement callableStatement = conn.prepareCall(LOAN_BILL_SCHEDULE)) {
			callableStatement.setString(1, StringUtils.appendDash(acct_no));
			try (ResultSet rset = callableStatement.executeQuery()) {
				if (rset != null && rset.isBeforeFirst()) {
					List<Map<String, Object>> list = asListMap(rset);
					if (!list.isEmpty()) {
						response.put("responseCode", XAPI_APPROVED);
						response.put("bill_schedule", list);
					} else {
						response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
					}
				}
			}

		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return response;
	}

	public Map<String, Object> loanAccounts(Integer cust_no) {
		Map<String, Object> response = new HashMap<String, Object>();

		try (CallableStatement callableStatement = conn.prepareCall(LOAN_ACCT_LIST)) {
			callableStatement.setInt(1, cust_no);
			try (ResultSet rset = callableStatement.executeQuery()) {
				if (rset != null && rset.isBeforeFirst()) {
					List<Map<String, Object>> list = asListMap(rset);
					if (list.isEmpty()) {
						response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
					} else {
						response.put("responseCode", XAPI_APPROVED);
						response.put("account_list", list);
					}
				}
			}

		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return response;
	}

	public Map<String, Object> getBankList() {
		Map<String, Object> response = new HashMap<String, Object>();
		try (Statement statement = conn.createStatement();
				ResultSet resultSet = statement.executeQuery("select * from v_bank_list")) {
			List<Map<String, Object>> mapList = asListMap(resultSet);
			if (mapList != null && !mapList.isEmpty()) {
				response.put("responseCode", XAPI_APPROVED);
				response.put("bank_list", mapList);
			} else {
				response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
			}
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return response;
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

	public Map<String, Object> processNibssNameEnquiry(String acct_no, String bank_code) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			HttpHandler handler = new HttpHandler();
			EnquireNameResponseReturn nameEnquiry = handler.nibssNameLookup(acct_no, bank_code);
			if (nameEnquiry != null) {
				response.put("responseCode", nameEnquiry.getResponseCode());
				response.put("account_title", nameEnquiry.getAccountName());
				response.put("account_type", "NIL");
				response.put("account_no", StringUtils.stripDashes(acct_no));
				response.put("currency", "NIL");
				response.put("responseTxt", nibssResponse(nameEnquiry.getResponseCode()));
			} else
				response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return response;
	}

	public Map<String, Object> internalNameLookup(String acctno) {
		Map<String, Object> map = new HashMap<String, Object>();
		String acct = StringUtils.stripDashes(acctno);
		try (Statement statement = conn.createStatement();
				ResultSet resultSet = statement
						.executeQuery(getBuilder(true).append("select * from v_nameLookup where nuban_acct = '")
								.append(acct).append("' or account_no = '").append(acct).append("'").toString())) {
			map = asMap(resultSet);
			map.remove("nuban_acct");
			map.put("responseCode", map.isEmpty() ? NO_CUSTOMER_RECORD : XAPI_APPROVED);
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return map;
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
			tran.setProcCode("31");
			tran.setPsAcctNo1(StringUtils.appendDash(acct_no));
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

	public Map<String, Object> loanAccountInquiry(String acct_no, String acct_type) {
		Map<String, Object> response = new HashMap<String, Object>();

		try (CallableStatement callableStatement = conn.prepareCall(SQL.LOAN_DISPLAY)) {
			List<Map<String, Object>> list = new ArrayList<>();
			callableStatement.setString(1, StringUtils.appendDash(acct_no));
			callableStatement.setString(2, acct_type);
			try (ResultSet resultSet = callableStatement.executeQuery()) {
				list = asListMap(resultSet);
			}
			if (!list.isEmpty()) {
				response.put("responseCode", XAPI_APPROVED);
				response.put("loan_details", list);
			} else {
				response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
			}
		} catch (SQLException ex) {
			ApiLogger.getLogger().error(ex);
		}
		return response;
	}

	public Map<String, Object> accountStatement(String acct_no) {
		Map<String, Object> response = new HashMap<String, Object>();

		try (CallableStatement callableStatement = conn.prepareCall(SQL.MOBILE_MINI_STMT)) {
			List<Map<String, Object>> list = new ArrayList<>();
			callableStatement.setString(1, StringUtils.appendDash(acct_no));
			callableStatement.setString(2, XapiCodes.DEFAULT_USER);
			try (ResultSet resultSet = callableStatement.executeQuery()) {
				list = asListMap(resultSet);
			}
			if (!list.isEmpty()) {
				response.put("responseCode", XAPI_APPROVED);
				response.put("history", list);
			} else {
				response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
			}
		} catch (SQLException e) {
			ApiLogger.getLogger().error(e);
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

	public AccountMap accountName(String accountNo, boolean nuban) {
		AccountMap accountMap = new AccountMap();
		try (Statement stmt = conn.createStatement()) {

			try (ResultSet rset = stmt
					.executeQuery(getBuilder(true).append("select a.value, b.title_1, b.acct_no from ")
							.append(XapiCodes.coreschema).append("..gb_user_defined a, ").append(XapiCodes.coreschema)
							.append("..dp_acct b where a.field_id=45 and ")
							.append(nuban ? "a.acct_no_key = '" : "b.acct_no = '").append(accountNo)
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
		Map<String, Object> response = new HashMap<String, Object>();

		try {
			XapiTran tran = buildTransfer(transfer);
			tran.setProcCode("40");

			if ("20".equalsIgnoreCase(transfer.getTran_code())) {
				// nibss transfer. check the recipient account name and number
				if (transfer.getRecipient_account_no() == null
						|| "".equalsIgnoreCase(transfer.getRecipient_account_no().trim())) {
					response.put("responseCode", "21");
					response.put("responseTxt", "Target account required for NIBSS transfer");
				} else {

					AccountMap fromAccount = accountName(tran.getPsAcctNo1(), true);
					if (fromAccount.getNubanAccount() == null) {
						// no nuban account equivalent
						response.put("responseCode", "58");
						response.put("responseTxt",
								"Unable to locate corresponding source nuban account for " + tran.getPsAcctNo1());
						return response;
					}

					tran.setPsAcctNo1(fromAccount.getNubanAccount());
					tran.setAcctName(fromAccount.getAccountTitle());

					if (isNotEmpty(transfer.getRecipient_account_name()) && isNotEmpty(transfer.getDescription()))
						tran.setPsDescription(String.format("FROM %s TO %s/%s", fromAccount.getAccountTitle(),
								transfer.getRecipient_account_name(), transfer.getDescription()));
					else if (isNotEmpty(transfer.getRecipient_account_name()) && isEmpty(transfer.getDescription()))
						tran.setPsDescription(String.format("FROM %s TO %s", fromAccount.getAccountTitle(),
								transfer.getRecipient_account_name()));
					else if (isEmpty(transfer.getRecipient_account_name()) && isNotEmpty(transfer.getDescription()))
						tran.setPsDescription(
								String.format("FROM %s/%s", fromAccount.getAccountTitle(), transfer.getDescription()));
					else
						tran.setPsDescription(String.format("FROM %s", fromAccount.getAccountTitle()));

					String returnCode = new NibssClient().postNIPTransfer(tran.getAcctName(),
							StringUtils.stripDashes(tran.getPsAcctNo1()), tran.getTranLocation(),
							tran.getPsDescription(), tran.getPsAcctNo2().replaceAll("-", "").trim(),
							tran.getPsTerminalID(), tran.getPnAmt1(), tran.getPnReferenceNo()).trim();

					if (returnCode != null) {
						response.put("responseCode", returnCode);
						response.put("responseTxt", nibssResponse(returnCode));
					} else {
						response.put("responseCode", "96");
						response.put("responseTxt", XapiCodes.getErrorDesc(96));
					}
				}
			} else {
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

				// might cause issues when transfering to account without nuban equivalent
				AccountMap fromAccount = accountName(tran.getPsAcctNo1(), false);
				if (fromAccount.getLocalAccount() == null) {
					// no local account account equivalent
					response.put("responseCode", "58");
					response.put("responseTxt", "Unable to authenticate account details for " + tran.getPsAcctNo1());
					return response;
				}

				if (isNotEmpty(transfer.getRecipient_account_name()) && isNotEmpty(transfer.getDescription()))
					tran.setPsDescription(String.format("FROM %s TO %s/%s", fromAccount.getAccountTitle(),
							transfer.getRecipient_account_name(), transfer.getDescription()));
				else if (isNotEmpty(transfer.getRecipient_account_name()) && isEmpty(transfer.getDescription()))
					tran.setPsDescription(String.format("FROM %s TO %s", fromAccount.getAccountTitle(),
							transfer.getRecipient_account_name()));
				else if (isEmpty(transfer.getRecipient_account_name()) && isNotEmpty(transfer.getDescription()))
					tran.setPsDescription(
							String.format("FROM %s/%s", fromAccount.getAccountTitle(), transfer.getDescription()));
				else
					tran.setPsDescription(String.format("FROM %s", fromAccount.getAccountTitle()));

				tran.setBillable(true);
				tran.setPnReferenceNo(RefUtils.get());
				if (processTxn(tran)) {
					response.put("available_bal", tran.getAvailableBal());
					response.put("current_bal", tran.getCurrentBal());
				}
				response.put("responseCode", tran.getResponseCode());
				response.put("responseTxt", tran.getResponseTxt());
			}
			response.put("reference", tran.getPnReferenceNo());
		} catch (Exception ex) {
			ApiLogger.getLogger().error(ex);
		}
		return response;
	}

	boolean isNotEmpty(String string) {
		return string != null && !string.trim().isEmpty();
	}

	boolean isEmpty(String string) {
		return string == null || string.isEmpty();
	}

	XapiTran buildUtility(BillRequest billRequest) {
		XapiTran tran = new XapiTran();
		tran.setProcCode("50");
		tran.setPsAcctNo1(StringUtils.appendDash(billRequest.getAccount_no()));
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

	private XapiTran buildTransfer(TransferRequest transfer) {
		XapiTran tran = new XapiTran();
		tran.setPsAcctNo1(StringUtils.appendDash(transfer.getAccount_no()));
		tran.setPsAcctNo2(StringUtils.appendDash(transfer.getRecipient_account_no()));
		tran.setPnAmt1(transfer.getTran_amount());
		tran.setPsTerminalID(transfer.getRecipient_bank_code());
		return tran;
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
				if (!rset.next())
					return false;
				acctType = rset.getString(1);
			}
			alertChargeStmt.registerOutParameter(1, Types.INTEGER);
			alertChargeStmt.setString(2, acct);
			alertChargeStmt.setString(3, acctType);
			alertChargeStmt.execute();
			return alertChargeStmt.getInt(1) > 0;
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
