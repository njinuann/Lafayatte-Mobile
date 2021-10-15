package org.redlamp.extras;

import org.redlamp.interfaces.ISO;
import org.redlamp.interfaces.SQL;
import org.redlamp.logger.ApiLogger;
import org.redlamp.model.BLAccount;
import org.redlamp.model.BLScoreCard;
import org.redlamp.model.BLScoreParameter;
import org.redlamp.model.LoanRequest;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoringHandler implements AutoCloseable, ISO, SQL {

    private Connection conn;
    private StringBuilder builder;
    private BigDecimal HUNDREAD = BigDecimal.valueOf(100);
    private BLScoreCard blScoreCard = new BLScoreCard();
    private CRCaller crCaller = new CRCaller();
    private long endTime;
    private long startTime;


    public ScoringHandler() {
        try {
            setBuilder(new StringBuilder());
            conn = XapiPool.getConnection();
            setCrCaller(new CRCaller());
            setStartTime(System.currentTimeMillis());
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

    public static boolean isBlank(Object object) {
        return object == null || "".equals(String.valueOf(object).trim()) || "null".equals(String.valueOf(object).trim())
                || String.valueOf(object).trim().toLowerCase().contains("---select");
    }

    public Map<String, Object> processScore(LoanRequest request) {
        setBlScoreCard(getCrCaller().getBlScoreCard());
        BigDecimal approach1ScoreAmount = BigDecimal.ZERO, approach2ScoreAmount = BigDecimal.ZERO,
                minApproachOneTwoMaxDepositorLnAmount = BigDecimal.ZERO, approvedScoreAmount = BigDecimal.ZERO,
                cycleScore;


        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        String additionalResponseText = "";


        //check if the customer is eligible based on rim classcode,deposit class code and no previous active loan
        BLAccount blAccount = queryRegistered(request.getAccountNo(), request.getPhoneNumber());
        System.out.println("getting account>>>>>>>>>>>>>>>>>>>>>>>>> " + blAccount.getAccountNumber());
        getCrCaller().setCall("blAccount", blAccount);
        if (!isBlank(blAccount.getAccountNumber())) {
            boolean lastLoanRepaidTimely;
            boolean hasVoluntaryDeposit = true;

            getBlScoreCard().setBorrower(false);
            getBlScoreCard().setLoanScoreTpe("Depositor Score");
            getBlScoreCard().setAccountType(blAccount.getAccountType());
            getBlScoreCard().setRimNumber(blAccount.getRimNo());
            getBlScoreCard().setAccountWithHighestDisb(blAccount.getLoanAccountNumber());

            getBlScoreCard().setAccountNumber(request.getAccountNo());
            getBlScoreCard().setPhoneNumber(blAccount.getPhoneNumber());

            //Start preliminary checks************/
            /* is class code allowed*/
            boolean isClassCodeAllowed = checkEligibleClasses(blAccount);
            getCrCaller().setCall("isClassCodeAllowed", isClassCodeAllowed);

            /*check if they had a previous digital loan and assign cycleScore*/
            boolean hasPreviousDigitalLoan = hasPreviousDigitalLoan(blAccount);
            getCrCaller().setCall("hasPreviousDigitalLoan", hasPreviousDigitalLoan);
            cycleScore = hasPreviousDigitalLoan ? XapiPool.dpCycle2Score : XapiPool.dpCycle1Score;
            getCrCaller().setCall("Assigned CycleScore", cycleScore);

            /*check if they have ever taken a loan which is not a digital loan and if it was repaid well*/
            boolean hasPreviousNonDigitalLoan = hasPreviousNonDigitalLoan(blAccount);
            getCrCaller().setCall("hasPreviousNonDigitalLoan", hasPreviousNonDigitalLoan);

            if (hasPreviousNonDigitalLoan) {
                //check if there was timely loan repayment in the last non-digital loan
                lastLoanRepaidTimely = lastLoanRepaidTimely(blAccount.getRimNo());
            } else {
                lastLoanRepaidTimely = true;
            }
            getCrCaller().setCall("lastLoanRepaidTimely", lastLoanRepaidTimely);

            //check if there is voluntary deposits for the last 3 months
            for (BLScoreParameter blScoreParameter : getTxnForPeriod(blAccount, XapiPool.dpAveVolumeMonths.intValue())) {
                getCrCaller().setCall("Txn For Month", blScoreParameter.getNoOfTxn() + " No of deposit for month " + blScoreParameter.getTxnMonth());
                if (blScoreParameter.getNoOfTxn() == 0) //evaluate
                {
                    //change to report false just in case it gets 0 for any month
                    hasVoluntaryDeposit = false;
                    break;
                }
            }
            getCrCaller().setCall("hasVoluntaryDeposit", hasVoluntaryDeposit);

            getBlScoreCard().getBlMainScoreCard().setCycleScore(cycleScore);
            getBlScoreCard().setClassCodesAllowed(isClassCodeAllowed);
            getBlScoreCard().setVoluntaryDepositor(hasVoluntaryDeposit);
            getBlScoreCard().setPrevLoanRepaidTimely(lastLoanRepaidTimely);
            getCrCaller().setCall("isClassCodeAllowed", isClassCodeAllowed);
            getCrCaller().setCall("hasVoluntaryDepositLast3Months", hasVoluntaryDeposit);
            getCrCaller().setCall("lastLoanRepaidTimely", lastLoanRepaidTimely);

            //check if the customer is allowed
            if (isClassCodeAllowed) {
                if (lastLoanRepaidTimely) {
                    if (hasVoluntaryDeposit) {
                        // approach 1
                        //start Variable A - rewarding Depositors
                        BigDecimal depositorScore = checkDepositPerPeriodScore(blAccount.getRimNo());
                        getBlScoreCard().setDepositorScore(depositorScore);
                        getCrCaller().setCall("Variable A - depositorScore ", depositorScore);

                        /*start Variable B - Get the average volume of net deposits - for every month; deposits - loans */
                        //check for 3 months
                        BigDecimal averageDepositVlmScore = checkAveDepositVolumeScore(blAccount.getRimNo(), XapiPool.dpAveVolumeMonths.intValue());
                        getBlScoreCard().setAverageVlmScore(averageDepositVlmScore);
                        getCrCaller().setCall("Variable B - averageVlmScore ", averageDepositVlmScore);

                        //start Variable c - check Repayment behaviour
                        BigDecimal RepaymentScore = hasPreviousDigitalLoan ? checkRepaymentScore(blAccount.getRimNo()) : BigDecimal.ONE;
                        getBlScoreCard().setRepaymentScore(RepaymentScore);
                        getCrCaller().setCall("Variable C - RepaymentScore ", RepaymentScore);
                        // END OF APPROACH 1

                        // APPROACH 2
                        BigDecimal minAverageBalance = checkMinAverageVolumeScore(blAccount.getRimNo(), XapiPool.dpAveVolumeMonths.intValue());
//                BigDecimal minAverageBalanceScored = (minAverageBalance.compareTo(XapiPool.minCycleDpEvalAmount) > 0)
//                        ? minAverageBalance.multiply(cycleScore) : BigDecimal.ZERO;
                        getCrCaller().setCall("Minimum AVG BAL of last " + XapiPool.dpAveVolumeMonths.intValue() + " Months", minAverageBalance);
                        BigDecimal minAverageBalanceScored = minAverageBalance.multiply(cycleScore);
                        getCrCaller().setCall("Multiply By Assigned CycleScore (" + cycleScore + ")", minAverageBalanceScored);
                        // END OF APPROACH 2

                        // APPROACH 1 Scored value
                        approach1ScoreAmount = depositorScore.multiply(averageDepositVlmScore).multiply(RepaymentScore);  // APPROACH 1 Scored value
                        getBlScoreCard().setFinalScore1(approach1ScoreAmount);
                        getCrCaller().setCall("ApproachOneScoreAmount-(AxBxC) ", approach1ScoreAmount);

                        // APPROACH 2 Scored value
                        approach2ScoreAmount = minAverageBalanceScored;
                        getBlScoreCard().setFinalScoreAvg(approach2ScoreAmount);
                        getCrCaller().setCall("ApproachTwoScoreAmount", minAverageBalanceScored);
                        BigDecimal maxDepositorLnAmount = hasPreviousDigitalLoan
                                ? XapiPool.maxDepositorLnAmount.multiply(new BigDecimal(2))
                                : XapiPool.maxDepositorLnAmount;
                        getCrCaller().setCall("maxDepositorLnAmount", maxDepositorLnAmount);
                        getCrCaller().setCall("minDepositorLnAmount", XapiPool.minDepositorLnAmount);

                        // get the minimum of both approaches
                        minApproachOneTwoMaxDepositorLnAmount = approach1ScoreAmount.min(approach2ScoreAmount).min(maxDepositorLnAmount);
                        getBlScoreCard().setOverallScore(minApproachOneTwoMaxDepositorLnAmount);
                        getCrCaller().setCall("minApproachOneTwoMaxDepositorLnAmount", minApproachOneTwoMaxDepositorLnAmount);

                        if (minApproachOneTwoMaxDepositorLnAmount.compareTo(maxDepositorLnAmount) > 0) {
                            approvedScoreAmount = maxDepositorLnAmount;
                        } else if (minApproachOneTwoMaxDepositorLnAmount.compareTo(XapiPool.minDepositorLnAmount) < 0) {
                            approvedScoreAmount = BigDecimal.ZERO;
                            additionalResponseText = "[Eligible Amount Less Than Minimum Allowed]";
                        } else {
                            approvedScoreAmount = minApproachOneTwoMaxDepositorLnAmount;
                        }
                        getCrCaller().setCall("approvedScoreAmount", approvedScoreAmount);
                    } else {
                        additionalResponseText = "[Voluntary Deposits Not Satisfied]";
                    }
                } else {
                    additionalResponseText = "[Last Loan Not Timely Repaid]";
                }
            } else {
                additionalResponseText = "[Class Code Not Allowed]";
            }
//            } else {
//                approvedScoreAmount = BigDecimal.ZERO;
//            }

            if (BigDecimal.ZERO.compareTo(approvedScoreAmount) >= 0) {
                list.add(map);
                response.put("responseCode", CUST_NOT_ELIGIBLE);
            } else {
                getBlScoreCard().setAccountNumber(blAccount.getAccountNumber());
                getBlScoreCard().setAmount(approvedScoreAmount);
                map.put("Eligible_Amount", getBlScoreCard().getAmount());
                map.put("Account", getBlScoreCard().getAccountNumber());
                //  map.put("Period", XapiPool.defaultLoanPeriod);
                //   map.put("Term", XapiPool.minLoanterm);
                list.add(map);
                response.put("responseCode", XAPI_APPROVED);
                additionalResponseText = "";
            }

        } else {
            response.put("responseCode", CUST_NOT_ELIGIBLE);
            additionalResponseText = "[No Registration Found]";
        }

        if (CUST_NOT_ELIGIBLE.equals(response.get("responseCode"))) {
            map.put("Eligible_Amount", BigDecimal.ZERO);
            map.put("Account", blScoreCard.getAccountNumber());
            list.add(map);
            response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
        } else {
            response.put("responseTxt", "Customer is Eligible");
        }

        response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode"))
                + ("".equals(additionalResponseText) ? "" : " " + additionalResponseText));
        response.put("Eligibility_Check", list);
        getCrCaller().setCall("response", response);
        getCrCaller().setCall("scoringitems", getBlScoreCard());
        getCrCaller().setXapiRespCode(String.valueOf(response.get("responseCode")));
        getCrCaller().setXapiRespMsg(String.valueOf(response.get("responseTxt")));
        windUp();

        return response;
    }

    public BigDecimal roundedAmount(int value) {
        return new BigDecimal((value / 1000) * 1000);
    }

    public void windUp() {
        writeToLog();

    }

    private void writeToLog() {
        //ApiLogger.getLogger().error(e1);
        setEndTime(System.currentTimeMillis());
        getCrCaller().setDuration(String.valueOf(getEndTime() - getStartTime()) + " Ms");


        logLoanScore(getCrCaller());
        new Thread(new ThreadLogger(new ApiLogger(), "<transaction>" + "\r\n\t" + getCrCaller() + "</transaction>")).start();

        // APMain.cqsLog.logEvent(gettXCaller());
        setCrCaller(new CRCaller());
    }

    private boolean logLoanScore(CRCaller crCaller) {
        ApiLogger.getLogger().info("crCaller.getXapiRespCode()",crCaller.getXapiRespCode());

        String saveLoanScore = "INSERT INTO " + XapiCodes.xapiSchema + "..E_LOAN_SCORE " +
                "(ACCOUNT_NO, ACCT_TYPE, RIM_NO, PHONE_NUMBER, SCORE_TYPE, AMOUNT, CYCLE_SCORE, DEPOSITOR_SCORE, " +
                "AVERAGE_VOLUME_SCORE, REPAYMENT_SCORE, FINAL_SCORE, HISTORY_SCORE, APPROVED_SCORE, LATE_INSTALMENT_SCORE, " +
                "BORROWER, CLASS_CODE_ALLOWED, PREV_TIMELY_REPAYMENT, VOLUNTARY_DESPOSITS, VALID_CYCLE, REQ_MIN_INSTALMENTS, " +
                "LOAN_RESTRUCTRED, CLOSURE_MONTH_VALID, PREV_DEFAULTED_LOAN7DAYS, PREV_DEFAULTED_LOAN30DAYS, CURRENT_DELAYED_PAYMENT, " +
                "MAX_LOAN_PER_YEAR, DELAYED_CURRENT_INSTALEMENT, RESPONSE_CODE, RESPONSE_MESSAGE, CREATE_DT )" +
                "VALUES('" + crCaller.getBlScoreCard().getAccountNumber() + "','" + crCaller.getBlScoreCard().getAccountType() + "'," +
                "" + crCaller.getBlScoreCard().getRimNumber() + ",'" + crCaller.getBlScoreCard().getPhoneNumber() + "'," +
                "'" + crCaller.getBlScoreCard().getLoanScoreTpe() + "'," + crCaller.getBlScoreCard().getAmount() + "," +
                "" + crCaller.getBlScoreCard().getCycleScore() + "," + crCaller.getBlScoreCard().getDepositorScore() + "," +
                "" + crCaller.getBlScoreCard().getAverageVlmScore() + "," + crCaller.getBlScoreCard().getRepaymentScore() + "," +
                "" + crCaller.getBlScoreCard().getFinalScore1() + ", " + crCaller.getBlScoreCard().getHistoryScore() + "," +
                "" + crCaller.getBlScoreCard().getApprovedScore() + "," + crCaller.getBlScoreCard().getLateInstalmentScore() + "," +
                "'" + yesNo(crCaller.getBlScoreCard().isBorrower()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isClassCodesAllowed()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isPrevLoanRepaidTimely()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isVoluntaryDepositor()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isHasValidCycle()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isHasRequiredMinInstalments()) + "', " +
                "'" + yesNo(crCaller.getBlScoreCard().isRestructured()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isHasClosureMonths()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isHasPreviousLoanDefaulted7days()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isHasPreviousLoanDefaulted30days()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isHasCurrentDelayedPayment()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isHasMaxLoansPerYear()) + "'," +
                "'" + yesNo(crCaller.getBlScoreCard().isHasDelayedCurrentInstallment()) + "'," +
                "'" + crCaller.getXapiRespCode() + "','" + XapiCodes.getErrorDesc(crCaller.getXapiRespCode()) + "',getdate())";

        ApiLogger.getLogger().info(saveLoanScore);
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(saveLoanScore);
            return true;
        } catch (Exception ex) {
            ApiLogger.getLogger().error(ex);
            return false;
        }

    }

    public String yesNo(boolean checkVal) {
        return checkVal ? "Y" : "N";
    }

    public BLAccount queryRegistered(String acctNo, String phoneNo) {

        BLAccount bLAccount = new BLAccount();

        String query = "select a.value bvn, b.acct_no,b.acct_type, b.rim_no,c.cust_service_key as phone_number "
                + "from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " +
                "" + XapiCodes.coreschema + "..rm_services  c where a.field_id=45 " +
                "and (a.acct_no_key = '" + acctNo.substring(0, 3) + "-" + acctNo.substring(3) + "' " +
                "or value = '" + acctNo.replace("-", "") + "') " +
                "and b.acct_no = a.acct_no_key and c.rim_no in (select rim_no " +
                "from " + XapiCodes.coreschema + "..rm_acct where class_code in (" + XapiPool.allowedRimClassDepositor + ")) " +
                "and c.rim_no = b.rim_no and c.cust_service_key = '" + phoneNo + "' and c.services_id =44";

        ApiLogger.getLogger().debug("queryRegistered \n\r\t " + query);

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            if (rs.next()) {
                bLAccount.setAccountType(rs.getString("acct_type").trim());
                bLAccount.setAccountNumber(rs.getString("acct_no").trim());
                bLAccount.setRimNo(rs.getLong("rim_no"));
                bLAccount.setNubanAccountNumber(rs.getString("bvn").trim());
                bLAccount.setPhoneNumber(rs.getString("phone_number").trim());
            }


        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        return bLAccount;
    }

    public boolean checkEligibleClasses(BLAccount bLAccount) {
        String query = "select dd.acct_no " +
                "from  " + XapiCodes.coreschema + "..dp_display dd,  " + XapiCodes.coreschema + "..rm_acct rm " +
                "where dd.rim_no=rm.rim_no and dd.status='Active' and rm.status='Active' " +
                "and rm.class_code in (" + XapiPool.allowedRimClassDepositor + " ) " +
                "and dd.class_code in (" + XapiPool.allowedDpClass + ")   and rm.rim_no=" + bLAccount.getRimNo() + " ";
        //and dd.rim_no not in (select rim_no from  " + XapiCodes.coreschema + "..ln_display where  rim_no=dd.rim_no and status ='Active'
        return checkIfExists("checkEligibleClasses", query);

    }

    public boolean hasPreviousDigitalLoan(BLAccount bLAccount) {
        String query = "select acct_no from " + XapiCodes.coreschema + "..ln_display " +
                "where class_code in (" + XapiPool.borrowerLoanClassCode + ") " +
                "and rim_no = (select b.rim_no from  " + XapiCodes.coreschema + "..gb_user_defined a,  " +
                "" + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services_acct c " +
                "where a.field_id = 45 " +
                "and (a.acct_no_key = '" + bLAccount.getAccountNumber().substring(0, 3) + bLAccount.getAccountNumber().substring(3) + "' " +
                "or value = '" + bLAccount.getAccountNumber().replace("-", "") + "') " +
                "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no and c.cust_service_key = '" + bLAccount.getPhoneNumber() + "')";

        return checkIfExists("hasPreviousDigitalLoan", query);
    }

    /*this used to be checkCycle*/
    public boolean hasPreviousNonDigitalLoan(BLAccount bLAccount) {
        String query = "select acct_no from " + XapiCodes.coreschema + "..ln_display " +
                "where class_code not in  (" + XapiPool.borrowerLoanClassCode + ") " +
                "and rim_no = (select b.rim_no from  " + XapiCodes.coreschema + "..gb_user_defined a,  " +
                "" + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services_acct c " +
                "where a.field_id = 45 " +
                "and (a.acct_no_key = '" + bLAccount.getAccountNumber().substring(0, 3) + bLAccount.getAccountNumber().substring(3) + "' " +
                "or value = '" + bLAccount.getAccountNumber().replace("-", "") + "') " +
                "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no and c.cust_service_key = '" + bLAccount.getPhoneNumber() + "')";

        return checkIfExists("hasPreviousNonDigitalLoan", query);
    }

    //    public boolean lastLoanRepaidTimely(Long rimNo) {
//        return checkIfExists("Check_if_last_Loan_Repaid_Timely",
//                "select * from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc " +
//                        "where bb.acct_no=cc.acct_no and bb.acct_type=cc.acct_type " +
//                        "and cc.acct_no = (select max(acct_no) from " + XapiCodes.coreschema + "..ln_acct bb " +
//                        "where rim_no = " + rimNo + " and status='Closed') and tran_code in (345) " +
//                        "and bb.effective_dt<=cc.mat_dt");
//
//    }

    public boolean lastLoanRepaidTimely(Long rimNo) {
        boolean lastLoanRepaidTimely = true;
        /*get no of days late for every instalment of this customer's last closed loan*/
        String query = "select aa.acct_no,aa.create_dt,aa.amt,cc.pmt_due_dt,aa.posting_dt_tm," +
                "cc.type, datediff(dd,cc.pmt_due_dt,aa.posting_dt_tm) noOfLateDays " +
                "from " + XapiCodes.coreschema + "..ln_history aa," + XapiCodes.coreschema + "..ln_bill_map bb ," +
                "" + XapiCodes.coreschema + "..ln_bill cc " +
                "where aa.acct_no = (select acct_no from " + XapiCodes.coreschema + "..ln_acct bb " +
                "where ptid = (select max(ptid) from " + XapiCodes.coreschema + "..ln_acct " +
                "where rim_no = " + rimNo + " and status='Closed' and class_code not in (" + XapiPool.borrowerLoanClassCode + "))) " +
                "and aa.acct_no = bb.acct_no and aa.acct_no=cc.acct_no and bb.acct_no=cc.acct_no " +
                "and bb.bill_id_no = cc.bill_id_no and bb.sub_no = cc.sub_no and bb.history_ptid = aa.ptid " +
                "and datediff(dd,cc.pmt_due_dt,aa.posting_dt_tm)>0 order by aa.posting_dt_tm desc";

        ApiLogger.getLogger().debug("lastLoanRepaidTimely \n\r\t " + query);
        try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                int noOfLateDays = rs.getInt("noOfLateDays");
                if (noOfLateDays > 0) {
                    lastLoanRepaidTimely = false;
                    break;
                }
            }
        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        return lastLoanRepaidTimely;
    }


    public ArrayList<BLScoreParameter> getTxnForPeriod(BLAccount blAccount, int noOfMonths) {
        String query = "select distinct sd.Txmonth,Txyear,sd.diff,sd.acct_no,noTxn " +
                "from " +
                "(select (select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control) today, " +
                "create_dt,month(create_dt) Txmonth,year(create_dt) Txyear," +
                "datediff(MM,create_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) diff" +
                ",acct_no from  " + XapiCodes.coreschema + "..dp_history dh where acct_no ='" + blAccount.getAccountNumber() + "' " +
                "and tran_code in (" + XapiPool.dpTranCode + ") and datediff(MM,create_dt,(select dateadd(dd,1,last_to_dt) " +
                "from " + XapiCodes.coreschema + "..ov_control)) between 1 and " + noOfMonths + ") sd " +
                "left join " +
                "(select count(*) noTxn,month(create_dt) Txmonth,acct_no from " + XapiCodes.coreschema + "..dp_history " +
                "where acct_no ='" + blAccount.getAccountNumber() + "' and tran_code in (" + XapiPool.dpTranCode + ") " +
                "and datediff(MM,create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) " +
                "between 1 and " + noOfMonths + " group by month(create_dt),acct_no) sf on sd.acct_no = sf.acct_no " +
                "where sd.Txmonth = sf.Txmonth order by diff";

        ArrayList<BLScoreParameter> blScoreParameters = new ArrayList<>();
        ApiLogger.getLogger().debug("check if there is voluntary deposits for the last 3 months \n\r\t " + query);

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                BLScoreParameter blScoreParameter = new BLScoreParameter();
                blScoreParameter.setTxnMonth(rs.getLong("Txmonth"));
                blScoreParameter.setAccountNumber(rs.getString("acct_no"));
                blScoreParameter.setTxnYear(rs.getLong("Txyear"));
                blScoreParameter.setNoOfTxn(rs.getLong("noTxn"));
                blScoreParameter.setMntCounter(rs.getLong("diff"));
                blScoreParameters.add(blScoreParameter);
            }

        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }

        return blScoreParameters;
    }

    public int getRoundedNumber(BigDecimal value) {
        int roundedNo = 0;
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select round(" + value + ", -3) as num")) {
            if (rs.next()) {
                roundedNo = rs.getInt("num");
            }

        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }

        return roundedNo;
    }

