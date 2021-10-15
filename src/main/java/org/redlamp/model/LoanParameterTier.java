package org.redlamp.model;

import java.math.BigDecimal;

public class LoanParameterTier
{
    private BigDecimal tierFloor;
    private BigDecimal tierCeiling;
    private BigDecimal tierValue;
    private String tierValueTwo;

    public BigDecimal getTierFloor()
    {
        return tierFloor;
    }

    public void setTierFloor(BigDecimal tierFloor)
    {
        this.tierFloor = tierFloor;
    }

    public BigDecimal getTierCeiling()
    {
        return tierCeiling;
    }

    public void setTierCeiling(BigDecimal tierCeiling)
    {
        this.tierCeiling = tierCeiling;
    }

    public BigDecimal getTierValue()
    {
        return tierValue;
    }

    public void setTierValue(BigDecimal tierValue)
    {
        this.tierValue = tierValue;
    }

    public String getTierValueTwo()
    {
        return tierValueTwo;
    }

    public void setTierValueTwo(String tierValueTwo)
    {
        this.tierValueTwo = tierValueTwo;
    }
}
