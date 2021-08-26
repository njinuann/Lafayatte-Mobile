package org.redlamp.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.jpos.iso.ISOMsg;
import org.redlamp.logger.IsoLogger;

public abstract class XapiCodes
{

    public static final String proprietory = "Y";
    public static final String manned_device = "Y";

    private static final Properties errorCodes = new Properties();
    private static final Properties currencyMapping = new Properties();

    static AtomicInteger count = new AtomicInteger();
    public static String atmLoroAcctType;
    public static String atmLoroAcctNo;
    public static String atmLoroCardNo;
    public static String mobInterBankSuspenseGL;
    public static String tellerInterBankSuspenseGL;
    public static String tellerLoroAcctType;
    public static String tellerLoroCardNo;
    public static String tellerLoroAcctNo;
    public static String DATE_FORMAT;
    public static String BANK_CODE;
    public static String NIBSS_WSDL;

    public static String PHONE_PREFIX;
    public static String rimClassCode;
    public static Long defaultRimClass;
    public static Long identityType;
    public static Long marketingId;
    public static Long defaultBranch;
    public static Long depositClassCode;

    public static String DEFAULT_USER;
    public static boolean SEND_ALERT = false;

    /*e loans */
    public static Long purposeId;
    public static Long lnClassCode;
    public static Long dpClassCode;

    static final SimpleDateFormat nowFormater = new SimpleDateFormat("MMddhhmmss");
    public static String coreschema, xapiSchema, BILL_PAYMENT_FLAG, airtime, quick_teller, internal_transfer, balance_enquiry,
            mini_statement;

    public synchronized static String stan()
    {
        count.compareAndSet(999999, 0);
        return format(count.incrementAndGet());
    }

    public static String format(int amount)
    {
        StringBuilder builder = new StringBuilder();
        return builder.append("000000").append(amount).substring(builder.length() - 6);
    }

    public static String date()
    {
        return nowFormater.format(new Date());
    }

    static
    {
        try (FileInputStream fis = new FileInputStream("conf/postcodes.xml");
             FileInputStream fos = new FileInputStream("conf/currency.xml"))
        {
            errorCodes.loadFromXML(fis);
            currencyMapping.loadFromXML(fos);
        } catch (IOException e)
        {
            IsoLogger.getLogger().error(e);
        }
    }

    public static synchronized String getErrorDesc(Object errorCode)
    {
        //return errorCodes.getProperty(String.valueOf(errorCode), "96");
        return replaceHolders(errorCodes.getProperty(String.valueOf(errorCode), "Failed"));
    }

    public static synchronized String replaceHolders(String templateMsg)
    {
        String message = "";
        if (templateMsg.contains("{") || templateMsg.contains("{"))
        {
            message = templateMsg.replace("{REPDAYS}", String.valueOf(XapiPool.repayAfterDays)
                    .replace("{DURATION}", XapiPool.loanDurationParam));
        }
        else
        {
            message = templateMsg;
        }
        return message;
    }

    public static synchronized String convertCurrency(String string)
    {
        return currencyMapping.getProperty(string, "NGN");
    }

    public static String toISOAmount(double digitalAmt)
    {
        return toISOAmountWithPad(digitalAmt);
    }

    public static String toISOAmountWithPad(double amount)
    {
        BigDecimal amt = (new BigDecimal(String.format("%.0f", amount))).abs().setScale(2, BigDecimal.ROUND_DOWN);
        String amtStr = "000000000000" + String.valueOf(amt).replace(".", "");
        return amtStr.substring(amtStr.length() - 12);
    }

    public static String errorMap(int returnCode, String procCode)
    {
        String RC;
        switch (returnCode)
        {
            case 0:
                RC = "00";
                break;
            case 10:
                RC = "53";
                break;
            case 11:
                RC = "52";
                break;
            case 28:
                RC = "52";
                break;
            case 24:
                RC = "14";
                break;
            case 30:
                RC = "13";
                break;
            case 39:
                if (procCode.equals("21"))
                {
                    RC = "58";
                }
                else
                {
                    RC = "51";
                }
                break;
            case 43:
                RC = "00";
                break;
            case 51:
                RC = "45";
                break;
            case 58:
            case -50040:
                RC = "58";
                break;
            case 60:
                RC = "13";
                break;
            case 70:
                RC = "26";
                break;
            case 79:
                RC = "13";
                break;
            case 111:
                RC = "57";
                break;
            case -50007:
                RC = "40";
                break;
            default:
                RC = "96";
                break;
        }
        return RC;
    }

    @SuppressWarnings("unchecked")
    public static synchronized HashMap getStructuredData(ISOMsg reqMsg)
    {
        HashMap dataMap = new HashMap();
        String fieldText = reqMsg.getString("127.22");
        if (fieldText != null)
        {
            try
            {
                fieldText.replaceAll("&gt;", ">");
                fieldText.replaceAll("&lt;", "<");
                fieldText.replaceAll("&quot;", "\"");

                while (fieldText.length() > 0)
                {
                    int lenWidth = Integer.parseInt(fieldText.substring(0, 1));
                    fieldText = fieldText.substring(1);
                    int length = Integer.parseInt(fieldText.substring(0, lenWidth));
                    fieldText = fieldText.substring(lenWidth);
                    String key = fieldText.substring(0, length);
                    fieldText = fieldText.substring(length);

                    lenWidth = Integer.parseInt(fieldText.substring(0, 1));
                    fieldText = fieldText.substring(1);
                    length = Integer.parseInt(fieldText.substring(0, lenWidth));
                    fieldText = fieldText.substring(lenWidth);
                    String value = fieldText.substring(0, length);
                    fieldText = fieldText.substring(length);
                    dataMap.put(key, value);
                }
            } catch (Exception ex)
            {
                IsoLogger.getLogger().error(ex);
            }
        }
        return dataMap;
    }

    protected abstract Object getNibssTranslation(String valueOf);

}
