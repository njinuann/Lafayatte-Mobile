package org.redlamp.extras;

import org.redlamp.interfaces.ISO;
import org.redlamp.interfaces.SQL;
import org.redlamp.logger.ApiLogger;
import org.redlamp.model.*;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoringHandler implements AutoCloseable, ISO, SQL
{

    private Connection conn;
    private StringBuilder builder;
    private BigDecimal HUNDREAD = BigDecimal.valueOf(100);
    private BLScoreCard blScoreCard = new BLScoreCard();
    private CRCaller crCaller = new CRCaller();
    private long endTime;
    private long startTime;


    public ScoringHandler()
    {
        try
        {
            setBuilder(new StringBuilder());
            conn = XapiPool.getConnection();
            setCrCaller(new CRCaller());
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

    public static boolean isBlank(Object object)
    {
        return object == null || "".equals(String.valueOf(object).trim()) || "null".equals(String.valueOf(object).trim()) || String.valueOf(object).trim().toLowerCase().contains("---select");
    }

    public Map<String, Object> processScore(LoanRequest request)
    {
        setBlScoreCard(getCrCaller().getBlScoreCard());
        BigDecimal finalScore1 = BigDecimal.ZERO;
        BigDecimal finalScore2 = BigDecimal.ZERO;
        BigDecimal minScore = BigDecimal.ZERO;
        BigDecimal approvedScore = BigDecimal.ZERO;


        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        BigDecimal cycleScore;

        BLAccount blAccount = queryRegistered(request.getAccountNo(), request.getPhoneNumber()); //check if the customer is eligible based on rim classcode,deposit class code and no previous active loan
        System.out.println("getting account>>>>>>>>>>>>>>>>>>>>>>>>> " + blAccount.getAccountNumber());
        getCrCaller().setCall("blAccount", blAccount);
        if (!isBlank(blAccount.getAccountNumber()))
        {
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
            boolean hadPreviousLoan = checkCycle(blAccount);
            getCrCaller().setCall("hadPreviousLoan", hadPreviousLoan);

            cycleScore = hadPreviousLoan ? XapiPool.dpCycle1Score : XapiPool.dpCycle2Score;


            boolean isClassCodeAllowed = checkEligibleClasses(blAccount);
            getCrCaller().setCall("isClassCodeAllowed", isClassCodeAllowed);


            if (hadPreviousLoan)
            {
                lastLoanRepaidTimely = lastLoanRepaidTimely(blAccount.getRimNo()); //check if there was timely loan repayment in the last loan
            }
            else
            {
                lastLoanRepaidTimely = true;
            }
            getCrCaller().setCall("lastLoanRepaidTimely", lastLoanRepaidTimely);

            for (BLScoreParameter blScoreParameter : getTxnForPeriod(blAccount, XapiPool.dpAveVolumeMonths.intValue())) //check if there is voluntary deposits for the last 3 months
            {
                System.out.println(blScoreParameter.getNoOfTxn() + "No of deposit for month " + blScoreParameter.getTxnMonth());
                if (blScoreParameter.getNoOfTxn() == 0) //evaluate
                {
                    hasVoluntaryDeposit = false; //change to report false just in case it gets 0 for any month
                    break;
                }
            }
            getCrCaller().setCall("hasVoluntaryDeposit", hasVoluntaryDeposit);

            getBlScoreCard().getBlMainScoreCard().setCycleScore(cycleScore);
            getBlScoreCard().setClassCodesAllowed(isClassCodeAllowed);
            getBlScoreCard().setVoluntaryDepositor(hasVoluntaryDeposit);
            getBlScoreCard().setPrevLoanRepaidTimely(lastLoanRepaidTimely);
            getCrCaller().setCall("isClassCodeAllowed", isClassCodeAllowed);
            getCrCaller().setCall("hasVoluntaryDeposit", hasVoluntaryDeposit);
            getCrCaller().setCall("lastLoanRepaidTimely", lastLoanRepaidTimely);


            System.out.println("isClassCodeAllowed " + isClassCodeAllowed);
            System.out.println("lastLoanRepaidTimely " + lastLoanRepaidTimely);
            System.out.println("hasVoluntaryDeposit " + hasVoluntaryDeposit);
            //check if the customer is allowed
            if (isClassCodeAllowed && lastLoanRepaidTimely && hasVoluntaryDeposit)
            {
                // approach 1
                //start Variable A - rewarding Depositors
                BigDecimal depositorScore = checkDepositPerPeriodScore(blAccount.getRimNo());
                getBlScoreCard().setDepositorScore(depositorScore);
                System.out.println(">>>depositorScore >>> " + depositorScore);
                getCrCaller().setCall("Variable A - depositorScore ", depositorScore);

                //start Variable B - Get the average volume of net deposits
                BigDecimal averageDepositVlmScore = checkAveDepositVolumeScore(blAccount.getRimNo(), XapiPool.dpAveVolumeMonths.intValue()); //check for 3 months
                getBlScoreCard().setAverageVlmScore(averageDepositVlmScore);
                getCrCaller().setCall("Variable B - averageVlmScore ", averageDepositVlmScore);

                //start Variable c - check Repayment behaviour
                BigDecimal RepaymentScore = hadPreviousLoan ? checkRepaymentScore(blAccount.getRimNo()) : BigDecimal.ONE;
                getBlScoreCard().setRepaymentScore(RepaymentScore);
                getCrCaller().setCall("Variable C - RepaymentScore ", RepaymentScore);
                // END OF APPROACH 1

                // APPROACH 2
                BigDecimal minAverageBalance = checkMinAverageVolumeScore(blAccount.getRimNo(), XapiPool.dpAveVolumeMonths.intValue());
                BigDecimal minAverageBalanceScored = (minAverageBalance.compareTo(XapiPool.minCycleDpEvalAmount) > 0)
                        ? minAverageBalance.multiply(cycleScore) : BigDecimal.ZERO; // scenario 2 scoring
                // END OF APPROACH 2

                getCrCaller().setCall("Variable C - RepaymentScore ", RepaymentScore);


                System.out.println("depositorScore " + depositorScore);
                System.out.println("averageVlmScore " + averageDepositVlmScore);
                System.out.println("RepaymentScore " + RepaymentScore);

                finalScore1 = depositorScore.multiply(averageDepositVlmScore).multiply(RepaymentScore);  // APPROACH 1 Scored value
                getBlScoreCard().setFinalScore1(finalScore1);
                getCrCaller().setCall("First finalScore Check(avSc*rpmntSc ", finalScore1);

                System.out.println("finalScore1 >>>> " + finalScore1);
                finalScore2 = minAverageBalanceScored;  // APPROACH 2 Scored value
                getBlScoreCard().setFinalScoreAvg(finalScore2);
                getCrCaller().setCall("Second finalScore Check (max(cycleScore on averageVlmScore) ", minAverageBalanceScored);

                System.out.println("finalScore2 >>>> " + minAverageBalanceScored);

                minScore = finalScore1.min(minAverageBalanceScored).min(XapiPool.maxDepositorLnAmount); // get the minimum of both approaches
                getBlScoreCard().setOverallScore(minScore);

                getCrCaller().setCall("last finalScore(OverallScore) Check (min of score- finalScore1.min(finalScore2).min(XapiPool.maxDepositorLnAmount)) ", minScore);
                System.out.println(" Min ln amt " + XapiPool.minDepositorLnAmount + " Max ln amt " + XapiPool.maxDepositorLnAmount + " minScore >>>> " + minScore);
                System.out.println("minScore1 >>>> " + minScore.compareTo(XapiPool.maxDepositorLnAmount));
                System.out.println("minScore2 >>>> " + minScore.compareTo(XapiPool.minDepositorLnAmount));

                if (minScore.compareTo(XapiPool.maxDepositorLnAmount) > 0)
                {
                    approvedScore = XapiPool.maxDepositorLnAmount;
                }
                else if (minScore.compareTo(XapiPool.minDepositorLnAmount) < 0)
                {
                    approvedScore = BigDecimal.ZERO;
                }
                else
                {
                    approvedScore = minScore;
                }
                getCrCaller().setCall(" approvedScore", approvedScore);
                System.out.println("approvedScore >>>> " + approvedScore);

            }
            else
            {
                approvedScore = BigDecimal.ZERO;
            }
            System.out.println("score card " + approvedScore);
            System.out.println("score card diff " + BigDecimal.ZERO.compareTo(minScore));

            if (BigDecimal.ZERO.compareTo(approvedScore) >= 0)
            {
                list.add(map);
                response.put("responseCode", CUST_NOT_ELIGIBLE);
            }
            else
            {
                getBlScoreCard().setAccountNumber(blAccount.getAccountNumber());
                getBlScoreCard().setAmount(approvedScore);

                map.put("Eligible_Amount", blScoreCard.getAmount());
                map.put("Account", blScoreCard.getAccountNumber());
                //  map.put("Period", XapiPool.defaultLoanPeriod);
                //   map.put("Term", XapiPool.minLoanterm);
                list.add(map);
                response.put("responseCode", XAPI_APPROVED);
            }

        }
        else
        {
            response.put("responseCode", CUST_NOT_ELIGIBLE);

        }
        if (CUST_NOT_ELIGIBLE.equals(response.get("responseCode")))
        {
            map.put("Eligible_Amount", BigDecimal.ZERO);
            map.put("Account", blScoreCard.getAccountNumber());
            list.add(map);
            response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
        }
        else
        {
            response.put("responseTxt", "Customer is Eligible");
        }
        System.out.println("Fina response " + response.get("responseCode"));
        response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
        response.put("Eligibility_Check", list);
        getCrCaller().setCall("response", response);
        getCrCaller().setCall("scoringitems", getBlScoreCard());
        getCrCaller().setXapiRespCode(String.valueOf(response.get("responseCode")));
        windUp();

        return response;
    }

    public BigDecimal roundedAmount(int value)
    {
        return new BigDecimal((value / 1000) * 1000);
    }

    public void windUp()
    {
        writeToLog();

    }

    private void writeToLog()
    {
        //ApiLogger.getLogger().error(e1);
        setEndTime(System.currentTimeMillis());
        getCrCaller().setDuration(String.valueOf(getEndTime() - getStartTime()) + " Ms");


        logLoanScore(getCrCaller());
        new Thread(new ThreadLogger(new ApiLogger(), "<transaction>" + "\r\n\t" + getCrCaller() + "</transaction>")).start();

        // APMain.cqsLog.logEvent(gettXCaller());
        setCrCaller(new CRCaller());
    }

    private boolean logLoanScore(CRCaller crCaller)
    {
        ApiLogger.getLogger().info("INSERT INTO " + XapiCodes.xapiSchema + "..E_LOAN_SCORE  (ACCOUNT_NO, ACCT_TYPE, RIM_NO, PHONE_NUMBER, SCORE_TYPE, AMOUNT, CYCLE_SCORE, DEPOSITOR_SCORE, AVERAGE_VOLUME_SCORE, REPAYMENT_SCORE, FINAL_SCORE, " +
                "HISTORY_SCORE, APPROVED_SCORE, LATE_INSTALMENT_SCORE, BORROWER, CLASS_CODE_ALLOWED, PREV_TIMELY_REPAYMENT, VOLUNTARY_DESPOSITS, VALID_CYCLE, REQ_MIN_INSTALMENTS, LOAN_RESTRUCTRED, " +
                "CLOSURE_MONTH_VALID, PREV_DEFAULTED_LOAN7DAYS, PREV_DEFAULTED_LOAN30DAYS, CURRENT_DELAYED_PAYMENT, MAX_LOAN_PER_YEAR, DELAYED_CURRENT_INSTALEMENT, RESPONSE_CODE, RESPONSE_MESSAGE, CREATE_DT )" +
                "VALUES('" + crCaller.getBlScoreCard().getAccountNumber() + "'," + crCaller.getBlScoreCard().getRimNumber() + ",'" + crCaller.getBlScoreCard().getAccountType() + "','" + crCaller.getBlScoreCard().getPhoneNumber() + "','" + crCaller.getBlScoreCard().getLoanScoreTpe() + "'," + crCaller.getBlScoreCard().getAmount() + "," + crCaller.getBlScoreCard().getCycleScore() + "," + crCaller.getBlScoreCard().getDepositorScore() + "," + crCaller.getBlScoreCard().getAverageVlmScore() + "," + crCaller.getBlScoreCard().getRepaymentScore() + "," + crCaller.getBlScoreCard().getFinalScore1() + ", " +
                "" + crCaller.getBlScoreCard().getHistoryScore() + "," + crCaller.getBlScoreCard().getApprovedScore() + "," + crCaller.getBlScoreCard().getLateInstalmentScore() + ",'" + yesNo(crCaller.getBlScoreCard().isBorrower()) + "','" + yesNo(crCaller.getBlScoreCard().isClassCodesAllowed()) + "','" + yesNo(crCaller.getBlScoreCard().isPrevLoanRepaidTimely()) + "','" + yesNo(crCaller.getBlScoreCard().isVoluntaryDepositor()) + "','" + yesNo(crCaller.getBlScoreCard().isHasValidCycle()) + "','" + yesNo(crCaller.getBlScoreCard().isHasRequiredMinInstalments()) + "', " +
                "'" + yesNo(crCaller.getBlScoreCard().isRestructured()) + "','" + yesNo(crCaller.getBlScoreCard().isHasClosureMonths()) + "','" + yesNo(crCaller.getBlScoreCard().isHasPreviousLoanDefaulted7days()) + "','" + yesNo(crCaller.getBlScoreCard().isHasPreviousLoanDefaulted30days()) + "','" + yesNo(crCaller.getBlScoreCard().isHasCurrentDelayedPayment()) + "','" + yesNo(crCaller.getBlScoreCard().isHasMaxLoansPerYear()) + "','" + yesNo(crCaller.getBlScoreCard().isHasDelayedCurrentInstallment()) + "','" + crCaller.getXapiRespCode() + "','" + XapiCodes.getErrorDesc(crCaller.getXapiRespCode()) + "',getdate())");
        try (Statement statement = conn.createStatement())
        {
            statement.executeUpdate("INSERT INTO " + XapiCodes.xapiSchema + "..E_LOAN_SCORE  (ACCOUNT_NO, ACCT_TYPE, RIM_NO, PHONE_NUMBER, SCORE_TYPE, AMOUNT, CYCLE_SCORE, DEPOSITOR_SCORE, AVERAGE_VOLUME_SCORE, REPAYMENT_SCORE, FINAL_SCORE, " +
                    "HISTORY_SCORE, APPROVED_SCORE, LATE_INSTALMENT_SCORE, BORROWER, CLASS_CODE_ALLOWED, PREV_TIMELY_REPAYMENT, VOLUNTARY_DESPOSITS, VALID_CYCLE, REQ_MIN_INSTALMENTS, LOAN_RESTRUCTRED, " +
                    "CLOSURE_MONTH_VALID, PREV_DEFAULTED_LOAN7DAYS, PREV_DEFAULTED_LOAN30DAYS, CURRENT_DELAYED_PAYMENT, MAX_LOAN_PER_YEAR, DELAYED_CURRENT_INSTALEMENT, RESPONSE_CODE, RESPONSE_MESSAGE, CREATE_DT )" +
                    "VALUES('" + crCaller.getBlScoreCard().getAccountNumber() + "','" + crCaller.getBlScoreCard().getAccountType() + "'," + crCaller.getBlScoreCard().getRimNumber() + ",'" + crCaller.getBlScoreCard().getPhoneNumber() + "','" + crCaller.getBlScoreCard().getLoanScoreTpe() + "'," + crCaller.getBlScoreCard().getAmount() + "," + crCaller.getBlScoreCard().getCycleScore() + "," + crCaller.getBlScoreCard().getDepositorScore() + "," + crCaller.getBlScoreCard().getAverageVlmScore() + "," + crCaller.getBlScoreCard().getRepaymentScore() + "," + crCaller.getBlScoreCard().getFinalScore1() + ", " +
                    "" + crCaller.getBlScoreCard().getHistoryScore() + "," + crCaller.getBlScoreCard().getApprovedScore() + "," + crCaller.getBlScoreCard().getLateInstalmentScore() + ",'" + yesNo(crCaller.getBlScoreCard().isBorrower()) + "','" + yesNo(crCaller.getBlScoreCard().isClassCodesAllowed()) + "','" + yesNo(crCaller.getBlScoreCard().isPrevLoanRepaidTimely()) + "','" + yesNo(crCaller.getBlScoreCard().isVoluntaryDepositor()) + "','" + yesNo(crCaller.getBlScoreCard().isHasValidCycle()) + "','" + yesNo(crCaller.getBlScoreCard().isHasRequiredMinInstalments()) + "', " +
                    "'" + yesNo(crCaller.getBlScoreCard().isRestructured()) + "','" + yesNo(crCaller.getBlScoreCard().isHasClosureMonths()) + "','" + yesNo(crCaller.getBlScoreCard().isHasPreviousLoanDefaulted7days()) + "','" + yesNo(crCaller.getBlScoreCard().isHasPreviousLoanDefaulted30days()) + "','" + yesNo(crCaller.getBlScoreCard().isHasCurrentDelayedPayment()) + "','" + yesNo(crCaller.getBlScoreCard().isHasMaxLoansPerYear()) + "','" + yesNo(crCaller.getBlScoreCard().isHasDelayedCurrentInstallment()) + "','" + crCaller.getXapiRespCode() + "','" + XapiCodes.getErrorDesc(crCaller.getXapiRespCode()) + "',getdate())");
            return true;
        } catch (Exception ex)
        {
            ApiLogger.getLogger().error(ex);
            return false;
        }

    }

    public String yesNo(boolean checkVal)
    {
        return checkVal ? "Y" : "N";
    }

    public BLAccount queryRegistered(String acctNo, String phoneNo)
    {

        BLAccount bLAccount = new BLAccount();

        ApiLogger.getLogger().debug("queryRegistered \n\r\t select a.value bvn, b.acct_no,b.acct_type, b.rim_no,c.cust_service_key as phone_number "
                + "from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                + "where a.field_id=45 and   (a.acct_no_key = '" + acctNo.substring(0, 3) + "-" + acctNo.substring(3) + "' or value = '" + acctNo.replace("-", "") + "') "
                + "and b.acct_no = a.acct_no_key and c.rim_no in (select rim_no from " + XapiCodes.coreschema + "..rm_acct where class_code in (" + XapiPool.allowedRimClassDepositor + ")) and c.rim_no = b.rim_no and c.cust_service_key = '" + phoneNo + "' and c.services_id =44 ");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select a.value bvn, b.acct_no,b.acct_type, b.rim_no,c.cust_service_key as phone_number "
                     + "from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                     + "where a.field_id=45 and   (a.acct_no_key = '" + acctNo.substring(0, 3) + "-" + acctNo.substring(3) + "' or value = '" + acctNo.replace("-", "") + "') "
                     + "and b.acct_no = a.acct_no_key and c.rim_no in (select rim_no from " + XapiCodes.coreschema + "..rm_acct where class_code in (" + XapiPool.allowedRimClassDepositor + ")) " +
                     "and c.rim_no = b.rim_no and c.cust_service_key = '" + phoneNo + "' and c.services_id =44 "))
        {

            if (rs.next())
            {
                bLAccount.setAccountType(rs.getString("acct_type"));
                bLAccount.setAccountNumber(rs.getString("acct_no"));
                bLAccount.setRimNo(rs.getLong("rim_no"));
                bLAccount.setNubanAccountNumber(rs.getString("bvn"));
                bLAccount.setPhoneNumber(rs.getString("phone_number"));
            }


        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        return bLAccount;
    }

    public boolean checkCycle(BLAccount bLAccount)
    {
        return checkIfExists("checkCycle", "select acct_no from  " + XapiCodes.coreschema + "..ln_display where class_code not in  (" + XapiPool.borrowerLoanClassCode + "," + XapiPool.borrowerLoanClassCode + ") and rim_no = (select b.rim_no from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services_acct c "
                + "where a.field_id=45 and   (a.acct_no_key = '" + bLAccount.getAccountNumber().substring(0, 3) + "-" + bLAccount.getAccountNumber().substring(3) + "' or value = '" + bLAccount.getAccountNumber().replace("-", "") + "') "
                + "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no and c.cust_service_key = '" + bLAccount.getPhoneNumber() + "' )");
    }

    public boolean checkEligibleClasses(BLAccount bLAccount)
    {
        return checkIfExists("checkEligibleClasses", "select dd.acct_no  from  " + XapiCodes.coreschema + "..dp_display dd,  " + XapiCodes.coreschema + "..rm_acct rm " +
                " where dd.rim_no=rm.rim_no and dd.status='Active' and rm.status='Active' "
                + " and rm.class_code in (" + XapiPool.allowedRimClassDepositor + " ) and dd.class_code in (" + XapiPool.allowedDpClass + ")   and rm.rim_no=" + bLAccount.getRimNo() + " ");
        //and dd.rim_no not in (select rim_no from  " + XapiCodes.coreschema + "..ln_display where  rim_no=dd.rim_no and status ='Active')
    }

    public ArrayList<BLScoreParameter> getTxnForPeriod(BLAccount blAccount, int noOfMonths)
    {
        ArrayList<BLScoreParameter> blScoreParameters = new ArrayList<>();
        ApiLogger.getLogger().debug("check if there is voluntary deposits for the last 3 months \n\r\t select distinct sd.Txmonth,Txyear,sd.diff,sd.acct_no,noTxn  "
                + "from (select (select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control) today,create_dt,month(create_dt) Txmonth,year(create_dt) Txyear,datediff(MM,create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) diff,acct_no "
                + "from  " + XapiCodes.coreschema + "..dp_history dh where acct_no ='" + blAccount.getAccountNumber() + "' and tran_code in (" + XapiPool.dpTranCode + ") "
                + "and datediff(MM,create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) between 1 and " + noOfMonths + ") sd left join "
                + "(select count(*) noTxn,month(create_dt) Txmonth,acct_no from  " + XapiCodes.coreschema + "..dp_history where acct_no ='" + blAccount.getAccountNumber() + "' and tran_code in (" + XapiPool.dpTranCode + ") and datediff(MM,create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) between 1 and " + noOfMonths + ""
                + "group by month(create_dt),acct_no) sf on sd.acct_no = sf.acct_no   where  sd.Txmonth = sf.Txmonth order by diff ");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select distinct sd.Txmonth,Txyear,sd.diff,sd.acct_no,noTxn  "
                     + "from (select (select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control) today,create_dt,month(create_dt) Txmonth,year(create_dt) Txyear,datediff(MM,create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) diff,acct_no "
                     + "from  " + XapiCodes.coreschema + "..dp_history dh where acct_no ='" + blAccount.getAccountNumber() + "' and tran_code in (" + XapiPool.dpTranCode + ") "
                     + "and datediff(MM,create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) between 1 and " + noOfMonths + ") sd left join "
                     + "(select count(*) noTxn,month(create_dt) Txmonth,acct_no from  " + XapiCodes.coreschema + "..dp_history where acct_no ='" + blAccount.getAccountNumber() + "' and tran_code in (" + XapiPool.dpTranCode + ") and datediff(MM,create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) between 1 and " + noOfMonths + ""
                     + "group by month(create_dt),acct_no) sf on sd.acct_no = sf.acct_no   where  sd.Txmonth = sf.Txmonth order by diff "))
        {
            while (rs.next())
            {
                BLScoreParameter blScoreParameter = new BLScoreParameter();
                blScoreParameter.setTxnMonth(rs.getLong("Txmonth"));
                blScoreParameter.setAccountNumber(rs.getString("acct_no"));
                blScoreParameter.setTxnYear(rs.getLong("Txyear"));
                blScoreParameter.setNoOfTxn(rs.getLong("noTxn"));
                blScoreParameter.setMntCounter(rs.getLong("diff"));
                blScoreParameters.add(blScoreParameter);
            }

        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }

        return blScoreParameters;
    }

    public int getRoundedNumber(BigDecimal value)
    {
        int roundedNo = 0;
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select round(" + value + ", -3) as num"))
        {
            if (rs.next())
            {
                roundedNo = rs.getInt("num");
            }

        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }

        return roundedNo;
    }

    public BigDecimal checkRepaymentScore(Long rimNo)
    {

        BigDecimal score = BigDecimal.ZERO;
        boolean repaidCriteria1 = queryRepaymentScore(rimNo, 1);
        System.out.println("criterial 1" + repaidCriteria1);
        boolean repaidCriteria2 = queryRepaymentScore(rimNo, 2);
        System.out.println("criterial 2" + repaidCriteria2);
        boolean repaidCriteria3 = queryRepaymentScore(rimNo, 3);
        System.out.println("criterial 3" + repaidCriteria3);
        boolean repaidCriteria4 = queryRepaymentScore(rimNo, 4);
        System.out.println("criterial 4" + repaidCriteria4);

        System.out.println("criterial 1" + repaidCriteria1);
        if (repaidCriteria1 && !repaidCriteria2 && !repaidCriteria3 && !repaidCriteria4)
        {
            score = XapiPool.repmt14DaysScore; //replace with variables
        }
        else if (!repaidCriteria1 && repaidCriteria2 && !repaidCriteria3 && !repaidCriteria4)
        {
            score = XapiPool.repmt25DaysScore;
        }
        if (!repaidCriteria1 && !repaidCriteria2 && repaidCriteria3 && !repaidCriteria4)
        {
            score = XapiPool.repmt30DaysScore;
        }
        if (!repaidCriteria1 && !repaidCriteria2 && !repaidCriteria3 && repaidCriteria4)
        {
            score = XapiPool.repmt3DaysLateDaysScore; //+ must be freezed for the period = late days
        }
        System.out.println("score repayment score " + score);
        return score;
    }

    public boolean queryRepaymentScore(Long rimNo, int repayedCriteria1)
    {
        String criteria = "";
        if (repayedCriteria1 == 1)
        {
            criteria = " and datediff(dd,bb.effective_dt,cc.mat_dt) between 0 and 14 ";
        }
        else if (repayedCriteria1 == 2)
        {
            criteria = " and datediff(dd,bb.effective_dt,cc.mat_dt) between 15 and 24 ";
        }
        else if (repayedCriteria1 == 3)
        {
            criteria = " and datediff(dd,bb.effective_dt,cc.mat_dt) between 15 and (select datediff(dd, create_dt,mat_dt) from " + XapiCodes.coreschema + "..ln_display where acct_no = bb.acct_no ) ";
        }
        else if (repayedCriteria1 == 4)
        {
            criteria = "and datediff(dd,bb.effective_dt,cc.mat_dt) between (select datediff(dd, create_dt,mat_dt) from " + XapiCodes.coreschema + "..ln_display where acct_no = bb.acct_no)  and (select datediff(dd, create_dt,mat_dt)+3 from " + XapiCodes.coreschema + "..ln_display where acct_no = bb.acct_no) ";
        }

        boolean verified = checkIfExists("VerifyRepaymentScore", "select * from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc where bb.acct_no=cc.acct_no " +
                "and cc.acct_no = (select max(acct_no) from " + XapiCodes.coreschema + "..ln_acct bb where rim_no = " + rimNo + " and status='Closed' and bb.class_code in  (" + XapiPool.borrowerLoanClassCode + "," + XapiPool.borrowerLoanClassCode + ")) and tran_code in (345) " + criteria + " ");
        return verified;
    }

    public boolean lastLoanRepaidTimely(Long rimNo)
    {
        return checkIfExists("Check_if_last_Loan_Repaid_Timely", "select * from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc " +
                " where bb.acct_no=cc.acct_no and bb.acct_type=cc.acct_type and cc.acct_no = (select max(acct_no) from " + XapiCodes.coreschema + "..ln_acct bb where rim_no = " + rimNo + " and status='Closed') and tran_code in (345) " +
                "and bb.effective_dt<=cc.mat_dt");

    }

    public BigDecimal checkAveDepositVolumeScore(Long RimNo, int NoOfMonths)
    {//njinu --- to change to average deposits..
        BigDecimal volumePercentage = XapiPool.dpAveVolPercentage.divide(BigDecimal.valueOf(100), 3, RoundingMode.DOWN);//parameter
        System.out.println(">>>>> volumePercentage" + volumePercentage);
        BigDecimal volumeAverage = BigDecimal.ZERO;
        BigDecimal finalScore;
        ApiLogger.getLogger().debug("checkAveDepositVolumeScore \n\r\t select count(*) noTxn ,bb.acct_no,cast(sum(bb.amt) as numeric(16,2)) totalAmt,cast(round(sum(bb.amt)/count(*),2) as numeric(16,2)) as average  " +
                "from " + XapiCodes.coreschema + "..dp_display aa, " + XapiCodes.coreschema + "..dp_history bb where aa.acct_no =bb.acct_no and bb.acct_type=aa.acct_type and  rim_no = " + RimNo + " and status='Active' and aa.class_code in (" + XapiPool.allowedDpClass + ") " +
                "and datediff(mm,bb.create_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) between 0 and " + NoOfMonths + " and bb.tran_code in (" + XapiPool.dpTranCode + ") " +
                "group by  bb.acct_no");
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select count(*) noTxn ,bb.acct_no,cast(sum(bb.amt) as numeric(16,2)) totalAmt,cast(round(sum(bb.amt)/count(*),2) as numeric(16,2)) as average  " +
                     "from " + XapiCodes.coreschema + "..dp_display aa, " + XapiCodes.coreschema + "..dp_history bb where aa.acct_no =bb.acct_no and bb.acct_type=aa.acct_type and  rim_no = " + RimNo + " and status='Active' and aa.class_code in (" + XapiPool.allowedDpClass + ") " +
                     "and datediff(mm,bb.create_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) between 0 and " + NoOfMonths + " and bb.tran_code in (" + XapiPool.dpTranCode + ") " +
                     "group by  bb.acct_no"))
        {
            if (rs.next())
            {
                volumeAverage = rs.getBigDecimal("average");
            }

        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        ApiLogger.getLogger().info("Average Volume " + volumeAverage);
        ApiLogger.getLogger().info("Average Volume " + volumePercentage);
        finalScore = volumeAverage.divide(volumePercentage).setScale(0, 2);
        ApiLogger.getLogger().info("Average finalScore  " + finalScore);
        getCrCaller().getBlScoreCard().setAverageVlmScore(finalScore);
        return finalScore;
    }

    public BigDecimal checkMinAverageVolumeScore(Long RimNo, int NoOfMonths)
    {//njinu --- to change to average deposits..
        BigDecimal volumePercentage = XapiPool.dpAveVolPercentage.divide(BigDecimal.valueOf(100), 3, RoundingMode.DOWN);//parameter
        System.out.println(">>>>> volumePercentage" + volumePercentage);
        BigDecimal volumeAverage = BigDecimal.ZERO;
        BigDecimal finalScore;
        ApiLogger.getLogger().debug("checkMinAverageVolume  \n\r\t select cast(min(cur_bal) as numeric(26,2)) average from (select month(cs.create_dt) create_dt,min(cs.cur_bal) cur_bal ,cs.acct_no  " +
                " from " + XapiCodes.coreschema + "..csa_avg_daily_bal cs," + XapiCodes.coreschema + "..dp_display dp where cs.acct_no = dp.acct_no   and cs.acct_type=dp.acct_type " +
                " and  rim_no = " + RimNo + "  and datediff(mm,cs.create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) between 0 and " + NoOfMonths + " " +
                " group by cs.acct_no,month(cs.create_dt)) av");
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select cast(min(cur_bal) as numeric(26,2)) average  from (select month(cs.create_dt) create_dt,min(cs.cur_bal) cur_bal ,cs.acct_no  " +
                     " from " + XapiCodes.coreschema + "..csa_avg_daily_bal cs," + XapiCodes.coreschema + "..dp_display dp where cs.acct_no = dp.acct_no   and cs.acct_type=dp.acct_type " +
                     " and  rim_no = " + RimNo + "  and datediff(mm,cs.create_dt,(select dateadd(dd,1,last_to_dt) from  " + XapiCodes.coreschema + "..ov_control)) between 0 and " + NoOfMonths + " " +
                     " group by cs.acct_no,month(cs.create_dt)) av"))
        {
            if (rs.next())
            {
                volumeAverage = rs.getBigDecimal("average");
            }

        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        ApiLogger.getLogger().info("Average Volume " + volumeAverage);
        ApiLogger.getLogger().info("Average Volume " + volumePercentage);
        finalScore = volumeAverage.divide(volumePercentage).setScale(0, 2);
        ApiLogger.getLogger().info("Average finalScore  " + finalScore);
        getCrCaller().getBlScoreCard().setAverageVlmScore(finalScore);
        return finalScore;
    }

    public BigDecimal checkDepositPerPeriodScore(Long rimNo)
    {
        System.out.println("<<<<< checking deposits per period >>>>>");
        HashMap<Integer, Boolean> periodMap = new HashMap<>();
        BigDecimal finalScore;
        int maxPeriod = XapiPool.dpMaxTxnPeriod.intValue();
        int midPeriod = XapiPool.dpMidTxnPeriod.intValue();
        int minPeriod = XapiPool.dpMinTxnPeriod.intValue();


        BigDecimal score3 = BigDecimal.ZERO;
        BigDecimal score6 = BigDecimal.ZERO;
        BigDecimal score9 = BigDecimal.ZERO;

        for (int i = 0; i <= maxPeriod; i++)
        {
            ApiLogger.getLogger().info("evaluate for period " + i);
            periodMap.put(i, verifyDepositsPerPeriod(i, rimNo));
            i += 2;
        }
        for (Map.Entry<Integer, Boolean> entry : periodMap.entrySet())
        {
            Integer key = entry.getKey();
            boolean value = entry.getValue();
            ApiLogger.getLogger().info("month Period= " + key + "No of Txn for Period " + value);

            if (key == minPeriod && value)
            {
                score3 = XapiPool.dp3monthScore; //change to reflect value on settings...
                ApiLogger.getLogger().info("Score= " + key + " Score is " + score3);
            }
            else if (key == midPeriod && value)
            {
                score6 = XapiPool.dp6monthScore;
                ApiLogger.getLogger().info("Score= " + key + " Score is " + score6);
            }
            else if (key == maxPeriod && value)
            {
                score9 = XapiPool.dp9monthScore;
                ApiLogger.getLogger().info("Score= " + key + " Score is " + score9);
            }
            else
            {
                score3 = BigDecimal.ZERO;
                ApiLogger.getLogger().info("Score= " + key + " Score is " + score3);
            }

        }

        if (score3.compareTo(BigDecimal.ZERO) <= 0) //check if the customer has deposit for at least 3 months
        {
            finalScore = BigDecimal.ZERO;
        }
        else
        {
            finalScore = score3.max(score6.max(score9));
        }
        ApiLogger.getLogger().info("Scored depositor== " + finalScore);
        getCrCaller().getBlScoreCard().setDepositorScore(finalScore);
        return finalScore;
    }

    public boolean verifyDepositsPerPeriod(int period, Long rimNo)
    {
        boolean isVerified = true;
        HashMap<Integer, Long> txnCountMap = new HashMap<>();
        ApiLogger.getLogger().info("verifyDepositsPerPeriod\n\r\t select count(*) noTxn, month(aa.create_dt) Txmonth  from " + XapiCodes.coreschema + "..dp_history aa, " + XapiCodes.coreschema + "..dp_display bb " +
                "where aa.acct_no = bb.acct_no  and bb.acct_type=aa.acct_type  and bb.rim_no =" + rimNo + " and aa.tran_code in (" + XapiPool.dpTranCode + ")  " +
                "and datediff(MM,aa.create_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) between 0 and " + period + " group by aa.create_dt");
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select count(*) noTxn, month(aa.create_dt) Txmonth  from " + XapiCodes.coreschema + "..dp_history aa, " + XapiCodes.coreschema + "..dp_display bb " +
                     "where aa.acct_no = bb.acct_no  and bb.acct_type=aa.acct_type  and bb.rim_no =" + rimNo + " and aa.tran_code in (" + XapiPool.dpTranCode + ")  " +
                     "and datediff(MM,aa.create_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) between 0 and " + period + " group by aa.create_dt"))
        {
            if (rs.next())
            {
                txnCountMap.put(rs.getInt("Txmonth"), rs.getLong("noTxn"));
                isVerified = true;
            }

        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }

        for (Map.Entry<Integer, Long> entry : txnCountMap.entrySet())
        {
            Integer key = entry.getKey();
            Long value = entry.getValue();
            ApiLogger.getLogger().info("month Period= " + key + "No of Txn for Period " + value);

            if (value == 0 && period != 0)
            {
                isVerified = false;
            }
        }

        return isVerified;
    }

    private boolean checkIfExists(String queryType, String query)
    {
        boolean exists = false;
        ApiLogger.getLogger().debug(queryType + "\n\t" + query + "\n");
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(query))
        {
            exists = rs.next();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        System.err.println("query+exists " + exists);
        return exists;
    }

    public StringBuilder getBuilder(boolean reset)
    {
        if (reset)
            builder.setLength(0);
        return builder;
    }

    public BLScoreCard getBlScoreCard()
    {
        return blScoreCard;
    }

    public void setBlScoreCard(BLScoreCard blScoreCard)
    {
        this.blScoreCard = blScoreCard;
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

    public StringBuilder getBuilder()
    {
        return builder;
    }

    public void setBuilder(StringBuilder builder)
    {
        this.builder = builder;
    }

}
