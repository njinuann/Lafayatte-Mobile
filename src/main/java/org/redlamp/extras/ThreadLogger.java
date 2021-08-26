package org.redlamp.extras;


import org.redlamp.logger.ApiLogger;
import ug.ac.mak.java.logger.Log;

public class ThreadLogger implements Runnable
{

    ApiLogger logger;
    String log;
    Log getLogger;

    public ThreadLogger(ApiLogger logger, String log)
    {
        this.logger = logger;
        this.log = log;
        getLogger = logger.getLogger();
    }

    @Override
    public void run()
    {
        getLogger.info("ThreadLogger", this.log);
        // this.logger.log("ThreadLogger", this.log);
    }

}
