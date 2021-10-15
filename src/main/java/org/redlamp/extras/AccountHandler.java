package org.redlamp.extras;

import NIPClient.Channel.EnquireNameResponseReturn;
import org.redlamp.interfaces.ISO;
import org.redlamp.interfaces.SQL;
import org.redlamp.io.MapUtils;
import org.redlamp.io.StringUtils;
import org.redlamp.logger.ApiLogger;
import org.redlamp.model.*;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class AccountHandler implements AutoCloseable, ISO, SQL {

    private Connection conn;
    private StringBuilder builder;
    private AlertRequest alertRequest;
    private long endTime;
    private long startTime;

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
        Map<String, Object> response = new HashMap<>();
        boolean isAcctnuban = !acct_no.contains("-");

        AccountMap fromAccount = accountName(acct_no, isAcctnuban);
        System.out.println("Nuban " + fromAccount.getNubanAccount());
        System.out.println("local " + fromAccount.getLocalAccount());
        System.out.println("title " + fromAccount.getAccountTitle());
        if (fromAccount.getNubanAccount() == null) {
            response.put("responseCode", "71");
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
                            System.err.println("result set from account fetch " + meta.getColumnName(i) + "" + rset.getObject(i));
                            item.put(meta.getColumnName(i), rset.getObject(i));
                        }
                        list.add(item);
                    }
                }
            }
            System.out.println("is list empty " + list.isEmpty());
            System.out.println("The list " + list);
            if (!list.isEmpty()) {
                response.put("responseCode", XAPI_APPROVED);
                response.put("account_list", list);
                System.out.println("here for  approved " + XAPI_APPROVED);
            } else {
                response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
            }
        } catch (Exception e1) {
            response.put("responseCode", TRY_LATER);

            ApiLogger.getLogger().error(e1);
        }
        System.out.println("Response code >>>>>>>>>>>>>>>>>>>>>>>> " + response.get("responseCode"));
        response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
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
        acctno = getLocalAccount(acctno);
        Map<String, Object> map = new HashMap<String, Object>();
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement
                     .executeQuery(getBuilder(true).append("select * from v_nameLookup where local_acct_no = '")
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
        boolean isAcctnuban = !acct_no.contains("-");
        System.out.println(acct_no + " evaluate account  " + isAcctnuban);
        AccountMap fromAccount = accountName(acct_no, isAcctnuban);
        if (fromAccount.getNubanAccount() == null) {
            response.put("responseCode", "71");
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
        accountNo = accountNo.replace("-", "");
        System.out.println("is nuban " + nuban);
        System.out.println("get account \n\t select a.value, b.title_1, b.acct_no "
                + "from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b "
                + "where a.field_id=45 and   (a.acct_no_key = '" + accountNo.substring(0, 3) + "-" + accountNo.substring(3) + "' or value = '" + accountNo.replace("-", "") + "') "
                + "and b.acct_no = a.acct_no_key ");
        AccountMap accountMap = new AccountMap();
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rset = stmt.executeQuery("select a.value, b.title_1, b.acct_no "
                    + "from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b "
                    + "where a.field_id=45 and   (a.acct_no_key = '" + accountNo.substring(0, 3) + "-" + accountNo.substring(3) + "' or value = '" + accountNo.replace("-", "") + "') "
                    + "and b.acct_no = a.acct_no_key ")) {
                if (rset.next()) {
                    accountMap.setAccountTitle(rset.getString(2));
                    accountMap.setLocalAccount(rset.getString(3));
                    accountMap.setNubanAccount(rset.getString(1));
                    ApiLogger.getLogger().info("title " + rset.getString(2));
                    ApiLogger.getLogger().info("local_acct " + rset.getString(3));
                    ApiLogger.getLogger().info("nuban_acct " + rset.getString(1));
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
    public boolean checkAccountHasAccount(Long rimNo, Long classCode) {
        return checkIfExists("select acct_no from " + XapiCodes.coreschema + "..dp_acct " +
                "where class_code = " + classCode + " and rim_no =" + rimNo + " and status not in ('Closed')");

    }

    public boolean validateRimType(Long rimNo, Long classCode) {
        String condition = Objects.equals("*", XapiCodes.rimClassCode) ? "" : "and ad.rim_class_code in (" + XapiCodes.rimClassCode + ")";
        //select rm.class_code from rm_acct rm,ad_dp_cls_rim ad where rm.class_code =ad.rim_class_code and ad.class_code =1223    and ad.rim_class_code rm.rim_no=119928
        return checkIfExists("select rm.class_code from " + XapiCodes.coreschema + "..rm_acct rm," + XapiCodes.coreschema + "..ad_dp_cls_rim ad " +
                "where rm.class_code = ad.rim_class_code and ad.class_code = " + classCode + " and rm.rim_no =" + rimNo + " " +
                "and rm.status not in ('Closed') " + condition + " ");

    }

    public Map<String, Object> createAccount(AccountRequest accountRequest) {
        CRCaller crCaller = new CRCaller();
        setStartTime(System.currentTimeMillis());
        Map<String, Object> response = new HashMap<>();
        StringBuilder stateBuilder = new StringBuilder();
        crCaller.setCall("dpaccountcreationRequest", accountRequest);
        if (!checkAccountHasAccount(accountRequest.getCust_no(), accountRequest.getClass_code())) {

            if (validateRimType(accountRequest.getCust_no(), accountRequest.getClass_code())) {
                try (CallableStatement callableStatement = conn.prepareCall(SQL.DP_ACCT_CREATION)) {
                    crCaller.setCall("call", SQL.DP_ACCT_CREATION);
                    stateBuilder.append(SQL.DP_ACCT_CREATION + " " + accountRequest.getCust_no()).append(", ");
                    callableStatement.setLong(1, accountRequest.getCust_no());

                    stateBuilder.append(accountRequest.getClass_code()).append(", ");
                    callableStatement.setLong(2, accountRequest.getClass_code());//accountRequest.getClass_code()

                    stateBuilder.append("'").append(XapiPool.userId).append("', ");
                    callableStatement.setString(3, XapiPool.userId);

                    stateBuilder.append("'").append("N").append("', ");
                    callableStatement.setString(4, "N");

                    stateBuilder.append("'").append("N").append("', ");
                    callableStatement.setString(5, "N");

                    stateBuilder.append("'").append("N").append("', ");
                    callableStatement.setString(6, "N");

                    stateBuilder.append(BigDecimal.ZERO).append(", ");
                    callableStatement.setBigDecimal(7, BigDecimal.ZERO);

                    stateBuilder.append("''").append(", ");
                    callableStatement.setString(8, "");

                    stateBuilder.append("'").append(Types.VARCHAR).append("', ");
                    callableStatement.registerOutParameter(9, Types.VARCHAR);

                    stateBuilder.append(Types.INTEGER).append(", ");
                    callableStatement.registerOutParameter(10, Types.INTEGER);
                    //callableStatement.executeUpdate();
// njinu
                    ApiLogger.getLogger().debug(stateBuilder);
                    crCaller.setCall("callable", stateBuilder);
                    int returnCode = -1;
                    BigDecimal rimNumber = new BigDecimal(0);
                    String newAccount = "";
                    // int returnCode = callableStatement.getInt(9);
//                try (ResultSet rset = callableStatement.executeQuery())
//                {
//                    if (rset != null && rset.isBeforeFirst())
//                    {
//                        ResultSetMetaData meta = rset.getMetaData();
//                        while (rset.next())
//                        {
//                            newAccount = rset.getString(1);
//                            returnCode = rset.getInt(2);
//                            //  rimNumber = rset.getBigDecimal(2);
//
//                        }
//                    }
//                }
                    callableStatement.execute();
                    System.out.println("output Account = " + callableStatement.getObject(9));
                    System.out.println("output Ret Code  = " + callableStatement.getObject(10));
                    newAccount = callableStatement.getString(9);
                    returnCode = callableStatement.getInt(10);

                    ApiLogger.getLogger().debug("ReturnCode " + returnCode);
                    ApiLogger.getLogger().debug("newAccount " + newAccount);
                    // Oops, failed to create the account

                    if (returnCode != 0) {
                        response.put("responseCode", returnCode);
                        return response;
                    }
                    crCaller.setAccountNo(newAccount);
                    // get the newly created account
                    //String newAccount = callableStatement.getString(8);
                    ApiLogger.getLogger().debug("Query new account \n\t select a.value, b.title_1, b.acct_no, b.rim_no " +
                            "from " + XapiCodes.coreschema + "..gb_user_defined a," + XapiCodes.coreschema + "..dp_acct b " +
                            "where a.field_id=45 and b.acct_no = '" + newAccount + "' and b.acct_no = a.acct_no_key");
                    try (Statement statement = conn.createStatement();
                         ResultSet resultSet = statement.executeQuery("select a.value, b.title_1, b.acct_no, b.rim_no " +
                                 "from " + XapiCodes.coreschema + "..gb_user_defined a," + XapiCodes.coreschema + "..dp_acct b " +
                                 "where a.field_id=45 and b.acct_no = '" + newAccount + "' and b.acct_no = a.acct_no_key")) {

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
            } else {
                response.put("responseCode", UNSUPPORTED_ACCOUNT_CATEGORY);
                response.put("responseTxt", "Account cannot be created for customer. Customer type not Allowed");
                crCaller.setCall("unspportedCategory", response);
            }
        } else {
            response.put("responseCode", DUPLICATE_REFERENCE);
            response.put("responseTxt", "Customer already has an Active account for this product");
            crCaller.setCall("duplicate", response);

        }
        crCaller.setCall("response", crCaller.convertToString(response));
        crCaller.setCall("debug", crCaller.convertToString(ApiLogger.getLogger()));
        ApiLogger.getLogger().debug(stateBuilder);

        windUp(crCaller);
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
                     .append("select a.value, b.title_1, b.acct_no, b.rim_no from ").append(XapiCodes.coreschema)
                     .append("..gb_user_defined a, ").append(XapiCodes.coreschema)
                     .append("..dp_acct b where a.field_id=45 and (a.acct_no_key = '" + account.substring(0, 3) + "-" + account.substring(3) + "' or value = '" + account.replace("-", "") + "') " +
                             " and b.acct_no = a.acct_no_key").toString())) {
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

    public Map<String, Object> getBVNDetail(CustomerRequest request) {
        Map<String, Object> response = new HashMap<String, Object>();
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(getBuilder(true).append("select b.first_name+' '+b.last_name as name,b.rim_no as cust_no,a.value as bvn " +
                     "from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..rm_acct b " +
                     "where cast(b.rim_no as varchar(15)) = a.acct_no_key and acct_prefix='RM' and class_code not in (130) and field_id =44 and a.value ='" + request.getBank_verification_number() + "'").toString())) {

            List<Map<String, Object>> mapList = asListMap(resultSet);
            if (mapList != null && !mapList.isEmpty()) {
                response.put("responseCode", XAPI_APPROVED);
                response.put("bvn_info", mapList);
            } else {
                response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
            }
        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        return response;

    }

    public Map<String, Object> getLinkedCustomer(CustomerVerificationRequest request) {
        Map<String, Object> response = new HashMap<String, Object>();
        ApiLogger.getLogger().info("select * from (select a.value bvn, b.acct_no, b.rim_no,c.cust_service_key as phone_number,  "
                + " (select  convert(char(12),birth_dt,103) from  " + XapiCodes.coreschema + "..rm_acct where rim_no = b.rim_no) as birth_dt "
                + " from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                + "  where a.field_id=45 and (a.acct_no_key = '" + request.getAccount_no().substring(0, 3) + "-" + request.getAccount_no().substring(3) + "' or value = '" + request.getAccount_no().replace("-", "") + "') "
                + "  and b.acct_no = a.acct_no_key and c.cust_service_key =  '" + request.getPhone_number() + "'   "
                + " and c.rim_no = b.rim_no) aa "
                + " where aa.birth_dt = '" + request.getDateOfBirth() + "'  ");
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("select * from (select a.value bvn, b.acct_no, b.rim_no,c.cust_service_key as phone_number,  "
                     + " (select  convert(char(12),birth_dt,103) from  " + XapiCodes.coreschema + "..rm_acct where rim_no = b.rim_no) as birth_dt "
                     + " from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                     + "  where a.field_id=45 and (a.acct_no_key = '" + request.getAccount_no().substring(0, 3) + "-" + request.getAccount_no().substring(3) + "' or value = '" + request.getAccount_no().replace("-", "") + "') "
                     + "  and b.acct_no = a.acct_no_key and c.cust_service_key =  '" + request.getPhone_number() + "'   "
                     + " and c.rim_no = b.rim_no) aa "
                     + " where aa.birth_dt = '" + request.getDateOfBirth() + "'  ")) {

            List<Map<String, Object>> mapList = asListMap(resultSet);
            if (mapList != null && !mapList.isEmpty()) {
                response.put("responseCode", XAPI_APPROVED);
                response.put("bvn_info", mapList);
            } else {
                response.put("responseCode", UNABLE_TO_LOCATE_RECORD);
            }
        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        return response;

    }

    public Map<String, Object> checkLoanEligibility(LoanRequest request) {
        String query = "select acct_no from  " + XapiCodes.coreschema + "..ln_display " +
                "where class_code not in (" + XapiPool.allowedLnClass + ") " +
                "and rim_no = (select b.rim_no from  " + XapiCodes.coreschema + "..gb_user_defined a, "
                + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services c "
                + "where a.field_id=45 and (a.acct_no_key = '" + request.getAccountNo().substring(0, 3)
                + "-" + request.getAccountNo().substring(3) + "' or value = '" + request.getAccountNo().replace("-", "") + "') "
                + "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no " +
                "and c.cust_service_key = '" + request.getPhoneNumber() + "'  " +
                "and b.acct_no = a.acct_no_key  and b.rim_no =c.rim_no  and b.acct_no = a.acct_no_key " +
                "and b.rim_no =c.rim_no and services_id =44)";

        ApiLogger.getLogger().info(query);
        Map<String, Object> response = new HashMap<String, Object>();
        boolean isBorrower = checkIfExists(query);

        if (isBorrower) {
            response = new BorrowerScoringHandler().processScore(request);
        } else {
            response = new ScoringHandler().processScore(request);

        }

        return response;

    }

    public String resolveperiod(String period) {
        String periodR = "";
        if ("M".equalsIgnoreCase(period)) {
            periodR = "Month(s)";
        } else if ("Y".equalsIgnoreCase(period)) {
            periodR = "Year(s)";
        } else if ("D".equalsIgnoreCase(period)) {
            periodR = "Day(s)";
        } else {
            periodR = period;
        }
        return periodR;
    }

    public Map<String, Object> loanApplication(LoanRequest request) {
        CRCaller crCaller = new CRCaller();
        setStartTime(System.currentTimeMillis());
        Map<String, Object> response = new HashMap<String, Object>();
        String responseCode = "";
        Long classCode = 0L;
        LoanHandler loanHandler = new LoanHandler(crCaller);
        crCaller.setCall("loanApplicationinitreq", request);
        LoanApplicationRequest loanApplicationRequest = new LoanApplicationRequest();
        crCaller.getBlScoreCard().setAccountNumber(request.getAccountNo());
        crCaller.getBlScoreCard().setPhoneNumber(request.getPhoneNumber());
        crCaller.setNarration("E loan Creation");
        ApiLogger.getLogger().info("get rim number \n select a.value, b.title_1, b.acct_no, b.rim_no from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..dp_acct b "
                + " where a.field_id=45 and (a.acct_no_key ='" + request.getAccountNo().substring(0, 3) + "-" + request.getAccountNo().substring(3) + "' or value = '" + request.getAccountNo().replace("-", "") + "')  " +
                "and b.acct_no = a.acct_no_key");
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select a.value, b.title_1, b.acct_no, b.rim_no from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..dp_acct b "
                     + " where a.field_id=45 and (a.acct_no_key ='" + request.getAccountNo().substring(0, 3) + "-" + request.getAccountNo().substring(3) + "' or value = '" + request.getAccountNo().replace("-", "") + "')  " +
                     "and b.acct_no = a.acct_no_key")) {
            if (rs != null && rs.isBeforeFirst()) {
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    loanApplicationRequest.setRimNo(rs.getLong("rim_no"));
                    loanApplicationRequest.setDepositAccountNo(rs.getString("acct_no"));
                }
            }

        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        if (loanApplicationRequest.getRimNo() == null || !MobileRegistered(request.getAccountNo(), request.getPhoneNumber())) {
            response.put("responseCode", ACCT_NOT_FOUND);
            //return  response;
        } else {
            ApiLogger.getLogger().info("check if borrower \n " +
                    "select acct_no from  " + XapiCodes.coreschema + "..ln_display where class_code not in (" + XapiPool.allowedBorrowerLnClass + "," + XapiPool.allowedLnClass + ") and rim_no = (select b.rim_no from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                    + "where a.field_id=45 and   (a.acct_no_key = '" + request.getAccountNo().substring(0, 3) + "-" + request.getAccountNo().substring(3) + "' or value = '" + request.getAccountNo().replace("-", "") + "') "
                    + "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no and c.cust_service_key = '" + request.getPhoneNumber() + "' and services_id =44 )");

            boolean isBorrower = checkIfExists("select acct_no from  " + XapiCodes.coreschema + "..ln_display where class_code not in (" + XapiPool.allowedBorrowerLnClass + "," + XapiPool.allowedLnClass + ") and rim_no = (select b.rim_no from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                    + "where a.field_id=45 and   (a.acct_no_key = '" + request.getAccountNo().substring(0, 3) + "-" + request.getAccountNo().substring(3) + "' or value = '" + request.getAccountNo().replace("-", "") + "') "
                    + "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no and c.cust_service_key = '" + request.getPhoneNumber() + "' and services_id =44 )");

            classCode = isBorrower ? XapiPool.borrowerLoanClassCode : XapiPool.depositorLoanClassCode;

            System.out.println(XapiPool.minLoanterm + " request.getTerm() " + request.getTerm());
            System.out.println(request.getPeriod() + " request.getPeriod() " + request.getPeriod().startsWith("M"));
            System.out.println(" THis ClassCode " + classCode);
            if (!hasActiveDigitalLoan(request)) {
                if (request.getTerm() == XapiPool.minLoanterm && request.getPeriod().startsWith("M")) {

                    System.out.println(">>>> " + loanApplicationRequest.getRimNo());
                    System.out.println(">>>> " + loanApplicationRequest.getDepositAccountNo());
                    loanApplicationRequest.setAccountType("LI");
                    loanApplicationRequest.setClassCode(classCode);//XapiCodes.lnClassCode
                    loanApplicationRequest.setPurposeId(2L);//XapiCodes.purposeId
                    loanApplicationRequest.setApplicationText("E Loan");
                    loanApplicationRequest.setLoanAmount(request.getLoanAmount());
                    loanApplicationRequest.setTerm(request.getTerm());
                    loanApplicationRequest.setPeriod(resolveperiod(request.getPeriod()));
                    loanApplicationRequest.setUserId(XapiPool.userId);
                    loanApplicationRequest.setPhoneNumber(request.getPhoneNumber());
                    loanApplicationRequest.setDepositAccountNo(loanApplicationRequest.getDepositAccountNo());

                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + loanApplicationRequest.getDepositAccountNo());


                    response = loanHandler.loanApplicationStartToEnd(loanApplicationRequest, responseCode);


                } else {
                    response.put("responseCode", INVALID_LOAN_TERM);
                }
            } else {
                response.put("responseCode", ACTIVE_LOAN_EXISTS);
            }
        }
        System.out.println("response >>>>>>>>>>>> " + response.get("responseCode"));
        System.out.println("RESPONSE " + crCaller.convertToString(response));
//        if (!Objects.equals(response.get("responseCode"),XAPI_APPROVED) || !response.get("responseCode").equals("0"))
//        {
//            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//            Map<String, Object> map = new HashMap<String, Object>();
//            Map<String, Object> respMap = new HashMap<String, Object>();
//            map.put("Account_ID", "");
//            map.put("Term", 0);
//            map.put("Acct_no", "");
//            map.put("Acct_type", "");
//            map.put("Status", "");
//            map.put("Period", "");
//            list.add(map);
//            response.put("ln_info", list);
//
//            // response.put("loan_Application", respMap);
//
//        }

        response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
        System.out.println("RESPONSE2 " + crCaller.convertToString(response));
        crCaller.setCall("loanApplicationinitres", response);
        windUp(crCaller);
        return response;

    }

    public boolean hasActiveDigitalLoan(LoanRequest request) {
        ApiLogger.getLogger().info("check if has digital loan \n" +
                "select acct_no from  " + XapiCodes.coreschema + "..ln_display where class_code in (" + XapiPool.allowedLnClass + ") and status not in ('Incomplete','Closed','Cancelled') and rim_no = (select b.rim_no from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                + "where a.field_id=45 and   (a.acct_no_key = '" + request.getAccountNo().substring(0, 3) + "-" + request.getAccountNo().substring(3) + "' or value = '" + request.getAccountNo().replace("-", "") + "') "
                + "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no and c.cust_service_key = '" + request.getPhoneNumber() + "'  and b.acct_no = a.acct_no_key  and b.rim_no =c.rim_no  and b.acct_no = a.acct_no_key and b.rim_no =c.rim_no and services_id =44)");
        return checkIfExists("select acct_no from  " + XapiCodes.coreschema + "..ln_display where class_code in (" + XapiPool.allowedLnClass + ") and status not in ('Incomplete','Closed','Cancelled') and rim_no = (select b.rim_no from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                + "where a.field_id=45 and   (a.acct_no_key = '" + request.getAccountNo().substring(0, 3) + "-" + request.getAccountNo().substring(3) + "' or value = '" + request.getAccountNo().replace("-", "") + "') "
                + "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no and c.cust_service_key = '" + request.getPhoneNumber() + "'  and b.acct_no = a.acct_no_key  and b.rim_no =c.rim_no  and b.acct_no = a.acct_no_key and b.rim_no =c.rim_no and services_id =44)");
    }

    public void windUp(CRCaller crCaller) {
        writeToLog(crCaller);

    }

    private void writeToLog(CRCaller crCaller) {
        //ApiLogger.getLogger().error(e1);
        setEndTime(System.currentTimeMillis());
        crCaller.setDuration(String.valueOf(getEndTime() - getStartTime()) + " Ms");
        new Thread(new ThreadLogger(new ApiLogger(), "<transaction>" + "\r\n" + crCaller + "\r\n" + "</transaction>")).start();
    }

    public Map<String, Object> checkLoanBalance(LoanRepaymentRequest request, boolean returnResponse) {
        CRCaller crCaller = new CRCaller();
        setStartTime(System.currentTimeMillis());
        ApiLogger.getLogger().info("checkLoanBalance \n " +
                "select  rim_no, acct_no,acct_type,status,class_code  " +
                "from " + XapiCodes.coreschema + "..ln_display where class_code in (" + XapiPool.allowedLnClass + ") and status not in ('Incomplete','Closed') " +
                "and  rim_no = (select b.rim_no from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..dp_acct b where  a.field_id=45  and (a.acct_no_key = '" + request.getRepaymentDpAccountNo().substring(0, 3) + "-" + request.getRepaymentDpAccountNo().substring(3) + "' or value = '" + request.getRepaymentDpAccountNo().replace("-", "") + "')   and b.acct_no = a.acct_no_key)");
        Map<String, Object> response = new HashMap<String, Object>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        LoanRepaymentData loanRepaymentData = new LoanRepaymentData();
        RPAccount rpAccount = getDpAccountType(request.getRepaymentDpAccountNo());
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select  rim_no, acct_no,acct_type,status,class_code  " +
                     "from " + XapiCodes.coreschema + "..ln_display where class_code in (" + XapiPool.allowedBorrowerLnClass + "," + XapiPool.allowedLnClass + ") and status not in ('Incomplete','Closed') " +
                     "and  rim_no = (select b.rim_no from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..dp_acct b where  a.field_id=45  " +
                     "and (a.acct_no_key = '" + request.getRepaymentDpAccountNo().substring(0, 3) + "-" + request.getRepaymentDpAccountNo().substring(3) + "' or value = '" + request.getRepaymentDpAccountNo().replace("-", "") + "')   and b.acct_no = a.acct_no_key)")) {
            if (rs != null && rs.isBeforeFirst()) {
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    loanRepaymentData.setRimNo(rs.getLong("rim_no"));
                    loanRepaymentData.setLnAccountNo(rs.getString("acct_no"));
                    loanRepaymentData.setLnAccountType(rs.getString("acct_type"));
                    loanRepaymentData.setClassCode(rs.getLong("class_code"));
                    loanRepaymentData.setStatus(rs.getString("status"));
                    loanRepaymentData.setDpAccountNo(request.getRepaymentDpAccountNo());
                    loanRepaymentData.setDpAccountType(rpAccount.getAcctType());
                }
            }

        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }

        if (isBlank(loanRepaymentData.getRimNo()) || !MobileRegistered(request.getRepaymentDpAccountNo(), request.getPhoneNumber())) {
            response.put("responseCode", ACCT_NOT_FOUND);
        } else {

            Long daysElapsed = getLoanElapsedDays(loanRepaymentData.getLnAccountNo());
            boolean evaluateEarlyPayment = daysElapsed <= 14 && daysElapsed > 7; // between 7 and 14 days
            System.err.println("evaluateEarlyPayment " + evaluateEarlyPayment);
            StringBuilder stateBuilder = new StringBuilder();


            crCaller.setAccountNo(loanRepaymentData.getLnAccountNo());
            crCaller.setCardNumber(request.getPhoneNumber());
            crCaller.setCall("earlyPayment", request.getEarlyPayment());
            crCaller.setCall("amount", request.getRepaymentAmount());
            crCaller.setCall("daysElapsed", daysElapsed.toString());
            try (CallableStatement callableStatement = conn.prepareCall(SQL.CHECK_LOAN_BALANCE)) {


                stateBuilder.append(loanRepaymentData.getRimNo()).append(", ");
                callableStatement.setLong(1, loanRepaymentData.getRimNo());

                stateBuilder.append(loanRepaymentData.getClassCode()).append(", ");
                callableStatement.setLong(2, loanRepaymentData.getClassCode());

                stateBuilder.append("'").append(XapiPool.userId).append("', ");
                callableStatement.setString(3, XapiPool.userId);

                stateBuilder.append("'").append(loanRepaymentData.getLnAccountType()).append("', ");
                callableStatement.setString(4, loanRepaymentData.getLnAccountType());

                stateBuilder.append("'").append(loanRepaymentData.getLnAccountNo()).append("', ");
                callableStatement.setString(5, loanRepaymentData.getLnAccountNo());

                stateBuilder.append(loanRepaymentData.getRepaymentAmount()).append(", ");
                callableStatement.setBigDecimal(6, loanRepaymentData.getRepaymentAmount());

                stateBuilder.append("'").append(loanRepaymentData.getDpAccountNo()).append("', ");
                callableStatement.setString(7, loanRepaymentData.getDpAccountNo());

                stateBuilder.append("'").append(loanRepaymentData.getDpAccountType()).append("', ");
                callableStatement.setString(8, loanRepaymentData.getDpAccountType());

                stateBuilder.append("'").append("E-Loan Repayment Check").append("', ");
                callableStatement.setString(9, "E-Loan Repayment Check");

                stateBuilder.append("'").append("Y").append("', ");
                callableStatement.setString(10, "Y");//check bal

                stateBuilder.append(304).append(", ");
                callableStatement.setLong(11, 304L);//tc

                stateBuilder.append("'").append(request.getEarlyPayment()).append("', ");
                callableStatement.setString(12, request.getEarlyPayment());//earlyPayment

                stateBuilder.append(0).append(", ");
                callableStatement.registerOutParameter(13, Types.INTEGER);

                stateBuilder.append(0).append(", ");
                callableStatement.registerOutParameter(14, Types.INTEGER);

                stateBuilder.append(0).append(", ");
                callableStatement.registerOutParameter(15, Types.INTEGER);

                stateBuilder.append(0).append(", ");
                callableStatement.registerOutParameter(16, Types.INTEGER);

                stateBuilder.append(0).append(", ");
                callableStatement.registerOutParameter(17, Types.INTEGER);
                ApiLogger.getLogger().info(stateBuilder);
                /// callableStatement.executeUpdate();

                System.out.println(stateBuilder);
                ApiLogger.getLogger().info(stateBuilder);
                int returnCode = -1;
                BigDecimal payoffBal = new BigDecimal(0);
                BigDecimal currentPrincBal = new BigDecimal(0);
                BigDecimal unconsideredIntBal = new BigDecimal(0);
                BigDecimal currentAccruedInt = new BigDecimal(0);

//                if (daysElapsed > 7 && request)
//                {
                try (ResultSet rset = callableStatement.executeQuery()) {
                    if (rset != null && rset.isBeforeFirst()) {
                        ResultSetMetaData meta = rset.getMetaData();
                        while (rset.next()) {
                            returnCode = rset.getInt(1);
                            payoffBal = rset.getBigDecimal(2);
                            unconsideredIntBal = rset.getBigDecimal(3);
                            currentPrincBal = rset.getBigDecimal(4);
                            currentAccruedInt = rset.getBigDecimal(5);
                            crCaller.setXapiRespCode(XAPI_APPROVED);

                        }
                        System.out.println("Payoff bal " + payoffBal);
                        System.out.println("unconsideredIntBal bal " + unconsideredIntBal);
                        System.out.println("currentPrincBal bal " + currentPrincBal);
                        System.out.println("currentAccruedInt bal " + currentAccruedInt);
                        crCaller.setCall("payoffBal", payoffBal);
                        crCaller.setCall("unconsideredIntBal", unconsideredIntBal);
                        crCaller.setCall("currentAccruedInt", currentAccruedInt);
                        crCaller.setCall("currentAccruedInt", currentAccruedInt);


                        //payoffBal = payoffBal;
                        //  payoffBal = "Y".equals(request.getEarlyPayment()) || evaluateEarlyPayment ? currentPrincBal.add(currentAccruedInt) : payoffBal.add(unconsideredIntBal);
                    }
                }
//                }
//                else
//                {
//                    returnCode = -5;
//                }

                if (returnCode != 0) {
                    if (returnCode == -5) {
                        response.put("responseCode", XapiPool.resolveError(REPAYMENT_NOT_ALLOWED));
                        response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
                        crCaller.setXapiRespCode(REPAYMENT_NOT_ALLOWED);
                    } else {
                        response.put("responseCode", XapiPool.resolveError(String.valueOf(returnCode)));
                        crCaller.setXapiRespCode(XapiPool.resolveError(String.valueOf(returnCode)));
                    }
                    return response;
                } else {
                    map.put("Loan_Bal", payoffBal.setScale(2, 2));
                    map.put("Loan_Account", loanRepaymentData.getLnAccountNo().trim());
                    //map.put("Principal", currentPrincBal);
                    //map.put("Accrued Interest", currentAccruedInt);

                    //map.put("Repayment Account", getNubanAccount(loanRepaymentData.getDpAccountNo()));
                    //  map.put("Unearned Interest", unconsideredIntBal);

                    list.add(map);
                    List<Map<String, Object>> mapList = list;

                    if (returnResponse) {
                        response.put("responseCode", XAPI_APPROVED);
                        response.put("loan_check", mapList);
                    } else {
                        response.put("responseCode", XAPI_APPROVED);
                        response.put("Balance", mapList);

                    }
                }

            } catch (SQLException ex) {
                ApiLogger.getLogger().error(ex);
            }
        }
        if (!XAPI_APPROVED.equals(response.get("responseCode"))) {
            map.put("Loan_Bal", BigDecimal.ZERO);
            map.put("Loan_Account", loanRepaymentData.getLnAccountNo().trim());

            list.add(map);
            List<Map<String, Object>> mapList = list;
            response.put("loan_check", mapList);
        }
        response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
        crCaller.setCall("Response", response);

        windUp(crCaller);
        return response;
    }

    public LoanRepaymentData checkLoanRepaymentBalance(LoanRepaymentRequest request, String acctNo, boolean returnResponse) {
        ApiLogger.getLogger().info("select top 1 rim_no, acct_no,acct_type,status,class_code  " +
                "from " + XapiCodes.coreschema + "..ln_display where class_code in (" + XapiPool.allowedLnClass + ") and status not in ('Incomplete') " +
                "and acct_no ='" + acctNo + "' and rim_no = (select b.rim_no from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..dp_acct b where  a.field_id=45  and (a.acct_no_key = '" + request.getRepaymentDpAccountNo().substring(0, 3) + "-" + request.getRepaymentDpAccountNo().substring(3) + "' or value = '" + request.getRepaymentDpAccountNo().replace("-", "") + "')   and b.acct_no = a.acct_no_key)");
        Map<String, Object> response = new HashMap<String, Object>();
        LoanRepaymentData loanRepaymentData = new LoanRepaymentData();
        RPAccount rpAccount = getDpAccountType(request.getRepaymentDpAccountNo());
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select  rim_no, acct_no,acct_type,status,class_code  " +
                     "from " + XapiCodes.coreschema + "..ln_display where class_code in (" + XapiPool.allowedLnClass + ") and status not in ('Incomplete') " +
                     "and acct_no ='" + acctNo + "' and rim_no = (select b.rim_no from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..dp_acct b where  a.field_id=45  " +
                     "and (a.acct_no_key = '" + request.getRepaymentDpAccountNo().substring(0, 3) + "-" + request.getRepaymentDpAccountNo().substring(3) + "' or value = '" + request.getRepaymentDpAccountNo().replace("-", "") + "')   and b.acct_no = a.acct_no_key)")) {
            if (rs != null && rs.isBeforeFirst()) {
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    loanRepaymentData.setRimNo(rs.getLong("rim_no"));
                    loanRepaymentData.setLnAccountNo(rs.getString("acct_no"));
                    loanRepaymentData.setLnAccountType(rs.getString("acct_type"));
                    loanRepaymentData.setClassCode(rs.getLong("class_code"));
                    loanRepaymentData.setStatus(rs.getString("status"));
                    loanRepaymentData.setDpAccountNo(request.getRepaymentDpAccountNo());
                    loanRepaymentData.setDpAccountType(rpAccount.getAcctType());
                }
            }

        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }

        if (isBlank(loanRepaymentData.getRimNo()) || !MobileRegistered(request.getRepaymentDpAccountNo(), request.getPhoneNumber())) {
            response.put("responseCode", ACCT_NOT_FOUND);
        } else if ("Closed".equals(loanRepaymentData.getStatus())) {
            response.put("responseCode", XAPI_APPROVED);
            loanRepaymentData.setRepaymentAmount(BigDecimal.ZERO);
        } else {
            try (CallableStatement callableStatement = conn.prepareCall(SQL.CHECK_LOAN_BALANCE)) {
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();


                callableStatement.setLong(1, loanRepaymentData.getRimNo());
                callableStatement.setLong(2, loanRepaymentData.getClassCode());//accountRequest.getClass_code()
                callableStatement.setString(3, XapiPool.userId);
                callableStatement.setString(4, loanRepaymentData.getLnAccountType());
                callableStatement.setString(5, loanRepaymentData.getLnAccountNo());
                callableStatement.setBigDecimal(6, loanRepaymentData.getRepaymentAmount());
                callableStatement.setString(7, loanRepaymentData.getDpAccountNo());
                callableStatement.setString(8, loanRepaymentData.getDpAccountType());
                callableStatement.setString(9, "E-Loan Repayment Check");
                callableStatement.setString(10, "Y");//check bal
                callableStatement.setLong(11, 304L);//tc
                callableStatement.setString(12, request.getEarlyPayment());//check bal
                callableStatement.registerOutParameter(13, Types.INTEGER);
                callableStatement.registerOutParameter(14, Types.INTEGER);
                callableStatement.registerOutParameter(15, Types.INTEGER);
                callableStatement.registerOutParameter(16, Types.INTEGER);
                callableStatement.registerOutParameter(17, Types.INTEGER);
                //callableStatement.executeUpdate();
                int returnCode = -1;
                BigDecimal payoffBal = new BigDecimal(0);
                BigDecimal currentPrincBal = new BigDecimal(0);
                BigDecimal unconsideredIntBal = new BigDecimal(0);
                BigDecimal currentAccruedInt = new BigDecimal(0);
                String newAccount = "";

                try (ResultSet rset = callableStatement.executeQuery()) {
                    if (rset != null && rset.isBeforeFirst()) {
                        ResultSetMetaData meta = rset.getMetaData();
                        while (rset.next()) {
                            returnCode = rset.getInt(1);
                            payoffBal = rset.getBigDecimal(2);
                            unconsideredIntBal = rset.getBigDecimal(3);
                            currentPrincBal = rset.getBigDecimal(4);
                            currentAccruedInt = rset.getBigDecimal(5);
                        }
                    }
                }

                if (returnCode != 0) {
                    response.put("responseCode", XapiPool.resolveError(String.valueOf(returnCode)));

                } else {
                    loanRepaymentData.setRepaymentAmount(payoffBal);

                }

            } catch (SQLException ex) {
                ApiLogger.getLogger().error(ex);
            }
        }
        response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
        return loanRepaymentData;
    }

    private boolean logLoanRepayment(BLLoanCreationResponse blResponse) {
        System.err.println("INSERT INTO " + XapiCodes.xapiSchema + "..E_LOAN_REPAYMENT(ACCT_NO,ACCT_TYPE,PHONE_NUMBER,CREATE_DT,AMOUNT,RESPONSE_CODE,RESPONSE_MESSAGE,PROC_RETURN_CODE)" +
                "VALUES('" + blResponse.getAccountNumber() + "','" + blResponse.getAccountType() + "','" + blResponse.getPhoneNumber() + "',getdate()," + blResponse.getAmount() + ",'" + blResponse.getResponseCode() + "','" + blResponse.getResponseMessage() + "','" + blResponse.getReturnCode() + "')");
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("INSERT INTO " + XapiCodes.xapiSchema + "..E_LOAN_REPAYMENT(ACCT_NO,ACCT_TYPE,PHONE_NUMBER,CREATE_DT,AMOUNT,RESPONSE_CODE,RESPONSE_MESSAGE,PROC_RETURN_CODE)" +
                    "VALUES('" + blResponse.getAccountNumber() + "','" + blResponse.getAccountType() + "','" + blResponse.getPhoneNumber() + "',getdate()," + blResponse.getAmount() + ",'" + blResponse.getResponseCode() + "','" + blResponse.getResponseMessage() + "','" + blResponse.getReturnCode() + "')");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public Map<String, Object> repayLoan(LoanRepaymentRequest request) {
        CRCaller crCaller = new CRCaller();
        setStartTime(System.currentTimeMillis());
        Map<String, Object> response = new HashMap<String, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        StringBuilder stateBuilder = new StringBuilder();
        BLLoanCreationResponse blResponse = new BLLoanCreationResponse();
        LoanRepaymentData loanRepaymentData = new LoanRepaymentData();
        RPAccount rpAccount = getDpAccountType(request.getRepaymentDpAccountNo());
        System.out.println("select top 1 rim_no, acct_no,acct_type,status,class_code  " +
                "from " + XapiCodes.coreschema + "..ln_display where class_code = " + XapiPool.allowedLnClass + " and status not in ('Incomplete','Closed') " +
                "and  rim_no = (select b.rim_no from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..dp_acct b where  a.field_id=45  " +
                "and (a.acct_no_key = '" + request.getRepaymentDpAccountNo().substring(0, 3) + "-" + request.getRepaymentDpAccountNo().substring(3) + "' or value = '" + request.getRepaymentDpAccountNo().replace("-", "") + "')   and b.acct_no = a.acct_no_key)");
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select top 1 rim_no, acct_no,acct_type,status,class_code  " +
                     "from " + XapiCodes.coreschema + "..ln_display where class_code in (" + XapiPool.allowedLnClass + ") and status not in ('Incomplete','Closed') " +
                     "and  rim_no = (select b.rim_no from " + XapiCodes.coreschema + "..gb_user_defined a, " + XapiCodes.coreschema + "..dp_acct b where  a.field_id=45  " +
                     "and (a.acct_no_key = '" + request.getRepaymentDpAccountNo().substring(0, 3) + "-" + request.getRepaymentDpAccountNo().substring(3) + "' or value = '" + request.getRepaymentDpAccountNo().replace("-", "") + "')   and b.acct_no = a.acct_no_key)")) {
            if (rs != null && rs.isBeforeFirst()) {
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    loanRepaymentData.setRimNo(rs.getLong("rim_no"));
                    loanRepaymentData.setLnAccountNo(rs.getString("acct_no"));
                    loanRepaymentData.setLnAccountType(rs.getString("acct_type"));
                    loanRepaymentData.setClassCode(rs.getLong("class_code"));
                    loanRepaymentData.setStatus(rs.getString("status"));
                    loanRepaymentData.setDpAccountNo(rpAccount.getAccountNo());
                    loanRepaymentData.setDpAccountType(rpAccount.getAcctType());
                    loanRepaymentData.setRepaymentAmount(request.getRepaymentAmount());
                }
            }

        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }

        if (isBlank(loanRepaymentData.getRimNo()) || !MobileRegistered(request.getRepaymentDpAccountNo(), request.getPhoneNumber())) {
            response.put("responseCode", ACCT_NOT_FOUND);
        } else {
            crCaller.setAccountNo(loanRepaymentData.getLnAccountNo());
            crCaller.setCardNumber(request.getPhoneNumber());
            crCaller.setCall("earlyPayment", request.getEarlyPayment());
            crCaller.setCall("amount", request.getRepaymentAmount());
            try (CallableStatement callableStatement = conn.prepareCall(SQL.REPAY_LOAN)) {
                Long daysElapsed = getLoanElapsedDays(loanRepaymentData.getLnAccountNo());
                boolean evaluateEarlyPayment = daysElapsed <= 14 && daysElapsed > 7; // between 7 and 14 days


                //Map<String, Object> map = new HashMap<String, Object>();

                stateBuilder.append("EXEC ").append(SQL.REPAY_LOAN).append("\n").append(loanRepaymentData.getRimNo()).append(", ");
                callableStatement.setLong(1, loanRepaymentData.getRimNo());

                stateBuilder.append(loanRepaymentData.getClassCode()).append(", ");
                callableStatement.setLong(2, loanRepaymentData.getClassCode());//accountRequest.getClass_code()

                stateBuilder.append("'").append(XapiPool.userId).append("', ");
                callableStatement.setString(3, XapiPool.userId);

                stateBuilder.append("'").append(loanRepaymentData.getLnAccountType()).append("', ");
                callableStatement.setString(4, loanRepaymentData.getLnAccountType());

                stateBuilder.append("'").append(loanRepaymentData.getLnAccountNo()).append("', ");
                callableStatement.setString(5, loanRepaymentData.getLnAccountNo());

                stateBuilder.append(loanRepaymentData.getRepaymentAmount()).append(", ");
                callableStatement.setBigDecimal(6, loanRepaymentData.getRepaymentAmount());

                stateBuilder.append("'").append(loanRepaymentData.getDpAccountNo()).append("', ");
                callableStatement.setString(7, loanRepaymentData.getDpAccountNo());

                stateBuilder.append("'").append(loanRepaymentData.getDpAccountType()).append("', ");
                callableStatement.setString(8, loanRepaymentData.getDpAccountType());

                stateBuilder.append("'").append("E-Loan Repayment").append("', ");
                callableStatement.setString(9, "E-Loan Repayment");

                stateBuilder.append("'").append("N").append("', ");
                callableStatement.setString(10, "N");

                stateBuilder.append(304L).append(", ");
                callableStatement.setLong(11, 304);

                stateBuilder.append("'").append(request.getEarlyPayment()).append("', ");
                callableStatement.setString(12, request.getEarlyPayment());

                stateBuilder.append(0);
                callableStatement.registerOutParameter(13, Types.INTEGER);


                System.out.println(stateBuilder);
                int returnCode = -1;
                ApiLogger.getLogger().info(stateBuilder);
                if (daysElapsed < XapiPool.repayAfterDays) {
                    if (loanRepaymentData.getRepaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
                        callableStatement.execute();
                        System.out.println("output = " + callableStatement.getObject(13));
                        returnCode = callableStatement.getInt(13);
                    } else {
                        returnCode = 13;
                    }
                } else {
                    returnCode = -5;

                }

                System.out.println(">>>>>>>>>>>>>>>>>>> " + returnCode);
                blResponse.setReturnCode(String.valueOf(returnCode));
                blResponse.setAccountNumber(loanRepaymentData.getLnAccountNo());
                blResponse.setAccountType(loanRepaymentData.getLnAccountType());
                blResponse.setAmount(loanRepaymentData.getRepaymentAmount());

                if (returnCode != 0) {
                    if (returnCode == -5) {
                        response.put("responseCode", XapiPool.resolveError(REPAYMENT_NOT_ALLOWED));
                        response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
                    } else {
                        response.put("responseCode", XapiPool.resolveError(String.valueOf(returnCode)));
                        response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
                    }
                    return response;
                } else {
                    LoanRepaymentData checkBalance = checkLoanRepaymentBalance(request, loanRepaymentData.getLnAccountNo(), false);
                    map.put("Loan_Account", loanRepaymentData.getLnAccountNo());
                    map.put("Loan_Bal", BigDecimal.ZERO.setScale(2, 2));

                    list.add(map);
                    //List<Map<String, Object>> mapList = list;

                    response.put("responseCode", XAPI_APPROVED);
                    response.put("repayment", list);
                }

            } catch (SQLException ex) {
                ApiLogger.getLogger().error(ex);
            }
        }
        if (!response.get("responseCode").equals(XAPI_APPROVED)) {
            LoanRepaymentData checkBalance = checkLoanRepaymentBalance(request, loanRepaymentData.getLnAccountNo(), false);
            map.put("Loan_Account", loanRepaymentData.getLnAccountNo());
            map.put("Loan_Bal", BigDecimal.ZERO);

            list.add(map);
            response.put("repayment", list);
            blResponse.setAmount(BigDecimal.ZERO);
        }
        blResponse.setResponseCode(response.get("responseCode").toString());
        blResponse.setResponseMessage(XapiCodes.getErrorDesc(response.get("responseCode")));

        response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
        logLoanRepayment(blResponse);
        crCaller.setCall("LoanAccount", loanRepaymentData.getLnAccountNo());
        crCaller.setCall("Loan Bal", response.get("Loan_Bal"));
        crCaller.setCall("respObject", blResponse);
        crCaller.setCall("response", response);

        windUp(crCaller);
        return response;

    }

    public static boolean isBlank(Object object) {
        return object == null || "".equals(String.valueOf(object).trim()) || "null".equals(String.valueOf(object).trim()) || String.valueOf(object).trim().toLowerCase().contains("---select");
    }

    public Map<String, Object> loanRepayment(LoanRepaymentRequest request) {
        //do the checks here

        Map<String, Object> response = repayLoan(request);

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

            List<Map<String, Object>> class_code = fetchListMap("select class_code,description from " + XapiCodes.coreschema + "..ad_dp_cls  " +
                    "where status = 'Active' and class_code in (select class_code from " + XapiCodes.coreschema + "..ad_dp_cls_rim where rim_class_code in (" + XapiCodes.rimClassCode + ") )", statement);
            response.put("class_code", class_code);

//            List<Map<String, Object>> marketing_types = fetchListMap("select marketing_id,description,status  from ad_rm_marketing where status = 'Active'",
//                    statement);
//            response.put("class_codes", marketing_types);

            response.put("responseCode", XAPI_APPROVED);

        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        return response;
    }

    public RPAccount getDpAccountType(String acctNo) {
        System.out.println("select b.acct_type,b.acct_no from " + XapiCodes.coreschema + "..gb_user_defined a," + XapiCodes.coreschema + "..dp_acct b " +
                "where a.field_id=45 and (a.acct_no_key = '" + acctNo.substring(0, 3) + "-" + acctNo.substring(3) + "' or value = '" + acctNo.replace("-", "") + "')   and b.acct_no = a.acct_no_key");
        RPAccount rpAccount = new RPAccount();
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select b.acct_type,acct_no from " + XapiCodes.coreschema + "..gb_user_defined a," + XapiCodes.coreschema + "..dp_acct b " +
                     "where a.field_id=45 and (a.acct_no_key = '" + acctNo.substring(0, 3) + "-" + acctNo.substring(3) + "' or value = '" + acctNo.replace("-", "") + "')   and b.acct_no = a.acct_no_key"
             )) {

            if (rs.next()) {
                rpAccount.setAcctType(rs.getString("acct_type"));
                rpAccount.setAccountNo(rs.getString("acct_no"));
            }


        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        return rpAccount;
    }

    public Long getLoanElapsedDays(String acctNo) {
        Long elapsed = 0L;
        System.out.println("select datediff(dd,contract_dt,(select dateadd(dd,1,last_to_dt)  from " + XapiCodes.coreschema + "..ov_control)) daysElapsed from " + XapiCodes.coreschema + "..ln_display where acct_no ='" + acctNo + "' ");
        RPAccount rpAccount = new RPAccount();
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select datediff(dd,contract_dt,(select dateadd(dd,1,last_to_dt)  from " + XapiCodes.coreschema + "..ov_control)) daysElapsed from " + XapiCodes.coreschema + "..ln_display where acct_no ='" + acctNo + "' "
             )) {

            if (rs.next()) {
                elapsed = rs.getLong("daysElapsed");
            }


        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        return elapsed;
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


    public boolean MobileRegistered(String acctNo, String phoneNo) {
        ApiLogger.getLogger().info("check if MobileRegistered\n" +
                "select a.value bvn, b.acct_no, b.rim_no,c.cust_service_key as phone_number "
                + "from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                + "where a.field_id=45 and   (a.acct_no_key = '" + acctNo.substring(0, 3) + "-" + acctNo.substring(3) + "' or value = '" + acctNo.replace("-", "") + "') "
                + "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no and c.cust_service_key = '" + phoneNo + "' and services_id =44 ");
        return checkIfExists("select a.value bvn, b.acct_no, b.rim_no,c.cust_service_key as phone_number "
                + "from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                + "where a.field_id=45 and   (a.acct_no_key = '" + acctNo.substring(0, 3) + "-" + acctNo.substring(3) + "' or value = '" + acctNo.replace("-", "") + "') "
                + "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no and c.cust_service_key = '" + phoneNo + "'  and services_id =44 ");
    }

    public String getLocalAccount(String acct_no) {
        ApiLogger.getLogger().info("select a.value from " + XapiCodes.coreschema + "..gb_user_defined a," + XapiCodes.coreschema + "..dp_acct b where a.field_id=45 " +
                "and (a.acct_no_key = '" + acct_no.substring(0, 3) + "-" + acct_no.substring(3) + "' or value = '" + acct_no.replace("-", "") + "')  and b.acct_no = a.acct_no_key");
        String local = "";

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select b.acct_no from " + XapiCodes.coreschema + "..gb_user_defined a," + XapiCodes.coreschema + "..dp_acct b where a.field_id=45 " +
                     "and (a.acct_no_key = '" + acct_no.substring(0, 3) + "-" + acct_no.substring(3) + "' or value = '" + acct_no.replace("-", "") + "')  and b.acct_no = a.acct_no_key")) {
            if (rs.next()) {
                local = rs.getString("acct_no");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ApiLogger.getLogger().error(ex);

        }
        return local;
    }

    private boolean checkIfExists(String query) {
        boolean exists = false;
        ApiLogger.debug("checkIfExists " + query);
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            exists = rs.next();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.err.println(" Exists?  >> " + exists);

        return exists;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
