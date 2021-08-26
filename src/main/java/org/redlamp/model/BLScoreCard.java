package org.redlamp.model;

import org.redlamp.util.XapiPool;

import java.math.BigDecimal;

public class BLScoreCard
{

    private String accountNumber;
    private String accountWithHighestDisb;
    private String accountType;
    private String PhoneNumber;
    private Long rimNumber;
    private String loanScoreTpe;
    private String scoreCategory;
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal cycleScore = BigDecimal.ZERO;
    private BigDecimal depositorScore = BigDecimal.ZERO;
    private BigDecimal averageVlmScore = BigDecimal.ZERO;
    private BigDecimal RepaymentScore = BigDecimal.ZERO;
    private BigDecimal finalScore1 = BigDecimal.ZERO;
    private BigDecimal finalScoreAvg = BigDecimal.ZERO;
    private BigDecimal overallScore = BigDecimal.ZERO;
    private BigDecimal historyScore = BigDecimal.ZERO;
    private BigDecimal approvedScore = BigDecimal.ZERO;
    private BigDecimal lateInstalmentScore = BigDecimal.ZERO;
    private BigDecimal scorePoints = BigDecimal.ZERO;
    private boolean borrower;
    private boolean classCodesAllowed;
    private boolean prevLoanRepaidTimely;
    private boolean voluntaryDepositor;
    private boolean hasValidCycle;
    private boolean hasRequiredMinInstalments;
    private boolean isRestructured;
    private boolean hasClosureMonths;
    private boolean hasPreviousLoanDefaulted7days;
    private boolean hasPreviousLoanDefaulted30days;
    private boolean hasCurrentDelayedPayment;
    private boolean hasMaxLoansPerYear;
    private boolean hasDelayedCurrentInstallment;
    private BLMainScoreCard blMainScoreCard = new BLMainScoreCard();

    public Long getRimNumber()
    {
        return rimNumber;
    }

    public BigDecimal getScorePoints()
    {
        return scorePoints;
    }

    public void setScorePoints(BigDecimal scorePoints)
    {
        this.scorePoints = scorePoints;
    }

    public String getScoreCategory()
    {
        return scoreCategory;
    }

    public void setScoreCategory(String scoreCategory)
    {
        this.scoreCategory = scoreCategory;
    }

    public void setRimNumber(Long rimNumber)
    {
        this.rimNumber = rimNumber;
    }

    public String getAccountWithHighestDisb()
    {
        return accountWithHighestDisb;
    }

    public void setAccountWithHighestDisb(String accountWithHighestDisb)
    {
        this.accountWithHighestDisb = accountWithHighestDisb;
    }

    public String getAccountType()
    {
        return accountType;
    }

    public void setAccountType(String accountType)
    {
        this.accountType = accountType;
    }

    public String getPhoneNumber()
    {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        PhoneNumber = phoneNumber;
    }

    public BigDecimal getLateInstalmentScore()
    {
        return lateInstalmentScore;
    }

    public void setLateInstalmentScore(BigDecimal lateInstalmentScore)
    {
        this.lateInstalmentScore = lateInstalmentScore;
    }

    public BigDecimal getApprovedScore()
    {
        return approvedScore;
    }

    public void setApprovedScore(BigDecimal approvedScore)
    {
        this.approvedScore = approvedScore;
    }

    public BigDecimal getHistoryScore()
    {
        return historyScore;
    }

    public void setHistoryScore(BigDecimal historyScore)
    {
        this.historyScore = historyScore;
    }

    public String getLoanScoreTpe()
    {
        return loanScoreTpe;
    }

    public void setLoanScoreTpe(String loanScoreTpe)
    {
        this.loanScoreTpe = loanScoreTpe;
    }

