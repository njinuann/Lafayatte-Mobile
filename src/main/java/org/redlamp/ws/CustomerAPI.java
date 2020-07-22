package org.redlamp.ws;

import org.redlamp.extras.CustomerHandler;
import org.redlamp.model.CustomerRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Olupot.D
 */
@Path("/v1.1/customer")
public class CustomerAPI {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> createCustomer(@HeaderParam("authcode") String authcode,
                                              @HeaderParam("authpass") String authpass, final CustomerRequest request) {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        } else if (authpass == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        } else if (request == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Invalid request data");
        } else {
            try (CustomerHandler helper = new CustomerHandler()) {
                outcome = helper.createCustomer(request);
            }
        }
        return outcome;
    }

}