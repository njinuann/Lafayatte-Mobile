package org.redlamp.model;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiUtils;

@SuppressWarnings("serial")
public class XapiTran implements Serializable {

	private int service_id, pnTranCode, pnPTID, pnPayeeNo, pnSupervisorId, pnEmployeeId, pnTranType, psSdAcctId,
			pnDebug;
	private Date pdtTranDt, pdtEffectiveDt, pdtDate1, pdtDate2;
	@SuppressWarnings("unused")
	private String psTerminalID, psProprietaryATM, psATMSwitchID, psMannedDevice, pnReferenceNo, pnOrigReferenceNo,
			psOffline, psReversal, psPIN, psNewPIN, psCardNo, psISOCurrency, psAcctNo1, psAcctNo2, psRegEDescription,
			psDescription, psPbUpdated, psVersion, psDepLoan, psUserName, responseCode, chargeFlag, billerCode,
			procCode, bankCode, nibssResponse, acctName, tranLocation;

	private double pnAmt1, pnAmt2, pnCheckNo1, pnCheckNo2, currentBal, availableBal;
	private boolean billable, isTxnPermitted;
	private List<Map<String, String>> statement;

	private int xapiCode;

	public String getTranLocation() {
		return tranLocation != null ? tranLocation : XapiUtils.DEFAULT_LOCATION;
	}

    public void setTranLocation(String tranLocation)
    {
        this.tranLocation = tranLocation;
    }

    public int getService_id()
    {
        return service_id <= 0 ? 44 : service_id;
    }

    public void setService_id(int service_id)
    {
        this.service_id = service_id;
    }

    public int getPnTranCode()
    {
        return pnTranCode;
    }

    public void setPnTranCode(int pnTranCode)
    {
        this.pnTranCode = pnTranCode;
    }

    public int getPnPTID()
    {
        return pnPTID;
    }

    public void setPnPTID(int pnPTID)
    {
        this.pnPTID = pnPTID;
    }

    public int getPnPayeeNo()
    {
        return pnPayeeNo;
    }

    public void setPnPayeeNo(int pnPayeeNo)
    {
        this.pnPayeeNo = pnPayeeNo;
    }

    public int getPnSupervisorId()
    {
        return pnSupervisorId;
    }

    public void setPnSupervisorId(int pnSupervisorId)
    {
        this.pnSupervisorId = pnSupervisorId;
    }

    public int getPnEmployeeId()
    {
        return pnEmployeeId;
    }

    public void setPnEmployeeId(int pnEmployeeId)
    {
        this.pnEmployeeId = pnEmployeeId;
    }

    public int getPnTranType()
    {
        return pnTranType;
    }

    public void setPnTranType(int pnTranType)
    {
        this.pnTranType = pnTranType;
    }

    public int getPsSdAcctId()
    {
        return psSdAcctId;
    }

    public void setPsSdAcctId(int psSdAcctId)
    {
        this.psSdAcctId = psSdAcctId;
    }

    public int getPnDebug()
    {
        return pnDebug;
    }

    public void setPnDebug(int pnDebug)
    {
        this.pnDebug = pnDebug;
    }

    public Date getPdtTranDt()
    {
        return pdtTranDt != null ? pdtTranDt : new Date(System.currentTimeMillis());
    }

    public void setPdtTranDt(Date pdtTranDt)
    {
        this.pdtTranDt = pdtTranDt;
    }

    public Date getPdtEffectiveDt()
    {
        return pdtEffectiveDt != null ? pdtEffectiveDt : getPdtTranDt();
    }

    public void setPdtEffectiveDt(Date pdtEffectiveDt)
    {
        this.pdtEffectiveDt = pdtEffectiveDt;
    }

    public Date getPdtDate1()
    {
        return pdtDate1;
    }

    public void setPdtDate1(Date pdtDate1)
    {
        this.pdtDate1 = pdtDate1;
    }

    public Date getPdtDate2()
    {
        return pdtDate2;
    }

    public void setPdtDate2(Date pdtDate2)
    {
        this.pdtDate2 = pdtDate2;
    }

    public String getPsTerminalID()
    {
        return psTerminalID != null ? psTerminalID : XapiUtils.DEFAULT_TERMINAL_ID;
    }

    public void setPsTerminalID(String psTerminalID)
    {
        this.psTerminalID = psTerminalID;
    }

