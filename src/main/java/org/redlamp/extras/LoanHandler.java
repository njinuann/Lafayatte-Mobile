package org.redlamp.extras;

import org.redlamp.interfaces.ISO;
import org.redlamp.interfaces.SQL;
import org.redlamp.io.StringUtils;
import org.redlamp.logger.ApiLogger;
import org.redlamp.model.BLLoanCreationResponse;
import org.redlamp.model.CustomerRequest;
import org.redlamp.model.LoanApplicationRequest;
import org.redlamp.model.LoanCreationResponse;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
//import sun.reflect.generics.tree.Tree;

public class LoanHandler implements AutoCloseable, ISO, SQL
{

    private Connection conn;
    private StringBuilder builder;
    private CRCaller crCaller = new CRCaller();
    private long endTime;
    private long startTime;

    public LoanHandler(CRCaller crCaller)
    {
        try
        {
            conn = XapiPool.getConnection();
            setCrCaller(crCaller);
            setStartTime(System.currentTimeMillis());
        } catch (SQLException ex)
        {
            ApiLogger.getLogger().error(ex);
        }
    }

    @Override
    public void close()
    {
        try
        {
            if (conn != null)
                conn.close();
        } catch (SQLException e)
        {
            ApiLogger.getLogger().error(e);
        }
    }

