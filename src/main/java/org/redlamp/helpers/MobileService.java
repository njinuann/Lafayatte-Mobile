package org.redlamp.helpers;

import java.math.BigDecimal;
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

import org.jpos.iso.ISOMsg;
import org.redlamp.interfaces.ISO;
import org.redlamp.interfaces.SQL;
import org.redlamp.logger.IsoLogger;
import org.redlamp.logger.ApiLogger;
import org.redlamp.model.XapiTran;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

public class MobileService implements AutoCloseable, SQL {

	private StringBuilder xapiRequest;
	private ISOMsg respMsg;
	private XapiTran txnData;

	public MobileService(ISOMsg reqMsg, ISOMsg respMsg) {
		super();
		this.respMsg = respMsg;
		this.txnData = new XapiTran();
		this.xapiRequest = new StringBuilder();
		buildTxnObject(reqMsg);
	}

	private void buildTxnObject(ISOMsg reqMsg) {
		txnData.setProcCode(reqMsg.getString(3).substring(0, 2));
		txnData.setPnReferenceNo(reqMsg.getString(11) + "-" + reqMsg.getString(7));
		txnData.setPsISOCurrency(XapiCodes.convertCurrency(reqMsg.getString(49)));
		if (reqMsg.hasField("127.33"))
			txnData.setBillerCode(reqMsg.getString("127.33"));

		txnData.setPsCardNo(reqMsg.getString(2));
		txnData.setPsAcctNo1(toOrbitAccount(reqMsg.getString("102")));
		txnData.setPsAcctNo2(toOrbitAccount(reqMsg.getString("103")));
		txnData.setBankCode(reqMsg.getString(32));
		txnData.setPnAmt1(getTranAmount(reqMsg).doubleValue());
		try {
			if (reqMsg.getMTI().startsWith("042") && reqMsg.hasField(90)) {
				txnData.setPnOrigReferenceNo(
						reqMsg.getString(90).substring(4, 10) + "-" + reqMsg.getString(90).substring(10, 20));
				txnData.setPsReversal("Y");
			} else {
				txnData.setPsReversal("N");
			}
		} catch (Exception e) {
			IsoLogger.getLogger().error(e);
		}
	}

	private BigDecimal getTranAmount(ISOMsg reqMsg) {
		if (reqMsg.hasField(4)) {
			try {
				return new BigDecimal(reqMsg.getString(4)).divide(BigDecimal.valueOf(100)).setScale(2);
			} catch (Exception e) {
				IsoLogger.getLogger().error(e);
			}
		}
		return BigDecimal.ZERO;
	}

	private void selectTxn(String procCode) {

		txnData.setTxnPermitted(true);
		switch (procCode) {
		case "31":
			txnData.setPnTranCode(100);
			txnData.setPsDescription("USSD Bal Enq Chrg");
			break;
		case "38":
			txnData.setPnTranCode(200);
			txnData.setPsDescription("USSD MiniStmnt Chrg");
			break;
		case "40":
			txnData.setPnTranCode(850);
			txnData.setPsDescription("USSD Transfer [" + txnData.getPsAcctNo1() + " - " + txnData.getPsAcctNo2() + "]");
			break;
		case "50":
			if (respMsg.hasField(103)) { // interbank transfer
				txnData.setPnTranCode(750);
				txnData.setPsDescription("USSD QT Transfer");
				if (XapiPool.QT_CC != null)
					txnData.setChargeFlag(XapiPool.QT_CC);
			} else {
				txnData.setPnTranCode(810);
				txnData.setPsDescription("USSD Bill Payment");
			}
			break;
		case "00":
			txnData.setPnTranCode(810);
			txnData.setPsDescription("USSD Airtime Purchase");
			break;
		default:
			txnData.setTxnPermitted(false);
			break;
		}
	}

	private String toOrbitAccount(String acctNo) {
		if (acctNo != null && acctNo.length() > 4)
			acctNo = acctNo.contains("-") ? acctNo : acctNo.substring(0, 3) + "-" + acctNo.substring(3);
		return acctNo;
	}

	protected String toISOAccount(String acctNo) {
		if (acctNo != null && acctNo.contains("-"))
			return acctNo.replaceAll("-", "");
		return acctNo;
	}

	@Override
	public void close() {
		xapiRequest.setLength(0);
	}

