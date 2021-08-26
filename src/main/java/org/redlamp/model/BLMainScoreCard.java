package org.redlamp.model;

import java.math.BigDecimal;

public class BLMainScoreCard
{

    private String accountNumber;
    private String accountType;
    private String PhoneNumber;
    private String loanScoreTpe;
    private BigDecimal weightedScore = BigDecimal.ZERO;
    private BigDecimal historyScore = BigDecimal.ZERO;
    private BigDecimal default3monthScore = BigDecimal.ZERO;
    private BigDecimal durationScore = BigDecimal.ZERO;
    private BigDecimal cycleScore = BigDecimal.ZERO;
    private BigDecimal residenceScore = BigDecimal.ZERO;
    private BigDecimal riskScore = BigDecimal.ZERO;
    private BigDecimal definitionScore = BigDecimal.ZERO;
    private String definitionValueScore;
    private BigDecimal variable1Score = BigDecimal.ZERO;
    private BigDecimal averageMonthlyScore = BigDecimal.ZERO;
    private BigDecimal repaymentScore = BigDecimal.ZERO;
    private BigDecimal approvedScore = BigDecimal.ZERO;

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

    public String getAccountNumber()
    {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber)
    {
        this.accountNumber = accountNumber;
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

    public String getLoanScoreTpe()
    {
        return loanScoreTpe;
    }

    public void setLoanScoreTpe(String loanScoreTpe)
    {
        this.loanScoreTpe = loanScoreTpe;
    }

    public BigDecimal getWeightedScore()
    {
        return weightedScore;
    }

    public void setWeightedScore(BigDecimal weightedScore)
    {
        this.weightedScore = weightedScore;
    }

    public BigDecimal getDefault3monthScore()
    {
        return default3monthScore;
    }

    public void setDefault3monthScore(BigDecimal default3monthScore)
    {
        this.default3monthScore = default3monthScore;
    }

    public BigDecimal getDurationScore()
    {
        return durationScore;
    }

    public void setDurationScore(BigDecimal durationScore)
    {
        this.durationScore = durationScore;
    }

    public BigDecimal getCycleScore()
    {
        return cycleScore;
    }

    public void setCycleScore(BigDecimal cycleScore)
    {
        this.cycleScore = cycleScore;
    }

    public BigDecimal getResidenceScore()
    {
        return residenceScore;
    }

    public void setResidenceScore(BigDecimal residenceScore)
    {
        this.residenceScore = residenceScore;
    }

    public BigDecimal getRiskScore()
    {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore)
    {
        this.riskScore = riskScore;
    }

    public BigDecimal getDefinitionScore()
    {
        return definitionScore;
    }

    public void setDefinitionScore(BigDecimal definitionScore)
    {
        this.definitionScore = definitionScore;
    }

    public String getDefinitionValueScore()
    {
        return definitionValueScore;
    }

    public void setDefinitionValueScore(String definitionValueScore)
    {
        this.definitionValueScore = definitionValueScore;
    }

    public BigDecimal getVariable1Score()
    {
        return variable1Score;
    }

    public void setVariable1Score(BigDecimal variable1Score)
    {
        this.variable1Score = variable1Score;
    }

    public BigDecimal getAverageMonthlyScore()
    {
        return averageMonthlyScore;
    }

    public void setAverageMonthlyScore(BigDecimal averageMonthlyScore)
    {
        this.averageMonthlyScore = averageMonthlyScore;
    }

    public BigDecimal getRepaymentScore()
    {
        return repaymentScore;
    }

    public void setRepaymentScore(BigDecimal repaymentScore)
    {
        this.repaymentScore = repaymentScore;
    }
}