//    public BigDecimal checkRepaymentScore(Long rimNo) {
//
//        BigDecimal score = BigDecimal.ZERO;
//        boolean repaidCriteria1 = queryRepaymentScore(rimNo, 1);
//        System.out.println("criterial 1" + repaidCriteria1);
//        boolean repaidCriteria2 = queryRepaymentScore(rimNo, 2);
//        System.out.println("criterial 2" + repaidCriteria2);
//        boolean repaidCriteria3 = queryRepaymentScore(rimNo, 3);
//        System.out.println("criterial 3" + repaidCriteria3);
//        boolean repaidCriteria4 = queryRepaymentScore(rimNo, 4);
//        System.out.println("criterial 4" + repaidCriteria4);
//
//        System.out.println("criterial 1" + repaidCriteria1);
//        if (repaidCriteria1 && !repaidCriteria2 && !repaidCriteria3 && !repaidCriteria4) {
//            score = XapiPool.repmt14DaysScore; //replace with variables
//        } else if (!repaidCriteria1 && repaidCriteria2 && !repaidCriteria3 && !repaidCriteria4) {
//            score = XapiPool.repmt25DaysScore;
//        }
//        if (!repaidCriteria1 && !repaidCriteria2 && repaidCriteria3 && !repaidCriteria4) {
//            score = XapiPool.repmt30DaysScore;
//        }
//        if (!repaidCriteria1 && !repaidCriteria2 && !repaidCriteria3 && repaidCriteria4) {
//            score = XapiPool.repmt3DaysLateDaysScore; //+ must be freezed for the period = late days
//        }
//        System.out.println("score repayment score " + score);
//        return score;
//    }

