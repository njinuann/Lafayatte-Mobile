package org.redlamp.ws;

import org.redlamp.extras.AccountHandler;
import org.redlamp.mdlwre.AccountMiddleware;
import org.redlamp.model.AccountRequest;
import org.redlamp.model.CustomerRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Olupot.D
 */
@Path("/v1.1/account")
public class AccountAPI {

    @GET
    @Path("/loadAccountMetaData")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> loadAccountMetaData(@HeaderParam("authcode") String authcode,
                                                   @HeaderParam("authpass") String authpass) {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        } else if (authpass == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        } else {
            try (AccountHandler helper = new AccountHandler()) {
                outcome = helper.findAccountCreationFeatures();
            }
        }
        return outcome;
    }

    @GET
    @Path("/{account}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getAccountDetailsByAccount(@HeaderParam("authcode") String authcode,
                                                          @HeaderParam("authpass") String authpass, @PathParam("account") String account) {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        } else if (authpass == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        } else if (account == null) {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing account path var {account}");
        } else {
            try (AccountMiddleware helper = new AccountMiddleware()) {
                outcome = helper.findByAccount(account);
            }
        }
        return outcome;
    }

    @POST
    @Path("/createDepositAccount")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> createAccount(@HeaderParam("authcode") String authcode,
                                             @HeaderParam("authpass") String authpass, final AccountRequest request) {

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
            try (AccountHandler helper = new AccountHandler()) {
                outcome = helper.createAccount(request);
            }
        }
        return outcome;
    }

    @POST
    @Path("/verifyBvn")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> verifyCustomerBvn(@HeaderParam("authcode") String authcode,
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
            try (AccountHandler helper = new AccountHandler()) {
                outcome = helper.getBVNDetail( request);
            }
        }
        return outcome;
    }
}