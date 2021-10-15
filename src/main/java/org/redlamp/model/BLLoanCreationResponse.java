package org.redlamp.model;

import java.math.BigDecimal;

public class BLLoanCreationResponse
{

	private String accountNumber;
	private String accountType;
	private String phoneNumber;
	private String period;
	private String returnCode;
	private Long rimNo;
	private Long term;
	private BigDecimal amount;
	private String responseCode;
	private String responseMessage;

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
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
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}

	public String getPeriod()
	{
		return period;
	}

	public void setPeriod(String period)
	{
		this.period = period;
	}

	public Long getRimNo()
	{
		return rimNo;
	}

	public void setRimNo(Long rimNo)
	{
		this.rimNo = rimNo;
	}

	public Long getTerm()
	{
		return term;
	}

	public void setTerm(Long term)
	{
		this.term = term;
	}

	public String getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(String returnCode)
	{
		this.returnCode = returnCode;
	}

	public String getResponseCode()
	{
		return responseCode;
	}

	public void setResponseCode(String responseCode)
	{
		this.responseCode = responseCode;
	}

	public String getResponseMessage()
	{
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage)
	{
		this.responseMessage = responseMessage;
	}
}