//    public boolean queryRepaymentScore(Long rimNo, int repayedCriteria1) {
//        String criteria = "";
//        if (repayedCriteria1 == 1) {
//            criteria = " and datediff(dd,bb.effective_dt,cc.mat_dt) between 0 and 14 ";
//        } else if (repayedCriteria1 == 2) {
//            criteria = " and datediff(dd,bb.effective_dt,cc.mat_dt) between 15 and 24 ";
//        } else if (repayedCriteria1 == 3) {
//            criteria = " and datediff(dd,bb.effective_dt,cc.mat_dt) between 15 " +
//                    "and (select datediff(dd, create_dt,mat_dt) from " + XapiCodes.coreschema + "..ln_display " +
//                    "where acct_no = bb.acct_no ) ";
//        } else if (repayedCriteria1 == 4) {
//            criteria = "and datediff(dd,bb.effective_dt,cc.mat_dt) " +
//                    "between (select datediff(dd, create_dt,mat_dt) from " + XapiCodes.coreschema + "..ln_display " +
//                    "where acct_no = bb.acct_no)  " +
//                    "and (select datediff(dd, create_dt,mat_dt)+3 from " + XapiCodes.coreschema + "..ln_display " +
//                    "where acct_no = bb.acct_no) ";
//        }
//
//        boolean verified = checkIfExists("VerifyRepaymentScore",
//                "select * from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc " +
//                        "where bb.acct_no=cc.acct_no " +
//                        "and cc.acct_no = (select max(acct_no) from " + XapiCodes.coreschema + "..ln_acct bb " +
//                        "where rim_no = " + rimNo + " and status='Closed' " +
//                        "and bb.class_code in  (" + XapiPool.borrowerLoanClassCode + "," + XapiPool.borrowerLoanClassCode + ")) " +
//                        "and tran_code in (345) " + criteria + " ");
//        return verified;
//    }

    public BigDecimal checkRepaymentScore(Long rimNo) {
        /*A loan can only be repaid Once; since we check the score sequentially,
        if any returns true, the remaining will fail*/
        BigDecimal score = BigDecimal.ZERO;

        boolean repaidIn14Days = queryRepaymentScore(rimNo, 1);
        getCrCaller().setCall("repaidIn14Days", repaidIn14Days);

        boolean repaidFrom14Daysto5DaysMat = repaidIn14Days ? false : queryRepaymentScore(rimNo, 2);
        getCrCaller().setCall("repaidFrom14Daysto5DaysMat", repaidFrom14Daysto5DaysMat);

        boolean repaidIn5DaysToMat = repaidFrom14Daysto5DaysMat ? false : queryRepaymentScore(rimNo, 3);
        getCrCaller().setCall("repaidIn5DaysToMat", repaidIn5DaysToMat);

        boolean repaid1to3DaysLate = repaidIn5DaysToMat ? false : queryRepaymentScore(rimNo, 4);
        getCrCaller().setCall("repaid1to3DaysLate", repaid1to3DaysLate);

        if (repaidIn14Days) {
            score = XapiPool.repmt14DaysScore;
        } else if (repaidFrom14Daysto5DaysMat) {
            score = XapiPool.repmt25DaysScore;
        } else if (repaidIn5DaysToMat) {
            score = XapiPool.repmt30DaysScore;
        } else if (repaid1to3DaysLate) {
            score = XapiPool.repmt3DaysLateDaysScore;
        }
        getCrCaller().setCall("CheckRepaymentScore", score);
        return score;
    }

    public boolean queryRepaymentScore(Long rimNo, int repayedCriteria) {
        boolean verified = false;
        String query = "";
        if (repayedCriteria == 1) {
            /*Repaid in 2 weeks or less - i.e. repayment dt - create dt between 0 and 14 days*/
            query = "select bb.effective_dt repay_dt,cc.create_dt,datediff(dd,cc.create_dt,bb.effective_dt) repayDiff, bb.*, cc.* " +
                    "from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc " +
                    "where bb.acct_no=cc.acct_no and cc.acct_no = (select max(acct_no) from " + XapiCodes.coreschema + "..ln_acct bb " +
                    "where rim_no = " + rimNo + " and status='Closed' " +
                    "and bb.class_code in  (" + XapiPool.borrowerLoanClassCode + "," + XapiPool.borrowerLoanClassCode + ")) " +
                    "and tran_code in (345)  and datediff(dd, cc.create_dt, bb.effective_dt) between 0 and 14";
        } else if (repayedCriteria == 2) {
            /*Repaid from 2 weeks to 5 days before maturity i.e. repayment dt - create dt between 15 and ((Mat_DT-5 days)-Create_DT) days*/
            query = "select bb.effective_dt repay_dt,cc.create_dt,cc.mat_dt,datediff(dd,cc.create_dt,bb.effective_dt) repayDiff, " +
                    "dateadd(dd,-5,cc.mat_dt) fiveDaysToMat, datediff(dd, cc.create_dt, dateadd(dd,-5,cc.mat_dt)) repayDiffFiveDaysToMat, " +
                    "bb.*, cc.* from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc " +
                    "where bb.acct_no = cc.acct_no and cc.acct_no = (select max(acct_no) from " + XapiCodes.coreschema + "..ln_acct bb " +
                    "where rim_no = " + rimNo + " and status='Closed' " +
                    "and bb.class_code in (" + XapiPool.borrowerLoanClassCode + "," + XapiPool.borrowerLoanClassCode + ")) " +
                    "and tran_code in (345) " +
                    "and datediff(dd, cc.create_dt, bb.effective_dt) between 15 and (datediff(dd, cc.create_dt, dateadd(dd,-5,cc.mat_dt)))";
        } else if (repayedCriteria == 3) {
            /*Repaid in the 5 days to the maturity i.e. maturity dt - repayment date between 1 and 5*/
            query = "select bb.effective_dt repay_dt,cc.create_dt,cc.mat_dt,datediff(dd,bb.effective_dt, cc.mat_dt) repayDiff, bb.*, cc.* " +
                    "from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc " +
                    "where bb.acct_no = cc.acct_no and cc.acct_no = (select max(acct_no) " +
                    "from " + XapiCodes.coreschema + "..ln_acct bb where rim_no = " + rimNo + " and status='Closed' " +
                    "and bb.class_code in (" + XapiPool.borrowerLoanClassCode + "," + XapiPool.borrowerLoanClassCode + ")) " +
                    "and tran_code in (345)  and datediff(dd, bb.effective_dt, cc.mat_dt) between 1 and 5";
        } else if (repayedCriteria == 4) {
            /*Repaid with 1 to 3 days late i.e. repayment date - maturity date between 1 and 3*/
            /*Also include freeze period of twice the number of late days i.e. reject this loan application*/
            /*( todaysDate - (loanRepayDate + (daysLate*2) ) ) >=1 */
            query = "select bb.effective_dt repay_dt,cc.create_dt,cc.mat_dt,datediff(dd, cc.mat_dt, bb.effective_dt) repayDiff, " +
                    "datediff(dd, cc.mat_dt, bb.effective_dt) lateDays, (datediff(dd, cc.mat_dt, bb.effective_dt)*2) freezePeriodDays, " +
                    "dateadd(dd,(datediff(dd, cc.mat_dt, bb.effective_dt)*2),bb.effective_dt) nextLoanDateAllowed, " +
                    "datediff(dd,(dateadd(dd,(datediff(dd, cc.mat_dt, bb.effective_dt)*2),bb.effective_dt)/*nextLoanDateAllowed*/)," +
                    "(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)/*todaysDate*/) todayMinusNextLoanDateAllowed, " +
                    "(case when " +
                    "datediff(dd,(dateadd(dd,(datediff(dd, cc.mat_dt, bb.effective_dt)*2),bb.effective_dt)/*nextLoanDateAllowed*/)," +
                    "(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)/*todaysDate*/) >=1 " +
                    "then 'Y' " +
                    "else 'N' end) isAllowed, bb.*, cc.* " +
                    "from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc " +
                    "where bb.acct_no = cc.acct_no and cc.acct_no = (select max(acct_no) from " + XapiCodes.coreschema + "..ln_acct bb " +
                    "where rim_no = " + rimNo + " and status='Closed' " +
                    "and bb.class_code in (" + XapiPool.borrowerLoanClassCode + "," + XapiPool.borrowerLoanClassCode + ")) " +
                    "and tran_code in (345) and datediff(dd, cc.mat_dt, bb.effective_dt) between 1 and 3";
        }

        ApiLogger.getLogger().debug("queryRepaymentScore Criteria [" + repayedCriteria + "] \n\r\t " + query);

        try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                if (repayedCriteria == 4) {
                    String lateDays = rs.getString("lateDays"),
                            freezePeriodDays = rs.getString("freezePeriodDays"),
                            nextLoanDateAllowed = rs.getString("nextLoanDateAllowed"),
                            todayMinusNextLoanDateAllowed = rs.getString("todayMinusNextLoanDateAllowed"),
                            isAllowed = rs.getString("lateDays");
                    getCrCaller().setCall("lateDays", lateDays);
                    getCrCaller().setCall("freezePeriodDays", freezePeriodDays);
                    getCrCaller().setCall("nextLoanDateAllowed", nextLoanDateAllowed);
                    getCrCaller().setCall("todayMinusNextLoanDateAllowed", todayMinusNextLoanDateAllowed);
                    getCrCaller().setCall("isAllowed", isAllowed);
                    if ("Y".equalsIgnoreCase(isAllowed)) {
                        /*if late days are too much; not eligible; this should be a setting*/
                        if (Integer.parseInt(lateDays) > 15) {
                            verified = false;
                            getCrCaller().setCall("**Late Days Eligibility", "Paid Too Late. Not Eligible.");
                        } else {
                            verified = true;
                        }
                    } else {
                        getCrCaller().setCall("Frozen", "Freeze Period in Effect");
                    }
                } else {
                    verified = true;
                }
            }
        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        return verified;
    }


