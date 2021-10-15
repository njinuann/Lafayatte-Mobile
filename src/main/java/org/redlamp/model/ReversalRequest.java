package org.redlamp.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReversalRequest {

	private String account_no, reference, biller_code, currency, reason;

	private double tran_amount;

	public String getAccount_no() {
		return account_no;
	}

	public void setAccount_no(String account_no) {
		this.account_no = account_no;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getBiller_code() {
		return biller_code;
	}

	public void setBiller_code(String biller_code) {
		this.biller_code = biller_code;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public double getTran_amount() {
		return tran_amount;
	}

	public void setTran_amount(double tran_amount) {
		this.tran_amount = tran_amount;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("[");
		for (Field f : getClass().getFields()) {
			if (!isStaticField(f)) {
				try {
					b.append(f.getName() + "=" + f.get(this) + " ");
				} catch (IllegalAccessException e) {
					// pass, don't print
				}
			}
		}
		b.append(']');
		return b.toString();
	}

	private boolean isStaticField(Field f) {
		return Modifier.isStatic(f.getModifiers());
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
