package org.redlamp.model;

public class BLAccount
{

	private String accountNumber;
	private String accountType;
	private String phoneNumber;
	private String nubanAccountNumber;
	private String accountTitle;
	private Long rimNo;
	private String loanAccountNumber;
	private String loanAccountType;


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

	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public String getNubanAccountNumber()
	{
		return nubanAccountNumber;
	}

	public void setNubanAccountNumber(String nubanAccountNumber)
	{
		this.nubanAccountNumber = nubanAccountNumber;
	}

	public String getAccountTitle()
	{
		return accountTitle;
	}

	public void setAccountTitle(String accountTitle)
	{
		this.accountTitle = accountTitle;
	}

	public Long getRimNo()
	{
		return rimNo;
	}

	public void setRimNo(Long rimNo)
	{
		this.rimNo = rimNo;
	}

	public String getLoanAccountNumber()
	{
		return loanAccountNumber;
	}

	public void setLoanAccountNumber(String loanAccountNumber)
	{
		this.loanAccountNumber = loanAccountNumber;
	}

	public String getLoanAccountType()
	{
		return loanAccountType;
	}

	public void setLoanAccountType(String loanAccountType)
	{
		this.loanAccountType = loanAccountType;
	}
}