//    public BigDecimal checkAveDepositVolumeScore(Long RimNo, int NoOfMonths) {
//        //njinu --- to change to average deposits..
//        BigDecimal volumePercentage = XapiPool.dpAveVolPercentage.divide(BigDecimal.valueOf(100), 3, RoundingMode.DOWN);//parameter
//        System.out.println(">>>>> volumePercentage" + volumePercentage);
//        BigDecimal volumeAverage = BigDecimal.ZERO;
//        BigDecimal finalScore;
//        String query = "select count(*) noTxn ,bb.acct_no,cast(sum(bb.amt) as numeric(16,2)) totalAmt," +
//                "cast(round(sum(bb.amt)/count(*),2) as numeric(16,2)) as average " +
//                "from " + XapiCodes.coreschema + "..dp_display aa, " + XapiCodes.coreschema + "..dp_history bb " +
//                "where aa.acct_no =bb.acct_no and bb.acct_type=aa.acct_type and  rim_no = " + RimNo + " " +
//                "and status='Active' and aa.class_code in (" + XapiPool.allowedDpClass + ") " +
//                "and datediff(mm,bb.create_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) " +
//                "between 0 and " + NoOfMonths + " and bb.tran_code in (" + XapiPool.dpTranCode + ") " +
//                "group by  bb.acct_no";
//
//        ApiLogger.getLogger().debug("checkAveDepositVolumeScore \n\r\t " + query);
//        try (Statement statement = conn.createStatement();
//             ResultSet rs = statement.executeQuery(query)) {
//            if (rs.next()) {
//                volumeAverage = rs.getBigDecimal("average");
//            }
//
//        } catch (Exception e1) {
//            ApiLogger.getLogger().error(e1);
//        }
//        ApiLogger.getLogger().info("Average Volume " + volumeAverage);
//        ApiLogger.getLogger().info("Average Volume " + volumePercentage);
//        finalScore = volumeAverage.divide(volumePercentage).setScale(0, 2);
//        ApiLogger.getLogger().info("Average finalScore  " + finalScore);
//        getCrCaller().getBlScoreCard().setAverageVlmScore(finalScore);
//        return finalScore;
//    }

    public BigDecimal checkAveDepositVolumeScore(Long RimNo, int NoOfMonths) {
        BigDecimal volumePercentage = XapiPool.dpAveVolPercentage.divide(BigDecimal.valueOf(100), 3, RoundingMode.DOWN),
                finalVariableBAmount = BigDecimal.ZERO,
                totalNetDeposits = BigDecimal.ZERO,
                averageNetDeposits = BigDecimal.ZERO;

        HashMap lnAmountsPerMonth = new HashMap(),
                dpAmountsPerMonth = new HashMap();

        /* get expected loan repayments for each month for the last 'NoOfMonths'*/
        String getSumLoanInstalmentsDue = "select sum(lb.amt) sum_amt,datepart(month,pmt_due_dt) txnMonth " +
                "from " + XapiCodes.coreschema + "..ln_bill lb," + XapiCodes.coreschema + "..ln_display ld " +
                "where lb.acct_no = ld.acct_no and rim_no = " + RimNo + " " +
                "and (datediff(mm,pmt_due_dt,(select dateadd(dd,1,last_to_dt) " +
                "from " + XapiCodes.coreschema + "..ov_control)) between 1 and 3) " +
                "and lb.status ='Unsatisfied' and type in ('Interest ','Principal','Late Fees') " +
                "group by datepart(month,pmt_due_dt)";

        /* get total deposits for each month for the last 'NoOfMonths'*/
        String getSumDeposits = "select sum(amt) sum_amt ,datepart(month,posting_dt_tm) txnMonth, count(*) noTxn /*,dh.acct_no*/ " +
                "from " + XapiCodes.coreschema + "..dp_history dh, " + XapiCodes.coreschema + "..dp_display di " +
                "where dh.acct_no = di.acct_no and rim_no = " + RimNo + " " +
                "and di.class_code in (" + XapiPool.allowedDpClass + ") and dh.tran_code in (" + XapiPool.dpTranCode + ") " +
                "and (datediff(mm,posting_dt_tm,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) between 1 and 3) " +
                "group by datepart(month,posting_dt_tm)";

        ApiLogger.getLogger().debug("checkAveDepositVolumeScore-getSumLoanInstalmentsDue \n\r\t " + getSumLoanInstalmentsDue);
        ApiLogger.getLogger().debug("checkAveDepositVolumeScore-getSumDeposits \n\r\t " + getSumDeposits);

        try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(getSumLoanInstalmentsDue)) {
            while (rs.next()) {
                lnAmountsPerMonth.put(rs.getInt("txnMonth"), rs.getBigDecimal("sum_amt"));
            }
        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }

        getCrCaller().setCall("lnAmountsPerMonth", lnAmountsPerMonth.toString());

        try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(getSumDeposits)) {
            while (rs.next()) {
                int month = rs.getInt("txnMonth");
                BigDecimal sumAmtForMonth = rs.getBigDecimal("sum_amt");
                getCrCaller().setCall("dpAmountsPerMonth", "Month: " + month + " SumAmtForMonth: " + sumAmtForMonth + "");
                if (lnAmountsPerMonth.containsKey(month)) {
                    BigDecimal lnAmtForMonth = new BigDecimal(lnAmountsPerMonth.get(month).toString());
                    getCrCaller().setCall("lnAmountsPerMonth", "Month: " + month + " has a loan due; " + lnAmtForMonth + "; Deduct");
                    BigDecimal netDeposit = sumAmtForMonth.subtract(lnAmtForMonth);
                    dpAmountsPerMonth.put(month, netDeposit);
                    totalNetDeposits = totalNetDeposits.add(netDeposit);
                } else {
                    dpAmountsPerMonth.put(month, sumAmtForMonth);
                    totalNetDeposits = totalNetDeposits.add(sumAmtForMonth);
                }
            }
        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }

        getCrCaller().setCall("dpAmountsPerMonth", dpAmountsPerMonth.toString());
        getCrCaller().setCall("totalNetDeposits", totalNetDeposits.toString());
        averageNetDeposits = totalNetDeposits.divide(new BigDecimal(NoOfMonths), 2, RoundingMode.HALF_UP).setScale(2, 2);
        getCrCaller().setCall("averageNetDeposits", averageNetDeposits);
        getCrCaller().setCall("volumePercentage", volumePercentage);

        finalVariableBAmount = averageNetDeposits.multiply(volumePercentage).setScale(2, 2);
        getCrCaller().setCall("finalVariableBAmount", finalVariableBAmount);
        getCrCaller().getBlScoreCard().setAverageVlmScore(finalVariableBAmount);
        return finalVariableBAmount;
    }

