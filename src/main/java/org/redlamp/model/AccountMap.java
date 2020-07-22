package org.redlamp.model;

public class AccountMap {

	private String localAccount;
	private String nubanAccount;
	private String accountTitle;

	public String getLocalAccount() {
		return localAccount;
	}

	public void setLocalAccount(String localAccount) {
		this.localAccount = localAccount;
	}

	public String getNubanAccount() {
		return nubanAccount;
	}

	public void setNubanAccount(String nubanAccount) {
		this.nubanAccount = nubanAccount;
	}

	public String getAccountTitle() {
		return accountTitle;
	}

	public void setAccountTitle(String accountTitle) {
		this.accountTitle = accountTitle;
	}

	@Override
	public String toString() {
		return "AccountMap [localAccount=" + localAccount + ", nubanAccount=" + nubanAccount + ", accountTitle="
				+ accountTitle + "]";
	}

}
