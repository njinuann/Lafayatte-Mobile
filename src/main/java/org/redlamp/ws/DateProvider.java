package org.redlamp.ws;

import java.text.SimpleDateFormat;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.redlamp.util.XapiCodes;

@Provider
@Produces("application/json")
public class DateProvider implements ContextResolver<ObjectMapper> {

	private ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("deprecation")
	public DateProvider() {
		SerializationConfig serConfig = mapper.getSerializationConfig();
		serConfig.setDateFormat(new SimpleDateFormat(XapiCodes.DATE_FORMAT));
		DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();
		deserializationConfig.setDateFormat(new SimpleDateFormat("MM/dd/yyyy"));
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
	}

	@Override
	public ObjectMapper getContext(Class<?> arg0) {
		return mapper;
	}

}
