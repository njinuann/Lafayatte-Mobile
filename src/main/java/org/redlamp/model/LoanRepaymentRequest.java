package org.redlamp.model;

import java.math.BigDecimal;

public class LoanRepaymentRequest
{

    private String RepaymentDpAccountNo, phoneNumber, earlyPayment="N";
    private BigDecimal repaymentAmount;

    public String getRepaymentDpAccountNo()
    {
        return RepaymentDpAccountNo;
    }

    public void setRepaymentDpAccountNo(String repaymentDpAccountNo)
    {
        RepaymentDpAccountNo = repaymentDpAccountNo;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public BigDecimal getRepaymentAmount()
    {
        return repaymentAmount;
    }

    public void setRepaymentAmount(BigDecimal repaymentAmount)
    {
        this.repaymentAmount = repaymentAmount;
    }

    public String getEarlyPayment()
    {
        return earlyPayment;
    }

    public void setEarlyPayment(String earlyPayment)
    {
        this.earlyPayment = earlyPayment;
    }

    @Override
    public String toString()
    {
        return "NameInquiryRequest [accountNo=" + getRepaymentDpAccountNo() + ", phoneNumber=" + phoneNumber + ",Amount =" + repaymentAmount + " ]";
    }

}
