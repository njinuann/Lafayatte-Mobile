package org.redlamp.extras;

import org.redlamp.interfaces.ISO;
import org.redlamp.interfaces.SQL;
import org.redlamp.logger.ApiLogger;
import org.redlamp.model.*;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class BorrowerScoringHandler implements AutoCloseable, ISO, SQL
{

    private Connection conn;
    private StringBuilder builder;
    private BigDecimal HUNDREAD = BigDecimal.valueOf(100);
    private BLScoreCard blScoreCard = new BLScoreCard();
    private BLScoreItems blScoreItems = new BLScoreItems();
    private CRCaller crCaller = new CRCaller();
    private long endTime;
    private long startTime;
    private String additionalResponseText = "";


    public BorrowerScoringHandler()
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
        try
        {
            if (conn == null || conn.isClosed())
            {
                conn = XapiPool.getConnection();
            }
        } catch (Exception ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        setBlScoreCard(getCrCaller().getBlScoreCard());
        BigDecimal finalScore1 = BigDecimal.ZERO;
        BigDecimal finalScore2 = BigDecimal.ZERO;
        BigDecimal minScore = BigDecimal.ZERO;
        BigDecimal approvedScore = BigDecimal.ZERO;
        boolean initialCheck = true;


        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        BigDecimal cycleScore;

        BLAccount blAccount = queryRegistered(request.getAccountNo(), request.getPhoneNumber()); //check if the customer is eligible based on rim classcode,deposit class code and no previous active loan

        System.out.println("getting account " + blAccount.getAccountNumber());

        if (!isBlank(blAccount.getAccountNumber()))
        {

            //Start preliminary checks************/

            LoanRequest acctWithLargestDisbursement = queryLargestDisbursement(blAccount.getRimNo());
            System.err.println("getting account Loan>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  " + acctWithLargestDisbursement);
            blAccount.setLoanAccountNumber(acctWithLargestDisbursement.getAccountNo());

            setBlScoreItems(selectScoringItems(blAccount.getLoanAccountNumber()));
            getCrCaller().setCall("SCORING ITEMS ", getBlScoreItems());

            getBlScoreCard().setAccountType(acctWithLargestDisbursement.getAccountType());
            getBlScoreCard().setRimNumber(blAccount.getRimNo());
            getBlScoreCard().setAccountWithHighestDisb(blAccount.getLoanAccountNumber());

            getBlScoreCard().setAccountNumber(request.getAccountNo());
            getBlScoreCard().setPhoneNumber(blAccount.getPhoneNumber());
            getCrCaller().setCall("blAccount", blAccount);

            boolean isClassCodeAllowed = checkEligibleClasses(blAccount);
            getCrCaller().setCall("isClassCodeAllowed", isClassCodeAllowed);
            boolean hadPreviousLoan = checkCycle(blAccount);
            getCrCaller().setCall("hadPreviousLoan", hadPreviousLoan);
            boolean hasValidCycle = checkCycle(blAccount);
            getCrCaller().setCall("hasValidCycle", hasValidCycle);
            boolean hasRequiredMinInstalments = hasMinReqInstalment(blAccount, XapiPool.minInstallments);
            getCrCaller().setCall("hasRequiredMinInstalments", hasRequiredMinInstalments);
            boolean noRestructuredLoan = !loanRestructured(blAccount);
            getCrCaller().setCall("No Restructured Loan", noRestructuredLoan);
            boolean hasClosureMonths = satisfiesLoanClosurePeriod(blAccount, XapiPool.previousLoanClosureDays);
            getCrCaller().setCall("hasClosureMonths", hasClosureMonths);
            boolean noPreviousLoanDefaulted7days = !hasLatePaymentsDaysCount(blAccount, XapiPool.latePmtMoreThan7Days, XapiPool.days7lateInstallmentCount); //parameter
            getCrCaller().setCall("hasNoPreviousLoanDefaultedSEVENdays", noPreviousLoanDefaulted7days);
            boolean hasNoPreviousLoanDefaulted30days = !hasLatePaymentsDaysCount(blAccount, XapiPool.latePmtMoreThan30Days, XapiPool.days30lateInstallmentCount);
            getCrCaller().setCall("hasNoPreviousLoanDefaultedTHIRTYdays", hasNoPreviousLoanDefaulted30days);
            boolean hasCurrentDelayedPayment = !hasCurrentDelayedPayment(blAccount);
            getCrCaller().setCall("hasCurrentDelayedPayment", hasCurrentDelayedPayment);
            boolean noMaxLoansPerYear = !hasMaxLoansPerYear(blAccount, XapiPool.allowedNoOfLoansPerYear);
            getCrCaller().setCall("noMaxLoansPerYear", noMaxLoansPerYear);
            boolean hasNoDelayedCurrentInstallment = !hasDelayedCurrentInstallment(blAccount);
            getCrCaller().setCall("hasNoDelayedCurrentInstallment", hasNoDelayedCurrentInstallment);
            boolean isStateEligible = Objects.equals(XapiPool.allowedStates, "*") ? true : isStateEligible(blAccount);
            getCrCaller().setCall("isStateEligible", isStateEligible);
            boolean hasLoanAccount = !Objects.equals("XoX", acctWithLargestDisbursement.getAccountNo());
            System.err.println("<><>><><><><><><><><><><><><>< " + acctWithLargestDisbursement.getAccountNo());

            getBlScoreCard().setLoanScoreTpe("Borrower Score");
            getCrCaller().setNarration(getBlScoreCard().getLoanScoreTpe());
            getBlScoreCard().setClassCodesAllowed(isClassCodeAllowed);
            getBlScoreCard().setHasValidCycle(hasValidCycle);
            getBlScoreCard().setRestructured(noRestructuredLoan);
            getBlScoreCard().setHasClosureMonths(hasClosureMonths);
            getBlScoreCard().setHasPreviousLoanDefaulted7days(noPreviousLoanDefaulted7days);
            getBlScoreCard().setHasPreviousLoanDefaulted30days(hasNoPreviousLoanDefaulted30days);
            getBlScoreCard().setHasCurrentDelayedPayment(hasCurrentDelayedPayment);
            getBlScoreCard().setHasMaxLoansPerYear(noMaxLoansPerYear);
            getBlScoreCard().setHasDelayedCurrentInstallment(hasNoDelayedCurrentInstallment);

            getCrCaller().setCall("Initial Checks", "\n==================\n\t ClassCode Allowed = " + isClassCodeAllowed + ", " +
                    "\n\t has Cycle Valid = " + hasValidCycle + ", " +
                    "\n\t has Required Min Instalments = " + hasRequiredMinInstalments + ", " +
                    "\n\t has No Restructured = " + noRestructuredLoan + ", " +
                    "\n\t has Months closure = " + hasClosureMonths + ", " +
                    "\n\t has No 7days default = " + noPreviousLoanDefaulted7days +
                    "\n\t has No30 days default = " + hasNoPreviousLoanDefaulted30days +
                    "\n\t has current delayed payment = " + hasCurrentDelayedPayment +
                    "\n\t has No max loan per year = " + noMaxLoansPerYear +
                    "\n\t has current delayed installment = " + hasNoDelayedCurrentInstallment +
                    "\n\t has valid State = " + isStateEligible +
                    "\n\t has valid loan = " + hasLoanAccount +
                    "\n\t has No max Loans per year = " + noMaxLoansPerYear + "\n ==================\n"
            );
            System.err.println("EVALUATING BIGGE " + hasLoanAccount);
            System.err.println("=========== BEGIN PRE ANALYSIS ===========");
            if (isClassCodeAllowed)
            {
                if (hasValidCycle)
                {
                    if (hasRequiredMinInstalments)
                    {
                        if (noRestructuredLoan)
                        {
                            if (hasClosureMonths)
                            {
                                if (noPreviousLoanDefaulted7days)
                                {
                                    if (hasNoPreviousLoanDefaulted30days)
                                    {
                                        if (hasCurrentDelayedPayment)
                                        {
                                            if (noMaxLoansPerYear)
                                            {
                                                if (hasNoDelayedCurrentInstallment)
                                                {
                                                    if (isStateEligible)
                                                    {
                                                        if (hasLoanAccount)
                                                        {
                                                            if (noMaxLoansPerYear)
                                                            {
                                                                initialCheck = false;
                                                                getCrCaller().setCall("=========== AFTER PRE ANALYSIS ===========", isStateEligible);
                                                                System.err.println("=========== AFTER PRE ANALYSIS ===========");
                                                                BigDecimal historyScore = historyScore(blAccount);
                                                                getBlScoreCard().setHistoryScore(historyScore);

                                                                BigDecimal averageInstalments = instalmentAverageForPeriod(blAccount, XapiPool.averageMonths);

                                                                getBlScoreCard().setAverageVlmScore(averageInstalments);

                                                                BigDecimal RepaymentScore = checkRepaymentScore(blAccount.getRimNo());
                                                                getBlScoreCard().setRepaymentScore(RepaymentScore);

                                                                System.err.println("histScore>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + historyScore);
                                                                System.err.println("averageInstalments>>>>>>>>>>>>>>>>>>>>>>>>>>>>  " + averageInstalments);
                                                                System.err.println("RepaymentScore>>>>>>>>>>>>>>>>>>>>>>>>>>>>  " + RepaymentScore);
                                                                if (averageInstalments.compareTo(BigDecimal.ZERO) <= 0)
                                                                {
                                                                    response.put("responseCode", CUST_NOT_ELIGIBLE);
                                                                    additionalResponseText = "[average installment is less than 0]";
                                                                }
                                                                else if (historyScore.compareTo(BigDecimal.ZERO) <= 0)
                                                                {
                                                                    response.put("responseCode", CUST_NOT_ELIGIBLE);
                                                                    additionalResponseText = "[history score is less than 0]";
                                                                }
                                                                else if (RepaymentScore.compareTo(BigDecimal.ZERO) <= 0)
                                                                {
                                                                    response.put("responseCode", CUST_NOT_ELIGIBLE);
                                                                    additionalResponseText = "[repayment score is less than 0]";
                                                                }
                                                                else
                                                                {


                                                                    finalScore1 = averageInstalments.multiply(historyScore).multiply(RepaymentScore); //add all the score to get scored Amt
                                                                    // finalScore1 = averageInstalments.multiply(historyScore); //to facilitate test
                                                                    getBlScoreCard().setFinalScore1(finalScore1);

                                                                    System.err.println(finalScore1 + " Check minBorrowerLnAmount " + XapiPool.minBorrowerLnAmount);
                                                                    System.err.println("Check finalScore " + finalScore1.compareTo(XapiPool.minBorrowerLnAmount));
                                                                    System.err.println("Check finalScore " + (finalScore1.compareTo(XapiPool.minBorrowerLnAmount) >= 0));


                                                                    //approvedScore = finalScore1.compareTo(XapiPool.minBorrowerLnAmount) <= 0 ? BigDecimal.ZERO : finalScore1;
                                                                    // approvedScore = roundedAmount(approvedScore.intValue());
                                                                    // getBlScoreCard().setApprovedScore(approvedScore);
                                                                    if (finalScore1.compareTo(XapiPool.minBorrowerLnAmount) <= 0)
                                                                    {
                                                                        response.put("responseCode", CUST_NOT_ELIGIBLE);
                                                                        additionalResponseText = "[repayment score is less than 0]";
                                                                        approvedScore = BigDecimal.ZERO;
                                                                    }
                                                                    else if (finalScore1.compareTo(getCrCaller().getBlScoreCard().getMaxLimitAmount()) >= 0)
                                                                    {
                                                                        approvedScore = roundedAmount(finalScore1.intValue());
//                                                                         approvedScore = roundedAmount(getCrCaller().getBlScoreCard().getMaxLimitAmount().intValue());
                                                                        getBlScoreCard().setApprovedScore(approvedScore);
                                                                    }
//                                                                    else if (finalScore1.compareTo(XapiPool.maxBorrowerLnAmount) < 0)
//                                                                    {
//
//                                                                        approvedScore = roundedAmount(XapiPool.maxBorrowerLnAmount.intValue());
//                                                                        getBlScoreCard().setApprovedScore(approvedScore);
//                                                                    }
                                                                    else
                                                                    {
                                                                        approvedScore = roundedAmount(finalScore1.intValue());
                                                                        // approvedScore = roundedAmount(approvedScore.intValue());
                                                                        getBlScoreCard().setApprovedScore(approvedScore);
                                                                    }


                                                                    getCrCaller().setCall("averageInstalments", averageInstalments);
                                                                    getCrCaller().setCall("historyScore", historyScore);
                                                                    getCrCaller().setCall("RepaymentScore", RepaymentScore);
                                                                    getCrCaller().setCall("finalScore1", finalScore1);
                                                                    getCrCaller().setCall("approvedScore", approvedScore);
                                                                    getCrCaller().setCall("mainScore", getBlScoreCard().getBlMainScoreCard());

                                                                    getBlScoreCard().getBlMainScoreCard().setApprovedScore(approvedScore);
                                                                }
                                                            }
                                                            else
                                                            {
                                                                response.put("responseCode", CUST_NOT_ELIGIBLE);
                                                                additionalResponseText = "[Has reached max Loan per year]";
                                                            }
                                                        }
                                                        else
                                                        {
                                                            response.put("responseCode", CUST_NOT_ELIGIBLE);
                                                            additionalResponseText = "[Has No active Loan Account]";
                                                        }
                                                    }
                                                    else
                                                    {
                                                        response.put("responseCode", CUST_NOT_ELIGIBLE);
                                                        additionalResponseText = "[State Not Eligible]";
                                                    }
                                                }
                                                else
                                                {
                                                    response.put("responseCode", CUST_NOT_ELIGIBLE);
                                                    additionalResponseText = "[Has Delayed installement]";
                                                }
                                            }
                                            else
                                            {
                                                response.put("responseCode", CUST_NOT_ELIGIBLE);
                                                additionalResponseText = "[Has maximum Loan Per Year]";
                                            }
                                        }
                                        else
                                        {
                                            response.put("responseCode", CUST_NOT_ELIGIBLE);
                                            additionalResponseText = "[Has delayed Payment on Prev Loan]";
                                        }
                                    }
                                    else
                                    {
                                        response.put("responseCode", CUST_NOT_ELIGIBLE);
                                        additionalResponseText = "[Has Defaulted Instalment -30 days]";
                                    }
                                }
                                else
                                {
                                    response.put("responseCode", CUST_NOT_ELIGIBLE);
                                    additionalResponseText = "[Has Defaulted Installment - 7 days]";
                                }
                            }
                            else
                            {
                                response.put("responseCode", CUST_NOT_ELIGIBLE);
                                additionalResponseText = "[Has exceeded Closure Months]";
                            }
                        }
                        else
                        {
                            response.put("responseCode", CUST_NOT_ELIGIBLE);
                            additionalResponseText = "[Has restructured Loan]";
                        }
                    }
                    else
                    {
                        response.put("responseCode", CUST_NOT_ELIGIBLE);
                        additionalResponseText = "[Has no minimum required Instalments]";
                    }
                }
                else
                {
                    response.put("responseCode", CUST_NOT_ELIGIBLE);
                    additionalResponseText = "[Has No Valid cycle]";
                }
            }
            else
            {
                response.put("responseCode", CUST_NOT_ELIGIBLE);
                additionalResponseText = "[Class code not Eligible]";
            }
//            if (isClassCodeAllowed && hasValidCycle && hasRequiredMinInstalments && noRestructuredLoan
//                    && hasClosureMonths && noPreviousLoanDefaulted7days && hasNoPreviousLoanDefaulted30days
//                    && hasCurrentDelayedPayment && noMaxLoansPerYear && hasNoDelayedCurrentInstallment && isStateEligible
//                    && hasLoanAccount)    //changed maxloanper year for testing purposes.. !hasMaxLoansPerYear
//            {
//
//            }

            if (BigDecimal.ZERO.compareTo(approvedScore) >= 0 && initialCheck)
            {
                list.add(map);
            }
            else if (BigDecimal.ZERO.compareTo(approvedScore) >= 0 && !initialCheck)
            {
                list.add(map);
                response.put("responseCode", CUST_NOT_ELIGIBLE);
                // additionalResponseText = "[Approved amt less than/equal to 0]";
            }
            else
            {
                getBlScoreCard().setAmount(approvedScore);
                getCrCaller().getBlScoreCard().setAmount(approvedScore);

                map.put("Eligible_Amount", blScoreCard.getAmount());
                if (getCrCaller().getBlScoreCard().getScoreCategory().equalsIgnoreCase("A"))
                {
                    getCrCaller().getBlScoreCard().setAmount(XapiPool.BRClassAMaxAmount);
                    // map.put("MaxAmount", XapiPool.BRClassAMaxAmount.compareTo(approvedScore) > 0 ? approvedScore : XapiPool.BRClassAMaxAmount);
                    map.put("MaxAmount", XapiPool.BRClassAMaxAmount.compareTo(getCrCaller().getBlScoreCard().getFinalScore1()) > 0 ? approvedScore : XapiPool.BRClassAMaxAmount);

                }
                else if (getCrCaller().getBlScoreCard().getScoreCategory().equalsIgnoreCase("B"))
                {
                    getCrCaller().getBlScoreCard().setAmount(XapiPool.BRClassBMaxAmount);
                    map.put("MaxAmount", XapiPool.BRClassBMaxAmount.compareTo(approvedScore) > 0 ? approvedScore : XapiPool.BRClassBMaxAmount);
                }
                else if (getCrCaller().getBlScoreCard().getScoreCategory().equalsIgnoreCase("C"))
                {
                    getCrCaller().getBlScoreCard().setAmount(XapiPool.BRClassCMaxAmount);
                    map.put("MaxAmount", XapiPool.BRClassCMaxAmount.compareTo(approvedScore) > 0 ? approvedScore : XapiPool.BRClassCMaxAmount);
                }
                else
                {
                    getCrCaller().getBlScoreCard().setAmount(XapiPool.BRClassCMaxAmount);
                    map.put("MaxAmount", XapiPool.BRClassCMaxAmount.compareTo(approvedScore) > 0 ? approvedScore : XapiPool.BRClassCMaxAmount);
                }
                BigDecimal rate = currentRate(XapiPool.borrowerLoanClassCode);

                map.put("MinAmount", XapiPool.minBorrowerLnAmount);
                map.put("Account", blScoreCard.getAccountNumber());
                map.put("Period", XapiPool.defaultLoanPeriod);
                map.put("Term", XapiPool.minLoanterm);
                map.put("Rate", rate);
                list.add(map);
                response.put("responseCode", XAPI_APPROVED);
            }

        }
        else
        {
            getBlScoreCard().setAccountNumber(request.getAccountNo());
            getBlScoreCard().setPhoneNumber(request.getPhoneNumber());
            getBlScoreCard().setLoanScoreTpe("Borrower");
            getBlScoreCard().setAccountType("n/a");
            getBlScoreCard().setRimNumber(0L);
            response.put("responseCode", CUST_NOT_ELIGIBLE);
            additionalResponseText = "[Account not found/not registered]";
        }
        if (!XAPI_APPROVED.equals(response.get("responseCode")))
        {
            map.put("Eligible_Amount", BigDecimal.ZERO);
            map.put("Account", blScoreCard.getAccountNumber());
            response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
            //additionalResponseText = "[" + XapiCodes.getErrorDesc(response.get("responseCode")) + "]";
        }
        else
        {
            response.put("responseCode", XAPI_APPROVED);
            response.put("responseTxt", "Customer is Eligible");
            additionalResponseText = "[Eligible]";
        }

        System.out.println("Fina response " + response.get("responseCode"));
        // response.put("responseTxt", XapiCodes.getErrorDesc(response.get("responseCode")));
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

    public BigDecimal currentRate(Long classCode)
    {
        BigDecimal rate = BigDecimal.ZERO;
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select aa.rate , bb.accr_basis from " + XapiCodes.coreschema + "..ad_gb_rate_history aa, " + XapiCodes.coreschema + "..ad_ln_cls_int_opt bb "
                     + " where aa.index_id = bb.index_id  and bb.class_code  =" + classCode + " and aa.ptid = (select max(ptid) from " + XapiCodes.coreschema + "..ad_gb_rate_history where index_id =aa.index_id)"))
        {
            if (rs.next())
            {
                BigDecimal classRate = rs.getBigDecimal("rate");
                rate = classRate.multiply((new BigDecimal(30).divide(new BigDecimal(360), MathContext.DECIMAL128))).setScale(2, RoundingMode.UP);

            }

        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        return rate;
    }

    public void windUp()
    {
        writeToLog();

    }

    private void writeToLog()
    {
        try
        {
            //ApiLogger.getLogger().error(e1);
            setEndTime(System.currentTimeMillis());
            getCrCaller().setDuration(String.valueOf(getEndTime() - getStartTime()) + " Ms");


            logLoanScore(getCrCaller());
            new Thread(new ThreadLogger(new ApiLogger(), "<transaction>" + "\r\n\t" + getCrCaller() + "</transaction>")).start();

            // APMain.cqsLog.logEvent(gettXCaller());
            setCrCaller(new CRCaller());
            close();
        } catch (Exception ex)
        {
            ApiLogger.getLogger().error(ex);
        }
    }

    private boolean logLoanScore(CRCaller crCaller)
    {
        String saveLoanScore = "INSERT INTO " + XapiCodes.xapiSchema + "..E_LOAN_SCORE  (ACCOUNT_NO, ACCT_TYPE, RIM_NO, PHONE_NUMBER, SCORE_TYPE, AMOUNT, CYCLE_SCORE, DEPOSITOR_SCORE, AVERAGE_VOLUME_SCORE, REPAYMENT_SCORE, FINAL_SCORE, " +
                "HISTORY_SCORE, APPROVED_SCORE, LATE_INSTALMENT_SCORE, BORROWER, CLASS_CODE_ALLOWED, PREV_TIMELY_REPAYMENT, VOLUNTARY_DESPOSITS, VALID_CYCLE, REQ_MIN_INSTALMENTS, LOAN_RESTRUCTRED, " +
                "CLOSURE_MONTH_VALID, PREV_DEFAULTED_LOAN7DAYS, PREV_DEFAULTED_LOAN30DAYS, CURRENT_DELAYED_PAYMENT, MAX_LOAN_PER_YEAR, DELAYED_CURRENT_INSTALEMENT, RESPONSE_CODE, RESPONSE_MESSAGE, CREATE_DT,SCORE_CLASS,SCORE_POINTS )" +
                "VALUES('" + crCaller.getBlScoreCard().getAccountNumber() + "'," +
                "'" + crCaller.getBlScoreCard().getAccountType() + "', " +
                "" + crCaller.getBlScoreCard().getRimNumber() + "," +
                "'" + crCaller.getBlScoreCard().getPhoneNumber().trim() + "'," +
                "'Borrower Score Card'," +
                "" + crCaller.getBlScoreCard().getAmount().setScale(0, 2) + "," +
                "" + crCaller.getBlScoreCard().getCycleScore().setScale(0, 2) + "," +
                "" + crCaller.getBlScoreCard().getDepositorScore().setScale(0, 2) + "," +
                "" + crCaller.getBlScoreCard().getAverageVlmScore().setScale(0, 2) + "," +
                "" + crCaller.getBlScoreCard().getRepaymentScore().setScale(0, 2) + "," +
                "" + crCaller.getBlScoreCard().getFinalScore1().setScale(0, 2) + ", " +
                "" + crCaller.getBlScoreCard().getHistoryScore().setScale(0, 2) + "," +
                "" + crCaller.getBlScoreCard().getApprovedScore().setScale(0, 2) + "," +
                "" + crCaller.getBlScoreCard().getLateInstalmentScore().setScale(0, 2) + "," +
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
                "'" + crCaller.getXapiRespCode() + "'," +
                "'" + getCrCaller().getXapiRespMsg() + "',getdate()," +
                "'" + crCaller.getBlScoreCard().getScoreCategory() + "'," +
                "" + crCaller.getBlScoreCard().getScorePoints() + ")";
        try
        {
            if (conn == null || conn.isClosed())
            {
                conn = XapiPool.getConnection();
            }
        } catch (Exception ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        ApiLogger.getLogger().info(saveLoanScore);
        try (Statement statement = conn.createStatement())
        {
            if (!isBlank(crCaller.getBlScoreCard().getAccountNumber()))
            {
                statement.executeUpdate(saveLoanScore);

            }
            return true;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }

    }

    public BigDecimal roundedAmount(int value)
    {
        return new BigDecimal((value / 1000) * 1000);
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

    public String yesNo(boolean checkVal)
    {
        return checkVal ? "Y" : "N";
    }

    public BigDecimal historyScore(BLAccount bLAccount)
    {
        String histScoreCategory = "";
        HashMap<BigDecimal, LoanParameterTier> definitionScoreParam = queryDefinitionScoreTier(XapiPool.scoreDefParameter);
        BigDecimal historyScore = BigDecimal.ZERO;
        BigDecimal finalHistoryScore = BigDecimal.ZERO;
        BigDecimal weightedScore = calculateWeightedScore(bLAccount); //weighted Instalments
        BigDecimal finalDefaultInstalmentScore = calculateLateInstalmentScore(bLAccount);
        BigDecimal loanDurationScore = calculateLoanDurationScore(bLAccount);
        BigDecimal calculateCycleScore = calculateCycleScore(bLAccount);
        BigDecimal riskScore = calculateRiskScore(bLAccount);
        BigDecimal residenceScore = calculateResidenceScore(bLAccount);

        System.out.println("XapiPool.scoreDefParameter " + XapiPool.scoreDefParameter);
        System.out.println("XapiPool.scoreDefParameter " + XapiPool.scoreDefParameter);

        historyScore = weightedScore.add(finalDefaultInstalmentScore).add(loanDurationScore).add(calculateCycleScore).add(riskScore).add(residenceScore);


        for (Map.Entry<BigDecimal, LoanParameterTier> params : definitionScoreParam.entrySet())
        {
            System.err.println("<<<><>><>< " + historyScore);
            System.err.println("<<<><>><><ceil " + params.getValue().getTierCeiling());
            System.err.println("<<<><>><><ceil " + (historyScore.compareTo(params.getValue().getTierCeiling()) <= 0));
            if (historyScore.doubleValue() >= params.getValue().getTierFloor().doubleValue() && historyScore.doubleValue() <= params.getValue().getTierCeiling().doubleValue())
            {
                histScoreCategory = params.getValue().getTierValueTwo();
            }
//            if (historyScore.compareTo(params.getValue().getTierCeiling()) <= 0)
//            {
//
//            }
        }
        getCrCaller().setCall("History Score", "\n==================" +
                "\n\t historyScore = " + historyScore + ", " +
                "\n\t points_weighted_late_inst = " + weightedScore + ", " +
                "\n\t points_default_on_first_three = " + finalDefaultInstalmentScore + ", " +
                "\n\t loanDurationScore = " + loanDurationScore + ", " +
                "\n\t calculateCycleScore = " + calculateCycleScore + ", " +
                "\n\t riskScore = " + riskScore +
                "\n\t class = " + histScoreCategory +
                "\n\t points_residence = " + residenceScore +
                "\n ==================\n");
        getBlScoreCard().getBlMainScoreCard().setDefinitionValueScore(histScoreCategory);
        getCrCaller().getBlScoreCard().setScorePoints(historyScore);
        getCrCaller().getBlScoreCard().setScoreCategory(histScoreCategory);
        getCrCaller().getBlScoreCard().setMinLimitAmount(XapiPool.minBorrowerLnAmount);

        if ("A".equalsIgnoreCase(histScoreCategory))
        {
            finalHistoryScore = XapiPool.definitionScoreA;
            getCrCaller().getBlScoreCard().setMaxLimitAmount(XapiPool.BRClassAMaxAmount);

        }
        else if ("B".equalsIgnoreCase(histScoreCategory))
        {
            finalHistoryScore = XapiPool.definitionScoreB;
            getCrCaller().getBlScoreCard().setMaxLimitAmount(XapiPool.BRClassBMaxAmount);
        }
        else if ("C".equalsIgnoreCase(histScoreCategory))
        {
            finalHistoryScore = XapiPool.definitionScoreC;
            getCrCaller().getBlScoreCard().setMaxLimitAmount(XapiPool.BRClassCMaxAmount);
        }
        else if ("D".equalsIgnoreCase(histScoreCategory))
        {
            finalHistoryScore = XapiPool.definitionScoreD;
            getCrCaller().getBlScoreCard().setMaxLimitAmount(XapiPool.BRClassCMaxAmount);
        }

        getCrCaller().setCall("weightedScore", weightedScore);
        getCrCaller().setCall("finalDefaultInstalmentScore", finalDefaultInstalmentScore);
        getCrCaller().setCall("loanDurationScore", loanDurationScore);
        getCrCaller().setCall("calculateCycleScore", calculateCycleScore);
        getCrCaller().setCall("riskScore", riskScore);
        getCrCaller().setCall("residenceScore", residenceScore);
        getCrCaller().setCall("historyScore1", historyScore);
        getCrCaller().setCall("histScoreCategory", histScoreCategory);
        getCrCaller().setCall("finalHistoryScore", finalHistoryScore);
        getCrCaller().setCall("maximumAmount", getCrCaller().getBlScoreCard().getMaxLimitAmount());
        getCrCaller().setCall("minimumAmount", getCrCaller().getBlScoreCard().getMinLimitAmount());

        getBlScoreCard().getBlMainScoreCard().setHistoryScore(finalHistoryScore);
        return finalHistoryScore;
    }

    public BigDecimal calculateLateInstalmentScore(BLAccount bLAccount)
    {
        BigDecimal finalDefaultInstalmentScore = BigDecimal.ZERO;
//        BigDecimal defaultFirst3Instalments = queryFirst3InstalmentLate(bLAccount);
        System.out.println("getDueOnSecondInstallment<<<><><><>><><><><><><> " + getBlScoreItems().getDueOnFirstInstallment());
        System.out.println("getDueOnSecondInstallment<<<><><><>><><><><><><> " + getBlScoreItems().getDueOnSecondInstallment());
        System.out.println("getDueOnSThirdInstallment<<<><><><>><><><><><><> " + getBlScoreItems().getDueOnSThirdInstallment());
        List<String> list1 = new ArrayList<>();
        list1.add(getBlScoreItems().getDueOnFirstInstallment());
        list1.add(getBlScoreItems().getDueOnSecondInstallment());
        list1.add(getBlScoreItems().getDueOnSThirdInstallment());

        int count = 0;
        for (String xx : list1)
        {
            if (Objects.equals("Y", xx))
            {
                count++;
            }
        }
        System.out.println("no fo defaults <<<><><><>><><><><><><> " + count);


        BigDecimal defaultFirst3Instalments = new BigDecimal(count);
        ;
        System.out.println("defaultFirst3Instalments<<<><><><>><><><><><><> " + defaultFirst3Instalments);
        System.out.println("defaultFirst3Instalments.compareTo(BigDecimal.ZERO)<<<><><><>><><><><><><> " + defaultFirst3Instalments.compareTo(BigDecimal.ZERO));
        if (defaultFirst3Instalments.compareTo(BigDecimal.ZERO) >= 0)
        {
            HashMap<BigDecimal, LoanParameterTier> instalmentParam = queryParameterTier(XapiPool.defaultInstalmentParam);

            for (Map.Entry<BigDecimal, LoanParameterTier> params : instalmentParam.entrySet())
            {
                System.out.println("getTierCeiling<<<>>>> " + params.getValue().getTierCeiling());
                System.out.println("defaultFirst3Instalments <<<>>>> " + defaultFirst3Instalments);
                System.out.println("defaultFirst3Instalments.compareTo(params.getValue().getTierCeiling()) <<<>>>> " + defaultFirst3Instalments.compareTo(params.getValue().getTierCeiling()));

//                if (defaultFirst3Instalments.compareTo(params.getValue().getTierCeiling()) <= 0)
//                {
                if (defaultFirst3Instalments.doubleValue() >= params.getValue().getTierFloor().doubleValue() && defaultFirst3Instalments.doubleValue() <= params.getValue().getTierCeiling().doubleValue())
                {
                    System.out.println("getTierCeiling22<<<>>>> " + params.getValue().getTierCeiling());
                    System.out.println("getTierCeiling22<<<>>>> " + params.getValue().getTierValue());
                    finalDefaultInstalmentScore = params.getValue().getTierValue();
                    System.out.println("finalDefaultInstalmentScore<<<>>>> " + finalDefaultInstalmentScore);
                }
            }

        }
        getBlScoreCard().getBlMainScoreCard().setDefault3monthScore(finalDefaultInstalmentScore);
        return finalDefaultInstalmentScore;
    }

    public BigDecimal calculateResidenceScore(BLAccount bLAccount)
    {
        BigDecimal residenceScore = BigDecimal.ZERO;
        String residenceRisk = queryResidence(bLAccount);
        System.err.println("XapiPool.residenceStatusOwn >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + XapiPool.residenceStatusOwn);
        System.err.println("residenceRisk >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + residenceRisk);
        System.err.println("check XapiPool.residenceStatusOwn.contains(residenceRisk) >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + XapiPool.residenceStatusOwn.trim().contains(residenceRisk.trim()));
        if (XapiPool.residenceStatusOwn.trim().contains(residenceRisk.trim()))
        {
            System.err.println("Scoring residence here >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + residenceRisk);
            residenceScore = XapiPool.residenceStatusOwnPoints; //13
            System.err.println("residenceScore >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + residenceScore);
        }
        else if (XapiPool.residenceStatusOther.trim().contains(residenceRisk.trim()))
        {
            System.err.println("Scoring residence here2 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + residenceRisk);
            residenceScore = XapiPool.residenceStatusOtherPoints; //0
        }
        getCrCaller().setCall("RESIDENSE SCORING", "\n==================" +
                "\n\t residenceRisk = " + residenceRisk + ", " +
                "\n\t residenceScore = " + residenceScore + ", " +
                "\n ==================\n"
        );
        getBlScoreCard().getBlMainScoreCard().setResidenceScore(residenceScore);
        return residenceScore;
    }

    public BigDecimal calculateRiskScore(BLAccount bLAccount)
    {

        BigDecimal riskScore = BigDecimal.ZERO;
        String residenceRisk = queryRisk(bLAccount);
        if (XapiPool.riskGroupNormal.contains(residenceRisk))
        {
            riskScore = XapiPool.riskGroupNormalPoints;
        }
        else if (XapiPool.riskGroupMedium.contains(residenceRisk))
        {
            riskScore = XapiPool.riskGroupMediumPoints;
        }
        else if (XapiPool.riskGroupOther.contains(residenceRisk))
        {
            riskScore = XapiPool.riskGroupOtherPoints;

        }
        getBlScoreCard().getBlMainScoreCard().setRiskScore(riskScore);
        return riskScore;
    }

    public BigDecimal calculateLoanDurationScore(BLAccount bLAccount)
    {
        BigDecimal finalLoanTermScore = BigDecimal.ZERO;
        BigDecimal LoanTermScore = queryLoanTerm(bLAccount);
        System.err.println("LOAN TERM SCORE >>>>>>>>>> >>>>>>>>>>>>>>> " + LoanTermScore);
        System.err.println("LOAN TERM LoanTermScore.compareTo(BigDecimal.ZERO) > 0 >>>>>>>>>> >>>>>>>>>>>>>>> " + LoanTermScore.compareTo(BigDecimal.ZERO));
        if (LoanTermScore.compareTo(BigDecimal.ZERO) > 0)
        {
            HashMap<BigDecimal, LoanParameterTier> instalmentParam = queryParameterTier(XapiPool.loanDurationParam);

            for (Map.Entry<BigDecimal, LoanParameterTier> params : instalmentParam.entrySet())
            {
                System.err.println(LoanTermScore + "<<<<<<<<<<<<<<<<ggg <<<<<<<<<<<<<<<<<<<< " + params.getValue().getTierCeiling());
                System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< " + LoanTermScore.compareTo(params.getValue().getTierCeiling()));
                if (LoanTermScore.doubleValue() >= params.getValue().getTierFloor().doubleValue() && LoanTermScore.doubleValue() <= params.getValue().getTierCeiling().doubleValue())
                {
                    finalLoanTermScore = params.getValue().getTierValue();
                }
//                if (LoanTermScore.compareTo(params.getValue().getTierCeiling()) >= 0)
//                {
//                    finalLoanTermScore = params.getValue().getTierValue();
//                }
            }

        }
        getBlScoreCard().getBlMainScoreCard().setDurationScore(finalLoanTermScore);
        System.err.println("finalLoanTermScore >>>>>>>>>> >>>>>>>>>>>>>>> " + finalLoanTermScore);
        return finalLoanTermScore;
    }

    public BigDecimal calculateCycleScore(BLAccount bLAccount)
    {
        BigDecimal finalLoanDurationScore = BigDecimal.ZERO;
        //BigDecimal loanDurationScore = queryLoanCycle(bLAccount);
        System.err.println(getBlScoreItems().getCurrentCycle() + "<<< getBlScoreItems().getCurrentCycle()>>>");
        BigDecimal loanDurationScore = new BigDecimal(isBlank(getBlScoreItems().getCurrentCycle()) ? -1 : getBlScoreItems().getCurrentCycle());
        System.err.println("  loanDurationScore >>>>>>>>>> >>>>>>>>>>>>>>> " + loanDurationScore);
        System.err.println("LOAN TERM LoanTermScore.compareTo(BigDecimal.ZERO) > 0 >>>>>>>>>> >>>>>>>>>>>>>>> " + loanDurationScore.compareTo(BigDecimal.ZERO));

        if (loanDurationScore.compareTo(BigDecimal.ZERO) > 0)
        {
            HashMap<BigDecimal, LoanParameterTier> instalmentParam = queryParameterTier(XapiPool.cycleParam);

            for (Map.Entry<BigDecimal, LoanParameterTier> params : instalmentParam.entrySet())
            {
                if (loanDurationScore.doubleValue() >= params.getValue().getTierFloor().doubleValue() && loanDurationScore.doubleValue() <= params.getValue().getTierCeiling().doubleValue())
                {
                    finalLoanDurationScore = params.getValue().getTierValue();
                }
            }

        }
        getBlScoreCard().getBlMainScoreCard().setCycleScore(finalLoanDurationScore);
        return finalLoanDurationScore;
    }

    public BigDecimal calculateWeightedScore(BLAccount bLAccount)
    {/////
        Long[] period1 = new Long[2];
        Long[] period2 = new Long[2];
        Long[] period3 = new Long[2];
        BigDecimal weightedCalculation = BigDecimal.ZERO;
        BigDecimal finalWeightedCalculation = BigDecimal.ZERO;
        HashMap<BigDecimal, LoanParameterTier> weighParam = queryParameterTier(XapiPool.weightScoreParam);

        if (XapiPool.weightedInstCycle1.contains(","))
        {
            period1[0] = Long.parseLong(XapiPool.weightedInstCycle1.split(",")[0]);
            period1[1] = Long.parseLong(XapiPool.weightedInstCycle1.split(",")[1]);
        }
        if (XapiPool.weightedInstCycle2.contains(","))
        {

            period2[0] = Long.parseLong(XapiPool.weightedInstCycle2.split(",")[0]);
            period2[1] = Long.parseLong(XapiPool.weightedInstCycle2.split(",")[1]);
        }
        if (XapiPool.weightedInstCycle3.contains(","))
        {

            period3[0] = Long.parseLong(XapiPool.weightedInstCycle3.split(",")[0]);
            period3[1] = Long.parseLong(XapiPool.weightedInstCycle3.split(",")[1]);
        }

//        BigDecimal noOfInstalments1 = queryWeightedInstalments(bLAccount, true, period1);
//        BigDecimal noOfInstalments2 = queryWeightedInstalments(bLAccount, true, period2);
//        BigDecimal noOfInstalments3 = queryWeightedInstalments(bLAccount, true, period3);
        BigDecimal noOfInstalments1 = new BigDecimal(isBlank(getBlScoreItems().getOverDue1to7Days()) ? -1 : getBlScoreItems().getOverDue1to7Days());
        BigDecimal noOfInstalments2 = new BigDecimal(isBlank(getBlScoreItems().getOverDue8to14Days()) ? -1 : getBlScoreItems().getOverDue8to14Days());
        BigDecimal noOfInstalments3 = new BigDecimal(isBlank(getBlScoreItems().getOverDue15to30Days()) ? -1 : getBlScoreItems().getOverDue15to30Days());

        weightedCalculation = XapiPool.weightedInstCycle1Points.multiply(noOfInstalments1)
                .add((XapiPool.weightedInstCycle2Points.multiply(noOfInstalments2)))
                .add((XapiPool.weightedInstCycle3Points.multiply(noOfInstalments3)));

        System.err.println(XapiPool.weightedInstCycle1Points + " Weighted inst 1. " + noOfInstalments1);
        System.err.println(XapiPool.weightedInstCycle2Points + " Weighted inst 2. " + noOfInstalments2);
        System.err.println(XapiPool.weightedInstCycle3Points + " Weighted inst 3. " + noOfInstalments3);

        for (Map.Entry<BigDecimal, LoanParameterTier> params : weighParam.entrySet())
        {
            if (weightedCalculation.doubleValue() >= params.getValue().getTierFloor().doubleValue() && weightedCalculation.doubleValue() <= params.getValue().getTierCeiling().doubleValue())
            {
                finalWeightedCalculation = params.getValue().getTierValue();
            }
        }
        System.err.println(XapiPool.weightedInstCycle3Points + " finalWeightedCalculation. " + finalWeightedCalculation);
        getBlScoreCard().getBlMainScoreCard().setWeightedScore(finalWeightedCalculation);
        return finalWeightedCalculation;
    }

    public BLAccount queryRegistered(String acctNo, String phoneNo)
    {
        BLAccount bLAccount = new BLAccount();
        System.out.println("select a.value bvn, b.acct_no, b.rim_no,c.cust_service_key as phone_number "
                + "from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services c "
                + "where a.field_id=45 and   (a.acct_no_key = '" + acctNo.substring(0, 3) + "-" + acctNo.substring(3) + "' or value = '" + acctNo.replace("-", "") + "') "
                + "and b.acct_no = a.acct_no_key  and c.rim_no = b.rim_no and c.rim_no in (select rim_no from " + XapiCodes.coreschema + "..rm_acct where class_code in (" + XapiPool.allowedRimClassBorrower + ")) " +
                "and c.cust_service_key = '" + phoneNo + "'  and c.services_id =44 ");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select a.value bvn, b.acct_no,b.acct_type, b.rim_no,c.cust_service_key as phone_number "
                     + "from  " + XapiCodes.coreschema + "..gb_user_defined a,  " + XapiCodes.coreschema + "..dp_acct b, " + XapiCodes.coreschema + "..rm_services  c "
                     + "where a.field_id=45 and   (a.acct_no_key = '" + acctNo.substring(0, 3) + "-" + acctNo.substring(3) + "' or value = '" + acctNo.replace("-", "") + "') "
                     + "and b.acct_no = a.acct_no_key and c.rim_no = b.rim_no  and c.rim_no in (select rim_no from " + XapiCodes.coreschema + "..rm_acct where class_code in (" + XapiPool.allowedRimClassBorrower + ")) " +
                     "and c.cust_service_key = '" + phoneNo + "'  and c.services_id =44 "))
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

    public BLScoreItems selectScoringItems(String acctNo)
    {
        BLScoreItems blScoreItems = new BLScoreItems();
        System.out.println("select isnull(One_and_7_Days_Late,0),isnull(Eight_and_14_Days_Late,0),isnull(Fifteen_and_30_Days_Late,0),isnull(Total_Inst,0),cycle,Residence,Risk_code,RIM_Class_Code,status," +
                "Loan_Product,rim_no,acct_no,isnull(Num_of_Instanta_Loan,0) from " + XapiCodes.coreschema + "..waited_deliquency_check where acct_no ='" + acctNo + "'");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select isnull(One_and_7_Days_Late,0) One_and_7_Days_Late,isnull(Eight_and_14_Days_Late,0) Eight_and_14_Days_Late,isnull(Fifteen_and_30_Days_Late,0) Fifteen_and_30_Days_Late,isnull(Total_Inst,0) Total_Inst,cycle,Residence,Risk_code,RIM_Class_Code,status," +
                     "Loan_Product,rim_no,acct_no,isnull(Num_of_Instanta_Loan,0) Num_of_Instanta_Loan from " + XapiCodes.coreschema + "..waited_deliquency_check where acct_no ='" + acctNo + "'"))
        {

            if (rs.next())
            {
                blScoreItems.setOverDue1to7Days(rs.getLong("One_and_7_Days_Late"));
                blScoreItems.setOverDue8to14Days(rs.getLong("Eight_and_14_Days_Late"));
                blScoreItems.setOverDue15to30Days(rs.getLong("Fifteen_and_30_Days_Late"));
                blScoreItems.setTotalNoOFInstallments(rs.getLong("Total_Inst"));
                blScoreItems.setResidence(rs.getString("Residence"));
                blScoreItems.setCurrentCycle(rs.getLong("cycle"));
                blScoreItems.setRiskGroup(rs.getString("Risk_code"));
                blScoreItems.setRimClass(rs.getLong("RIM_Class_Code"));
                blScoreItems.setStatus(rs.getString("status"));
                blScoreItems.setLoanProductName(rs.getString("Loan_Product"));
                blScoreItems.setRimNo(rs.getLong("rim_no"));
                blScoreItems.setAccountNumber(rs.getString("acct_no"));
                blScoreItems.setNoOfDigitalLoansInTheYear(rs.getLong("Num_of_Instanta_Loan"));

            }


        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }

        blScoreItems = selectScoringItems2(acctNo, blScoreItems);
        getCrCaller().setCall("SCORING ITEMS 1 ", blScoreItems);
        return blScoreItems;
    }

    public BLScoreItems selectScoringItems2(String acctNo, BLScoreItems blScoreItems)
    {

        System.out.println("SELECT isnull(GT_7_Days_Late,0),isnull(GT_30_Days_Late,0) from " + XapiCodes.coreschema + "..Eligibility_Criteria where acct_no ='" + acctNo + "'");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT isnull(GT_7_Days_Late,0) GT_7_Days_Late,isnull(GT_30_Days_Late,0) GT_30_Days_Late  from " + XapiCodes.coreschema + "..Eligibility_Criteria where acct_no ='" + acctNo + "'"))
        {

            if (rs.next())
            {
                blScoreItems.setMoreThan7DaysLate(rs.getLong("GT_7_Days_Late"));
                blScoreItems.setMoreThan30DaysLate(rs.getLong("GT_30_Days_Late"));
            }


        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        blScoreItems = selectScoringItems3(acctNo, blScoreItems);
        getCrCaller().setCall("SCORING ITEMS 2 ", blScoreItems);

        return blScoreItems;
    }

    public BLScoreItems selectScoringItems3(String acctNo, BLScoreItems blScoreItems)
    {

        System.out.println("SELECT  Days_Late_1st_Install,Days_Late_2nd_Install,Days_Late_3RD_Install from " + XapiCodes.coreschema + "..Score_Card_First_to_Third_Inst where acct_no ='" + acctNo + "'");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT  Days_Late_1st_Install,Days_Late_2nd_Install,Days_Late_3RD_Install from " + XapiCodes.coreschema + "..Score_Card_First_to_Third_Inst where acct_no ='" + acctNo + "'"))
        {

            if (rs.next())
            {
                blScoreItems.setDueOnFirstInstallment(rs.getString("Days_Late_1st_Install"));
                blScoreItems.setDueOnSecondInstallment(rs.getString("Days_Late_2nd_Install"));
                blScoreItems.setDueOnSThirdInstallment(rs.getString("Days_Late_3RD_Install"));
            }


        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        getCrCaller().setCall("SCORING ITEMS 3 ", blScoreItems);
        return blScoreItems;
    }

    public HashMap<BigDecimal, LoanParameterTier> queryParameterTier(String code)
    {
        HashMap<BigDecimal, LoanParameterTier> paramTier = new HashMap<>();
        System.out.println("SELECT TIER_CODE,TIER_FLOOR,TIER_CEILING,TIER_VALUE FROM " + XapiCodes.xapiSchema + "..E_LOAN_TIER WHERE TIER_CODE ='" + code + "' ");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT TIER_CODE,TIER_FLOOR,TIER_CEILING,TIER_VALUE FROM " + XapiCodes.xapiSchema + "..E_LOAN_TIER WHERE TIER_CODE ='" + code + "' "))
        {

            while (rs.next())
            {
                LoanParameterTier loanParameterTier = new LoanParameterTier();
                loanParameterTier.setTierCeiling(rs.getBigDecimal("TIER_CEILING"));
                loanParameterTier.setTierFloor(rs.getBigDecimal("TIER_FLOOR"));
                loanParameterTier.setTierValue(rs.getBigDecimal("TIER_VALUE"));
                paramTier.put(loanParameterTier.getTierCeiling(), loanParameterTier);
            }


        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        return paramTier;
    }

    public HashMap<BigDecimal, LoanParameterTier> queryDefinitionScoreTier(String code)
    {
        HashMap<BigDecimal, LoanParameterTier> paramTier = new HashMap<>();
        System.out.println("SELECT TIER_CODE,TIER_FLOOR,TIER_CEILING,TIER_VALUE FROM " + XapiCodes.xapiSchema + "..E_LOAN_DEFINITION_SCORE WHERE TIER_CODE ='" + code + "' ");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT TIER_CODE,TIER_FLOOR,TIER_CEILING,TIER_VALUE FROM " + XapiCodes.xapiSchema + "..E_LOAN_DEFINITION_SCORE WHERE TIER_CODE ='" + code + "' "))
        {

            while (rs.next())
            {
                LoanParameterTier loanParameterTier = new LoanParameterTier();
                loanParameterTier.setTierCeiling(rs.getBigDecimal("TIER_CEILING"));
                loanParameterTier.setTierFloor(rs.getBigDecimal("TIER_FLOOR"));
                loanParameterTier.setTierValueTwo(rs.getString("TIER_VALUE"));
                paramTier.put(loanParameterTier.getTierCeiling(), loanParameterTier);
            }


        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }

        return paramTier;
    }

    public BigDecimal queryFirst3InstalmentLate(BLAccount blAccount)
    {
        BigDecimal noOfInstalments = BigDecimal.ZERO;
        System.out.println("queryFirst3InstalmentLate \n\n select aa.acct_no,datediff(dd,cc.pmt_due_dt,aa.posting_dt_tm) nodays,cc.bill_id_no " +
                "from " + XapiCodes.coreschema + "..ln_history aa," + XapiCodes.coreschema + "..ln_bill_map bb ," + XapiCodes.coreschema + "..ln_bill cc " +
                "where aa.acct_no = bb.acct_no  and aa.acct_type=bb.acct_type and bb.acct_type=cc.acct_type   and aa.acct_type=cc.acct_type and aa.acct_no=cc.acct_no  and bb.acct_no=cc.acct_no and  cc.bill_id_no in (1,2,3) and cc.sub_no =2 " +
                "and bb.bill_id_no = cc.bill_id_no and bb.sub_no =cc.sub_no " +
                "and bb.history_ptid = aa.ptid and aa.acct_no ='" + blAccount.getLoanAccountNumber() + "'");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select aa.acct_no,datediff(dd,cc.pmt_due_dt,aa.posting_dt_tm) nodays,cc.bill_id_no " +
                     "from " + XapiCodes.coreschema + "..ln_history aa," + XapiCodes.coreschema + "..ln_bill_map bb ," + XapiCodes.coreschema + "..ln_bill cc " +
                     "where aa.acct_no = bb.acct_no   and aa.acct_type=bb.acct_type and bb.acct_type=cc.acct_type   and bb.acct_type=aa.acct_type   and aa.acct_type=cc.acct_type  and aa.acct_no=cc.acct_no and bb.acct_no=cc.acct_no and  cc.bill_id_no in (1,2,3) and cc.sub_no =2 " +
                     "and bb.bill_id_no = cc.bill_id_no and bb.sub_no =cc.sub_no " +
                     "and bb.history_ptid = aa.ptid and aa.acct_no ='" + blAccount.getLoanAccountNumber() + "'"))
        {

            if (rs.next())
            {
                noOfInstalments = rs.getBigDecimal("nodays");
            }


        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        return noOfInstalments;
    }

    public String queryResidence(BLAccount blAccount)
    {
        String residenceRisk = "";
        System.out.println("select rp.residence,ra.risk_code,ag.description " +
                "from " + XapiCodes.coreschema + "..rm_personal_info rp," + XapiCodes.coreschema + "..rm_acct ra," + XapiCodes.coreschema + "..ad_gb_risk ag  " +
                "where ra.rim_no = rp.rim_no and ra.risk_code = ag.risk_code and  ra.RIM_NO=" + blAccount.getRimNo() + "");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select rp.residence,ra.risk_code,ag.description " +
                     "from " + XapiCodes.coreschema + "..rm_personal_info rp," + XapiCodes.coreschema + "..rm_acct ra," + XapiCodes.coreschema + "..ad_gb_risk ag  " +
                     "where ra.rim_no = rp.rim_no and ra.risk_code = ag.risk_code and  ra.RIM_NO=" + blAccount.getRimNo() + ""))
        {

            if (rs.next())
            {
                residenceRisk = rs.getString("residence");

            }
        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        return residenceRisk;
    }

    public String queryRisk(BLAccount blAccount)
    {
        String residenceRisk = "";
        System.out.println("select rp.residence,ra.risk_code,ag.description " +
                "from " + XapiCodes.coreschema + "..rm_personal_info rp," + XapiCodes.coreschema + "..rm_acct ra," + XapiCodes.coreschema + "..ad_gb_risk ag  " +
                "where ra.rim_no = rp.rim_no and ra.risk_code = ag.risk_code and  ra.RIM_NO=" + blAccount.getRimNo() + "");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select rp.residence,ra.risk_code,ag.description " +
                     "from " + XapiCodes.coreschema + "..rm_personal_info rp," + XapiCodes.coreschema + "..rm_acct ra," + XapiCodes.coreschema + "..ad_gb_risk ag  " +
                     "where ra.rim_no = rp.rim_no and ra.risk_code = ag.risk_code and  ra.RIM_NO=" + blAccount.getRimNo() + ""))
        {

            if (rs.next())
            {
                residenceRisk = rs.getString("description");

            }
        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>> residenceRisk >>>>> " + residenceRisk);
        return residenceRisk;
    }

    public BigDecimal queryLoanTerm(BLAccount blAccount)
    {
        BigDecimal loanTerm = BigDecimal.ZERO;
        System.out.println("Select period,trm from " + XapiCodes.coreschema + "..ln_display ld  where acct_no ='" + blAccount.getLoanAccountNumber() + "'");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("Select period,trm from " + XapiCodes.coreschema + "..ln_display ld  where acct_no ='" + blAccount.getLoanAccountNumber() + "'"))
        {

            if (rs.next())
            {
                loanTerm = rs.getBigDecimal("trm");
            }


        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        return loanTerm;
    }

    public BigDecimal queryLoanCycle(BLAccount blAccount)
    {
        BigDecimal loanTerm = BigDecimal.ZERO;
        System.out.println("Select count(*) noOfLoans from " + XapiCodes.coreschema + "..ln_display ld  where rim_no =" + blAccount.getRimNo() + " /*and class_code in (" + XapiPool.allowedBorrowerLnClass + ")*/");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("Select count(*) noOfLoans from " + XapiCodes.coreschema + "..ln_display ld  where rim_no =" + blAccount.getRimNo() + ""))
        {
            if (rs.next())
            {
                loanTerm = rs.getBigDecimal("noOfLoans");
            }


        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        System.err.println("<><><><><><> " + loanTerm);
        return loanTerm;
    }

    public LoanRequest queryLargestDisbursement(Long rimNo)
    {
        LoanRequest accountDetail = new LoanRequest(); // and status ='Active'
        System.out.println("select acct_no,ld.acct_type,amt_financed,trm,period from " + XapiCodes.coreschema + "..ln_display ld where RIM_NO=" + rimNo + "  " +
                "and amt_financed =(select max(amt_financed) from " + XapiCodes.coreschema + "..ln_display where RIM_NO=ld.rim_no   and status ='Active')   and status ='Active' and class_code in (501,502,503,531,541,591,511,521)");

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select acct_no,ld.acct_type,amt_financed,trm,period from " + XapiCodes.coreschema + "..ln_display ld where RIM_NO=" + rimNo + "  " +
                     "and amt_financed =(select max(amt_financed) from " + XapiCodes.coreschema + "..ln_display where RIM_NO=ld.rim_no   and status ='Active')   and status ='Active' and class_code in (501,502,503,531,541,591,511,521)"))
        {
            if (rs.next())
            {
                accountDetail.setAccountNo(rs.getString("acct_no"));
                accountDetail.setAccountType(rs.getString("acct_type"));
                accountDetail.setLargestDisbursement(rs.getBigDecimal("amt_financed"));
                accountDetail.setTerm(rs.getLong("trm"));
                accountDetail.setPeriod(rs.getString("period"));
            }
            else
            {
                accountDetail.setAccountNo("XoX");
                additionalResponseText = "[No Loan account Found]";
            }


        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        return accountDetail;
    }

    public boolean checkCycle(BLAccount bLAccount)
    {
        return checkIfExists("checkCycle", "select count(*) from " + XapiCodes.coreschema + "..ln_display where RIM_NO=" + bLAccount.getRimNo() + " and class_code  in (" + XapiPool.allowedBorrowerLnClass + "," + XapiPool.allowedLnClass + ")");
    }

    public boolean hasPreviousDigitalLoan(Long rimNo)
    {
        return checkIfExists("hasPreviousDigitalLoan", "select acct_no from " + XapiCodes.coreschema + "..ln_display where RIM_NO=" + rimNo + " and class_code  in (" + XapiPool.allowedBorrowerLnClass + "," + XapiPool.allowedLnClass + ")");
    }

    public boolean hasLatePaymentsDaysCount(BLAccount bLAccount, Long noDayslate, Long lateInstallmentCount)
    {
        return checkIfExists("has" + lateInstallmentCount + "DefaultedInstallement for " + noDayslate + " Days", "select count(*), gh.acct_no from (" +
                "select aa.acct_no,aa.amt,aa.posting_dt_tm,aa.create_dt,cc.pmt_due_dt,cc.type, datediff(dd,cc.pmt_due_dt,aa.posting_dt_tm) nodays " +
                "from " + XapiCodes.coreschema + "..ln_history aa," + XapiCodes.coreschema + "..ln_bill_map bb ," + XapiCodes.coreschema + "..ln_bill cc," + XapiCodes.coreschema + "..ln_acct la " +
                "where aa.acct_no = bb.acct_no  and bb.acct_type=aa.acct_type  and bb.acct_type=cc.acct_type  and aa.acct_type=cc.acct_type  and aa.acct_no=cc.acct_no and cc.status = 'Unsatisfied' and bb.acct_no=cc.acct_no and bb.acct_no=la.acct_no and cc.acct_no=la.acct_no and aa.acct_no=la.acct_no " +
                "and bb.bill_id_no = cc.bill_id_no and bb.sub_no =cc.sub_no " +
                "and bb.history_ptid = aa.ptid and la.rim_no=" + bLAccount.getRimNo() + " " +
                ") gh where gh.nodays > " + noDayslate + "   group by gh.acct_no having count(*)>=" + lateInstallmentCount + "");
    }

    public boolean hasCurrentDelayedPayment(BLAccount bLAccount)
    {
        return checkIfExists("hasCurrentDelayedPayment", "select aa.acct_no,datediff(dd,cc.pmt_due_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) nodays,cc.bill_id_no,aa.posting_dt_tm " +
                "from " + XapiCodes.coreschema + "..ln_history aa," + XapiCodes.coreschema + "..ln_bill_map bb ," + XapiCodes.coreschema + "..ln_bill cc " +
                "where aa.acct_no = bb.acct_no and aa.acct_no=cc.acct_no and bb.acct_no=cc.acct_no and bb.acct_type=aa.acct_type  and aa.acct_type=cc.acct_type  and bb.acct_type=cc.acct_type and status ='Unsatisfied' " +
                "and bb.bill_id_no = cc.bill_id_no and bb.sub_no =cc.sub_no " +
                "and bb.history_ptid = aa.ptid and aa.acct_no ='" + bLAccount.getLoanAccountNumber() + "'");
    }

    public boolean hasMinReqInstalment(BLAccount bLAccount, Long minInstalments)
    {
        return checkIfExists("hasMinReqInstalment", "select count(*) from " + XapiCodes.coreschema + "..ln_display ld, " + XapiCodes.coreschema + "..ln_bill lb where ld.acct_no =lb.acct_no and ld.acct_type =lb.acct_type and lb.type = 'Principal' and ld.rim_no =" + bLAccount.getRimNo() + "  having count(*) >=" + minInstalments + "");
    }

    public boolean loanRestructured(BLAccount bLAccount)
    {
        return checkIfExists("loanRestructured", "select  la.opening_reason_id  from " + XapiCodes.coreschema + "..ln_acct  la, " + XapiCodes.coreschema + "..ad_gb_reason ar " +
                "where ar.reason_id = la.opening_reason_id and la.opening_reason_id is not null and ar.reason_id =90  and la.acct_no ='" + bLAccount.getLoanAccountNumber() + "' ");
    }

    public boolean satisfiesLoanClosurePeriod(BLAccount bLAccount, Long previousLoanClosureDays)
    {
        boolean checkIfHasActiveLoan = checkIfExists("checkActiveLoanForClosureDays", "" +
                "Select acct_no from  " + XapiCodes.coreschema + "..ln_display where rim_no =" + bLAccount.getRimNo() + " and status ='Active'");
        if (checkIfHasActiveLoan)
        {
            return true;
        }
        else
        {
            return checkIfExists("hassatisfiesLoanClosurePeriod", "select rim_no,* from " + XapiCodes.coreschema + "..ln_display ld where status ='Closed' and RIM_NO=" + bLAccount.getRimNo() + " " +
                    "and ptid = (select max(ptid) from " + XapiCodes.coreschema + "..ln_display where status ='Closed' and RIM_NO=ld.rim_no) " +
                    // " and not exists (select acct_no from " + XapiCodes.coreschema + "..ln_display where status not in ('Closed','Incomplete') and rim_no =ld.rim_no) " +
                    "and datediff(mm, closed_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) <=" + previousLoanClosureDays + "");
        }
    }

    public boolean hasMaxLoansPerYear(BLAccount bLAccount, Long allowedNoOfLoansPerYear)
    {
        return checkIfExists("hasMaxLoansPerYear", "select count(*) from " + XapiCodes.coreschema + "..ln_display ld  where ld.rim_no =" + bLAccount.getRimNo() + "  " +
                "and datepart(year,create_dt) = datepart(year,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) having count(*)>" + allowedNoOfLoansPerYear + "");
    }

    public boolean hasDelayedCurrentInstallment(BLAccount bLAccount)
    {
        return checkIfExists("hasDelayedCurrentInstallment", "select nodays from (select sum(amt) amt,bill_id_no,datediff(dd,pmt_due_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) nodays " +
                " from " + XapiCodes.coreschema + "..ln_bill where acct_no ='" + bLAccount.getLoanAccountNumber() + "'  and status ='Unsatisfied' " +
                "and pmt_due_dt >= (select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control) and  month(pmt_due_dt) = month((select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) " +
                "group by bill_id_no ,pmt_due_dt) dc where nodays>0");
    }

    public boolean isStateEligible(BLAccount bLAccount)
    {
        return checkIfExists("isBranchEligible", "select rm.rim_no from " + XapiCodes.coreschema + "..rm_address rm  " +
                " where    rim_no=" + bLAccount.getRimNo() + "  and rm.branch in (" + XapiPool.allowedStates + ")");

//        return checkIfExists("isStateEligible", "select rm.state from " + XapiCodes.coreschema + "..rm_address rm," + XapiCodes.coreschema + "..ad_gb_state pc " +
//                " where  pc.description = rm.state and rim_no=" + bLAccount.getRimNo() + " and addr_type_id =1 " +
//                "and pc.state  in " + formatString(XapiPool.allowedStates) + "");
    }

    public String formatString(String unformattedString)
    {

        String xx = unformattedString.replace("(", "").replace(")", "").trim();
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        StringTokenizer nn = new StringTokenizer(xx, ",");
        while (nn.hasMoreTokens())
        {
            builder.append("'").append(nn.nextElement()).append("',");
        }
        builder.append("'0S')");
        System.err.println("S.... " + builder.toString());
        return builder.toString();
    }

    public BigDecimal queryWeightedInstalments(BLAccount bLAccount, boolean applyCriteria, Long params[])
    {
        String condition = "";
        BigDecimal noOfInstallments = BigDecimal.ZERO;
        if (applyCriteria)
        {
            condition = "having count(*) between " + params[0] + " and " + params[1] + "";
        }

        System.out.println("select count(*) as lateInstallments from (select sum(amt) amt,bill_id_no from " + XapiCodes.coreschema + "..ln_bill where acct_no ='" + bLAccount.getLoanAccountNumber() + "'  " +
                "and status ='Unsatisfied' and pmt_due_dt <= (select  dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)   group by bill_id_no) WL " + condition + "");
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select count(*) as lateInstallments from (select sum(amt) amt,bill_id_no from " + XapiCodes.coreschema + "..ln_bill where acct_no ='" + bLAccount.getLoanAccountNumber() + "'  " +
                     "and status ='Unsatisfied' and pmt_due_dt <= (select  dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)  group by bill_id_no) WL " + condition + ""))
        {
            while (rs.next())
            {
                noOfInstallments = rs.getBigDecimal("lateInstallments");
            }

        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        System.err.println("no of installments >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + noOfInstallments);
        return noOfInstallments;
    }

    public boolean checkEligibleClasses(BLAccount bLAccount)
    {
        return checkIfExists("checkEligibleClasses", "select dd.acct_no  from  " + XapiCodes.coreschema + "..dp_display dd,  " + XapiCodes.coreschema + "..rm_acct rm where dd.rim_no=rm.rim_no and dd.status='Active' and rm.status='Active' "
                + " and rm.class_code in (" + XapiPool.allowedRimClassDepositor + " ) and dd.class_code in (" + XapiPool.allowedBorrowerDpClass + ") "
                + "   and rm.rim_no=" + bLAccount.getRimNo() + " ");
    }

    public BigDecimal instalmentAverageForPeriod(BLAccount blAccount, Long noOfMonths)
    {
        BigDecimal calculatedAverage = BigDecimal.ZERO;
        System.out.println("instalmentAverageForPeriod \n select cast(SUM(instalment) as numeric (16,2)) SumInstalment,COUNT(monthNo) noOfMonths, cast(ROUND(SUM(instalment)/COUNT(monthNo),2) as numeric (16,2)) Average  "
                + "from (      select month_value,isnull(amt,0) instalment,rim_no,pmt_due_dt,isnull(monthNo,0) monthNo,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control) today "
                + "from xapi..E_Loan_Calender_Day lcd, (select sum(aa.amt) amt,la.rim_no,aa.pmt_due_dt, datediff(mm,aa.pmt_due_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) monthNo "
                + "from " + XapiCodes.coreschema + "..ln_bill aa, " + XapiCodes.coreschema + "..ln_acct la  where aa.acct_no = la.acct_no and aa.acct_type = la.acct_type and la.rim_no=" + blAccount.getRimNo() + " and la.acct_no ='" + blAccount.getLoanAccountNumber() + "' and aa.type in ('Principal','Interest') group by la.rim_no,aa.pmt_due_dt ) bb "
                + "where lcd.month_value *= bb.monthNo and lcd.month_value <= " + noOfMonths + ") IA ");
//        ResultSet rs = statement.executeQuery("select cast(SUM(instalment) as numeric (16,2)) SumInstalment,COUNT(monthNo) noOfMonths, cast(ROUND(SUM(instalment)/COUNT(monthNo),2) as numeric (16,2)) Average  from ( " +
//                "select month_value,isnull(amt,0) instalment,rim_no,pmt_due_dt,isnull(monthNo,0) monthNo " +
//                "from " + XapiCodes.xapiSchema + "..E_Loan_Calender_Day lcd, (select sum(aa.amt) amt,la.rim_no,aa.pmt_due_dt, datediff(mm,aa.pmt_due_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) monthNo " +
//                "from " + XapiCodes.coreschema + "..ln_bill aa, " + XapiCodes.coreschema + "..ln_acct la  where aa.acct_no = la.acct_no  and la.rim_no=" + blAccount.getRimNo() + " and la.acct_no ='" + blAccount.getLoanAccountType() + "' and aa.type in ('Principal','Interest') " +
//                "group by la.rim_no,aa.pmt_due_dt ) bb where lcd.month_value *= bb.monthNo and lcd.month_value <= " + noOfMonths + ") IA"))
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select cast(SUM(instalment) as numeric (16,2)) SumInstalment,COUNT(monthNo) noOfMonths, cast(ROUND(SUM(instalment)/COUNT(monthNo),2) as numeric (16,2)) Average  "
                     + "from (      select month_value,isnull(amt,0) instalment,rim_no,pmt_due_dt,isnull(monthNo,0) monthNo,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control) today "
                     + "from xapi..E_Loan_Calender_Day lcd, (select sum(aa.amt) amt,la.rim_no,aa.pmt_due_dt, datediff(mm,aa.pmt_due_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) monthNo "
                     + "from " + XapiCodes.coreschema + "..ln_bill aa, " + XapiCodes.coreschema + "..ln_acct la  where aa.acct_no = la.acct_no   and aa.acct_type = la.acct_type and la.rim_no=" + blAccount.getRimNo() + " and la.acct_no ='" + blAccount.getLoanAccountNumber() + "' and aa.type in ('Principal','Interest') group by la.rim_no,aa.pmt_due_dt ) bb "
                     + "where lcd.month_value *= bb.monthNo and lcd.month_value <= " + noOfMonths + ") IA "))
        {
            while (rs.next())
            {
                calculatedAverage = rs.getBigDecimal("Average");
            }

        } catch (Exception e1)
        {
            ApiLogger.getLogger().error(e1);
        }
        getBlScoreCard().getBlMainScoreCard().setAverageMonthlyScore(calculatedAverage);
        return calculatedAverage;
    }


    public ArrayList<BLScoreParameter> getTxnForPeriod(BLAccount blAccount, int noOfMonths)
    {
        ArrayList<BLScoreParameter> blScoreParameters = new ArrayList<>();
        System.out.println("select distinct sd.Txmonth,Txyear,sd.diff,sd.acct_no,noTxn  "
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

    public BigDecimal checkRepaymentScore(Long rimNo)
    {
        BigDecimal score = BigDecimal.ZERO;
        boolean hasPreviousDigitalLoan = hasPreviousDigitalLoan(rimNo);
        System.out.println("has previosu digital Loan<<<<<<<<<<<<<<<<<<<   " + hasPreviousDigitalLoan);
        if (hasPreviousDigitalLoan)
        {

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
        }
        else
        {
            score = BigDecimal.ONE; //replace with variables
        }
        System.out.println("score repayment score " + score);
        getBlScoreCard().getBlMainScoreCard().setRepaymentScore(score);
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

        boolean verified = checkIfExists("IsRepaymentScoreVerified", "select * from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc where bb.acct_no=cc.acct_no " +
                "and cc.acct_no = (select max(acct_no) from " + XapiCodes.coreschema + "..ln_acct bb where rim_no = " + rimNo + " and status='Closed' and bb.class_code in (" + XapiPool.borrowerLoanClassCode + "," + XapiPool.borrowerLoanClassCode + ")) and tran_code in (345) " + criteria + " ");
        return verified;
    }

    public boolean lastLoanRepaidTimely(Long rimNo)
    {
        return checkIfExists("lastLoanRepaidTimely", "select * from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc " +
                " where bb.acct_no=cc.acct_no and cc.acct_no = (select max(acct_no) from " + XapiCodes.coreschema + "..ln_acct bb where rim_no = " + rimNo + " and status='Closed') and tran_code in (345) " +
                "and bb.effective_dt<=cc.mat_dt");

    }

    public boolean hasExistingdigitalLoan(Long rimNo)
    {
        return checkIfExists("hasExistingdigitalLoan", "select * from " + XapiCodes.coreschema + "..ln_history bb," + XapiCodes.coreschema + "..ln_display cc " +
                " where bb.acct_no=cc.acct_no  and bb.acct_type = cc.acct_type and cc.acct_no = (select max(acct_no) from " + XapiCodes.coreschema + "..ln_acct bb where rim_no = " + rimNo + " and status='Closed') and tran_code in (345) " +
                "and bb.effective_dt<=cc.mat_dt");

    }

    public BigDecimal checkAveVolumeScore(Long RimNo, int NoOfMonths)
    {
        BigDecimal volumePercentage = XapiPool.dpAveVolPercentage.divide(BigDecimal.valueOf(100), 3, RoundingMode.DOWN);//parameter
        System.out.println(">>>>> volumePercentage" + volumePercentage);
        BigDecimal volumeAverage = BigDecimal.ZERO;
        BigDecimal finalScore;
        System.out.println("select count(*) noTxn ,bb.acct_no,cast(sum(bb.amt) as numeric(16,2)) totalAmt,cast(round(sum(bb.amt)/count(*),2) as numeric(16,2)) as average  " +
                "from " + XapiCodes.coreschema + "..dp_display aa, " + XapiCodes.coreschema + "..dp_history bb where aa.acct_no =bb.acct_no and bb.acct_type = aa.acct_type and  rim_no = " + RimNo + " and status='Active' and aa.class_code in (" + XapiPool.allowedDpClass + ") " +
                "and datediff(dd,bb.create_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) between 0 and " + NoOfMonths + " and bb.tran_code in (" + XapiPool.dpTranCode + ") " +
                "group by  bb.acct_no");
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select count(*) noTxn ,bb.acct_no,cast(sum(bb.amt) as numeric(16,2)) totalAmt,cast(round(sum(bb.amt)/count(*),2) as numeric(16,2)) as average  " +
                     "from " + XapiCodes.coreschema + "..dp_display aa, " + XapiCodes.coreschema + "..dp_history bb where aa.acct_no =bb.acct_no and bb.acct_type = aa.acct_type and  rim_no = " + RimNo + " and status='Active' and aa.class_code in (" + XapiPool.allowedDpClass + ") " +
                     "and datediff(dd,bb.create_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) between 0 and " + NoOfMonths + " and bb.tran_code in (" + XapiPool.dpTranCode + ") " +
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
        System.out.println("Average Volume " + volumeAverage);
        System.out.println("Average Volume " + volumePercentage);
        finalScore = volumeAverage.divide(volumePercentage);
        System.out.println("Average finalScore  " + finalScore);
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
            System.out.println("evaluate for period " + i);
            periodMap.put(i, verifyDepositsPerPeriod(i, rimNo));
            i += 2;
        }
        for (Map.Entry<Integer, Boolean> entry : periodMap.entrySet())
        {
            Integer key = entry.getKey();
            boolean value = entry.getValue();
            System.out.println("month Period= " + key + "No of Txn for Period " + value);

            if (key == minPeriod && value)
            {
                score3 = XapiPool.dp3monthScore; //change to reflect value on settings...
                System.out.println("Score= " + key + " Score is " + score3);
            }
            else if (key == midPeriod && value)
            {
                score6 = XapiPool.dp6monthScore;
                System.out.println("Score= " + key + " Score is " + score6);
            }
            else if (key == maxPeriod && value)
            {
                score9 = XapiPool.dp9monthScore;
                System.out.println("Score= " + key + " Score is " + score9);
            }
            else
            {
                score3 = BigDecimal.ZERO;
                System.out.println("Score= " + key + " Score is " + score3);
            }

        }
        System.out.println();
        if (score3.compareTo(BigDecimal.ZERO) <= 0) //check if the customer has deposit for at least 3 months
        {
            finalScore = BigDecimal.ZERO;
        }
        else
        {
            finalScore = score3.max(score6.max(score9));
        }
        System.out.println("Final Score== " + finalScore);
        return finalScore;
    }

    public boolean verifyDepositsPerPeriod(int period, Long rimNo)
    {
        boolean isVerified = true;
        HashMap<Integer, Long> txnCountMap = new HashMap<>();
        System.out.println("select count(*) noTxn, month(aa.create_dt) Txmonth  from " + XapiCodes.coreschema + "..dp_history aa, " + XapiCodes.coreschema + "..dp_display bb " +
                "where aa.acct_no = bb.acct_no and bb.acct_type = aa.acct_type and bb.rim_no =" + rimNo + " and aa.tran_code in (" + XapiPool.dpTranCode + ")  " +
                "and datediff(MM,aa.create_dt,(select dateadd(dd,1,last_to_dt) from " + XapiCodes.coreschema + "..ov_control)) between 0 and " + period + " group by aa.create_dt");
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select count(*) noTxn, month(aa.create_dt) Txmonth  from " + XapiCodes.coreschema + "..dp_history aa, " + XapiCodes.coreschema + "..dp_display bb " +
                     "where aa.acct_no = bb.acct_no and bb.acct_type = aa.acct_type and bb.rim_no =" + rimNo + " and aa.tran_code in (" + XapiPool.dpTranCode + ")  " +
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
            System.out.println("month Period= " + key + "No of Txn for Period " + value);

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

    public StringBuilder getBuilder()
    {
        return builder;
    }

    public void setBuilder(StringBuilder builder)
    {
        this.builder = builder;
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

    public BLScoreItems getBlScoreItems()
    {
        return blScoreItems;
    }

    public void setBlScoreItems(BLScoreItems blScoreItems)
    {
        this.blScoreItems = blScoreItems;
    }
}
