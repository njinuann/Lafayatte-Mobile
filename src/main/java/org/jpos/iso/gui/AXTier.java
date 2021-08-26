/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpos.iso.gui;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author Pecherk
 */
public class AXTier implements Serializable, Comparable<AXTier>, Cloneable
{
    private Long tierId;
    private String tierCode;
    private BigDecimal value;
     private BigDecimal charValue;
    private BigDecimal tierMax = BigDecimal.ZERO;
    private BigDecimal tierMin = BigDecimal.ZERO;

    /**
     * @return the tierMax
     */
    public BigDecimal getTierMax()
    {
        return tierMax;
    }

    /**
     * @param tierMax the tierMax to set
     */
    public void setTierMax(BigDecimal tierMax)
    {
        this.tierMax = tierMax.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @return the value
     */
    public BigDecimal getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(BigDecimal value)
    {
        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @return the tierMin
     */
    public BigDecimal getTierMin()
    {
        return tierMin;
    }

    /**
     * @param tierMin the tierMin to set
     */
    public void setTierMin(BigDecimal tierMin)
    {
        this.tierMin = tierMin.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @return the tierId
     */
    public Long getTierId()
    {
        return tierId;
    }

    /**
     * @param tierId the tierId to set
     */
    public void setTierId(Long tierId)
    {
        this.tierId = tierId;
    }

    @Override
    public int compareTo(AXTier o)
    {
        return getTierMax().compareTo(o.getTierMax());
    }

    @Override
    public String toString()
    {
        return "Tier [ " + getTierMin().toPlainString() + " - " + getTierMax().toPlainString() + " ]";
    }

    @Override
    public AXTier clone() throws CloneNotSupportedException
    {
        return (AXTier) super.clone();
    }

    /**
     * @return the tierCode
     */
    public String getTierCode()
    {
        return tierCode;
    }

    /**
     * @param tierCode the tierCode to set
     */
    public void setTierCode(String tierCode)
    {
        this.tierCode = tierCode;
    }

    /**
     * @return the charValue
     */
    public BigDecimal getCharValue()
    {
        return charValue;
    }

    /**
     * @param charValue the charValue to set
     */
    public void setCharValue(BigDecimal charValue)
    {
        this.charValue = charValue;
    }
}