	public ISOMsg postTxn() {
		try (Connection conn = XapiPool.getConnection()) {

			try (CallableStatement validateTxn = conn.prepareCall(SQL.VALIDATE_TXN);
					CallableStatement main2Statement = conn.prepareCall(MOB_CALLABLE);
					Statement stmt = conn.createStatement()) {

				txnData.setBillable(true);
				selectTxn(txnData.getProcCode());

				if (txnData.isTxnPermitted()) {

					if (txnData.getBillerCode() != null) {
						xapiRequest.setLength(0);
						xapiRequest.append("select txnType, tranDesc from eb_charges where serviceID = ")
								.append(MOBILE_SERVICE).append(" and biller_code = '").append(txnData.getBillerCode())
								.append("' and status = 'Active'");
						if (XapiPool.DEBUG_ENABLED)
							ApiLogger.getLogger().info(xapiRequest);
						// try to retrieve the charge flags
						try (ResultSet rset = stmt.executeQuery(xapiRequest.toString())) {
							if (rset != null && rset.next()) {
								txnData.setChargeFlag(rset.getString("txnType"));
								txnData.setPsDescription(rset.getString("tranDesc"));
							}
						} catch (Exception e) {
							ApiLogger.getLogger().error(e);
						}
					}

					xapiRequest.setLength(0);
					if (isValidTxn(validateTxn)) {
						handleTxn(main2Statement);
						if ("00".equals(txnData.getResponseCode()) && !"31".equals(txnData.getProcCode())
								&& !"Y".equalsIgnoreCase(txnData.getPsReversal())) {
							txnData.setBillable(false);
							selectTxn("31");
							handleTxn(main2Statement);
						}
						respMsg.set(39, txnData.getResponseCode());
					}
				} else {
					respMsg.set(39, ISO.TRANSACTION_NOT_PERMITTED_ON_TERMINAL);
				}
			}

		} catch (Exception e) {
			respMsg.set(39, ISO.SYSTEM_ERROR);
			IsoLogger.getLogger().error(e);
		}
		return respMsg;
	}

	public boolean isValidTxn(CallableStatement validateTxn) {
		try {
			validateTxn.registerOutParameter(1, Types.INTEGER);
			if (txnData.isBillable())
				validateTxn.setString(2, txnData.getPnReferenceNo());
			else
				validateTxn.setString(2, "C" + txnData.getPnReferenceNo());
			validateTxn.setString(3, txnData.getPnOrigReferenceNo());
			validateTxn.setString(4, txnData.getPsReversal());
			validateTxn.setString(5, txnData.getPsCardNo());
			validateTxn.setInt(6, txnData.getPnTranCode());
			validateTxn.setString(7, txnData.getPsISOCurrency());
			validateTxn.setString(8, txnData.getPsAcctType1());
			validateTxn.setString(9, txnData.getPsAcctNo1());
			validateTxn.setString(10, txnData.getPsAcctType2());
			validateTxn.setString(11, txnData.getPsAcctNo2());
			validateTxn.setDouble(12, txnData.getPnAmt1());
			validateTxn.setDouble(13, txnData.getPnAmt2());
			validateTxn.setString(14, txnData.getChargeFlag());
			validateTxn.setInt(15, 0);
			validateTxn.execute();
			try {
				// need to call again to process output params
				validateTxn.execute();
				int returnCode = validateTxn.getInt(1);
				if (returnCode == 0) {
					return true;
				}
				txnData.setResponseCode(errorMap(returnCode));
			} catch (Exception e1) {
				IsoLogger.getLogger().error(e1);
			}
		} catch (Exception e) {
			IsoLogger.getLogger().error(e);
			txnData.setResponseCode(ISO.SYSTEM_ERROR);
		}
		return true;
	}

