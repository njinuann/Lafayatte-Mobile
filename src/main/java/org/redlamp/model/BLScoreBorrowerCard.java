package org.redlamp.model;

import java.math.BigDecimal;

public class BLScoreBorrowerCard
{

	private String accountNumber;
	private BigDecimal amount;
	private BigDecimal cycleScore;
	private BigDecimal depositorScore;
	private BigDecimal averageVlmScore;
	private BigDecimal RepaymentScore;
	private BigDecimal finalScore1;
	private BigDecimal finalScoreAvg;
	private BigDecimal overallScore;
	private boolean borrower;
	private boolean classCodesAllowed;
	private boolean prevLoanRepaidTimely;
	private boolean voluntaryDepositor;


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
}
