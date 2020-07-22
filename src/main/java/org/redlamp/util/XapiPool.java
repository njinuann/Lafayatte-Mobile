package org.redlamp.util;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.redlamp.logger.IsoLogger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class XapiPool extends QBeanSupport implements Configurable {

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

	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		XapiPool.cfg = cfg;
	}

	@Override
	public synchronized void start() {
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
		try {
			servicePool.setDriverClass(jdbc_driver);
		} catch (PropertyVetoException e) {
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

		scheduleWithFixedDelay = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try (Connection conn = servicePool.getConnection(); Statement stm = conn.createStatement();) {
					try (ResultSet rset = stm.executeQuery("select ltrim(rtrim(status)) from eb_switch_mgr")) {
						if (rset.next()) {
							if ("ONLINE".equalsIgnoreCase(rset.getString(1))) {
								XapiPool.switchToOnline();
							} else {
								XapiPool.switchToOffline();
							}
						}
					}
				} catch (SQLException e) {
					IsoLogger.getLogger().error(e);
				}
			}
		}, 5, offline_ping_interval, TimeUnit.SECONDS);
		super.start();
	}

	public static Connection getConnection() throws SQLException {
		return transactionPool != null && transactionPool.getNumIdleConnections() > 0 ? transactionPool.getConnection()
				: buildConnection();
	}

	private static Connection buildConnection() throws SQLException {
		return DriverManager.getConnection(isOffline ? offline_jdbc_url : online_jdbc_url,
				isOffline ? offline_jdbc_user : online_jdbc_user, isOffline ? offline_jdbc_paswd : online_jdbc_paswd);
	}

	public static void switchToOnline() {
		if (isOffline) {
			isOffline = false;
			configureTransactionPool();
			IsoLogger.getLogger().info("Service Status: ONLINE");
		} else if (!configured) {
			configureTransactionPool();
			IsoLogger.getLogger().info("Service Status: ONLINE");
		}
	}

	public static boolean isInOfflineMode() {
		return isOffline;
	}

	public static void switchToOffline() {
		if (!isOffline) {
			isOffline = true;
			IsoLogger.getLogger().info("Service Status: OFFLINE");
		} else if (!configured) {
			configureTransactionPool();
			IsoLogger.getLogger().info("Service Status: OFFLINE");
		}
	}

	private static void configureTransactionPool() {
		// close out the existing object prior to
		if (transactionPool != null) {
			transactionPool.close();
			transactionPool = null;
			IsoLogger.getLogger().info("Transaction Pool Closed");
		}
		transactionPool = new ComboPooledDataSource();
		try {
			transactionPool.setDriverClass(jdbc_driver);
		} catch (PropertyVetoException e) {
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
		try (Connection testConn = transactionPool.getConnection()) {
			IsoLogger.getLogger().info("Testing " + (isOffline ? "offline " : "online ") + "connection: "
					+ (testConn.isClosed() ? "Not Connected" : "Connected"));
		} catch (SQLException e) {
			IsoLogger.getLogger().error(e);
		}
	}

	@Override
	public void destroy() {
		scheduleWithFixedDelay.cancel(true);
		if (transactionPool != null) {
			transactionPool.close();
			transactionPool = null;
		}
		if (servicePool != null) {
			servicePool.close();
			servicePool = null;
		}
		super.destroy();
	}

}