	private boolean handleTxn(CallableStatement main2Statement) {
		try {

			xapiRequest.append(44).append(", ");
			main2Statement.registerOutParameter(1, Types.INTEGER);

			xapiRequest.append("'").append(txnData.getPdtTranDt()).append("', ");
			main2Statement.setDate(2, txnData.getPdtTranDt());

			xapiRequest.append("'").append(txnData.getPdtEffectiveDt()).append("', ");
			main2Statement.setDate(3, txnData.getPdtEffectiveDt());

			xapiRequest.append("'").append(txnData.getPsTerminalID()).append("', ");
			main2Statement.setString(4, txnData.getPsTerminalID());

			xapiRequest.append("'").append(txnData.getPsProprietaryATM()).append("',");
			main2Statement.setString(5, txnData.getPsProprietaryATM());

			xapiRequest.append("'").append(txnData.getPsATMSwitchID()).append("', ");
			main2Statement.setString(6, txnData.getPsATMSwitchID());

			xapiRequest.append("'").append(txnData.getPsMannedDevice()).append("',");
			main2Statement.setString(7, txnData.getPsMannedDevice());

			if (txnData.isBillable()) {
				xapiRequest.append("'").append(txnData.getPnReferenceNo()).append("', ");
				main2Statement.setString(8, txnData.getPnReferenceNo());
			} else {
				xapiRequest.append("'").append(txnData.getPnReferenceNo()).append("', ");
				main2Statement.setString(8, "C" + txnData.getPnReferenceNo());
			}

			xapiRequest.append("'").append(txnData.getPnOrigReferenceNo()).append("', ");
			main2Statement.setString(9, txnData.getPnOrigReferenceNo());

			xapiRequest.append("'").append(txnData.getPsOffline()).append("', ");
			main2Statement.setString(10, txnData.getPsOffline());

			xapiRequest.append("'").append(txnData.getPsReversal()).append("', ");
			main2Statement.setString(11, txnData.getPsReversal());

			xapiRequest.append("'").append(txnData.getPsCardNo()).append("', ");
			main2Statement.setString(12, txnData.getPsCardNo());

			xapiRequest.append("null, ");
			main2Statement.setString(13, null);

			xapiRequest.append("'").append(txnData.getPsCardNo()).append("', ");
			main2Statement.setString(14, txnData.getPsCardNo());

			xapiRequest.append(txnData.getPnTranCode()).append(", ");
			main2Statement.setInt(15, txnData.getPnTranCode());

			xapiRequest.append("'").append(txnData.getPsISOCurrency()).append("', ");
			main2Statement.setString(16, txnData.getPsISOCurrency());

			xapiRequest.append("0, ");
			main2Statement.setInt(17, 0);

			xapiRequest.append("'").append(txnData.getPsApplType1()).append("', ");
			main2Statement.setString(18, txnData.getPsApplType1());

			xapiRequest.append("'").append(txnData.getPsAcctType1()).append("', ");
			main2Statement.setString(19, txnData.getPsAcctType1());

			xapiRequest.append("'").append(txnData.getPsAcctNo1()).append("', ");
			main2Statement.setString(20, txnData.getPsAcctNo1());

			xapiRequest.append("'").append(txnData.getPsApplType2()).append("', ");
			main2Statement.setString(21, txnData.getPsApplType2());

			xapiRequest.append("'").append(txnData.getPsAcctType2()).append("', ");
			main2Statement.setString(22, txnData.getPsAcctType2());

			xapiRequest.append("'").append(txnData.getPsAcctNo2()).append("', ");
			main2Statement.setString(23, txnData.getPsAcctNo2());

			xapiRequest.append(txnData.getPnAmt1()).append(", ");
			main2Statement.setDouble(24, txnData.getPnAmt1());

			xapiRequest.append(txnData.getPnAmt2()).append(", ");
			main2Statement.setDouble(25, txnData.getPnAmt2());

			xapiRequest.append(txnData.getPnCheckNo1()).append(", ");
			main2Statement.setDouble(26, txnData.getPnCheckNo1());

			xapiRequest.append(txnData.getPnCheckNo2()).append(", ");
			main2Statement.setDouble(27, txnData.getPnCheckNo2());

			xapiRequest.append("null, ");
			main2Statement.setString(28, null);

			xapiRequest.append("null, ");
			main2Statement.setString(29, null);

			xapiRequest.append("0, ");
			main2Statement.setInt(30, 0);

			xapiRequest.append("'").append(txnData.getPsRegEDescription()).append("', ");
			main2Statement.setString(31, txnData.getPsRegEDescription());

			xapiRequest.append("'").append(txnData.getPsDescription()).append("', ");
			main2Statement.setString(32, txnData.getPsDescription());

			if (txnData.isBillable()) {
				main2Statement.setString(33, txnData.getChargeFlag());
			} else {
				main2Statement.setString(33, null);
			}
			xapiRequest.append("'").append(txnData.getChargeFlag()).append("', ");

			xapiRequest.append("1,");
			main2Statement.setInt(34, 1);

			xapiRequest.append("1,");
			main2Statement.setInt(35, 1);

			xapiRequest.append("'2.0' ,");
			main2Statement.setString(36, "2.0");

			xapiRequest.append("0,");
			main2Statement.setInt(37, 0);

			xapiRequest.append("'DP' ,");
			main2Statement.setString(38, "DP");

			xapiRequest.append("0,");
			main2Statement.setInt(39, 0);

			xapiRequest.append("'SMS' ,");
			main2Statement.setString(40, "SMS");

			xapiRequest.append("0");
			main2Statement.setInt(41, 0);

			if (txnData.getPnTranCode() != 200 && "N".equalsIgnoreCase(txnData.getPsReversal())) {
				try (ResultSet resultset = main2Statement.executeQuery()) {
					if (resultset.next()) {
						ResultSetMetaData metaData = resultset.getMetaData();
						if (metaData.getColumnCount() >= 2) {
							txnData.setCurrentBal(resultset.getDouble(1));
							txnData.setAvailableBal(resultset.getDouble(2));
							respMsg.set(54,
									buildBalanceEnquiryBody(txnData.getCurrentBal(), txnData.getAvailableBal()));
						} else
							respMsg.set(54, buildBalanceEnquiryBody(0, 0));
					}
				} catch (Exception ex) {
					IsoLogger.getLogger().error(ex);
					respMsg.set(54, buildBalanceEnquiryBody(0, 0));
				}
				txnData.setResponseCode(errorMap(main2Statement.getInt(1)));

			} else if (txnData.getPnTranCode() == 200) {
				try (ResultSet resultSet = main2Statement.executeQuery()) {
					ResultSetMetaData metaData = resultSet.getMetaData();
					if (metaData.getColumnCount() >= 6 && resultSet.isBeforeFirst()) {
						ArrayList<Map<String, String>> list = new ArrayList<>();
						while (resultSet.next()) {
							Map<String, String> item = new HashMap<>();
							item.put("tran_date", resultSet.getString(1));
							item.put("tran_flag", resultSet.getString(2));
							item.put("tran_amount", resultSet.getString(3));
							item.put("tran_id", resultSet.getString(5));
							item.put("tran_desc", resultSet.getString(6) != null ? resultSet.getString(6) : "N/A");
							item.put("currency",
									txnData.getPsISOCurrency().matches("-?\\d+(.\\d+)?") ? txnData.getPsISOCurrency()
											: resultSet.getString(8));
							list.add(item);
						}
						txnData.setStatement(list);
						respMsg.set(48, buildMiniStatementBody(list));
					}
				} catch (Exception ex) {
					IsoLogger.getLogger().error(ex);
				}
			} else {
				main2Statement.execute();
			}
			txnData.setResponseCode(errorMap(main2Statement.getInt(1)));
			return true;
		} catch (SQLException ex) {
			IsoLogger.getLogger().error(ex);
		} finally {
			IsoLogger.getLogger().info(xapiRequest.toString().replaceAll("'null'", "null"));
			xapiRequest.setLength(0);
		}
		return false;
	}

