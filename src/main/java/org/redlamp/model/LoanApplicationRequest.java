package org.redlamp.model;

import java.math.BigDecimal;

public class LoanApplicationRequest
{

	private String  accountType,phoneNumber,applicationText ,period,applicationNewNo,approvalAction,userId;
	private BigDecimal loanAmount;
	private Long rimNo,classCode,purposeId,term;
	private int returnCode;

	private String applicationType,loanApplNo,accountNo,depositAccountNo,DepositAccountType;

	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}

	public Long getRimNo()
	{
		return rimNo;
	}

	public void setRimNo(Long rimNo)
	{
		this.rimNo = rimNo;
	}

	public String getAccountType()
	{
		return accountType;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public String getApplicationText()
	{
		return applicationText;
	}

	public void setApplicationText(String applicationText)
	{
		this.applicationText = applicationText;
	}

	public String getPeriod()
	{
		return period;
	}

	public void setPeriod(String period)
	{
		this.period = period;
	}

	public String getApplicationNewNo()
	{
		return applicationNewNo;
	}

	public void setApplicationNewNo(String applicationNewNo)
	{
		this.applicationNewNo = applicationNewNo;
	}

	public BigDecimal getLoanAmount()
	{
		return loanAmount;
	}

	public void setLoanAmount(BigDecimal loanAmount)
	{
		this.loanAmount = loanAmount;
	}

	public Long getClassCode()
	{
		return classCode;
	}

	public void setClassCode(Long classCode)
	{
		this.classCode = classCode;
	}

	public Long getPurposeId()
	{
		return purposeId;
	}

	public void setPurposeId(Long purposeId)
	{
		this.purposeId = purposeId;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public Long getTerm()
	{
		return term;
	}

	public void setTerm(Long term)
	{
		this.term = term;
	}

	public int getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(int returnCode)
	{
		this.returnCode = returnCode;
	}

	public String getApprovalAction()
	{
		return approvalAction;
	}

	public void setApprovalAction(String approvalAction)
	{
		this.approvalAction = approvalAction;
	}

	public String getApplicationType()
	{
		return applicationType;
	}

	public void setApplicationType(String applicationType)
	{
		this.applicationType = applicationType;
	}

	public String getLoanApplNo()
	{
		return loanApplNo;
	}

	public void setLoanApplNo(String loanApplNo)
	{
		this.loanApplNo = loanApplNo;
	}

	public String getAccountNo()
	{
		return accountNo;
	}

	public void setAccountNo(String accountNo)
	{
		this.accountNo = accountNo;
	}

	public String getDepositAccountNo()
	{
		return depositAccountNo;
	}

	public void setDepositAccountNo(String depositAccountNo)
	{
		this.depositAccountNo = depositAccountNo;
	}

	public String getDepositAccountType()
	{
		return DepositAccountType;
	}

	public void setDepositAccountType(String depositAccountType)
	{
		DepositAccountType = depositAccountType;
	}

	@Override
	public String toString() {
		return "NameInquiryRequest [rimNo=" + rimNo + ", accounttype=" + accountType + ",Amount =" + loanAmount + " ,Period=" + period + "Term=" + term + "]";
	}

}