//    public BigDecimal checkMinAverageVolumeScore(Long RimNo, int NoOfMonths) {
//        //njinu --- to change to average deposits..
//        BigDecimal volumePercentage = XapiPool.dpAveVolPercentage.divide(BigDecimal.valueOf(100), 3, RoundingMode.DOWN);//parameter
//        System.out.println(">>>>> volumePercentage" + volumePercentage);
//        BigDecimal volumeAverage = BigDecimal.ZERO;
//        BigDecimal finalScore;
//        ApiLogger.getLogger().debug("checkMinAverageVolume  \n\r\t select cast(min(cur_bal) as numeric(26,2)) average from (select month(cs.create_dt) create_dt,min(cs.cur_bal) cur_bal ,cs.acct_no  " +
//                " from " + XapiCodes.coreschema + "..csa_avg_daily_bal cs," + XapiCodes.coreschema + "..dp_display dp where cs.acct_no = dp.acct_no   and cs.acct_type=dp.acct_type " +
//                " and  rim_no = " + RimNo + "  and datediff(mm,cs.create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) between 0 and " + NoOfMonths + " " +
//                " group by cs.acct_no,month(cs.create_dt)) av");
//
//        try (Statement statement = conn.createStatement();
//             ResultSet rs = statement.executeQuery("select cast(min(cur_bal) as numeric(26,2)) average  from (select month(cs.create_dt) create_dt,min(cs.cur_bal) cur_bal ,cs.acct_no  " +
//                     " from " + XapiCodes.coreschema + "..csa_avg_daily_bal cs," + XapiCodes.coreschema + "..dp_display dp where cs.acct_no = dp.acct_no   and cs.acct_type=dp.acct_type " +
//                     " and  rim_no = " + RimNo + "  and datediff(mm,cs.create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) between 0 and " + NoOfMonths + " " +
//                     " group by cs.acct_no,month(cs.create_dt)) av")) {
//            if (rs.next()) {
//                volumeAverage = rs.getBigDecimal("average");
//            }
//
//        } catch (Exception e1) {
//            ApiLogger.getLogger().error(e1);
//        }
//        ApiLogger.getLogger().info("Average Volume " + volumeAverage);
//        ApiLogger.getLogger().info("Average Volume " + volumePercentage);
//        finalScore = volumeAverage.divide(volumePercentage).setScale(0, 2);
//        ApiLogger.getLogger().info("Average finalScore  " + finalScore);
//        getCrCaller().getBlScoreCard().setAverageVlmScore(finalScore);
//        return finalScore;
//    }

