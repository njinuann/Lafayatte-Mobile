package org.redlamp.model;

public class BLScoreItems
{

	private Long overDue1to7Days;
	private Long overDue8to14Days;
	private Long overDue15to30Days;
	private Long moreThan7DaysLate;
	private Long moreThan30DaysLate;
	private Long totalNoOFInstallments;
	private Long currentCycle;
	private Long riskClass;private Long rimClass;
	private Long noOfDigitalLoansInTheYear;
	private Long rimNo;

	private String dueOnFirstInstallment;
	private String dueOnSecondInstallment;
	private String dueOnSThirdInstallment;
	private String residence;
	private String riskGroup;
	private String status;
	private String loanProductName;
	private String  AccountNumber;
	private String phoneNumber;

	public Long getRimClass()
	{
		return rimClass;
	}

	public void setRimClass(Long rimClass)
	{
		this.rimClass = rimClass;
	}

	public Long getOverDue1to7Days()
	{
		return overDue1to7Days;
	}

	public void setOverDue1to7Days(Long overDue1to7Days)
	{
		this.overDue1to7Days = overDue1to7Days;
	}

	public Long getOverDue8to14Days()
	{
		return overDue8to14Days;
	}

	public void setOverDue8to14Days(Long overDue8to14Days)
	{
		this.overDue8to14Days = overDue8to14Days;
	}

	public Long getOverDue15to30Days()
	{
		return overDue15to30Days;
	}

	public void setOverDue15to30Days(Long overDue15to30Days)
	{
		this.overDue15to30Days = overDue15to30Days;
	}

	public Long getMoreThan7DaysLate()
	{
		return moreThan7DaysLate;
	}

	public void setMoreThan7DaysLate(Long moreThan7DaysLate)
	{
		this.moreThan7DaysLate = moreThan7DaysLate;
	}

	public Long getMoreThan30DaysLate()
	{
		return moreThan30DaysLate;
	}

	public void setMoreThan30DaysLate(Long moreThan30DaysLate)
	{
		this.moreThan30DaysLate = moreThan30DaysLate;
	}

	public Long getTotalNoOFInstallments()
	{
		return totalNoOFInstallments;
	}

	public void setTotalNoOFInstallments(Long totalNoOFInstallments)
	{
		this.totalNoOFInstallments = totalNoOFInstallments;
	}

	public Long getCurrentCycle()
	{
		return currentCycle;
	}

	public void setCurrentCycle(Long currentCycle)
	{
		this.currentCycle = currentCycle;
	}

	public Long getRiskClass()
	{
		return riskClass;
	}

	public void setRiskClass(Long riskClass)
	{
		this.riskClass = riskClass;
	}

	public Long getNoOfDigitalLoansInTheYear()
	{
		return noOfDigitalLoansInTheYear;
	}

	public void setNoOfDigitalLoansInTheYear(Long noOfDigitalLoansInTheYear)
	{
		this.noOfDigitalLoansInTheYear = noOfDigitalLoansInTheYear;
	}

	public Long getRimNo()
	{
		return rimNo;
	}

	public void setRimNo(Long rimNo)
	{
		this.rimNo = rimNo;
	}

	public String getDueOnFirstInstallment()
	{
		return dueOnFirstInstallment;
	}

	public void setDueOnFirstInstallment(String dueOnFirstInstallment)
	{
		this.dueOnFirstInstallment = dueOnFirstInstallment;
	}

	public String getDueOnSecondInstallment()
	{
		return dueOnSecondInstallment;
	}

	public void setDueOnSecondInstallment(String dueOnSecondInstallment)
	{
		this.dueOnSecondInstallment = dueOnSecondInstallment;
	}

	public String getDueOnSThirdInstallment()
	{
		return dueOnSThirdInstallment;
	}

	public void setDueOnSThirdInstallment(String dueOnSThirdInstallment)
	{
		this.dueOnSThirdInstallment = dueOnSThirdInstallment;
	}

	public String getResidence()
	{
		return residence;
	}

	public void setResidence(String residence)
	{
		this.residence = residence;
	}

	public String getRiskGroup()
	{
		return riskGroup;
	}

	public void setRiskGroup(String riskGroup)
	{
		this.riskGroup = riskGroup;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getLoanProductName()
	{
		return loanProductName;
	}

	public void setLoanProductName(String loanProductName)
	{
		this.loanProductName = loanProductName;
	}

	public String getAccountNumber()
	{
		return AccountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		AccountNumber = accountNumber;
	}

	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}
}