    public String getAccountNumber()
    {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber)
    {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getCycleScore()
    {
        return cycleScore;
    }

    public void setCycleScore(BigDecimal cycleScore)
    {
        this.cycleScore = cycleScore;
    }

    public BigDecimal getDepositorScore()
    {
        return depositorScore;
    }

    public void setDepositorScore(BigDecimal depositorScore)
    {
        this.depositorScore = depositorScore;
    }

    public BigDecimal getAverageVlmScore()
    {
        return averageVlmScore;
    }

    public void setAverageVlmScore(BigDecimal averageVlmScore)
    {
        this.averageVlmScore = averageVlmScore;
    }

    public BigDecimal getRepaymentScore()
    {
        return RepaymentScore;
    }

    public void setRepaymentScore(BigDecimal repaymentScore)
    {
        RepaymentScore = repaymentScore;
    }

    public BigDecimal getFinalScore1()
    {
        return finalScore1;
    }

    public void setFinalScore1(BigDecimal finalScore1)
    {
        this.finalScore1 = finalScore1;
    }

    public BigDecimal getFinalScoreAvg()
    {
        return finalScoreAvg;
    }

    public void setFinalScoreAvg(BigDecimal finalScoreAvg)
    {
        this.finalScoreAvg = finalScoreAvg;
    }

    public BigDecimal getOverallScore()
    {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore)
    {
        this.overallScore = overallScore;
    }

    public boolean isBorrower()
    {
        return borrower;
    }

    public void setBorrower(boolean borrower)
    {
        this.borrower = borrower;
    }

    public boolean isClassCodesAllowed()
    {
        return classCodesAllowed;
    }

    public void setClassCodesAllowed(boolean classCodesAllowed)
    {
        this.classCodesAllowed = classCodesAllowed;
    }

    public boolean isPrevLoanRepaidTimely()
    {
        return prevLoanRepaidTimely;
    }

    public void setPrevLoanRepaidTimely(boolean prevLoanRepaidTimely)
    {
        this.prevLoanRepaidTimely = prevLoanRepaidTimely;
    }

    public boolean isVoluntaryDepositor()
    {
        return voluntaryDepositor;
    }

    public void setVoluntaryDepositor(boolean voluntaryDepositor)
    {
        this.voluntaryDepositor = voluntaryDepositor;
    }

    public boolean isHasValidCycle()
    {
        return hasValidCycle;
    }

    public void setHasValidCycle(boolean hasValidCycle)
    {
        this.hasValidCycle = hasValidCycle;
    }

    public boolean isHasRequiredMinInstalments()
    {
        return hasRequiredMinInstalments;
    }

    public void setHasRequiredMinInstalments(boolean hasRequiredMinInstalments)
    {
        this.hasRequiredMinInstalments = hasRequiredMinInstalments;
    }

    public boolean isRestructured()
    {
        return isRestructured;
    }

    public void setRestructured(boolean restructured)
    {
        isRestructured = restructured;
    }

    public boolean isHasClosureMonths()
    {
        return hasClosureMonths;
    }

    public void setHasClosureMonths(boolean hasClosureMonths)
    {
        this.hasClosureMonths = hasClosureMonths;
    }

    public boolean isHasPreviousLoanDefaulted7days()
    {
        return hasPreviousLoanDefaulted7days;
    }

    public void setHasPreviousLoanDefaulted7days(boolean hasPreviousLoanDefaulted7days)
    {
        this.hasPreviousLoanDefaulted7days = hasPreviousLoanDefaulted7days;
    }

    public boolean isHasPreviousLoanDefaulted30days()
    {
        return hasPreviousLoanDefaulted30days;
    }

    public void setHasPreviousLoanDefaulted30days(boolean hasPreviousLoanDefaulted30days)
    {
        this.hasPreviousLoanDefaulted30days = hasPreviousLoanDefaulted30days;
    }

    public boolean isHasCurrentDelayedPayment()
    {
        return hasCurrentDelayedPayment;
    }

    public void setHasCurrentDelayedPayment(boolean hasCurrentDelayedPayment)
    {
        this.hasCurrentDelayedPayment = hasCurrentDelayedPayment;
    }

    public boolean isHasMaxLoansPerYear()
    {
        return hasMaxLoansPerYear;
    }

    public void setHasMaxLoansPerYear(boolean hasMaxLoansPerYear)
    {
        this.hasMaxLoansPerYear = hasMaxLoansPerYear;
    }

    public boolean isHasDelayedCurrentInstallment()
    {
        return hasDelayedCurrentInstallment;
    }

    public void setHasDelayedCurrentInstallment(boolean hasDelayedCurrentInstallment)
    {
        this.hasDelayedCurrentInstallment = hasDelayedCurrentInstallment;
    }

    public BLMainScoreCard getBlMainScoreCard()
    {
        return blMainScoreCard;
    }

    public void setBlMainScoreCard(BLMainScoreCard blMainScoreCard)
    {
        this.blMainScoreCard = blMainScoreCard;
    }
}
