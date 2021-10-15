package org.redlamp.model;

public class NameInquiryRequest {

	private String account_no, bank_code;

	public String getAccount_no() {
		return account_no;
	}

	public void setAccount_no(String account_no) {
		this.account_no = account_no;
	}

	public String getBank_code() {
		return bank_code;
	}

	public void setBank_code(String bank_code) {
		this.bank_code = bank_code;
	}

	@Override
	public String toString() {
		return "NameInquiryRequest [account_no=" + account_no + ", bank_code=" + bank_code + "]";
	}

}
