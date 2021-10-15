package org.redlamp.model;

import java.math.BigDecimal;

public class LoanRequest
{

	private String accountNo,accountType, phoneNumber,period ;
	private BigDecimal loanAmount,largestDisbursement;
	private Long term;

	public String getAccountType()
	{
		return accountType;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public String getAccountNo()
	{
		return accountNo;
	}

	public void setAccountNo(String accountNo)
	{
		this.accountNo = accountNo;
	}

	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}

	public Long getTerm()
	{
		return term;
	}

	public void setTerm(Long term)
	{
		this.term = term;
	}

	public BigDecimal getLoanAmount()
	{
		return loanAmount;
	}

	public void setLoanAmount(BigDecimal loanAmount)
	{
		this.loanAmount = loanAmount;
	}

	public String getPeriod()
	{
		return period;
	}

	public void setPeriod(String period)
	{
		this.period = period;
	}

	public BigDecimal getLargestDisbursement()
	{
		return largestDisbursement;
	}

	public void setLargestDisbursement(BigDecimal largestDisbursement)
	{
		this.largestDisbursement = largestDisbursement;
	}

	@Override
	public String toString() {
		return "NameInquiryRequest [accountNo=" + accountNo + ", phoneNumber=" + phoneNumber + ",Amount =" + loanAmount + " ,Period=" + period + "Term=" + term + "]";
	}

}
