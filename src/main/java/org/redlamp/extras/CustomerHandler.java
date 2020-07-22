package org.redlamp.extras;

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

import org.redlamp.interfaces.ISO;
import org.redlamp.interfaces.SQL;
import org.redlamp.io.StringUtils;
import org.redlamp.logger.ApiLogger;
import org.redlamp.model.CustomerRequest;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

public class CustomerHandler implements AutoCloseable, ISO, SQL {

	private Connection conn;

	public CustomerHandler() {
		try {
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

	public Map<String, Object> createCustomer(CustomerRequest customerRequest) {
		Map<String, Object> response = new HashMap<String, Object>();

		try (CallableStatement callableStatement = conn.prepareCall(SQL.CUSTOMER_CREATION)) {

			callableStatement.setString(1, customerRequest.getFirst_name());
			callableStatement.setString(2, customerRequest.getLast_name());
			callableStatement.setDate(3, new java.sql.Date(customerRequest.getBirth_date().getTime()));
			callableStatement.setString(4, customerRequest.getAddress_1());
			callableStatement.setString(5, customerRequest.getAddress_2());
			callableStatement.setString(6, customerRequest.getAddress_3());

			callableStatement.setString(7, customerRequest.getTown());
			callableStatement.setString(8, customerRequest.getDistrict());
			callableStatement.setString(9, customerRequest.getResidence());
			callableStatement.setString(10, customerRequest.getCounty());
			callableStatement.setString(11, customerRequest.getCity());
			callableStatement.setString(12, customerRequest.getPhone_number());
			callableStatement.setString(13, customerRequest.getGender());
			// account title
			callableStatement.setString(14, customerRequest.getFirst_name() + customerRequest.getLast_name());
			callableStatement.setLong(15, customerRequest.getClass_code());
			callableStatement.setString(16, XapiPool.userId);

			callableStatement.setLong(17, customerRequest.getIdentity_type_id());
			callableStatement.setString(18, customerRequest.getIdentity_no());
			callableStatement.setDate(19, new java.sql.Date(customerRequest.getId_issue_date().getTime()));
			callableStatement.setDate(20, new java.sql.Date(customerRequest.getId_expiry_date().getTime()));
			callableStatement.setString(21, customerRequest.getMarital_status());
			callableStatement.setLong(22, customerRequest.getOccupation_id());

			callableStatement.registerOutParameter(23, Types.NUMERIC);
			callableStatement.registerOutParameter(24, Types.INTEGER);

			callableStatement.setString(25, customerRequest.getHome_address_1());
			callableStatement.setString(26, customerRequest.getHome_address_2());
			callableStatement.setString(27, customerRequest.getHome_address_3());
			callableStatement.setLong(28, customerRequest.getMarketing_info_id());
			callableStatement.setLong(29, customerRequest.getRisk_code_id());
			callableStatement.setLong(30, customerRequest.getOpening_reason_id());
			callableStatement.setLong(31, customerRequest.getTitle_id());

			callableStatement.executeUpdate();
			int returnCode = callableStatement.getInt(23);

			// Oops, failed to create the account
			if (returnCode != 0) {
				response.put("responseCode", returnCode);
				return response;
			}

			// get the newly created customer rim
			BigDecimal rimNumber = callableStatement.getBigDecimal(24);
			try (Statement statement = conn.createStatement();
					ResultSet resultSet = statement.executeQuery(
							new StringBuilder().append("select status, rim_no from ").append(XapiCodes.coreschema)
									.append("..rm_acct where rim_no = ").append(rimNumber).toString())) {

				Map<String, Object> asMap = asMap(resultSet);
				if (rimNumber != null) {
					response.put("responseCode", XAPI_APPROVED);
					response.put("cust_info", asMap);
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

	public Map<String, Object> findRimCreationFeatures() {

		Map<String, Object> response = new HashMap<String, Object>();
		try (Statement statement = conn.createStatement()) {

			List<Map<String, Object>> identity_types = fetchListMap("", statement);
			response.put("identity_types", identity_types);

			List<Map<String, Object>> marketing_types = fetchListMap("", statement);
			response.put("marketing_types", marketing_types);

			List<Map<String, Object>> risk_code_types = fetchListMap("", statement);
			response.put("risk_code_types", risk_code_types);

			List<Map<String, Object>> opening_reason_types = fetchListMap("", statement);
			response.put("opening_reason_types", opening_reason_types);

			List<Map<String, Object>> title_types = fetchListMap("", statement);
			response.put("title_types", title_types);

			List<Map<String, Object>> occupation_types = fetchListMap("", statement);
			response.put("occupation_types", occupation_types);

			List<Map<String, Object>> class_code_types = fetchListMap("", statement);
			response.put("class_code_types", class_code_types);

			List<Map<String, Object>> counties = fetchListMap("", statement);
			response.put("counties", counties);

			List<Map<String, Object>> districts = fetchListMap("", statement);
			response.put("district_list", districts);

			List<Map<String, Object>> cities = fetchListMap("", statement);
			response.put("cities", cities);

			List<Map<String, Object>> towns = fetchListMap("", statement);
			response.put("towns", towns);

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
