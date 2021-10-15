package org.redlamp.model;

import java.sql.Date;

public class CustomerVerificationRequest {

	private String account_no, phone_number;
	private String dateOfBirth;

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getAccount_no() {

		return account_no;
	}

	public void setAccount_no(String account_no) {

		this.account_no = account_no;
	}



	@Override
	public String toString() {
		return "NameInquiryRequest [account_no=" + account_no + ", bank_code=" + phone_number + "]";
	}

}