//    public BigDecimal checkMinAverageVolumeScore(Long RimNo, int NoOfMonths) {
//        BigDecimal minimumAverageBalance = BigDecimal.ZERO;
//        /* get the minimum of the monthly average balance over the 3 previous months - returns sorted results, pick first row*/
//        String query = "select datepart(month,cs.create_dt)txnMonth, count(*) numTxns, sum(cs.cur_bal) totalBal, " +
//                "(sum(cs.cur_bal)/count(*)) averageBal from " + XapiCodes.coreschema + "..csa_avg_daily_bal cs," +
//                "" + XapiCodes.coreschema + "..dp_display dp where cs.acct_no = dp.acct_no " +
//                "and cs.acct_type = dp.acct_type and rim_no = " + RimNo + " " +
//                "and (datediff(mm, cs.create_dt, (select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) " +
//                "between 1 and " + NoOfMonths + ") group by datepart(month,cs.create_dt) order by 4 asc";
//
//        ApiLogger.getLogger().debug("checkMinAverageVolume (monthly average balance) \n\r\t " + query);
//
//        try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(query)) {
//            if (rs.next()) {
//                minimumAverageBalance = rs.getBigDecimal("averageBal");
//            }
//        } catch (Exception e1) {
//            ApiLogger.getLogger().error(e1);
//        }
//        ApiLogger.getLogger().info("minimumAverageBalance " + minimumAverageBalance);
//        return minimumAverageBalance;
//    }

    public BigDecimal checkMinAverageVolumeScore(Long RimNo, int NoOfMonths) {
        BigDecimal minimumAverageBalance = BigDecimal.ZERO;
        /* get the minimum of the monthly average balance over the 3 previous months*/
        /* 01/10/2021-changed to get average balance over the last ~90 days
        as the table csa_avg_daily_bal only maintains last 90 day records per RIM */
        String query = "select count(*) numTxns, sum(cs.cur_bal) totalBal, (sum(cs.cur_bal)/count(*)) averageBal from " +
                "" + XapiCodes.coreschema + "..csa_avg_daily_bal cs, " + XapiCodes.coreschema + "..dp_display dp " +
                "where cs.acct_no = dp.acct_no and cs.acct_type = dp.acct_type and rim_no = "+RimNo;

        ApiLogger.getLogger().debug("checkMinAverageVolume (monthly average balance) \n\r\t " + query);

        try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                minimumAverageBalance = rs.getBigDecimal("averageBal");
            }
        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }
        ApiLogger.getLogger().info("AverageBalance " + minimumAverageBalance);
        return minimumAverageBalance;
    }

    public BigDecimal checkDepositPerPeriodScore(Long rimNo) {
        System.out.println("<<<<< checking deposits per period >>>>>");
        HashMap<Integer, Boolean> periodMap = new HashMap<>();
        BigDecimal finalScore;
        int maxPeriod = XapiPool.dpMaxTxnPeriod.intValue();
        int midPeriod = XapiPool.dpMidTxnPeriod.intValue();
        int minPeriod = XapiPool.dpMinTxnPeriod.intValue();


        BigDecimal score3 = BigDecimal.ZERO;
        BigDecimal score6 = BigDecimal.ZERO;
        BigDecimal score9 = BigDecimal.ZERO;

        for (int i = 0; i <= maxPeriod; i++) {
            ApiLogger.getLogger().info("Evaluate for Period: Last " + i + " Months");
            periodMap.put(i, verifyDepositsPerPeriod(i, rimNo));
            i += 2;
        }
        for (Map.Entry<Integer, Boolean> entry : periodMap.entrySet()) {
            Integer monthPeriod = entry.getKey();
            boolean depositsVerifiedEachMonth = entry.getValue();
            ApiLogger.getLogger().info(rimNo + "-Month Period:  " + monthPeriod + " - Deposits Verified for Period: " + depositsVerifiedEachMonth);

            //change to reflect value on settings...
            if ((monthPeriod == minPeriod) && depositsVerifiedEachMonth) {
                score3 = XapiPool.dp3monthScore;
                ApiLogger.getLogger().info(rimNo + "-Month Period: " + monthPeriod + " Score: " + score3);
            } else if ((monthPeriod == midPeriod) && depositsVerifiedEachMonth) {
                score6 = XapiPool.dp6monthScore;
                ApiLogger.getLogger().info(rimNo + "-Month Period: " + monthPeriod + " Score: " + score6);
            } else if ((monthPeriod == maxPeriod) && depositsVerifiedEachMonth) {
                score9 = XapiPool.dp9monthScore;
                ApiLogger.getLogger().info(rimNo + "-Month Period: " + monthPeriod + " Score: " + score9);
            } else {
                score3 = BigDecimal.ZERO;
                ApiLogger.getLogger().info(rimNo + "-Month Period: " + monthPeriod + " Score: " + score3);
            }

        }

        //check if the customer has deposit for at least 3 months
        if (score3.compareTo(BigDecimal.ZERO) <= 0) {
            finalScore = BigDecimal.ZERO;
        } else {
            finalScore = score3.max(score6.max(score9));
        }
        ApiLogger.getLogger().info(rimNo + "-FinalScore Depositor: " + finalScore);
        getCrCaller().getBlScoreCard().setDepositorScore(finalScore);
        return finalScore;
    }

    public boolean verifyDepositsPerPeriod(int period, Long rimNo) {
        boolean isVerified = true;
        HashMap<Integer, Long> txnCountMap = new HashMap<>();
//        String query = "select count(*) noTxn, month(aa.create_dt) Txmonth " +
//                "from " + XapiCodes.coreschema + "..dp_history aa, " + XapiCodes.coreschema + "..dp_display bb " +
//                "where aa.acct_no = bb.acct_no  and bb.acct_type=aa.acct_type  and bb.rim_no =" + rimNo + " " +
//                "and aa.tran_code in (" + XapiPool.dpTranCode + ") and datediff(MM,aa.create_dt, " +
//                "(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) " +
//                "between 0 and " + period + " group by aa.create_dt";

        String query = "select count(*) noTxn, month(aa.create_dt) Txmonth " +
                "from " + XapiCodes.coreschema + "..dp_history aa, " + XapiCodes.coreschema + "..dp_display bb " +
                "where aa.acct_no = bb.acct_no  and bb.acct_type=aa.acct_type  and bb.rim_no =" + rimNo + " " +
                "and aa.tran_code in (" + XapiPool.dpTranCode + ") and datediff(MM,aa.create_dt, " +
                "(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) " +
                "between 1 and " + period + " group by month(aa.create_dt)";

        ApiLogger.getLogger().info(rimNo + "-verifyDepositsPerPeriod\n\r\t " + query);
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
//            if (rs.next()) {
//                txnCountMap.put(rs.getInt("Txmonth"), rs.getLong("noTxn"));
//                isVerified = true;
//            }
            while (rs.next()) {
                txnCountMap.put(rs.getInt("Txmonth"), rs.getLong("noTxn"));
            }

        } catch (Exception e1) {
            ApiLogger.getLogger().error(e1);
        }

        for (Map.Entry<Integer, Long> entry : txnCountMap.entrySet()) {
            Integer monthPeriod = entry.getKey();
            Long noOfTxns = entry.getValue();
            ApiLogger.getLogger().info(rimNo + "-TxnCountMap - Month Period: " + monthPeriod + " No of Txn for Period: " + noOfTxns);

            if (noOfTxns == 0 && period != 0) {
                isVerified = false;
            }
        }

        return isVerified;
    }

    private boolean checkIfExists(String queryType, String query) {
        boolean exists = false;
        ApiLogger.getLogger().debug(queryType + "\n\t" + query + "\n");
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            exists = rs.next();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.err.println("query+exists " + exists);
        return exists;
    }

    public StringBuilder getBuilder(boolean reset) {
        if (reset)
            builder.setLength(0);
        return builder;
    }

    public BLScoreCard getBlScoreCard() {
        return blScoreCard;
    }

    public void setBlScoreCard(BLScoreCard blScoreCard) {
        this.blScoreCard = blScoreCard;
    }

    public CRCaller getCrCaller() {
        return crCaller;
    }

    public void setCrCaller(CRCaller crCaller) {
        this.crCaller = crCaller;
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

    public StringBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(StringBuilder builder) {
        this.builder = builder;
    }

}