    public Map<String, Object> loanApplicationStartToEnd(LoanApplicationRequest request, String responseCode)
    {
        int count = 0;
        Map<String, Object> response = new HashMap<String, Object>();
        StringBuilder stateBuilder = new StringBuilder();
        BLLoanCreationResponse blLoanCreationResponse = new BLLoanCreationResponse();
        blLoanCreationResponse.setPhoneNumber(request.getPhoneNumber());

        try (CallableStatement callableStatement = conn.prepareCall(SQL.LOAN_APPL_DISBURSE))
        {

            stateBuilder.append("EXEC ").append(SQL.LOAN_APPL_DISBURSE).append(request.getRimNo()).append(", ");
            callableStatement.setLong(1, request.getRimNo());

            stateBuilder.append("'").append(request.getAccountType()).append("', ");
            callableStatement.setString(2, request.getAccountType());

            stateBuilder.append("").append(request.getClassCode()).append(", ");
            callableStatement.setLong(3, request.getClassCode());

            stateBuilder.append("").append(request.getPurposeId()).append(", ");
            callableStatement.setLong(4, request.getPurposeId());

            stateBuilder.append("'").append(request.getUserId()).append("', ");
            callableStatement.setString(5, XapiPool.userId);

            stateBuilder.append("'").append(request.getApplicationText()).append("', ");
            callableStatement.setString(6, request.getApplicationText());

            stateBuilder.append("").append(request.getLoanAmount()).append(", ");
            callableStatement.setBigDecimal(7, request.getLoanAmount());

            stateBuilder.append("").append(request.getTerm()).append(", ");
            callableStatement.setLong(8, request.getTerm());

            stateBuilder.append("'").append(request.getPeriod()).append("', ");
            callableStatement.setString(9, request.getPeriod());

            stateBuilder.append("'").append(request.getDepositAccountNo()).append("', ");
            callableStatement.setString(10, request.getDepositAccountNo());

            stateBuilder.append("'").append("").append("', ");
            callableStatement.registerOutParameter(11, Types.VARCHAR);//applicationNo(acct_no in la_acct)

            stateBuilder.append("'").append("").append("', ");
            callableStatement.registerOutParameter(12, Types.VARCHAR);//ACCT_NO(acct_no in la_acct)

            stateBuilder.append("").append(0);
            callableStatement.registerOutParameter(13, Types.INTEGER); //return code

            ApiLogger.debug(">> exec <<", stateBuilder);
            callableStatement.executeQuery();

            System.out.println(stateBuilder);
            System.out.println("returnCode " + callableStatement.getInt(13));
            ApiLogger.debug(">> exec <<", stateBuilder);
            getCrCaller().setCall("exec", stateBuilder);
            getCrCaller().setCall("loanCreationreq", request);

            int returnCode = -1;
            String applicationNoNew = "";
            String accountNo = "";
            ApiLogger.getLogger().info(stateBuilder);
            System.out.println("applicationNoNew " + callableStatement.getString(11));
            System.out.println("accountNo " + callableStatement.getString(12));
            System.out.println("returnCode " + callableStatement.getInt(13));
            returnCode = isBlank(callableStatement.getInt(13)) ? -1 : callableStatement.getInt(13);
            System.out.println("returnCode " + returnCode);
            try
            {
                applicationNoNew = callableStatement.getString(11);
                accountNo = callableStatement.getString(12);
                returnCode = isBlank(callableStatement.getInt(13)) ? -1 : callableStatement.getInt(13);
                blLoanCreationResponse.setAccountNumber(accountNo);
                blLoanCreationResponse.setReturnCode(String.valueOf(returnCode));

            } catch (Exception ex)
            {
                response.put("responseCode", returnCode);
                return response;
            }

            // Oops, failed to create the account
            if (returnCode != 0)
            {
                response.put("responseCode", returnCode);
                return response;
            }
            else
            {
                // get the newly created account
                System.out.println("getaccountNo " + accountNo);
                System.out.println("select rim_no, acct_no,acct_type,status,amt,period,trm,mat_dt from " + XapiCodes.coreschema + "..ln_acct where acct_no ='" + accountNo + "' and acct_type='" + request.getAccountType() + "'");
                String acct_no = "", acct_type = "", status = "", period = "";
                Long rim_no = null, trm = null;
                BigDecimal amt = BigDecimal.ZERO;
                Date mat_dt = null;

                System.out.println("get generated account \nselect rim_no, acct_no,acct_type,status,amt,period,trm,mat_dt from " + XapiCodes.coreschema + "..ln_acct where acct_no ='" + accountNo + "'");
                try (Statement statement = conn.createStatement();
                     ResultSet rs = statement.executeQuery("select rim_no, acct_no,acct_type,status,amt,period,trm,mat_dt from " + XapiCodes.coreschema + "..ln_acct where acct_no ='" + accountNo + "'"))
                {
                    if (rs.next())
                    {
                        System.out.println(">>>>>> check status  " + rs.getString("acct_no"));
                        rim_no = rs.getLong("rim_no");
                        trm = rs.getLong("trm");
                        acct_no = rs.getString("acct_no");
                        acct_type = rs.getString("acct_type");
                        status = rs.getString("status");
                        period = rs.getString("period");

                    }

                } catch (Exception e1)
                {
                    ApiLogger.getLogger().error(e1);
                }
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                Map<String, Object> map = new HashMap<String, Object>();

                BigDecimal rate = BigDecimal.ZERO;
                try (Statement statement = conn.createStatement();
                     ResultSet rs = statement.executeQuery("select aa.rate , bb.accr_basis from " + XapiCodes.coreschema + "..ad_gb_rate_history aa, " + XapiCodes.coreschema + "..ad_ln_cls_int_opt bb," + XapiCodes.coreschema + "..ln_display cc "
                             + " where aa.index_id = bb.index_id  and bb.class_code = cc.class_code and cc.acct_no ='" + acct_no.trim() + "' and aa.ptid = (select max(ptid) from " + XapiCodes.coreschema + "..ad_gb_rate_history where index_id =aa.index_id)"))
                {
                    if (rs.next())
                    {
//                        BigDecimal num1 = new BigDecimal(splitItem(rs.getString("accr_basis"), 0));
//                        BigDecimal num2 = new BigDecimal(splitItem(rs.getString("accr_basis"), 1));
                      //  BigDecimal divider = num1.divide(num2, MathContext.DECIMAL128).setScale(4, RoundingMode.UP);
                        BigDecimal classRate = rs.getBigDecimal("rate");
                        rate = classRate.multiply((new BigDecimal(30).divide(new BigDecimal(360),MathContext.DECIMAL128))).setScale(2, RoundingMode.UP);

                    }

                } catch (Exception e1)
                {
                    ApiLogger.getLogger().error(e1);
                }

                if (!isBlank(rim_no))
                {
                    map.put("Account_ID", String.valueOf(rim_no));
                    map.put("Term", String.valueOf(trm));
                    map.put("Acct_no", acct_no.trim());
                    map.put("Acct_type", acct_type.trim());
                    map.put("Status", status.trim());
                    map.put("Period", period.trim());
                    map.put("rate", rate);
                    response.put("responseCode", XAPI_APPROVED);

                    blLoanCreationResponse.setAccountNumber(acct_no.trim());
                    blLoanCreationResponse.setAccountType(acct_type.trim());
                    blLoanCreationResponse.setRimNo(rim_no);
                    blLoanCreationResponse.setPeriod(period.trim());
                    blLoanCreationResponse.setTerm(trm);
                    blLoanCreationResponse.setAmount(request.getLoanAmount());

                    list.add(map);
                    List<Map<String, Object>> mapList = list;
                    response.put("ln_info", mapList);

                }
                else
                {
                    response.put("responseCode", LN_ACCT_CREATION_FAILED);
                    return response;
                }

            }


        } catch (SQLException ex)
        {
            getCrCaller().setCall("exec", stateBuilder);
            ApiLogger.getLogger().error(ex);
            if (ex.getMessage().contains("exec " + stateBuilder))
            {
                while (count < 1)
                {
                    if (count == 1)
                    {
                        System.out.println("Attempt<><><><><><><><><><><><> " + count);
                        response = loanApplicationStartToEnd(request, responseCode);
                        break;
                    }
                    count = +1;
                }
            }
            else
            {
                ApiLogger.getLogger().error(ex);
            }

        }

        blLoanCreationResponse.setResponseCode(response.get("responseCode").toString());
        blLoanCreationResponse.setResponseMessage(XapiCodes.getErrorDesc(response.get("responseCode")));
        logLoanCreation(blLoanCreationResponse);
        getCrCaller().setCall("loanCreationres", blLoanCreationResponse);
        System.out.println("amount " + blLoanCreationResponse.getAmount());
        writeToLog(getCrCaller());
        // responseCode = blLoanCreationResponse.getResponseCode();
        return response;
    }

