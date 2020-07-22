package org.redlamp.extras;

import NIPClient.Channel.EnquireNameResponseReturn;
import NIPClient.Channel.NibssClient;
import org.redlamp.io.StringUtils;
import org.redlamp.logger.ApiLogger;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;

public class HttpHandler {

    public EnquireNameResponseReturn nibssNameLookup(String acct_no, String bank_code) {
        try {
            NibssClient client = new NibssClient();
            return client.nameEnquiry(StringUtils.stripDashes(acct_no), bank_code);
        } catch (RemoteException | ServiceException ex) {
            ApiLogger.getLogger().error(ex);
        }
        return null;
    }

    public boolean sendAlert(String acct_no, String txt_message,
                             String recipient_phone, String chargeable, String referenceNo) {

        return false;
    }
}
