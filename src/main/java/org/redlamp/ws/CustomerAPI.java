package org.redlamp.ws;

import org.redlamp.extras.CustomerHandler;
import org.redlamp.mdlwre.AccountMiddleware;
import org.redlamp.model.CustomerRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Olupot.D
 */
@Path("/v1.1")
public class CustomerAPI {

    @POST
    @Path("/createCustomer")
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
        } else if (request.getBank_verification_number() == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Invalid BVN");
        }else {
            try (CustomerHandler helper = new CustomerHandler()) {
                outcome = helper.createCustomer(request);
            }
        }
        return outcome;
    }
//
    @POST
    @Path("/createCustomerWithDep")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> createCustomerWithDep(@HeaderParam("authcode") String authcode,
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
        } else if (request.getBank_verification_number() == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Invalid BVN");
        } else {
            try (CustomerHandler helper = new CustomerHandler()) {
                outcome = helper.createCustomerAndDeposit(request);
            }
        }
        return outcome;
    }
//
    @GET
    @Path("/loadCustomerMetaData")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> findCustCreationFeatures(@HeaderParam("authcode") String authcode,
                                                     @HeaderParam("authpass") String authpass) {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        } else if (authpass == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        }
//        else if (request == null) {
//            outcome.put("responseCode", 12);
//            outcome.put("responseTxt", "Invalid request data");
//        }
        else {
            try (CustomerHandler helper = new CustomerHandler()) {
                outcome = helper.findRimCreationFeatures();
            }
        }
        return outcome;
    }

}