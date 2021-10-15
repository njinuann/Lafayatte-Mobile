package org.redlamp.model;

public class ChargeMap {

	private String chargeFlag;
	private String chargeDesc;

	public String getChargeFlag() {
		return chargeFlag;
	}

	public void setChargeFlag(String chargeFlag) {
		this.chargeFlag = chargeFlag;
	}

	public String getChargeDesc() {
		return chargeDesc;
	}

	public void setChargeDesc(String chargeDesc) {
		this.chargeDesc = chargeDesc;
	}

	@Override
	public String toString() {
		return "ChargeMap [chargeFlag=" + chargeFlag + ", chargeDesc=" + chargeDesc + "]";
	}

}