    public String getPsProprietaryATM()
    {
        return psProprietaryATM != null ? psProprietaryATM : "Y";
    }

    public void setPsProprietaryATM(String psProprietaryATM)
    {
        this.psProprietaryATM = psProprietaryATM;
    }

    public String getPsATMSwitchID()
    {
        return psATMSwitchID != null ? psATMSwitchID : XapiUtils.DEFAULT_SWITCH_ID;
    }

    public void setPsATMSwitchID(String psATMSwitchID)
    {
        this.psATMSwitchID = psATMSwitchID;
    }

    public String getPsMannedDevice()
    {
        return psMannedDevice != null ? psMannedDevice : "Y";
    }

    public void setPsMannedDevice(String psMannedDevice)
    {
        this.psMannedDevice = psMannedDevice;
    }

    public String getPnReferenceNo()
    {
        return pnReferenceNo;
    }

    public void setPnReferenceNo(String pnReferenceNo)
    {
        this.pnReferenceNo = pnReferenceNo;
    }

    public String getPnOrigReferenceNo()
    {
        return pnOrigReferenceNo;
    }

    public void setPnOrigReferenceNo(String pnOrigReferenceNo)
    {
        this.pnOrigReferenceNo = pnOrigReferenceNo;
    }

    public String getPsOffline()
    {
        return psOffline != null ? psOffline : "N";
    }

    public void setPsOffline(String psOffline)
    {
        this.psOffline = psOffline;
    }

    public String getPsReversal()
    {
        return psReversal != null ? psReversal : "N";
    }

    public void setPsReversal(String psReversal)
    {
        this.psReversal = psReversal;
    }

    public String getPsPIN()
    {
        return psPIN;
    }

    public void setPsPIN(String psPIN)
    {
        this.psPIN = psPIN;
    }

    public String getPsNewPIN()
    {
        return psNewPIN;
    }

    public void setPsNewPIN(String psNewPIN)
    {
        this.psNewPIN = psNewPIN;
    }

    public String getPsCardNo()
    {
        return psCardNo != null ? psCardNo : getPsAcctNo1();
    }

    public void setPsCardNo(String psCardNo)
    {
        this.psCardNo = psCardNo;
    }

    public String getPsISOCurrency()
    {
        return psISOCurrency != null ? psISOCurrency : XapiUtils.DEFAULT_CURRENCY;
    }

    public void setPsISOCurrency(String psISOCurrency)
    {
        this.psISOCurrency = psISOCurrency;
    }

    public String getPsApplType1()
    {
        return "DF";
    }

    public void setPsApplType1(String psApplType1)
    {
    }

    public String getPsAcctType1()
    {
        return "DF";
    }

    public void setPsAcctType1(String psAcctType1)
    {
    }

    public String getPsAcctNo1()
    {
        return psAcctNo1;
    }

    public void setPsAcctNo1(String psAcctNo1)
    {
        this.psAcctNo1 = psAcctNo1;
    }

    public String getPsApplType2()
    {
        return "DF";
    }

    public void setPsApplType2(String psApplType2)
    {
    }

    public String getPsAcctType2()
    {
        return "DF";
    }

    public void setPsAcctType2(String psAcctType2)
    {
    }

    public String getPsAcctNo2()
    {
        return psAcctNo2;
    }

    public void setPsAcctNo2(String psAcctNo2)
    {
        this.psAcctNo2 = psAcctNo2;
    }

    public String getPsRegEDescription()
    {
        return psRegEDescription != null ? psRegEDescription : psDescription;
    }

    public void setPsRegEDescription(String psRegEDescription)
    {
        this.psRegEDescription = psRegEDescription;
    }

    public String getPsDescription()
    {
        return psDescription;
    }

    public void setPsDescription(String psDescription)
    {
        this.psDescription = psDescription;
    }

    public String getPsPbUpdated()
    {
        return psPbUpdated;
    }

    public void setPsPbUpdated(String psPbUpdated)
    {
        this.psPbUpdated = psPbUpdated;
    }

    public String getPsVersion()
    {
        return psVersion;
    }

    public void setPsVersion(String psVersion)
    {
        this.psVersion = psVersion;
    }

    public String getPsDepLoan()
    {
        return psDepLoan;
    }

