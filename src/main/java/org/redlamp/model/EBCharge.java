package org.redlamp.model;

import java.math.BigDecimal;
import java.util.Date;

public class EBCharge {

	private Long ptid;
	private int serviceID, charge_code, tranCode;
	private BigDecimal value, minAmt, maxAmt;
	private Date modifiedDate;

	private String chargeDesc, txnType, mode, chargeAcct, status, modifiedBy, biller_code, biller_name, tranDesc;

	public Long getPtid() {
		return ptid;
	}

	public void setPtid(Long ptid) {
		this.ptid = ptid;
	}

	public int getServiceID() {
		return serviceID;
	}

	public void setServiceID(int serviceID) {
		this.serviceID = serviceID;
	}

	public int getCharge_code() {
		return charge_code;
	}

	public void setCharge_code(int charge_code) {
		this.charge_code = charge_code;
	}

	public int getTranCode() {
		return tranCode;
	}

	public void setTranCode(int tranCode) {
		this.tranCode = tranCode;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public BigDecimal getMinAmt() {
		return minAmt;
	}

	public void setMinAmt(BigDecimal minAmt) {
		this.minAmt = minAmt;
	}

	public BigDecimal getMaxAmt() {
		return maxAmt;
	}

	public void setMaxAmt(BigDecimal maxAmt) {
		this.maxAmt = maxAmt;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getChargeDesc() {
		return chargeDesc;
	}

	public void setChargeDesc(String chargeDesc) {
		this.chargeDesc = chargeDesc;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getChargeAcct() {
		return chargeAcct;
	}

	public void setChargeAcct(String chargeAcct) {
		this.chargeAcct = chargeAcct;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getBiller_code() {
		return biller_code;
	}

	public void setBiller_code(String biller_code) {
		this.biller_code = biller_code;
	}

	public String getBiller_name() {
		return biller_name;
	}

	public void setBiller_name(String biller_name) {
		this.biller_name = biller_name;
	}

	public String getTranDesc() {
		return tranDesc;
	}

	public void setTranDesc(String tranDesc) {
		this.tranDesc = tranDesc;
	}

}
