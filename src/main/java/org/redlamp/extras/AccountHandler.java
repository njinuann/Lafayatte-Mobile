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
import org.redlamp.io.MapUtils;
import org.redlamp.io.StringUtils;
import org.redlamp.logger.ApiLogger;
import org.redlamp.model.AccountMap;
import org.redlamp.model.AccountRequest;
import org.redlamp.model.AlertRequest;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

import NIPClient.Channel.EnquireNameResponseReturn;

public class AccountHandler implements AutoCloseable, ISO, SQL {

	private Connection conn;
	private StringBuilder builder;
	private AlertRequest alertRequest;

	public AccountHandler() {
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

	public Map<String, Object> depositAccountList(String acct_no) {
		Map<String, Object> response = new HashMap<String, Object>();

		AccountMap fromAccount = accountName(acct_no, true);
		if (fromAccount.getNubanAccount() == null) {
			response.put("responseCode", "58");
			response.put("responseTxt", "Unable to locate account " + acct_no);
			return response;
		}

		try (CallableStatement callableStatement = conn.prepareCall(DEPOSIT_ACCT_LIST)) {
			ArrayList<Map<String, Object>> list = new ArrayList<>();
			callableStatement.setString(1, fromAccount.getLocalAccount());
			try (ResultSet rset = callableStatement.executeQuery()) {
				if (rset != null && rset.isBeforeFirst()) {
					ResultSetMetaData meta = rset.getMetaData();
					while (rset.next()) {
						Map<String, Object> item = new HashMap<>();
						for (int i = 1; i <= meta.getColumnCount(); i++) {
							item.put(meta.getColumnName(i), rset.getObject(i));
						}
						list.add(item);
					}
				}
			}
			if (!list.isEmpty()) {
				response.put("responseCode", XAPI_APPROVED);
				response.put("account_list", list);
			} else {
				response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
			}
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return response;
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
		try (Statement statement = conn.createStatement();
				ResultSet resultSet = statement
						.executeQuery(getBuilder(true).append("select * from v_nameLookup where nuban_acct = '")
								.append(acctno).append("'").toString())) {
			map = asMap(resultSet);
			if (map != null)
				map.put("responseCode", map.isEmpty() ? NO_CUSTOMER_RECORD : XAPI_APPROVED);
			else {
				map = MapUtils.buildMap(map);
				map.put("responseCode", UNABLE_TO_LOCATE_RECORD);
			}
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return map;
	}

	public Map<String, Object> accountStatement(String acct_no) {
		Map<String, Object> response = new HashMap<String, Object>();

		AccountMap fromAccount = accountName(acct_no, true);
		if (fromAccount.getNubanAccount() == null) {
			response.put("responseCode", "58");
			response.put("responseTxt", "Unable to locate nuban entry for account " + acct_no);
			return response;
		}

		try (CallableStatement callableStatement = conn.prepareCall(SQL.MOBILE_MINI_STMT)) {
			List<Map<String, Object>> list = new ArrayList<>();
			callableStatement.setString(1, fromAccount.getLocalAccount());
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

	public Map<String, Object> loanBillSchedule(String acct_no) {
		Map<String, Object> response = new HashMap<String, Object>();

		try (CallableStatement callableStatement = conn.prepareCall(LOAN_BILL_SCHEDULE)) {
			ArrayList<Map<String, Object>> list = new ArrayList<>();
			callableStatement.setString(1, StringUtils.appendDash(acct_no));
			try (ResultSet rset = callableStatement.executeQuery()) {
				if (rset != null && rset.isBeforeFirst()) {
					ResultSetMetaData meta = rset.getMetaData();
					while (rset.next()) {
						Map<String, Object> item = new HashMap<>();
						for (int i = 1; i <= meta.getColumnCount(); i++)
							item.put(meta.getColumnName(i), rset.getObject(i));
						list.add(item);
					}
				}
			}
			if (!list.isEmpty()) {
				response.put("responseCode", XAPI_APPROVED);
				response.put("bill_schedule", list);
			} else {
				response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
			}
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return response;
	}

	public Map<String, Object> loanAccounts(Integer cust_no) {
		Map<String, Object> response = new HashMap<String, Object>();

		try (CallableStatement callableStatement = conn.prepareCall(LOAN_ACCT_LIST)) {
			ArrayList<Map<String, Object>> list = new ArrayList<>();
			callableStatement.setInt(1, cust_no);
			try (ResultSet rset = callableStatement.executeQuery()) {
				if (rset != null && rset.isBeforeFirst()) {
					ResultSetMetaData meta = rset.getMetaData();
					while (rset.next()) {
						Map<String, Object> item = new HashMap<>();
						for (int i = 1; i <= meta.getColumnCount(); i++)
							item.put(meta.getColumnName(i), rset.getObject(i));
						list.add(item);
					}
				}
			}
			if (list.isEmpty()) {
				response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
			} else {
				response.put("responseCode", XAPI_APPROVED);
				response.put("account_list", list);
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

	// NEWLY ADDED APIS

	public Map<String, Object> createAccount(AccountRequest accountRequest) {
		Map<String, Object> response = new HashMap<String, Object>();

		try (CallableStatement callableStatement = conn.prepareCall(SQL.DP_ACCT_CREATION)) {

			callableStatement.setLong(1, accountRequest.getCust_no());
			callableStatement.setString(2, accountRequest.getAccount_type());
			callableStatement.setLong(3, accountRequest.getClass_code());
			callableStatement.setString(4, XapiPool.userId);

			callableStatement.registerOutParameter(5, Types.VARCHAR);
			callableStatement.registerOutParameter(6, Types.INTEGER);
			callableStatement.executeUpdate();
			int returnCode = callableStatement.getInt(6);
			// Oops, failed to create the account
			if (returnCode != 0) {
				response.put("responseCode", returnCode);
				return response;
			}
			// get the newly created account
			String newAccount = callableStatement.getString(5);
			try (Statement statement = conn.createStatement();
					ResultSet resultSet = statement.executeQuery(getBuilder(true)
							.append("select a.value, b.title_1, b.acct_no, b.rim from ").append(XapiCodes.coreschema)
							.append("..gb_user_defined a, ").append(XapiCodes.coreschema)
							.append("..dp_acct b where a.field_id=45 and b.acct_no = '").append(newAccount)
							.append("' and b.acct_no = a.acct_no_key").toString())) {

				Map<String, Object> asMap = asMap(resultSet);
				if (newAccount != null && !newAccount.isEmpty()) {
					response.put("responseCode", XAPI_APPROVED);
					response.put("account_info", asMap);
				} else {
					response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
				}

			} catch (Exception e1) {
				ApiLogger.getLogger().error(e1);
			}

		} catch (SQLException ex) {
			ApiLogger.getLogger().error(ex);
		}
		return response;
	}

	public Map<String, Object> findByRim(Integer rim) {
		Map<String, Object> response = new HashMap<String, Object>();
		try (Statement statement = conn.createStatement();
				ResultSet resultSet = statement
						.executeQuery(getBuilder(true).append("select a.value, b.title_1, b.acct_no, b.rim from ")
								.append(XapiCodes.coreschema).append("..gb_user_defined a, ")
								.append(XapiCodes.coreschema).append("..dp_acct b where a.field_id=45 and b.rim = ")
								.append(rim).append(" and b.acct_no = a.acct_no_key").toString())) {
			List<Map<String, Object>> mapList = asListMap(resultSet);
			if (mapList != null && !mapList.isEmpty()) {
				response.put("responseCode", XAPI_APPROVED);
				response.put("account_info", mapList);
			} else {
				response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
			}
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return response;
	}

	public Map<String, Object> findByAccount(String account) {
		Map<String, Object> response = new HashMap<String, Object>();
		try (Statement statement = conn.createStatement();
				ResultSet resultSet = statement.executeQuery(getBuilder(true)
						.append("select a.value, b.title_1, b.acct_no, b.rim from ").append(XapiCodes.coreschema)
						.append("..gb_user_defined a, ").append(XapiCodes.coreschema)
						.append("..dp_acct b where a.field_id=45 and a.acct_no_key = '").append(account)
						.append("' and b.acct_no = a.acct_no_key").toString())) {
			List<Map<String, Object>> mapList = asListMap(resultSet);
			if (mapList != null && !mapList.isEmpty()) {
				response.put("responseCode", XAPI_APPROVED);
				response.put("account_info", mapList);
			} else {
				response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
			}
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return response;
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

	public Map<String, Object> findAccountCreationFeatures() {

		Map<String, Object> response = new HashMap<String, Object>();
		try (Statement statement = conn.createStatement()) {

			List<Map<String, Object>> identity_types = fetchListMap("", statement);
			response.put("account_types", identity_types);

			List<Map<String, Object>> marketing_types = fetchListMap("select * from Ad_Dp_cls where status = 'Active'",
					statement);
			response.put("class_codes", marketing_types);

			response.put("responseCode", XAPI_APPROVED);

		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return response;
	}

	public List<Map<String, Object>> fetchListMap(String query, Statement statement) {
		try (ResultSet resultSet = statement.executeQuery(query)) {
			List<Map<String, Object>> mapList = asListMap(resultSet);
			return mapList;
		} catch (Exception e1) {
			ApiLogger.getLogger().error(e1);
		}
		return null;
	}

}