    public String splitItem(String bsName, int position)
    {
        String temp = bsName;
        String[] splitString = temp.split("\\/");
        return splitString[position];
    }

    private void writeToLog(CRCaller crCaller)
    {
        //ApiLogger.getLogger().error(e1);
        setEndTime(System.currentTimeMillis());
        crCaller.setDuration(String.valueOf(getEndTime() - getStartTime()) + " Ms");


        // logLoanCreation(getCrCaller());
        new Thread(new ThreadLogger(new ApiLogger(), "<transaction>" + "\r\n" + crCaller.getExtraIndent() + "\r\n" + "</transaction>")).start();
    }

    private boolean logLoanCreation(BLLoanCreationResponse blResponse)
    {
        try (Statement statement = conn.createStatement())
        {
            statement.executeUpdate("INSERT INTO " + XapiCodes.xapiSchema + "..E_LOAN_CREATION(ACCT_NO,ACCT_TYPE,PHONE_NUMBER,CREATE_DT,PERIOD,TERM,AMOUNT,RESPONSE_CODE,RESPONSE_MESSAGE,PROC_RETURN_CODE)" +
                    "VALUES('" + blResponse.getAccountNumber() + "','" + blResponse.getAccountType() + "','" + blResponse.getPhoneNumber() + "',getdate(),'" + blResponse.getPeriod() + "'," + blResponse.getTerm() + "," + blResponse.getAmount() + ",'" + blResponse.getResponseCode() + "','" + blResponse.getResponseMessage() + "','" + blResponse.getReturnCode() + "')");
            return true;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }

    }

    private boolean updatePtid()
    {
        try (Statement statement = conn.createStatement())
        {
            statement.executeUpdate("update " + XapiCodes.coreschema + "..PC_PTID set ptid =ptid+1 ");
            return true;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }

    }

