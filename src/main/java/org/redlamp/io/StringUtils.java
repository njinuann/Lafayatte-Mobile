package org.redlamp.io;

import org.redlamp.util.XapiCodes;

public class StringUtils {

	public static synchronized String appendDash(String acctNo) {
		if (acctNo != null && acctNo.length() > 3 && !acctNo.contains("-")) {
			acctNo = acctNo.substring(0, 3) + "-" + acctNo.substring(3);
		}
		return acctNo;
	}

	public static synchronized String stripDashes(String acctNo) {
		if (acctNo != null && acctNo.contains("-")) {
			acctNo = acctNo.replaceAll("-", "").trim();
		}
		return acctNo;
	}

	public static String formatPhone(String rawPhone) {
		if (rawPhone == null || rawPhone.isEmpty())
			return rawPhone;
		return rawPhone.trim().startsWith(XapiCodes.PHONE_PREFIX) || rawPhone.trim().startsWith("+") ? rawPhone
				: XapiCodes.PHONE_PREFIX + rawPhone.substring(1);
	}

}
