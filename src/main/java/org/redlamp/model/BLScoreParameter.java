package org.redlamp.model;

public class BLScoreParameter
{

	private String accountNumber;
	private Long txnMonth;
	private Long txnYear;
	private Long noOfTxn;
	private Long mntCounter;
	private BLBorrowerScoreCard blBorrowerScoreCard = new BLBorrowerScoreCard();

	public Long getMntCounter()
	{
		return mntCounter;
	}

	public void setMntCounter(Long mntCounter)
	{
		this.mntCounter = mntCounter;
	}

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public Long getTxnMonth()
	{
		return txnMonth;
	}

	public void setTxnMonth(Long txnMonth)
	{
		this.txnMonth = txnMonth;
	}

	public Long getTxnYear()
	{
		return txnYear;
	}

	public void setTxnYear(Long txnYear)
	{
		this.txnYear = txnYear;
	}

	public Long getNoOfTxn()
	{
		return noOfTxn;
	}

	public void setNoOfTxn(Long noOfTxn)
	{
		this.noOfTxn = noOfTxn;
	}

	public BLBorrowerScoreCard getBlBorrowerScoreCard()
	{
		return blBorrowerScoreCard;
	}

	public void setBlBorrowerScoreCard(BLBorrowerScoreCard blBorrowerScoreCard)
	{
		this.blBorrowerScoreCard = blBorrowerScoreCard;
	}
}
