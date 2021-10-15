package org.redlamp.util;

import java.beans.PropertyVetoException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.redlamp.logger.IsoLogger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

public class XapiPool extends QBeanSupport implements Configurable
{

    private static Configuration cfg;
    private static ComboPooledDataSource transactionPool, servicePool;
    private static boolean isOffline;
    public static int MAX_MESSAGE_LENGTH = 0;

    public static int SERVICE_ID;
    public static String QT_CC;
    private static boolean configured;
    public static boolean nibss_allowed;

    public static String online_jdbc_url, online_jdbc_paswd, online_jdbc_user, jdbc_driver;
    private static String offline_jdbc_url, offline_jdbc_paswd, offline_jdbc_user, online_core, offline_core;
    protected static int max_stmts, max_stmt_per_conn, max_pool_size, init_conns, min_idle, offline_ping_interval;

    public static boolean hasProxy = false, startAlertsService;
    public static String lanProxyServerIp, SMSGatewayUser, SMSGatewayPassword, SMSGatewaySourceName, SMSGatewayURL,
            CLEARING_DB, NIBSS_ONLINE;
    public static int lanProxySeverPort;
    public static long smsAlertInterval = 30;

    private static ScheduledFuture<?> scheduleWithFixedDelay;
    public static boolean DEBUG_ENABLED;
    public static long SENDER_ID;
    public static int NIBSS_CHANNEL;

    public static String userId = "SYSTEM";
    private static TreeMap<String, BRSetting> settings = new TreeMap<>();
    public static BigDecimal maxDepositAmt,minCycleDpEvalAmount,
            maxWithdrawalAmt, maxCollectedBal, dpCycle1Score, dpCycle2Score, voluntaryDpMonths, dpAveVolumeMonths,
            dpAveVolPercentage, dpMaxTxnPeriod, dpMidTxnPeriod, dpMinTxnPeriod, dp3monthScore, dp6monthScore, dp9monthScore,
            repmt14DaysScore, repmt25DaysScore, repmt30DaysScore, repmt3DaysLateDaysScore, minDepositorLnAmount, maxDepositorLnAmount;
    public static String allowedLnClass, allowedRimClassDepositor,allowedRimClassBorrower, allowedDpClass, dpTranCode;

    //Digital Borrower loan parameters
    public static BigDecimal minBorrowerLnAmount, maxBorrowerLnAmount, weightedInstCycle1Points, weightedInstCycle2Points, weightedInstCycle3Points,
            riskGroupNormalPoints, riskGroupMediumPoints, riskGroupOtherPoints, residenceStatusOwnPoints, residenceStatusOtherPoints,
            definitionScoreA, definitionScoreB, definitionScoreC, definitionScoreD;
    public static Long latePmtMoreThan7Days, latePmtMoreThan30Days, days7lateInstallmentCount, days30lateInstallmentCount, minInstallments, previousLoanClosureDays,
            averageMonths, allowedNoOfLoansPerYear,minLoanterm,repayAfterDays,borrowerLoanClassCode,depositorLoanClassCode;
    public static String allowedBorrowerLnClass,  allowedBorrowerDpClass, borrowerdpTranCode,
            weightedInstCycle1, weightedInstCycle2, weightedInstCycle3, weightScoreParam = "WS01", scoreDefParameter = "DS01", defaultInstalmentParam = "DI01", loanDurationParam = "DL01", cycleParam = "CY01",
            riskGroupNormal, riskGroupMedium, riskGroupOther, residenceStatusOwn, residenceStatusOther,defaultLoanPeriod,allowedStates;

    public void setConfiguration(Configuration cfg) throws ConfigurationException
    {
        XapiPool.cfg = cfg;
    }

