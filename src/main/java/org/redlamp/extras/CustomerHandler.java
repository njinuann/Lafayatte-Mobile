package org.redlamp.extras;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

import org.redlamp.interfaces.ISO;
import org.redlamp.interfaces.SQL;
import org.redlamp.io.StringUtils;
import org.redlamp.logger.ApiLogger;
//import org.redlamp.model.AccountMap;
//import org.redlamp.model.CustomerMap;
import org.redlamp.model.CustomerRequest;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;
//import sun.reflect.generics.tree.Tree;

public class CustomerHandler implements AutoCloseable, ISO, SQL
{

    private Connection conn;
    private StringBuilder builder;

    public CustomerHandler()
    {
        try {
            conn = XapiPool.getConnection();
        }
        catch (SQLException ex) {
            ApiLogger.getLogger().error(ex);
        }
    }

    @Override
    public void close()
    {
        try {
            if (conn != null)
                conn.close();
        }
        catch (SQLException e) {
            ApiLogger.getLogger().error(e);
        }
    }

    public Map<String, Object> register(String acct_no, String phone_1)
    {
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
                        }
                        else {
                            response.put("responseCode", ErrorHandler.mapCode(returnout));
                        }
                    }
                    else
                        response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
                }
                else {
                    // no service attached
                    response.put("responseCode", NO_ACTION_TAKEN);
                }
            }
            catch (SQLException ex) {
                ApiLogger.getLogger().error(ex);
                response.put("responseCode", SYSTEM_ERROR);
            }
        }
        catch (SQLException ex) {
            ApiLogger.getLogger().error(ex);
            response.put("responseCode", SYSTEM_ERROR);
        }
        return response;
    }

    public Map<String, Object> createCustomer(CustomerRequest customerRequest)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date());
        c.add(Calendar.YEAR, 1);
        java.util.Date newDate = c.getTime();
        Map<String, Object> response = new HashMap<String, Object>();

        try (CallableStatement callableStatement = conn.prepareCall(SQL.CUSTOMER_CREATION)) {

            callableStatement.setString(1, customerRequest.getFirst_name());
            callableStatement.setString(2, customerRequest.getLast_name());
            callableStatement.setDate(3, Date.valueOf(customerRequest.getBirth_date()));
            callableStatement.setString(4, customerRequest.getAddress_1());
            callableStatement.setString(5, customerRequest.getAddress_1());
            callableStatement.setString(6, customerRequest.getAddress_1());
            callableStatement.setString(7, customerRequest.getCity());//customerRequest.getTown()
            callableStatement.setString(8, customerRequest.getCity());//customerRequest.getDistrict()
            callableStatement.setString(9, "Other");//customerRequest.getResidence()
            callableStatement.setString(10, customerRequest.getState());// customerRequest.getCounty()
            callableStatement.setString(11, customerRequest.getCity());
            callableStatement.setString(12, customerRequest.getPhone_number());
            callableStatement.setString(13, customerRequest.getGender());
            callableStatement.setLong(14, XapiCodes.rimClassCode);//customerRequest.getClass_code()
            callableStatement.setString(15, XapiPool.userId);
            callableStatement.setLong(16, XapiCodes.identityType);//customerRequest.getIdentity_type_id()
            callableStatement.setString(17, customerRequest.getPhone_number()); //customerRequest.getIdentity_no()
            callableStatement.setDate(18, Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())));//customerRequest.getId_expiry_date()
            callableStatement.setDate(19, Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())));//customerRequest.getId_issue_date()
            callableStatement.setLong(20, XapiCodes.marketingId);//customerRequest.getMarketing_info_id()
            callableStatement.setString(21, "");//zip code
            callableStatement.setString(22, "");//email
            callableStatement.setString(23, customerRequest.getFirst_name() + " " + customerRequest.getLast_name());//suffix
            callableStatement.setString(24, customerRequest.getMiddle_name());//middle name
            callableStatement.setLong(25, XapiCodes.defaultBranch);//branch number 110
            callableStatement.registerOutParameter(26, Types.NUMERIC);
            callableStatement.registerOutParameter(27, Types.INTEGER);

            int returnCode = -1;
            BigDecimal rimNumber = new BigDecimal(0);

            try (ResultSet rset = callableStatement.executeQuery()) {
                if (rset != null && rset.isBeforeFirst()) {
                    ResultSetMetaData meta = rset.getMetaData();
                    while (rset.next()) {
                        returnCode = rset.getInt(1);
                        rimNumber = rset.getBigDecimal(2);
                    }
                }
            }

            // Oops, failed to create the account
            if (returnCode != 0) {
                response.put("responseCode", returnCode);
                return response;
            }

            if (insertAdditionalField(customerRequest.getBank_verification_number(), 44, rimNumber.toPlainString(), XapiPool.userId, "RM")) {
                ApiLogger.getLogger().info("BVN inserted");
            }
            else {
                ApiLogger.getLogger().info("BVN not inserted");
            }
            // get the newly created customer rim
            // BigDecimal rimNumber = callableStatement.getBigDecimal(26);
            try (Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         new StringBuilder().append("select status, rim_no, " +
                                 "(select a.value from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..rm_acct b  " +
                                 " where cast(b.rim_no as varchar(15)) = a.acct_no_key and acct_prefix='RM' and field_id =44 and b.rim_no =" + rimNumber + ") as bvn    " +
                                 " from " + XapiCodes.coreschema + "..rm_acct where rim_no = " + rimNumber + "").toString())) {

                Map<String, Object> asMap = asMap(resultSet);
                if (rimNumber != null) {
                    response.put("responseCode", XAPI_APPROVED);
                    response.put("cust_info", asMap);
                }
                else {
                    response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
                }

            }
            catch (Exception e1) {
                ApiLogger.getLogger().error(e1);
            }


        }
        catch (SQLException ex) {
            ApiLogger.getLogger().error(ex);
        }
        return response;
    }

    public Map<String, Object> createCustomerAndDeposit(CustomerRequest customerRequest)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date());
        c.add(Calendar.YEAR, 1);
        java.util.Date newDate = c.getTime();
        StringBuilder stateBuilder = new StringBuilder();
        Map<String, Object> response = new HashMap<String, Object>();

        try (CallableStatement callableStatement = conn.prepareCall(SQL.CUST_AND_DEP_CREATION)) {

            stateBuilder.append("'").append(customerRequest.getFirst_name()).append("', ");
            callableStatement.setString(1, customerRequest.getFirst_name());
            stateBuilder.append("'").append(customerRequest.getLast_name()).append("', ");
            callableStatement.setString(2, customerRequest.getLast_name());
            stateBuilder.append("'").append(Date.valueOf(customerRequest.getBirth_date())).append("', ");
            callableStatement.setDate(3, Date.valueOf(customerRequest.getBirth_date()));
            stateBuilder.append("'").append(customerRequest.getAddress_1()).append("', ");
            callableStatement.setString(4, customerRequest.getAddress_1());
            stateBuilder.append("'").append(customerRequest.getAddress_1()).append("', ");
            callableStatement.setString(5, customerRequest.getAddress_1());
            stateBuilder.append("'").append(customerRequest.getAddress_1()).append("', ");
            callableStatement.setString(6, customerRequest.getAddress_1());
            stateBuilder.append("'").append(customerRequest.getCity()).append("', ");
            callableStatement.setString(7, customerRequest.getCity());//customerRequest.getTown()
            stateBuilder.append("'").append(customerRequest.getCity()).append("', ");
            callableStatement.setString(8, customerRequest.getCity());//customerRequest.getDistrict()
            stateBuilder.append("'").append("Other").append("', ");
            callableStatement.setString(9, "Other");//customerRequest.getResidence()
            stateBuilder.append("'").append(customerRequest.getState()).append("', ");
            callableStatement.setString(10, customerRequest.getState());// customerRequest.getCounty()
            stateBuilder.append("'").append(customerRequest.getCity()).append("', ");
            callableStatement.setString(11, customerRequest.getCity());
            stateBuilder.append("'").append(customerRequest.getPhone_number()).append("', ");
            callableStatement.setString(12, customerRequest.getPhone_number());
            stateBuilder.append("'").append(customerRequest.getGender()).append("', ");
            callableStatement.setString(13, customerRequest.getGender());
            stateBuilder.append(XapiCodes.rimClassCode).append(", ");
            callableStatement.setLong(14, XapiCodes.rimClassCode);//customerRequest.getClass_code()
            stateBuilder.append("'").append(XapiPool.userId).append("', ");
            callableStatement.setString(15, XapiPool.userId);
            stateBuilder.append(XapiCodes.identityType).append(", ");
            callableStatement.setLong(16, XapiCodes.identityType);//customerRequest.getIdentity_type_id()
            stateBuilder.append("'").append(customerRequest.getPhone_number()).append("', ");
            callableStatement.setString(17, customerRequest.getPhone_number()); //customerRequest.getIdentity_no()
            stateBuilder.append("'").append(Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()))).append("', ");
            callableStatement.setDate(18, Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())));//customerRequest.getId_expiry_date()
            stateBuilder.append("'").append(new Date(System.currentTimeMillis())).append("', ");
            callableStatement.setDate(19, new Date(System.currentTimeMillis()));//customerRequest.getId_issue_date()
            stateBuilder.append(XapiCodes.marketingId).append(", ");
            callableStatement.setLong(20, XapiCodes.marketingId);//customerRequest.getMarketing_info_id()
            stateBuilder.append("null").append(", ");
            callableStatement.setString(21, "");//zip code
            stateBuilder.append("null").append(", ");
            callableStatement.setString(22, "");//email
            stateBuilder.append("'").append(customerRequest.getFirst_name() + " " + customerRequest.getLast_name()).append("', ");
            callableStatement.setString(23, customerRequest.getFirst_name() + " " + customerRequest.getLast_name());//suffix
            stateBuilder.append("'").append(customerRequest.getMiddle_name()).append("', ");
            callableStatement.setString(24, customerRequest.getMiddle_name());//middle name
            stateBuilder.append(110).append(", ");
            callableStatement.setLong(25, XapiCodes.defaultBranch);//branch number 110
            stateBuilder.append(XapiCodes.depositClassCode).append(", ");
            callableStatement.setLong(26, XapiCodes.depositClassCode);//customerRequest.getDeposit_class_code()) default to default depsoit class
            stateBuilder.append(0).append(", ");
            callableStatement.registerOutParameter(27, Types.NUMERIC);
            stateBuilder.append("''").append(", ");
            callableStatement.registerOutParameter(28, Types.VARCHAR);
            stateBuilder.append(0).append(", ");
            callableStatement.registerOutParameter(29, Types.INTEGER);

            int returnCode = -1;
            BigDecimal rimNumber = new BigDecimal(0);
            ApiLogger.getLogger().info(stateBuilder);

            try (ResultSet rset = callableStatement.executeQuery()) {
                if (rset != null && rset.isBeforeFirst()) {
                    ResultSetMetaData meta = rset.getMetaData();
                    while (rset.next()) {
                        returnCode = rset.getInt(1);
                        rimNumber = rset.getBigDecimal(2);
                    }
                }
            }

            // Oops, failed to create the account
            if (returnCode != 0) {
                response.put("responseCode", returnCode);
                return response;
            }
            if (insertAdditionalField(customerRequest.getBank_verification_number(), 44, rimNumber.toPlainString(), XapiPool.userId, "RM")) {
                ApiLogger.getLogger().info("BVN inserted");
            }
            else {
                ApiLogger.getLogger().info("BVN not inserted");
            }
            // get the newly created customer rim
            // BigDecimal rimNumber = callableStatement.getBigDecimal(26);
            try (Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         new StringBuilder().append("select a.value as nibs_Acct, b.title_1, b.acct_no, b.rim_no, " +
                                 " (select a.value from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..rm_acct b " +
                                 " where cast(b.rim_no as varchar(15)) = a.acct_no_key and acct_prefix='RM' and field_id =44 and b.rim_no =" + rimNumber + ") as bvn from ").append(XapiCodes.coreschema)
                                 .append("..gb_user_defined a, ").append(XapiCodes.coreschema)
                                 .append("..dp_acct b where a.field_id=45 and b.rim_no = ").append(rimNumber)
                                 .append(" and b.acct_no = a.acct_no_key").toString())) {

                Map<String, Object> asMap = asMap(resultSet);
                if (rimNumber != null) {
                    response.put("responseCode", XAPI_APPROVED);
                    response.put("cust_info", asMap);
                }
                else {
                    response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
                }

            }
            catch (Exception e1) {
                ApiLogger.getLogger().error(e1);
            }



        }
        catch (SQLException ex) {
            ApiLogger.getLogger().error(ex);
        }
        return response;
    }

    public StringBuilder getBuilder(boolean reset)
    {
        if (reset)
            builder.setLength(0);
        return builder;
    }

    Map<String, Object> asMap(ResultSet resultSet)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (resultSet != null && resultSet.isBeforeFirst()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                if (resultSet.next()) {
                    for (int i = 1; i <= metaData.getColumnCount(); i++)
                        map.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
            }
        }
        catch (SQLException e) {
            ApiLogger.getLogger().error(e);
        }
        return map;
    }

    List<Map<String, Object>> asListMap(ResultSet resultSet)
    {
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
        }
        catch (SQLException e) {
            ApiLogger.getLogger().error(e);
        }
        return list;
    }

    public Map<String, Object> findRimCreationFeatures()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> response = new HashMap<String, Object>();
        try (Statement statement = conn.createStatement()) {

            List<Map<String, Object>> title_types = fetchListMap("select title_id, title from " + XapiCodes.coreschema + "..ad_rm_title where title_id in (1,4,9)", statement);
            response.put("title_types", title_types);

            List<Map<String, Object>> states = fetchListMap2("select city from " + XapiCodes.coreschema + "..ei_city", statement);
            response.put("states", states);

            response.put("responseCode", XAPI_APPROVED);

        }
        catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        return response;
    }

    public List<Map<String, Object>> fetchListMap(String query, Statement statement)
    {
        try (ResultSet resultSet = statement.executeQuery(query)) {
            List<Map<String, Object>> mapList = asListMap(resultSet);
            return mapList;
        }
        catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        return null;
    }

    public List<Map<String, Object>> fetchListMap2(String query, Statement statement)
    {
        List<Map<String, Object>> mapList1 = new ArrayList<>();

        Map<String, Object> Objectmap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        //Map<String, Object> Objectmap1 = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<Integer, String> cityMap = new HashMap<>();
        String query2 = "select ptid,state from " + XapiCodes.coreschema + "..ei_state";
        try (ResultSet resultSet = statement.executeQuery(query2)) {
            while (resultSet.next()) {
                cityMap.put(resultSet.getInt(1), resultSet.getString(2));
            }

        }
        catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }

        for (Map.Entry m : cityMap.entrySet()) {
          //  System.out.println(m.getKey() + " " + m.getValue());
            String stateString = ("state".concat(":\"").concat(String.valueOf(m.getValue())).concat("\",\"").concat("cities"));
          //  String stateString = ("state".concat(formatReplaceStr(String.valueOf(m.getValue()))).concat(",").concat(formatReplaceStr("cities")));
          Objectmap.put(stateString, fetchListMap(query + " where state_id =" + m.getKey() + " order by state_id", statement));
        //    Objectmap1.put("state",Objectmap);
        }
        mapList1.add(Objectmap);
        return mapList1;
        //  return null;
    }
    public static String formatReplaceStr(String txt)
    {
        String Fstring = "\"".concat(txt).concat("\"") ;


        return Fstring;
    }
    public boolean insertAdditionalField(String value, int fieldId, String key, String user, String objectType)
    {
        String acctType = "'RM'", applType = "RM", acctPrefix = "RM", cls_id = "";
        switch (objectType.toUpperCase()) {
            case "RM":
                cls_id = "(select class_code from " + XapiCodes.coreschema + "..rm_acct "
                        + "where rim_no = " + key + ")";
                break;
            case "LN":
                cls_id = "(select class_code from " + XapiCodes.coreschema + "..ln_acct "
                        + "where acct_no = '" + key + "')";
                acctType = "(select acct_type from " + XapiCodes.coreschema + "..ln_acct "
                        + "where acct_no = '" + key + "')";
                applType = "LN";
                acctPrefix = "LN";
                break;
        }
        boolean success = false;
        ResultSet rs = null;
        Statement stmt = null;
        PreparedStatement getGbUserDefinePtid = null;
        try {
            getGbUserDefinePtid = conn.prepareCall("{call " + XapiCodes.coreschema + "..psp_class_get_ptid (GB_USER_DEFINED,1)}");
            rs = getGbUserDefinePtid.executeQuery();
            if (rs.next()) {
                int ptid = rs.getInt(1);

                String insertAdditionalField = "Insert into " + XapiCodes.coreschema + "..GB_USER_DEFINED "
                        + "(VALUE, ACCT_TYPE, CLS_ID, FIELD_ID, XTRA_KEY, ACCT_NO_KEY, APPL_TYPE, ACCT_PREFIX, "
                        + "CREATE_DT, EMPL_ID, ROW_VERSION, PTID, MEMO_TYPE ) "
                        + "Select "
                        + " '" + value.toUpperCase() + "', " + acctType + ", " + cls_id + ", " + fieldId + ", 0, '" + key + "', '" + applType + "', '" + acctPrefix + "',getdate(), "
                        + "(select employee_id from " + XapiCodes.coreschema + "..Ad_gb_rsm where "
                        + "user_name = '" + user + "'), 1, " + ptid + ", ' '";

                stmt = conn.createStatement();
                stmt.execute(insertAdditionalField);
                success = true;
            }
        }
        catch (SQLException ex) {
            ApiLogger.getLogger().error("[inserAdditionalField]", ex);
        }
        finally {

            if (rs != null) {
                try {
                    rs.close();
                }
                catch (SQLException ex) {
                    rs = null;
                }
            }
            if (getGbUserDefinePtid != null) {
                try {
                    getGbUserDefinePtid.close();
                }
                catch (SQLException ex) {
                    getGbUserDefinePtid = null;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException ex) {
                    stmt = null;
                }
            }
        }
        return success;
    }
}
