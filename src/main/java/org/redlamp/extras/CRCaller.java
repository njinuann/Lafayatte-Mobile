package org.redlamp.extras;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import org.redlamp.model.BLScoreCard;
import org.redlamp.util.XapiCodes;

import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pecherk
 */
public class CRCaller
{

    private String narration = "", duration = "";
    private String cardNumber = "", accountNo = "";
    private String extraIndent = "\t";
    private String xapiRespCode = "";
    private String xapiRespMsg="";
    private ArrayList<Exception> exceptionsList = new ArrayList();
    private final HashMap<String, Object> callsMap = new HashMap<>();
    private BLScoreCard blScoreCard = new BLScoreCard();


    public void logException(Exception ex)
    {
        getExceptionsList().add(ex);
    }

    @Override
    public String toString()
    {

        StringBuilder buffer = new StringBuilder();
        buffer.append("<call>");
        buffer.append("\r\n").append(getExtraIndent()).append("<cardnumber>").append(getBlScoreCard().getPhoneNumber()).append("</cardnumber>");
        buffer.append("\r\n").append(getExtraIndent()).append("<accountno>").append(getBlScoreCard().getAccountNumber()).append("</accountno>");
        buffer.append("\r\n").append(getExtraIndent()).append("<highestdisblnAcct>").append(getBlScoreCard().getAccountWithHighestDisb()).append("</highestdisblnAcct>");
        buffer.append("\r\n").append(getExtraIndent()).append("<narration>").append(getNarration()).append("</narration>");
        String[] callKeys = callsMap.keySet().toArray(new String[callsMap.size()]);
        Arrays.sort(callKeys);
        for (String key : callKeys)
        {
//            buffer.append("\r\n").append(getExtraIndent()).append("<").append(String.valueOf(key).replaceAll("\\d", "")).append(">").append(convertToString(callsMap.get(key))).append("</").append(String.valueOf(key).replaceAll("\\d", "")).append(">");
            buffer.append("\r\n").append(getExtraIndent()).append("<").append(String.valueOf(key)).append(">").append(convertToString(callsMap.get(key))).append("</").append(String.valueOf(key)).append(">");
        }

        for (Object exception : getExceptionsList().toArray())
        {
            if (exception != null)
            {
//                if (exception instanceof XAPIException)
//                {
//                    buffer.append("\r\n").append(getExtraIndent()).append("<exception>").append(convertToString(exception)).append("</exception>");
//                }
//                else
//                {
                buffer.append("\r\n").append(getExtraIndent()).append("<exception>");
                buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("<class>").append(((Exception) exception).getClass().getSimpleName()).append("</class>");
                String emsg = (((Exception) exception).getMessage() == null) ? "" : ((Exception) exception).getMessage();

                if (emsg.contains("\r\n"))
                {
                    buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("<message>");
                    buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append(getExtraIndent()).append(((Exception) exception).getMessage().replaceAll("\r\n", "\r\n" + getExtraIndent() + getExtraIndent() + getExtraIndent()));
                    buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("</message>");
                }
                else
                {
                    buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("<message>").append(((Exception) exception).getMessage()).append("</message>");
                }
                buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("<stacktrace>");
                for (StackTraceElement s : ((Throwable) exception).getStackTrace())
                {
                    buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append(getExtraIndent()).append("at ").append(s.toString());
                }
                buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("</stacktrace>");
                buffer.append("\r\n").append(getExtraIndent()).append("</exception>");
//            }
            }
        }
        buffer.append("\r\n").append(getExtraIndent()).append("<xapirespcode>").append(getXapiRespCode()).append(" ~ ").append(XapiCodes.getErrorDesc(getXapiRespCode())).append("</xapirespcode>");
        buffer.append("\r\n").append(getExtraIndent()).append("<xapiRespMsg>").append(getXapiRespMsg()).append(" ~ ").append(XapiCodes.getErrorDesc(getXapiRespMsg())).append("</xapiRespMsg>");
        buffer.append("\r\n").append(getExtraIndent()).append("<duration>").append(getDuration()).append("</duration>");
        buffer.append("\r\n").append("</call>");
        return buffer.toString();
    }

    private String cleanText(String text)
    {
        String line = "", buffer = "";
        text = (text != null) ? text : "";

        InputStream is = new ByteArrayInputStream(text.getBytes());
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        try
        {
            while (line != null)
            {
                buffer += line;
                line = bis.readLine();
            }
        } catch (IOException ex)
        {
            return buffer;
        }

        return buffer;
    }

    public String convertToString(Object object)
    {
        return convertToString(object, null);
    }