    public void setPsDepLoan(String psDepLoan)
    {
        this.psDepLoan = psDepLoan;
    }

    public String getPsUserName()
    {
        return psUserName;
    }

    public void setPsUserName(String psUserName)
    {
        this.psUserName = psUserName;
    }

    public double getPnAmt1()
    {
        return pnAmt1;
    }

    public void setPnAmt1(double pnAmt1)
    {
        this.pnAmt1 = pnAmt1;
    }

    public double getPnAmt2()
    {
        return pnAmt2;
    }

    public void setPnAmt2(double pnAmt2)
    {
        this.pnAmt2 = pnAmt2;
    }

    public double getPnCheckNo1()
    {
        return pnCheckNo1;
    }

    public void setPnCheckNo1(double pnCheckNo1)
    {
        this.pnCheckNo1 = pnCheckNo1;
    }

    public double getPnCheckNo2()
    {
        return pnCheckNo2;
    }

    public void setPnCheckNo2(double pnCheckNo2)
    {
        this.pnCheckNo2 = pnCheckNo2;
    }

    public String getResponseCode()
    {
        return errorMap(getXapiCode());
    }

    public void setResponseCode(String returnCode)
    {
        this.responseCode = returnCode;
    }

    public String getResponseTxt()
    {
        return XapiCodes.getErrorDesc(getResponseCode());
    }

    public String getChargeFlag()
    {
        return chargeFlag;
    }

    public void setChargeFlag(String chargeFlag)
    {
        this.chargeFlag = chargeFlag;
    }

    public double getCurrentBal()
    {
        return currentBal;
    }

    public void setCurrentBal(double currentBal)
    {
        this.currentBal = currentBal;
    }

    public double getAvailableBal()
    {
        return availableBal;
    }

    public void setAvailableBal(double availableBal)
    {
        this.availableBal = availableBal;
    }

    public List<Map<String, String>> getStatement()
    {
        return statement;
    }

    public void setStatement(List<Map<String, String>> statement)
    {
        this.statement = statement;
    }

    public boolean isBillable()
    {
        return billable;
    }

    public void setBillable(boolean billable)
    {
        this.billable = billable;
    }

    public String getBillerCode()
    {
        return billerCode;
    }

    public void setBillerCode(String billerCode)
    {
        this.billerCode = billerCode;
    }

    public String getProcCode()
    {
        return procCode;
    }

    public void setProcCode(String procCode)
    {
        this.procCode = procCode;
    }

    public String getBankCode()
    {
        return bankCode;
    }

    public void setBankCode(String bankCode)
    {
        this.bankCode = bankCode;
    }

    public boolean isTxnPermitted()
    {
        return isTxnPermitted;
    }

    public void setTxnPermitted(boolean isTxnPermitted)
    {
        this.isTxnPermitted = isTxnPermitted;
    }

    public int getXapiCode()
    {
        return xapiCode;
    }

    public void setXapiCode(int xapiCode)
    {
        this.xapiCode = xapiCode;
    }

    public String errorMap(int returnCode)
    {
        String RC;
        switch (returnCode)
        {
            case 0:
                RC = "00";
                break;
            case 10:
                RC = "53";
                break;
            case 11:
                RC = "52";
                break;
            case 28:
                RC = "52";
                break;
            case 24:
                RC = "14";
                break;
            case 30:
                RC = "13";
                break;
            case 39:
                RC = "51";
                break;
            case 43:
                RC = "25";
                break;
            case 51:
                RC = "45";
                break;
            case 58:
            case -50040:
                RC = "58";
                break;
            case 60:
                RC = "13";
                break;
            case 70:
                RC = "26";
                break;
            case 79:
                RC = "13";
                break;
            case -50007:
                RC = "40";
                break;
            case 12:
                RC = "12";
                break;
            case 111:
                RC = "12";
                break;
            case 45:
                RC = "45";
                break;
            default:
                RC = "96";
                break;
        }
        return RC;
    }

    public String getNibssResponse()
    {
        return nibssResponse;
    }

    public void setNibssResponse(String nibssResponse)
    {
        this.nibssResponse = nibssResponse;
    }

    public String getAcctName()
    {
        return acctName != null ? acctName.toUpperCase() : "";
    }

    public void setAcctName(String acctName)
    {
        this.acctName = acctName;
    }

}
