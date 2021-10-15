package org.redlamp.model;

import java.math.BigDecimal;

public class LoanRepaymentData
{

	private String dpAccountNo, dpAccountType,lnAccountNo="", lnAccountType,status;
	private BigDecimal repaymentAmount;
	private Long rimNo,classCode;
	private RPAccount rpAccount;

	public String getDpAccountNo()
	{
		return dpAccountNo;
	}

	public void setDpAccountNo(String dpAccountNo)
	{
		this.dpAccountNo = dpAccountNo;
	}

	public String getDpAccountType()
	{
		return dpAccountType;
	}

	public void setDpAccountType(String dpAccountType)
	{
		this.dpAccountType = dpAccountType;
	}

	public String getLnAccountNo()
	{
		return lnAccountNo;
	}

	public void setLnAccountNo(String lnAccountNo)
	{
		this.lnAccountNo = lnAccountNo;
	}

	public String getLnAccountType()
	{
		return lnAccountType;
	}

	public void setLnAccountType(String lnAccountType)
	{
		this.lnAccountType = lnAccountType;
	}

	public BigDecimal getRepaymentAmount()
	{
		return repaymentAmount;
	}

	public void setRepaymentAmount(BigDecimal repaymentAmount)
	{
		this.repaymentAmount = repaymentAmount;
	}

	public Long getRimNo()
	{
		return rimNo;
	}

	public void setRimNo(Long rimNo)
	{
		this.rimNo = rimNo;
	}

	public Long getClassCode()
	{
		return classCode;
	}

	public void setClassCode(Long classCode)
	{
		this.classCode = classCode;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public RPAccount getRpAccount()
	{
		return rpAccount;
	}

	public void setRpAccount(RPAccount rpAccount)
	{
		this.rpAccount = rpAccount;
	}
}