	private String buildMiniStatementBody(List<Map<String, String>> statement) {
		StringBuilder builder = new StringBuilder("DATE_TIME|SEQ_NR|TRAN_TYPE|TRAN_AMOUNT|CURR_CODE~");
		try {
			for (Map<String, String> entry : statement) {
				String txnDesc = (entry.get("tran_desc").length() > 40 ? entry.get("tran_desc").substring(0, 40)
						: entry.get("tran_desc")).trim().replaceAll("~", "").replaceAll("|", "");
				builder.append(entry.get("tran_date").substring(0, 19).replaceAll(":", "").replaceAll(" ", "")
						.replaceAll("-", "")).append("|").append(padString(entry.get("tran_id"), 6, '0', true))
						.append("|").append(txnDesc).append("|")
						.append(formatAmount(Double.parseDouble(entry.get("tran_amount")))).append("|")
						.append(respMsg.getString(49)).append("~");
			}
		} catch (Exception ex) {
			IsoLogger.getLogger().error(ex);
		}
		return builder.toString();
	}

	private String padString(String s, int i, char c, boolean flag) {
		StringBuilder buffer = new StringBuilder(s);
		int j = buffer.length();
		if (i > 0 && i > j) {
			for (int k = 0; k <= i; k++) {
				if (flag) {
					if (k < i - j) {
						buffer.insert(0, c);
					}
					continue;
				}
				if (k > j) {
					buffer.append(c);
				}
			}
		}
		return buffer.toString();
	}

	private String buildBalanceEnquiryBody(double current_bal, double avail_bal) {
		StringBuilder builder = new StringBuilder();
		String acct_type = null;
		if (respMsg.hasField(3))
			acct_type = respMsg.getString(3).length() >= 4 ? respMsg.getString(3).substring(2, 4) : "10";
		builder.append(acct_type).append("01").append(respMsg.getString(49)).append((current_bal >= 0.0D ? "C" : "D"))
				.append(formatAmount(current_bal)).append(acct_type).append("02").append(respMsg.getString(49))
				.append((avail_bal >= 0.0D ? "C" : "D")).append(formatAmount(avail_bal));
		return builder.toString();
	}

	private String formatAmount(double amount) {
		BigDecimal amt = (new BigDecimal(amount)).abs().setScale(2, BigDecimal.ROUND_DOWN);
		String amtStr = "000000000000" + String.valueOf(amt).replace(".", "");
		return amtStr.substring(amtStr.length() - 12);
	}

	public XapiTran getTransaction() {
		return txnData;
	}

	public void setTransaction(XapiTran tran) {
		this.txnData = tran;
	}

	public String errorMap(int returnCode) {
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
			if ("21".equals(txnData.getProcCode())) {
				RC = "58";
			} else {
				RC = "51";
			}
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
		case 12:
			RC = "12";
			break;
		case 45:
			RC = "45";
			break;
		default:
			RC = "96";
			break;
		}
		return RC;
	}

}
