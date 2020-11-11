package org.redlamp.iso;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.Context;
import org.redlamp.interfaces.ISO;
import org.redlamp.logger.IsoLogger;

public class ServiceListener implements ISORequestListener, Configurable
{

    private Configuration configuration;

    @Override
    public void setConfiguration(Configuration configuration) throws ConfigurationException
    {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean process(ISOSource isoSource, ISOMsg isoMsg)
    {
        String spaceN = configuration.get("space");
        Long timeout = configuration.getLong("spaceTimeout");
        String queueN = configuration.get("queue");
        Context context = new Context();
        try {
            ISOMsg respMsg = (ISOMsg) isoMsg.clone();
            respMsg.setResponseMTI();
            context.put(ISO.REQUEST_KEY, isoMsg);
            context.put(ISO.RESPONSE_KEY, respMsg);
            context.put(ISO.RESOURCE_KEY, isoSource);
        }
        catch (ISOException ex) {
            IsoLogger.getLogger().error(ex);
        }
        SpaceFactory.getSpace(spaceN).out(queueN, context, timeout);
        return true;
    }
}
