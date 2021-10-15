package org.redlamp.util;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

public class XapiUtils {

	private static final AtomicInteger count = new AtomicInteger();
	private static final SimpleDateFormat dateFormater = new SimpleDateFormat("MMdd");
	private static final SimpleDateFormat nowFormater = new SimpleDateFormat("MMyyyyddhhmmss");
	public static final String LOGS_DIR = ".";
	public static String DEFAULT_CURRENCY = "NGN";
	public static String DEFAULT_SWITCH_ID = "637633";
	public static String DEFAULT_TERMINAL_ID = "637633";
	public static String DEFAULT_LOCATION = "7.390249,3.879771";

	public static String now() {
		return nowFormater.format(new Date());
	}

	public synchronized static String stan() {
		count.compareAndSet(999999, 0);
		return format(count.incrementAndGet());
	}

	public synchronized static String generateReference() {
		return stan() + now();
	}

	public static String format(int amount) {
		StringBuilder builder = new StringBuilder();
		return builder.append("000000").append(amount).substring(builder.length() - 6);
	}

	public static String date() {
		return dateFormater.format(new Date());
	}

	public static String toISOAmount(double digitalAmt) {
		return toISOAmountWithPad(digitalAmt);
	}

	public static String toISOAmountWithPad(double amount) {
		BigDecimal amt = (new BigDecimal(String.format("%.0f", amount))).abs().setScale(2, BigDecimal.ROUND_DOWN);
		String amtStr = "000000000000" + String.valueOf(amt).replace(".", "");
		return amtStr.substring(amtStr.length() - 12);
	}

	public static String toISOAmountWithoutPad(double amount) {
		BigDecimal amt = (new BigDecimal(String.format("%.0f", amount))).abs().setScale(2, BigDecimal.ROUND_DOWN);
		String amtStr = "000000000000" + String.valueOf(amt).replace(".00", "");
		return amtStr.substring(amtStr.length() - 12);
	}

	public static String formatISODesc(String narration) {
		return narration != null && narration.length() > 40 ? narration.substring(0, 40) : narration;
	}

	public static String formatCode(String amount) {
		BigDecimal amt = (new BigDecimal(amount)).abs().setScale(0, BigDecimal.ROUND_DOWN);
		String amtStr = "00000000000" + String.valueOf(amt).replace(".", "");
		return amtStr.substring(amtStr.length() - 11);
	}

	public static String asISOAmount(double amount) {
		String txnAmount = null;
		try {
			txnAmount = ISOUtil.zeropad(ISOUtil.formatDouble(amount, 12), 12).replace(".", "");
		} catch (ISOException e) {
			e.printStackTrace();
		}
		return txnAmount;
	}

	public static synchronized ISOMsg explode(Map<String, Object> regInput) {
		String reference = XapiCodes.date();
		ISOMsg reqMsg = new ISOMsg(
				(regInput.containsKey("0") && regInput.get("0") != null) ? String.valueOf(regInput.get("0")) : "0200");
		reqMsg.set(3, regInput.get("3") != null ? String.valueOf(regInput.get("3")) : null);
		reqMsg.set(4,
				XapiCodes.toISOAmount(Double.parseDouble(String.valueOf(regInput.getOrDefault("tran_amount", "0")))));
		reqMsg.set(7, reference);
		reqMsg.set(11, XapiCodes.stan());
		reqMsg.set(12, reference.substring(4));
		reqMsg.set(13, XapiCodes.date());
		reqMsg.set(15, XapiCodes.date());
		reqMsg.set(18, "6011");
		reqMsg.set(22, "001");
		reqMsg.set(23, "001");
		reqMsg.set(25, "00");
		reqMsg.set(26, "12");
		reqMsg.set(28, "C00000000");
		reqMsg.set(30, "C00000000");
		reqMsg.set(32,
				regInput.get("bank_code") != null ? String.valueOf(regInput.get("bank_code")) : XapiCodes.BANK_CODE);
		reqMsg.set(33, XapiCodes.BANK_CODE);
		reqMsg.set(37, reference);
		reqMsg.set(40, "501");
		reqMsg.set(41, "LAMOBILE");
		reqMsg.set(43, String.valueOf(regInput.getOrDefault("narration", "")));
		reqMsg.set(49, regInput.get("currency") != null ? String.valueOf(regInput.get("currency")) : null);

		if ("0420".equals(regInput.get("0")))
			reqMsg.set(90,
					"0200" + String.valueOf(regInput.get("reference")).replace("-", "") + "0000000003900000000000");

		reqMsg.set(102, regInput.get("account_no") != null ? String.valueOf(regInput.get("account_no")) : null);
		reqMsg.set(103,
				regInput.get("recipient_account_no") != null ? String.valueOf(regInput.get("recipient_account_no"))
						: null);
		reqMsg.set(33, XapiCodes.BANK_CODE);
		reqMsg.set("127.22", "16MSISDN213" + String.valueOf(regInput.get("phone_no")));
		reqMsg.set("127.33", regInput.get("biller_code") != null ? String.valueOf(regInput.get("biller_code"))
				: String.valueOf(regInput.get("tran_code")));
		return reqMsg;
	}

	public static void open(String dir) {
		if (!Desktop.isDesktopSupported()) {
			JOptionPane.showMessageDialog(null, "This operation isn't supported by your operating system.",
					"Operation Not Supported Exception", JOptionPane.OK_OPTION);
			return;
		}
		try {
			Desktop.getDesktop().browse(Paths.get(dir).toUri());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
