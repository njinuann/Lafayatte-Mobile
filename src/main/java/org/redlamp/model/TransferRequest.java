package org.redlamp.model;

public class TransferRequest {

	private String phone_no, account_type, account_no, currency, recipient_bank_code, tran_code, recipient_account_no,
			recipient_account_name, description;

	private double tran_amount;

	public String getRecipient_account_no() {
		return recipient_account_no != null ? recipient_account_no : "";
	}

	public void setRecipient_account_no(String recipient_account_no) {
		this.recipient_account_no = recipient_account_no;
	}

	public String getRecipient_account_name() {
		return recipient_account_name;
	}

	public void setRecipient_account_name(String recipient_account_name) {
		this.recipient_account_name = recipient_account_name;
	}

	public String getPhone_no() {
		return phone_no;
	}

	public void setPhone_no(String phone_no) {
		this.phone_no = phone_no;
	}

	public String getAccount_type() {
		return account_type;
	}

	public void setAccount_type(String account_type) {
		this.account_type = account_type;
	}

	public String getAccount_no() {
		return account_no;
	}

	public void setAccount_no(String account_no) {
		this.account_no = account_no;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getRecipient_bank_code() {
		return recipient_bank_code;
	}

	public void setRecipient_bank_code(String recipient_bank_code) {
		this.recipient_bank_code = recipient_bank_code;
	}

	public String getTran_code() {
		return tran_code;
	}

	public void setTran_code(String tran_code) {
		this.tran_code = tran_code;
	}

	public double getTran_amount() {
		return tran_amount;
	}

	public void setTran_amount(double tran_amount) {
		this.tran_amount = tran_amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
