package org.redlamp.model;

import java.math.BigDecimal;

public class LoanCreationResponse
{

	private String  accountType,accountNo ,repaymentAccountNo,repaymentAccountType,applicationAcctType,ApplicationAcctNo;
	private int returnCode;

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

	public String getRepaymentAccountNo()
	{
		return repaymentAccountNo;
	}

	public void setRepaymentAccountNo(String repaymentAccountNo)
	{
		this.repaymentAccountNo = repaymentAccountNo;
	}

	public String getRepaymentAccountType()
	{
		return repaymentAccountType;
	}

	public void setRepaymentAccountType(String repaymentAccountType)
	{
		this.repaymentAccountType = repaymentAccountType;
	}

	public int getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(int returnCode)
	{
		this.returnCode = returnCode;
	}

	public String getApplicationAcctType()
	{
		return applicationAcctType;
	}

	public void setApplicationAcctType(String applicationAcctType)
	{
		this.applicationAcctType = applicationAcctType;
	}

	public String getApplicationAcctNo()
	{
		return ApplicationAcctNo;
	}

	public void setApplicationAcctNo(String applicationAcctNo)
	{
		ApplicationAcctNo = applicationAcctNo;
	}

	@Override
	public String toString() {
		return "NameInquiryRequest [accountNo=" + accountNo + ", accounttype=" + accountType + ",repaymentAccountNo =" + repaymentAccountNo + " ,repaymentAccountType=" + repaymentAccountType + "returnCode" + returnCode + "]";
	}

}