    public Map<String, Object> applyLoan(LoanApplicationRequest request)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date());
        c.add(Calendar.YEAR, 1);
        java.util.Date newDate = c.getTime();
        Map<String, Object> response = new HashMap<String, Object>();

        try (CallableStatement callableStatement = conn.prepareCall(SQL.LOAN_APPLICATION))
        {

            callableStatement.setLong(1, request.getRimNo());
            callableStatement.setString(2, request.getAccountType());
            callableStatement.setLong(3, request.getClassCode());
            callableStatement.setLong(4, request.getPurposeId());
            callableStatement.setString(5, request.getUserId());
            callableStatement.setString(6, request.getApplicationText());
            callableStatement.setBigDecimal(7, request.getLoanAmount());
            callableStatement.setLong(8, request.getTerm());
            callableStatement.setString(9, request.getPeriod());
            callableStatement.registerOutParameter(10, Types.VARCHAR);//applicationNo(acct_no in la_acct)
            callableStatement.registerOutParameter(11, Types.INTEGER); //return code

            int returnCode = -1;
            String applicationNoNew = "";

            try (ResultSet rset = callableStatement.executeQuery())
            {
                if (rset != null && rset.isBeforeFirst())
                {
                    ResultSetMetaData meta = rset.getMetaData();
                    while (rset.next())
                    {
                        applicationNoNew = rset.getString(1);
                        returnCode = rset.getInt(2);
                    }
                }
            }

            // Oops, failed to create the account
            if (returnCode != 0)
            {
                response.put("responseCode", returnCode);
                return response;
            }
            else
            {
                int approvalResponse1, approvalResponse2 = -1;
                request.setApplicationNewNo(applicationNoNew);
                request.setApprovalAction("Reviewed");

                approvalResponse1 = approveLoan(request);
                if (approvalResponse1 == 00)
                {
                    request.setApprovalAction("Approved");
                    approvalResponse2 = approveLoan(request);
                    if (approvalResponse2 == 00)
                    {
                        LoanCreationResponse loanCreationResponse = createLoan(request);
                        if (loanCreationResponse.getReturnCode() == 00)
                        {
                            try (Statement statement = conn.createStatement();
                                 ResultSet rs = statement.executeQuery("select acct_no as applNo,acct_type as applType,classCode,ln_acct_no,ln_acct_type from " + XapiCodes.coreschema + "..la_acct where acct_no ='" + applicationNoNew + "'"))
                            {
                                loanCreationResponse.setApplicationAcctType(rs.getString("applType"));
                                loanCreationResponse.setApplicationAcctNo(rs.getString("applNo"));
                                loanCreationResponse.setAccountType(rs.getString("ln_acct_type"));
                                loanCreationResponse.setAccountNo(rs.getString("ln_acct_no"));

                            } catch (Exception e1)
                            {
                                ApiLogger.getLogger().error(e1);
                            }
                            //create loan payment record
                            int paymentRecordResponse = createLoanPaymentRecord(request, loanCreationResponse);
                            if (paymentRecordResponse == 00)
                            {
                                //generate bills
                                request.setAccountNo(loanCreationResponse.getAccountNo());
                                request.setAccountType(loanCreationResponse.getAccountType());
                                request.setDepositAccountNo(loanCreationResponse.getRepaymentAccountNo());
                                request.setDepositAccountType(loanCreationResponse.getRepaymentAccountType());

                                int generateBillsResponse = generateBills(request);
                                if (generateBillsResponse == 00)
                                {
                                    //ln_worksheet
                                    int lnWorksheetResponse = createLoanWorkSheet(request);
                                    if (lnWorksheetResponse == 00)
                                    {
                                        //disburse loan
                                        int disbursementResponse = disburseLoan(request);
                                        if (disbursementResponse == 00)
                                        {
                                            //advance loan
                                            int advanceLoanResponse = advanceLoan(request);
                                            if (advanceLoanResponse == 00)
                                            {
                                                // get the newly created account
                                                String accountNo = "";
                                                try (Statement statement = conn.createStatement();
                                                     ResultSet resultSet = statement.executeQuery(
                                                             new StringBuilder().append("select rim_no, acct_no,acct_type,status,amt,period,term,mat_dt from " + XapiCodes.coreschema + "..ln_acct where acct_no ='" + request.getAccountNo() + "' and acct_type='" + request.getAccountType() + "'").toString()))
                                                {
                                                    Map<String, Object> asMap = asMap(resultSet);
                                                    if (!isBlank(resultSet.getString("acct_no")))
                                                    {
                                                        response.put("responseCode", XAPI_APPROVED);
                                                        response.put("cust_info", asMap);
                                                    }
                                                    else
                                                    {
                                                        response.put("responseCode", LN_ACCT_CREATION_FAILED);
                                                    }

                                                } catch (Exception e1)
                                                {
                                                    ApiLogger.getLogger().error(e1);
                                                }

                                            }
                                        }
                                        else
                                        {
                                            response.put("responseCode", returnCode);
                                            return response;
                                        }
                                    }
                                    else
                                    {
                                        response.put("responseCode", returnCode);
                                        return response;
                                    }
                                }
                                else
                                {
                                    response.put("responseCode", returnCode);
                                    return response;
                                }

                            }
                            else
                            {
                                response.put("responseCode", returnCode);
                                return response;
                            }
                        }
                        else
                        {
                            response.put("responseCode", returnCode);
                            return response;

                        }
                    }
                    else
                    {
                        response.put("responseCode", returnCode);
                        return response;

                    }
                }
                else
                {
                    response.put("responseCode", returnCode);
                    return response;

                }
            }


        } catch (SQLException ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        return response;
    }

    public int approveLoan(LoanApplicationRequest request)
    {
        int returnCode = -1;
        try (CallableStatement callableStatement = conn.prepareCall(SQL.APPROVE_LOAN))
        {

            callableStatement.setLong(1, request.getRimNo());
            callableStatement.setString(2, request.getAccountType());
            callableStatement.setDate(3, Date.valueOf(request.getApplicationNewNo()));
            callableStatement.setString(4, request.getUserId());
            callableStatement.setString(5, request.getApplicationText());
            callableStatement.setBigDecimal(6, request.getLoanAmount());
            callableStatement.setLong(7, request.getTerm());
            callableStatement.setString(8, request.getPeriod());
            callableStatement.setString(9, request.getApprovalAction());
            callableStatement.registerOutParameter(10, Types.NUMERIC);


            BigDecimal rimNumber = new BigDecimal(0);

            try (ResultSet rset = callableStatement.executeQuery())
            {
                if (rset != null && rset.isBeforeFirst())
                {
                    ResultSetMetaData meta = rset.getMetaData();
                    while (rset.next())
                    {
                        returnCode = rset.getInt(1);
                    }
                }
            }

        } catch (SQLException ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        return returnCode;
    }

    public LoanCreationResponse createLoan(LoanApplicationRequest request)
    {
        LoanCreationResponse loanCreationResponse = new LoanCreationResponse();
        StringBuilder stateBuilder = new StringBuilder();
        try (CallableStatement callableStatement = conn.prepareCall(SQL.CREATE_LOAN))
        {

            stateBuilder.append("'").append(request.getRimNo()).append("', ");
            callableStatement.setLong(1, request.getRimNo());
            callableStatement.setLong(2, request.getClassCode());
            callableStatement.setString(3, request.getUserId());
            callableStatement.setLong(4, request.getTerm());
            callableStatement.setString(5, request.getPeriod());
            callableStatement.setBigDecimal(6, request.getLoanAmount());
            callableStatement.setString(7, request.getApplicationType());
            callableStatement.setString(8, request.getAccountType());
            callableStatement.setString(9, request.getLoanApplNo());

            callableStatement.setString(10, "");
            callableStatement.setString(11, "");
            callableStatement.registerOutParameter(12, Types.VARCHAR);
            callableStatement.registerOutParameter(13, Types.INTEGER);

            int returnCode = -1;
            String accountNumber = "";
            ApiLogger.getLogger().info(stateBuilder);

            try (ResultSet rset = callableStatement.executeQuery())
            {
                if (rset != null && rset.isBeforeFirst())
                {
                    ResultSetMetaData meta = rset.getMetaData();
                    if (rset.next())
                    {
                        accountNumber = rset.getString(1);
                        returnCode = rset.getInt(2);

                    }
                }
            }

            loanCreationResponse.setAccountNo(accountNumber);
            loanCreationResponse.setReturnCode(returnCode);
            loanCreationResponse.setRepaymentAccountNo(request.getAccountNo());

        } catch (SQLException ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        return loanCreationResponse;
    }

    public int createLoanPaymentRecord(LoanApplicationRequest request, LoanCreationResponse loanCreationResponse)
    {
        StringBuilder stateBuilder = new StringBuilder();
        stateBuilder.append("'").append(request.getRimNo()).append("', ");
        int returnCode = -1;
        try (CallableStatement callableStatement = conn.prepareCall(SQL.CREATE_PAYMENT_RECORD))
        {

            stateBuilder.append("'").append(request.getRimNo()).append("', ");
            callableStatement.setLong(1, request.getRimNo());
            callableStatement.setLong(2, request.getClassCode());
            callableStatement.setString(3, request.getUserId());
            callableStatement.setLong(4, request.getTerm());
            callableStatement.setString(5, request.getPeriod());
            callableStatement.setBigDecimal(6, request.getLoanAmount());
            callableStatement.setString(7, loanCreationResponse.getAccountType());
            callableStatement.setString(8, loanCreationResponse.getAccountNo());
            callableStatement.setString(9, loanCreationResponse.getRepaymentAccountType());
            callableStatement.setString(10, loanCreationResponse.getRepaymentAccountNo());

            callableStatement.setString(11, "");
            callableStatement.setString(12, "");
            callableStatement.registerOutParameter(13, Types.INTEGER);


            ApiLogger.getLogger().info(stateBuilder);

            try (ResultSet rset = callableStatement.executeQuery())
            {
                if (rset != null && rset.isBeforeFirst())
                {
                    ResultSetMetaData meta = rset.getMetaData();
                    while (rset.next())
                    {
                        returnCode = rset.getInt(1);
                    }
                }
            }

        } catch (SQLException ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        return returnCode;
    }

    public int generateBills(LoanApplicationRequest request)
    {
        StringBuilder stateBuilder = new StringBuilder();
        int returnCode = -1;
        try (CallableStatement callableStatement = conn.prepareCall(SQL.CREATE_PAYMENT_RECORD))
        {

            stateBuilder.append("'").append(request.getRimNo()).append("', ");
            callableStatement.setBigDecimal(1, request.getLoanAmount());
            callableStatement.setString(2, request.getAccountType());
            callableStatement.setString(3, request.getAccountNo());
            callableStatement.registerOutParameter(4, Types.INTEGER);


            ApiLogger.getLogger().info(stateBuilder);

            try (ResultSet rset = callableStatement.executeQuery())
            {
                if (rset != null && rset.isBeforeFirst())
                {
                    ResultSetMetaData meta = rset.getMetaData();
                    while (rset.next())
                    {
                        returnCode = rset.getInt(1);
                    }
                }
            }

        } catch (SQLException ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        return returnCode;
    }

    public int createLoanWorkSheet(LoanApplicationRequest request)
    {

        StringBuilder stateBuilder = new StringBuilder();
        int returnCode = -1;

        try (CallableStatement callableStatement = conn.prepareCall(SQL.GENERATE_LOAN_WORKSHEET))
        {

            //   stateBuilder.append("'").append(customerRequest.getFirst_name()).append("', ");

            callableStatement.setLong(1, request.getRimNo());
            callableStatement.setLong(2, request.getClassCode());
            callableStatement.setString(3, request.getUserId());
            callableStatement.setBigDecimal(4, request.getLoanAmount());
            callableStatement.setString(5, request.getAccountType());
            callableStatement.setString(6, request.getAccountNo());
            callableStatement.registerOutParameter(7, Types.NUMERIC);


            ApiLogger.getLogger().info(stateBuilder);

            try (ResultSet rset = callableStatement.executeQuery())
            {
                if (rset != null && rset.isBeforeFirst())
                {
                    ResultSetMetaData meta = rset.getMetaData();
                    while (rset.next())
                    {
                        returnCode = rset.getInt(1);
                    }
                }
            }


        } catch (SQLException ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        return returnCode;
    }

    public int disburseLoan(LoanApplicationRequest request)
    {

        StringBuilder stateBuilder = new StringBuilder();
        int returnCode = -1;

        try (CallableStatement callableStatement = conn.prepareCall(SQL.DISBURSE_LOAN))
        {

            stateBuilder.append("'").append(request.getRimNo()).append("', ");
            callableStatement.setLong(1, request.getRimNo());
            callableStatement.setLong(2, request.getClassCode());
            callableStatement.setString(3, request.getUserId());
            callableStatement.setString(4, request.getAccountType());
            callableStatement.setString(5, request.getAccountNo());
            callableStatement.setString(6, request.getDepositAccountType());
            callableStatement.setString(7, request.getDepositAccountNo());
            callableStatement.registerOutParameter(8, Types.NUMERIC);


            try (ResultSet rset = callableStatement.executeQuery())
            {
                if (rset != null && rset.isBeforeFirst())
                {
                    ResultSetMetaData meta = rset.getMetaData();
                    while (rset.next())
                    {
                        returnCode = rset.getInt(1);

                    }
                }
            }

        } catch (SQLException ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        return returnCode;
    }

    public int advanceLoan(LoanApplicationRequest request)
    {
        StringBuilder stateBuilder = new StringBuilder();
        int returnCode = -1;

        try (CallableStatement callableStatement = conn.prepareCall(SQL.ADVANCE_LOAN))
        {

            stateBuilder.append("'").append(request.getRimNo()).append("', ");
            callableStatement.setLong(1, request.getRimNo());
            callableStatement.setLong(2, request.getClassCode());
            callableStatement.setString(3, request.getUserId());
            callableStatement.setString(4, request.getAccountType());
            callableStatement.setString(5, request.getAccountNo());
            callableStatement.registerOutParameter(6, Types.NUMERIC);

            ApiLogger.getLogger().info(stateBuilder);

            try (ResultSet rset = callableStatement.executeQuery())
            {
                if (rset != null && rset.isBeforeFirst())
                {
                    ResultSetMetaData meta = rset.getMetaData();
                    while (rset.next())
                    {
                        returnCode = rset.getInt(1);
                    }
                }
            }


        } catch (SQLException ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        return returnCode;
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
        try
        {
            if (resultSet != null && resultSet.isBeforeFirst())
            {
                ResultSetMetaData metaData = resultSet.getMetaData();
                if (resultSet.next())
                {
                    for (int i = 1; i <= metaData.getColumnCount(); i++)
                        map.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
            }
        } catch (SQLException e)
        {
            ApiLogger.getLogger().error(e);
        }
        return map;
    }

    List<Map<String, Object>> asListMap(ResultSet resultSet)
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try
        {
            if (resultSet != null && resultSet.isBeforeFirst())
            {
                ResultSetMetaData metaData = resultSet.getMetaData();
                while (resultSet.next())
                {
                    Map<String, Object> map = new HashMap<String, Object>();
                    for (int i = 1; i <= metaData.getColumnCount(); i++)
                        map.put(metaData.getColumnName(i), resultSet.getObject(i));
                    list.add(map);
                }
            }
        } catch (SQLException e)
        {
            ApiLogger.getLogger().error(e);
        }
        return list;
    }

    public Map<String, Object> findRimCreationFeatures()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> response = new HashMap<String, Object>();
        try (Statement statement = conn.createStatement())
        {

            List<Map<String, Object>> title_types = fetchListMap("select title_id, title from " + XapiCodes.coreschema + "..ad_rm_title where title_id in (1,4,9)", statement);
            response.put("title_types", title_types);

            List<Map<String, Object>> states = fetchListMap2("select city from " + XapiCodes.coreschema + "..ei_city", statement);
            response.put("states", states);

            response.put("responseCode", XAPI_APPROVED);

        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        return response;
    }

    public List<Map<String, Object>> fetchListMap(String query, Statement statement)
    {
        try (ResultSet resultSet = statement.executeQuery(query))
        {
            List<Map<String, Object>> mapList = asListMap(resultSet);
            return mapList;
        } catch (Exception e1)
        {
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
        try (ResultSet resultSet = statement.executeQuery(query2))
        {
            while (resultSet.next())
            {
                cityMap.put(resultSet.getInt(1), resultSet.getString(2));
            }

        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }

        for (Map.Entry m : cityMap.entrySet())
        {
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
        String Fstring = "\"".concat(txt).concat("\"");


        return Fstring;
    }

    public static boolean isBlank(Object object)
    {
        return object == null || "".equals(String.valueOf(object).trim()) || "null".equals(String.valueOf(object).trim()) || String.valueOf(object).trim().toLowerCase().contains("---select");
    }

    public CRCaller getCrCaller()
    {
        return crCaller;
    }

    public void setCrCaller(CRCaller crCaller)
    {
        this.crCaller = crCaller;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }
}