    private String convertToString(Object object, String tag)
    {
        String text = "";
        boolean empty = true;
        tag = decapitalize(tag);
        Class<?> beanClass = object != null ? object.getClass() : String.class;
        try
        {
            if (object != null)
            {
                for (MethodDescriptor methodDescriptor : Introspector.getBeanInfo(beanClass).getMethodDescriptors())
                {
                    if ("toString".equalsIgnoreCase(methodDescriptor.getName()) && beanClass == methodDescriptor.getMethod().getDeclaringClass())
                    {
                        return tag != null ? (tag + "=<" + String.valueOf(object) + ">") : String.valueOf(object);
                    }
                }

                tag = tag == null ? beanClass.getSimpleName() : tag;

                if (object instanceof List)
                {
                    boolean append = false;
                    text += (empty ? "" : ", ");
                    for (Object item : ((List) object).toArray())
                    {
                        if (item != null)
                        {
                            text += (append ? ", " : "") + convertToString(item, null);
                            append = true;
                        }
                    }
                    empty = false;
                }
                else if (object instanceof Map)
                {
                    boolean append = false;
                    text += (empty ? "" : ", ");
                    for (Object key : ((Map) object).keySet())
                    {
                        text += (append ? ", " : "") + convertToString(((Map) object).get(key), String.valueOf(key));
                        append = true;
                    }
                    empty = false;
                }
                else if (object instanceof byte[])
                {
                    return tag != null ? (tag + "=<" + new String((byte[]) object) + ">") : String.valueOf(object);
                }
                else if (beanClass.isArray())
                {
                    text += tag + "=<[\r\n";
                    for (Object item : (Object[]) object)
                    {
                        text += convertToString(item, null) + "\r\n";
                    }
                    text += "]>";
                }
                else
                {
                    for (PropertyDescriptor propertyDesc : Introspector.getBeanInfo(beanClass).getPropertyDescriptors())
                    {
                        Method readMethod = propertyDesc.getReadMethod();
                        if (readMethod != null)
                        {
                            Object value = propertyDesc.getReadMethod().invoke(object);
                            if (!(value instanceof Class))
                            {
                                text += (empty ? "" : ", ") + convertToString(value, propertyDesc.getName());
                                empty = false;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return empty ? (tag != null ? (tag + "=<" + String.valueOf(object) + ">") : String.valueOf(object)) : (tag != null ? (tag + "=<[ " + text + " ]>") : text);
    }

    public String capitalize(String text)
    {
        if (text != null ? text.length() > 0 : false)
        {
            StringBuilder builder = new StringBuilder();
            for (String word : text.toLowerCase().split("\\s"))
            {
                builder.append(word.substring(0, 1).toUpperCase()).append(word.length() > 1 ? word.substring(1).toLowerCase() : "").append(" ");
            }
            return builder.toString().trim();
        }
        return text;
    }

    public String capitalize(String text, int minLen)
    {
        if (text != null)
        {
            StringBuilder builder = new StringBuilder();
            for (String word : text.split("\\s"))
            {
                builder.append(word.length() > minLen ? capitalize(word) : word).append(" ");
            }
            return builder.toString().trim();
        }
        return text;
    }

    public String decapitalize(String text)
    {
        if (text != null ? text.length() > 0 : false)
        {
            StringBuilder builder = new StringBuilder();
            for (String word : text.toLowerCase().split("\\s"))
            {
                builder.append(word.substring(0, 1).toLowerCase()).append(word.substring(1)).append(" ");
            }
            return builder.toString().trim();
        }
        return text;
    }

    /**
     * @return the duration
     */
    public String getDuration()
    {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(String duration)
    {
        this.duration = duration;
    }

    /**
     * @return the exceptionsList
     */
    private ArrayList<Exception> getExceptionsList()
    {
        return exceptionsList;
    }

    /**
     * @param exceptionsList the exceptionsList to set
     */
    public void setExceptionsList(ArrayList<Exception> exceptionsList)
    {
        this.exceptionsList = exceptionsList;
    }

    /**
     * @return the accountNo
     */
    public String getAccountNo()
    {
        return accountNo;
    }

    /**
     * @param accountno the accountNo to set
     */
    public void setAccountNo(String accountno)
    {
        this.accountNo = accountno;
    }

    /**
     * @return the extraIndent
     */
    public String getExtraIndent()
    {
        return extraIndent;
    }

    /**
     * @param extraIndent the extraIndent to set
     */
    public void setExtraIndent(String extraIndent)
    {
        this.extraIndent = extraIndent;
    }

    /**
     * @return the xapiRespCode
     */
    public String getXapiRespCode()
    {
        return xapiRespCode;
    }

    /**
     * @param xapiRespCode the xapiRespCode to set
     */
    public void setXapiRespCode(String xapiRespCode)
    {
        this.xapiRespCode = xapiRespCode;
    }

    public void setCall(String callRef, Object callObject)
    {
        this.callsMap.put((callsMap.size() + 101) + "~" +callRef, convertToString(callObject));
    }

    /**
     * @return the cardNumber
     */
    public String getCardNumber()
    {
        return cardNumber;
    }

    /**
     * @param cardNumber the cardNumber to set
     */
    public void setCardNumber(String cardNumber)
    {
        this.cardNumber = cardNumber;
    }

    /**
     * @return the narration
     */
    public String getNarration()
    {
        return narration;
    }

    /**
     * @param narration the narration to set
     */
    public void setNarration(String narration)
    {
        this.narration = narration;
    }

    public BLScoreCard getBlScoreCard()
    {
        return blScoreCard;
    }

    public void setBlScoreCard(BLScoreCard blScoreCard)
    {
        this.blScoreCard = blScoreCard;
    }

    public String indentAllLines(String text)
    {
        String line = "", buffer = "";
        try (BufferedReader bis = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes()))))
        {
            while (line != null)
            {
                buffer += extraIndent + line + "\r\n";
                line = bis.readLine();
            }
        } catch (IOException ex)
        {
            return buffer;
        }

        return extraIndent + buffer.trim();
    }

    public String getXapiRespMsg() {
        return xapiRespMsg;
    }

    public void setXapiRespMsg(String xapiRespMsg) {
        this.xapiRespMsg = xapiRespMsg;
    }
}