    @Override
    public synchronized void start()
    {
        IsoLogger.setLogger(getLog());

        jdbc_driver = cfg.get("jdbc.driver");
        online_jdbc_url = cfg.get("online.jdbc.url") + "?charset=iso_1&GET_COLUMN_LABEL_FOR_NAME=true";
        online_jdbc_paswd = EICrypt.decrypt(cfg.get("online.jdbc.paswd"));
        online_jdbc_user = cfg.get("online.jdbc.user");
        offline_jdbc_url = cfg.get("offline.jdbc.url") + "?charset=iso_1&GET_COLUMN_LABEL_FOR_NAME=true";
        offline_jdbc_paswd = EICrypt.decrypt(cfg.get("offline.jdbc.paswd"));
        offline_jdbc_user = cfg.get("offline.jdbc.user");

        max_stmts = cfg.getInt("max.stmts", 0);
        max_stmt_per_conn = cfg.getInt("max.stmt.per.conn", 0);
        max_pool_size = cfg.getInt("max.conns", 5);
        min_idle = cfg.getInt("min.idle", 2);
        init_conns = cfg.getInt("init.conns", 2);
        offline_ping_interval = cfg.getInt("offline.ping.interval", 5);

        SENDER_ID = cfg.getInt("SENDER_ID", 1);
        NIBSS_CHANNEL = cfg.getInt("NIBSS_CHANNEL", 3);

        XapiCodes.BANK_CODE = cfg.get("bank.code", "999100");
        XapiCodes.DATE_FORMAT = cfg.get("date.format", "dd-MMM-yyyy");

        QT_CC = cfg.get("QT_CC", "717");

        online_core = cfg.get("core.schema", "banking");
        XapiCodes.xapiSchema = cfg.get("channel.schema", "xapi");
        offline_core = cfg.get("core.offline.schema", "xapi_offline");

        NIBSS_ONLINE = cfg.get("nibss_online", "nibss");
        MAX_MESSAGE_LENGTH = cfg.getInt("MAX_MESSAGE_LENGTH", 160);

        XapiCodes.BILL_PAYMENT_FLAG = cfg.get("billpayment", "P");
        XapiCodes.airtime = cfg.get("airtime", "C");
        XapiCodes.quick_teller = cfg.get("quick-teller", "Q");
        XapiCodes.internal_transfer = cfg.get("internal.transfer", "T");
        XapiCodes.balance_enquiry = cfg.get("balance.enquiry", "B");
        XapiCodes.mini_statement = cfg.get("mini.statement", "M");

        XapiCodes.NIBSS_WSDL = cfg.get("nibss.url", "http://192.168.4.23:5588/NIPClient/CNService");
        XapiCodes.PHONE_PREFIX = cfg.get("phone.prefix", "234");
        XapiCodes.DEFAULT_USER = cfg.get("default.user", "PHOENIX");

        XapiCodes.SEND_ALERT = cfg.getBoolean("sendAlerts", false);
        XapiCodes.rimClassCode = cfg.get("rimClassCode", "120");
        XapiCodes.defaultRimClass = cfg.getLong("defaultRimClass", 111L);

        XapiCodes.identityType = cfg.getLong("identityType", 28L);
        XapiCodes.marketingId = cfg.getLong("marketingId", 2L);
        XapiCodes.defaultBranch = cfg.getLong("defaultBranch", 120L);
        XapiCodes.depositClassCode = cfg.getLong("depositClassCode", 120L);

        DEBUG_ENABLED = cfg.getBoolean("DEBUG_ENABLED", false);
        nibss_allowed = cfg.getBoolean("nibss.allow", false);

        hasProxy = cfg.getBoolean("lanHasProxy", false);
        startAlertsService = cfg.getBoolean("startAlertsService", false);
        lanProxySeverPort = cfg.getInt("lanProxyPort", 3128);
        lanProxyServerIp = cfg.get("lanProxyServer", "10.176.11.3");
        SMSGatewayUser = cfg.get("SMSGatewayUser", "Advans");
        SMSGatewayPassword = EICrypt.decrypt(cfg.get("SMSGatewayPassword", "601421201311201750021850950"));
        SMSGatewaySourceName = cfg.get("SMSGatewaySourceName", "Advans");
        SMSGatewayURL = cfg.get("SMSGatewayURL", "http://localhost:8000/api/v1/alerts");
        smsAlertInterval = cfg.getLong("smsAlertInterval", 30);

        SERVICE_ID = cfg.getInt("service.id", 44);
        servicePool = new ComboPooledDataSource();
        try
        {
            servicePool.setDriverClass(jdbc_driver);
        } catch (PropertyVetoException e)
        {
        }
        servicePool.setJdbcUrl(offline_jdbc_url);
        servicePool.setUser(offline_jdbc_user);
        servicePool.setPassword(offline_jdbc_paswd);

        servicePool.setMinPoolSize(1);
        servicePool.setMaxPoolSize(2);
        servicePool.setAcquireRetryDelay(3000);
        servicePool.setAcquireRetryAttempts(0);// retry indefinitely
        servicePool.setTestConnectionOnCheckout(true);
        servicePool.setTestConnectionOnCheckin(true);

        scheduleWithFixedDelay = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                try (Connection conn = servicePool.getConnection(); Statement stm = conn.createStatement();)
                {
                    try (ResultSet rset = stm.executeQuery("select ltrim(rtrim(status)) from eb_switch_mgr"))
                    {
                        if (rset.next())
                        {
                            if ("ONLINE".equalsIgnoreCase(rset.getString(1)))
                            {
                                XapiPool.switchToOnline();

                            }
                            else
                            {
                                XapiPool.switchToOffline();
                            }
                        }
                    }
                } catch (SQLException e)
                {
                    IsoLogger.getLogger().error(e);
                }
            }
        }, 5, offline_ping_interval, TimeUnit.SECONDS);
        super.start();

    }

    public static Connection getConnection() throws SQLException
    {
        return transactionPool != null && transactionPool.getNumIdleConnections() > 0 ? transactionPool.getConnection()
                : buildConnection();
    }

    private static Connection buildConnection() throws SQLException
    {
        return DriverManager.getConnection(isOffline ? offline_jdbc_url : online_jdbc_url,
                isOffline ? offline_jdbc_user : online_jdbc_user, isOffline ? offline_jdbc_paswd : online_jdbc_paswd);
    }

    public static void switchToOnline()
    {
        if (isOffline)
        {
            isOffline = false;
            configureTransactionPool();
            IsoLogger.getLogger().info("Service Status: ONLINE");
        }
        else if (!configured)
        {
            configureTransactionPool();
            IsoLogger.getLogger().info("Service Status: ONLINE");

        }
    }

    public static boolean isInOfflineMode()
    {
        return isOffline;
    }

    public static void switchToOffline()
    {
        if (!isOffline)
        {
            isOffline = true;
            IsoLogger.getLogger().info("Service Status: OFFLINE");
        }
        else if (!configured)
        {
            configureTransactionPool();
            IsoLogger.getLogger().info("Service Status: OFFLINE");
        }
    }

    private static void configureTransactionPool()
    {
        // close out the existing object prior to
        if (transactionPool != null)
        {
            transactionPool.close();
            transactionPool = null;
            IsoLogger.getLogger().info("Transaction Pool Closed");
        }
        transactionPool = new ComboPooledDataSource();
        try
        {
            transactionPool.setDriverClass(jdbc_driver);
        } catch (PropertyVetoException e)
        {
        }

        IsoLogger.getLogger().info("Activating " + (isOffline ? "offline channel" : "online channel"));
        XapiCodes.coreschema = isOffline ? offline_core : online_core;
        CLEARING_DB = isOffline ? offline_core : NIBSS_ONLINE;

        transactionPool.setJdbcUrl(isOffline ? offline_jdbc_url : online_jdbc_url);
        transactionPool.setUser(isOffline ? offline_jdbc_user : online_jdbc_user);
        transactionPool.setPassword(isOffline ? offline_jdbc_paswd : online_jdbc_paswd);

        transactionPool.setMinPoolSize(min_idle);
        transactionPool.setInitialPoolSize(init_conns);
        transactionPool.setMaxPoolSize(max_pool_size);

        transactionPool.setAcquireRetryDelay(3000);
        transactionPool.setAcquireRetryAttempts(0);// retry indefinately

        transactionPool.setMaxStatements(max_stmts);
        transactionPool.setMaxStatementsPerConnection(max_stmt_per_conn);

        transactionPool.setTestConnectionOnCheckin(true);
        transactionPool.setMaxConnectionAge((int) TimeUnit.MINUTES.toSeconds(45));

        configured = true;

        IsoLogger.getLogger().info((isOffline ? "offline channel" : "online channel") + " activation completed");
        try (Connection testConn = transactionPool.getConnection())
        {
            IsoLogger.getLogger().info("Testing " + (isOffline ? "offline " : "online ") + "connection: "
                    + (testConn.isClosed() ? "Not Connected" : "Connected"));
        } catch (SQLException e)
        {
            IsoLogger.getLogger().error(e);
        }

        querySettings(transactionPool);
    }

    public static TreeMap<String, BRSetting> queryDBSettings(String module, ComboPooledDataSource transactionPool)
    {
        System.out.println("SELECT CODE, VALUE, MODULE, DESCRIPTION, MODIFIED_BY, DATE_MODIFIED " +
                "FROM xapi..EI_SETTING WHERE MODULE='" + module + "' ORDER BY CODE ASC");
        TreeMap<String, BRSetting> bRSettings = new TreeMap<>();
        try (Connection conn = transactionPool.getConnection(); Statement stm = conn.createStatement();)
        {
            try (ResultSet rs = stm.executeQuery("SELECT CODE, VALUE, MODULE, DESCRIPTION, MODIFIED_BY, DATE_MODIFIED " +
                    "FROM xapi..EI_SETTING WHERE MODULE='" + module + "' ORDER BY CODE ASC"))
            {
                while (rs.next())
                {
                    BRSetting bRSetting = new BRSetting();
                    bRSetting.setCode(rs.getString("CODE"));
                    // bRSetting.setEncrypted(BRCrypt.isEncrypted(rs.getString("VALUE")));
                    // bRSetting.setValue(bRSetting.isEncrypted() ? BRCrypt.decrypt(rs.getString("VALUE")) : rs.getString("VALUE"));
                    bRSetting.setValue(rs.getString("VALUE"));
                    bRSetting.setModule(rs.getString("MODULE"));
                    bRSetting.setDescription(rs.getString("DESCRIPTION"));
                    bRSetting.setLastModifiedBy(rs.getString("MODIFIED_BY"));
                    bRSetting.setDateModified(rs.getDate("DATE_MODIFIED"));
                    System.err.println(bRSetting.getCode() + "~" + bRSetting.getValue());
                    bRSettings.put(bRSetting.getCode(), bRSetting);
                }
            }
        } catch (Exception ex)
        {
            IsoLogger.getLogger().error(ex);
        }

        return bRSettings;
    }

    public static String resolveError(String errorCode)
    {
        String resolvedError = "96";
        if (errorCode.startsWith("L") || errorCode.startsWith("00"))
        {
            resolvedError = errorCode;
        }
        else if ("-30005".equalsIgnoreCase(errorCode))
        {
            resolvedError = "L04";
        }
        else if ("-51216".equalsIgnoreCase(errorCode))
        {
            resolvedError = "L03";
        }
        return resolvedError;
    }

    public static boolean querySettings(ComboPooledDataSource transactionPool)
    {
        setSettings(queryDBSettings("Mobile", transactionPool));

        maxDepositAmt = getDecimalSetting("maxDepositAmt");
        minCycleDpEvalAmount= getDecimalSetting("minCycleDpEvalAmount");
        maxWithdrawalAmt = getDecimalSetting("maxWithdrawalAmt");
        maxCollectedBal = getDecimalSetting("maxCollectedBal");
        dpCycle1Score = getDecimalSetting("dpCycle1Score");
        dpCycle2Score = getDecimalSetting("dpCycle2Score");
        voluntaryDpMonths = getDecimalSetting("voluntaryDpMonths");
        dpAveVolumeMonths = getDecimalSetting("dpAveVolumeMonths");
        dpAveVolPercentage = getDecimalSetting("dpAveVolPercentage");
        dpMaxTxnPeriod = getDecimalSetting("dpMaxTxnPeriod");
        dpMidTxnPeriod = getDecimalSetting("dpMidTxnPeriod");
        dpMinTxnPeriod = getDecimalSetting("dpMinTxnPeriod");
        dp3monthScore = getDecimalSetting("dp3monthScore");
        dp6monthScore = getDecimalSetting("dp6monthScore");
        dp9monthScore = getDecimalSetting("dp9monthScore");
        repmt14DaysScore = getDecimalSetting("repmt14DaysScore");
        repmt25DaysScore = getDecimalSetting("repmt25DaysScore");
        repmt30DaysScore = getDecimalSetting("repmt30DaysScore");
        repmt3DaysLateDaysScore = getDecimalSetting("repmt3DaysLateDaysScore");
        maxDepositorLnAmount = getDecimalSetting("maxDepositorLnAmount");
        minDepositorLnAmount = getDecimalSetting("minDepositorLnAmount");
        allowedRimClassDepositor = getSetting("allowedRimClassDepositor");
        allowedRimClassBorrower = getSetting("allowedRimClassBorrower");
        allowedDpClass = getSetting("allowedDpClass");
        allowedLnClass = getSetting("allowedLnClass");
        dpTranCode = getSetting("dpTranCode");
        //loan
        maxBorrowerLnAmount = getDecimalSetting("maxBorrowerLnAmount");
        minBorrowerLnAmount = getDecimalSetting("minBorrowerLnAmount");
        weightedInstCycle1Points = getDecimalSetting("weightedInstCycle1Points");
        weightedInstCycle2Points = getDecimalSetting("weightedInstCycle2Points");
        weightedInstCycle3Points = getDecimalSetting("weightedInstCycle3Points");
        riskGroupNormalPoints = getDecimalSetting("riskGroupNormalPoints");
        riskGroupMediumPoints = getDecimalSetting("riskGroupMediumPoints");
        riskGroupOtherPoints = getDecimalSetting("riskGroupOtherPoints");
        residenceStatusOwnPoints = getDecimalSetting("residenceStatusOwnPoints");
        residenceStatusOtherPoints = getDecimalSetting("residenceStatusOtherPoints");
        definitionScoreA = getDecimalSetting("definitionScoreA");
        definitionScoreB = getDecimalSetting("definitionScoreB");
        definitionScoreC = getDecimalSetting("definitionScoreC");
        definitionScoreD = getDecimalSetting("definitionScoreD");
        latePmtMoreThan7Days = getLongSetting("latePmtMoreThan7Days");
        latePmtMoreThan30Days = getLongSetting("latePmtMoreThan30Days");
        days7lateInstallmentCount = getLongSetting("days7lateInstallmentCount");
        days30lateInstallmentCount = getLongSetting("days30lateInstallmentCount");
        minInstallments = getLongSetting("minInstallments");
        previousLoanClosureDays = getLongSetting("previousLoanClosureDays");
        averageMonths = getLongSetting("averageMonths");
        allowedNoOfLoansPerYear = getLongSetting("allowedNoOfLoansPerYear");
        minLoanterm = getLongSetting("minLoanterm");
        repayAfterDays= getLongSetting("repayAfterDays");

        borrowerLoanClassCode = getLongSetting("borrowerLoanClassCode");
        depositorLoanClassCode = getLongSetting("depositorLoanClassCode");
        allowedBorrowerLnClass = getSetting("allowedBorrowerLnClass");
        allowedBorrowerDpClass = getSetting("allowedBorrowerDpClass");
        borrowerdpTranCode = getSetting("borrowerdpTranCode");
        weightedInstCycle1 = getSetting("weightedInstCycle1");
        weightedInstCycle2 = getSetting("weightedInstCycle2");
        weightedInstCycle3 = getSetting("weightedInstCycle3");
        weightScoreParam = getSetting("weightScoreParam");
        scoreDefParameter = getSetting("scoreDefParameter");
        defaultInstalmentParam = getSetting("defaultInstalmentParam");
        loanDurationParam = getSetting("loanDurationParam");
        cycleParam = getSetting("cycleParam");
        riskGroupNormal = getSetting("riskGroupNormal");
        riskGroupMedium = getSetting("riskGroupMedium");
        riskGroupOther = getSetting("riskGroupOther");
        residenceStatusOwn = getSetting("residenceStatusOwn");
        residenceStatusOther = getSetting("residenceStatusOther");
        defaultLoanPeriod = getSetting("defaultLoanPeriod");
        allowedStates = getSetting("allowedStates");


//        readCsvListProperty(allowedRimClass, getSetting("allowedRimClass"));
//        readCsvListProperty(allowedDpClass, getSetting("allowedDpClass"));
//        readCsvListProperty(allowedLnClass, getSetting("allowedLnClass"));
//        readCsvListProperty(dpTranCode, getSetting("dpTranCode"));

        return !getSettings().isEmpty();
    }

    public static String getSetting(String code)
    {
        if (getSettings().containsKey(code))
        {
            return getSettings().get(code).getValue();
        }
        return null;
    }

    public static long getLongSetting(String code)
    {
        try
        {
            if (getSettings().containsKey(code))
            {
                return Long.parseLong(getSettings().get(code).getValue());
            }
        } catch (Exception e)
        {
            IsoLogger.getLogger().error(e);
        }
        return 0L;
    }

    public static int getIntSetting(String code)
    {
        try
        {
            if (getSettings().containsKey(code))
            {
                return Integer.parseInt(getSettings().get(code).getValue());
            }
        } catch (Exception e)
        {
            IsoLogger.getLogger().error(e);
        }
        return 0;
    }

    public static BigDecimal getDecimalSetting(String code)
    {
        try
        {
            if (getSettings().containsKey(code))
            {
                return new BigDecimal(getSettings().get(code).getValue());
            }
        } catch (Exception e)
        {
            IsoLogger.getLogger().error(e);
        }
        return BigDecimal.ZERO;
    }
    public static String convertToXml(Object object, boolean formatted)
    {
        if (!isBlank(object))
        {
            try
            {
                Class clazz = object instanceof JAXBElement ? ((JAXBElement) object).getDeclaredType() : object.getClass();
                Marshaller marshaller = JAXBContext.newInstance(clazz).createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);

                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                StringWriter writer = new StringWriter();
                marshaller.marshal(object, writer);
                return cleanXmlXters(writer.toString().trim());
            }
            catch (Exception ex)
            {
                IsoLogger.getLogger().error(ex);
            }
        }
        return null;
    }
    public static String cleanXmlXters(String xmlText)
    {
        return xmlText.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&").replaceAll("&quot;", "\"").replaceAll("&apos;", "\'").trim();
    }
    public static boolean isBlank(Object object)
    {
        return object == null || "".equals(String.valueOf(object).trim()) || "null".equals(String.valueOf(object).trim()) || String.valueOf(object).trim().toLowerCase().contains("---select");
    }
    @SuppressWarnings("unchecked")
    public static void readCsvListProperty(ArrayList arrayList, String csvList)
    {
        System.out.println(arrayList + "readCsvListProperty ++ " + csvList);
        arrayList.clear();
        if (csvList != null)
        {
            for (String listItem : csvList.replace(";", ",").split(","))
            {
                arrayList.add(listItem.trim());
            }
        }
    }

    public static TreeMap<String, BRSetting> getSettings()
    {
        return settings;
    }

    public static void setSettings(TreeMap<String, BRSetting> aSettings)
    {
        settings = aSettings;
    }

    @Override
    public void destroy()
    {
        scheduleWithFixedDelay.cancel(true);
        if (transactionPool != null)
        {
            transactionPool.close();
            transactionPool = null;
        }
        if (servicePool != null)
        {
            servicePool.close();
            servicePool = null;
        }
        super.destroy();
    }

}
