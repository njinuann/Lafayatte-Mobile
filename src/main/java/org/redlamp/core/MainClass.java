package org.redlamp.core;

import com.sybase.jdbcx.SybDriver;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.gui.ISOChannelPanel;
import org.jpos.iso.gui.ISOMeter;
import org.jpos.iso.gui.IsoCast;
import org.jpos.q2.Q2;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.redlamp.beans.ResourceBean;
import org.redlamp.logger.ApiLogger;
import org.redlamp.logger.IsoLogger;

import java.lang.management.ManagementFactory;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MainClass {

    public static final ExecutorService runner = Executors.newFixedThreadPool(5);
    public static IsoCast serviceUI;
    public static ScheduledFuture<?> scheduleWithFixedDelay;

    static {
        try {
            SybDriver sybDriver = (SybDriver) Class.forName("com.sybase.jdbc4.jdbc.SybDriver").newInstance();
            sybDriver.setVersion(com.sybase.jdbcx.SybDriver.VERSION_LATEST);
            DriverManager.registerDriver(sybDriver);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        for (String l : Collections.list(LogManager.getLogManager().getLoggerNames()))
            Logger.getLogger(l).setLevel(Level.OFF);
        MainClass mainClass = new MainClass();
        mainClass.setLookAndFeel();
        mainClass.startISOServer();
        mainClass.startWebServer();
    }

    public void startISOServer() {
        Q2 server = new Q2();
        server.start();
        ISOUtil.sleep(5000);
        IsoLogger.setLogger(server.getLog());
        showUI();
    }

    private void setLookAndFeel() {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | javax.swing.UnsupportedLookAndFeelException ex) {
            ApiLogger.getLogger().error(ex);
        }
        serviceUI = new IsoCast();
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                serviceUI.setVisible(true);
            }
        });
    }

    private void showUI() {
        ISOMeter ussdMeter = getIsoMeter("txnsvr");
        if (ussdMeter != null) {
            ussdMeter.setCaption("USSD");
            ussdMeter.setStatus("OPEN");
            serviceUI.setUssdIsoMeter(ussdMeter);
        }
    }

    public ISOMeter getIsoMeter(String serverName) {
        try {
            Object obj = (Object) NameRegistrar.get("server." + serverName);
            if (obj instanceof ISOChannel) {
                return new ISOChannelPanel((ISOChannel) obj, serverName).getISOMeter();
            } else if (obj instanceof Observable) {
                ISOChannelPanel icp = new ISOChannelPanel(serverName);
                ((Observable) obj).addObserver(icp);
                return icp.getISOMeter();
            }
            return null;
        } catch (NotFoundException ex) {
            return null;
        }
    }

    public void startWebServer() {
        try {
            Resource xmlConfigFile = Resource.newResource("conf/server.xml");
            XmlConfiguration configuration = new XmlConfiguration(xmlConfigFile.getInputStream());
            Server server = (Server) configuration.configure();
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            server.setRequestLog(new ApiLogger());
            context.setContextPath("/");
            server.setHandler(context);
            ServletHolder jerseyServlet = context
                    .addServlet(com.sun.jersey.spi.container.servlet.ServletContainer.class, "/advanslite/*");
            jerseyServlet.setInitOrder(1);
            jerseyServlet.setInitParameter("com.sun.jersey.config.property.packages", "org.redlamp.ws");
            jerseyServlet.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

            // 3. CreatingManaged Managed Bean container
            MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());

            // 4. Adding Managed Bean container to the server as an Event Listener and Bean
            server.addEventListener(mbContainer);
            server.addBean(mbContainer);
            server.addBean(new ResourceBean());

            // 5. Adding Log
            server.addBean(Log.getLog());

            server.start();
            server.join();
        } catch (Exception ex) {
            ApiLogger.getLogger().error(ex);
        }
    }

}
