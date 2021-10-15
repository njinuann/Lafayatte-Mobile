package org.redlamp.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AccountRequest implements Serializable {

	private long cust_no;
	private long class_code;
	private String account_type;
	private String currency;

	public long getCust_no() {
		return cust_no;
	}

	public void setCust_no(long cust_no) {
		this.cust_no = cust_no;
	}

	public long getClass_code() {
		return class_code;
	}

	public void setClass_code(long class_code) {
		this.class_code = class_code;
	}

	public String getAccount_type() {
		return account_type;
	}

	public void setAccount_type(String account_type) {
		this.account_type = account_type;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public String toString() {
		return "AccountRequest [cust_no=" + cust_no + ", class_code=" + class_code + ", account_type=" + account_type
				+ ", currency=" + currency + "]";
	}

}
