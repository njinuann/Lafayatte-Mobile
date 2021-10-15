package org.redlamp.model;

import java.math.BigDecimal;

public class BLBorrowerScoreCard
{

	private String accountNumber;
	private Long monthDifference;
	private Long ThreeMonthsScore;
	private Long sixMonthsScore;
	private Long nineMonthScore;
	private Long fivedaysToMaturityRepmntScore;
	private Long threedaysToMaturityRepmntScore;;
	private Long tewntyfivedaysRepmntScore;
	private Long twoWeeksRepmntScore;
	private Long lateDays;
	private Long cycle1Score ;
	private Long cycle2Score ;


	public String getAccountNumber()
	{
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public Long getMonthDifference()
	{
		return monthDifference;
	}

	public void setMonthDifference(Long monthDifference)
	{
		this.monthDifference = monthDifference;
	}

	public Long getThreeMonthsScore()
	{
		return ThreeMonthsScore;
	}

	public void setThreeMonthsScore(Long threeMonthsScore)
	{
		ThreeMonthsScore = threeMonthsScore;
	}

	public Long getSixMonthsScore()
	{
		return sixMonthsScore;
	}

	public void setSixMonthsScore(Long sixMonthsScore)
	{
		this.sixMonthsScore = sixMonthsScore;
	}

	public Long getNineMonthScore()
	{
		return nineMonthScore;
	}

	public void setNineMonthScore(Long nineMonthScore)
	{
		this.nineMonthScore = nineMonthScore;
	}

	public Long getFivedaysToMaturityRepmntScore()
	{
		return fivedaysToMaturityRepmntScore;
	}

	public void setFivedaysToMaturityRepmntScore(Long fivedaysToMaturityRepmntScore)
	{
		this.fivedaysToMaturityRepmntScore = fivedaysToMaturityRepmntScore;
	}

	public Long getThreedaysToMaturityRepmntScore()
	{
		return threedaysToMaturityRepmntScore;
	}

	public void setThreedaysToMaturityRepmntScore(Long threedaysToMaturityRepmntScore)
	{
		this.threedaysToMaturityRepmntScore = threedaysToMaturityRepmntScore;
	}

	public Long getTewntyfivedaysRepmntScore()
	{
		return tewntyfivedaysRepmntScore;
	}

	public void setTewntyfivedaysRepmntScore(Long tewntyfivedaysRepmntScore)
	{
		this.tewntyfivedaysRepmntScore = tewntyfivedaysRepmntScore;
	}

	public Long getTwoWeeksRepmntScore()
	{
		return twoWeeksRepmntScore;
	}

	public void setTwoWeeksRepmntScore(Long twoWeeksRepmntScore)
	{
		this.twoWeeksRepmntScore = twoWeeksRepmntScore;
	}

	public Long getLateDays()
	{
		return lateDays;
	}

	public void setLateDays(Long lateDays)
	{
		this.lateDays = lateDays;
	}

	public Long getCycle1Score()
	{
		return cycle1Score;
	}

	public void setCycle1Score(Long cycle1Score)
	{
		this.cycle1Score = cycle1Score;
	}

	public Long getCycle2Score()
	{
		return cycle2Score;
	}

	public void setCycle2Score(Long cycle2Score)
	{
		this.cycle2Score = cycle2Score;
	}
}
