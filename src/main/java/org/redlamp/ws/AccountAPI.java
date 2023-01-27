package org.redlamp.ws;

import org.redlamp.extras.AccountHandler;
import org.redlamp.mdlwre.AccountMiddleware;
import org.redlamp.model.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Olupot.D
 */
@Path("/v1.1/account")
public class AccountAPI
{

    @GET
    @Path("/loadAccountMetaData")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> loadAccountMetaData(@HeaderParam("authcode") String authcode,
                                                   @HeaderParam("authpass") String authpass)
    {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        }
        else if (authpass == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        }
        else
        {
            try (AccountHandler helper = new AccountHandler())
            {
                outcome = helper.findAccountCreationFeatures();
            }
        }
        return outcome;
    }

    @GET
    @Path("/{account}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getAccountDetailsByAccount(@HeaderParam("authcode") String authcode,
                                                          @HeaderParam("authpass") String authpass, @PathParam("account") String account)
    {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        }
        else if (authpass == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        }
        else if (account == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing account path var {account}");
        }
        else
        {
            try (AccountMiddleware helper = new AccountMiddleware())
            {
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
                                             @HeaderParam("authpass") String authpass, final AccountRequest request)
    {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        }
        else if (authpass == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        }
        else if (request == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Invalid request data");
        }
        else
        {
            try (AccountHandler helper = new AccountHandler())
            {
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
                                                 @HeaderParam("authpass") String authpass, final CustomerRequest request)
    {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        }
        else if (authpass == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        }
        else if (request == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Invalid request data");
        }
        else
        {
            try (AccountHandler helper = new AccountHandler())
            {
                outcome = helper.getBVNDetail(request);
            }
        }
        return outcome;
    }

    @POST
    @Path("/verifyLinkedCustomer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> verifyCustomerMobile(@HeaderParam("authcode") String authcode,
                                                    @HeaderParam("authpass") String authpass, final CustomerVerificationRequest request)
    {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        }
        else if (authpass == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        }
        else if (request == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Invalid request data");
        }
        else if (request.getAccount_no() == null || request.getPhone_number() == null || request.getDateOfBirth() == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "missing validation parameter");
        }
        else
        {
            try (AccountHandler helper = new AccountHandler())
            {
                outcome = helper.getLinkedCustomer(request);
            }
        }
        return outcome;
    }

    @POST
    @Path("/checkLoanEligibility")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> checkEligibility(@HeaderParam("authcode") String authcode,
                                                @HeaderParam("authpass") String authpass,
                                                final LoanRequest request)
    {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        }
        else if (authpass == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        }
        else if (request == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Invalid request data");
        }
        else if (request.getAccountNo() == null || request.getPhoneNumber() == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "missing validation parameter");
        }
        else
        {
            try (AccountHandler helper = new AccountHandler())
            {
                outcome = helper.checkLoanEligibility(request);
            }
        }
        return outcome;
    }


    @POST
    @Path("/eLoanApplication")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> loanApplication(@HeaderParam("authcode") String authcode,
                                               @HeaderParam("authpass") String authpass, final LoanRequest request)
    {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        }
        else if (authpass == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        }
        else if (request == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Invalid request data");
        }
        else if (request.getAccountNo() == null || request.getPhoneNumber() == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "missing validation parameter[account or phone number]");
        }
        else if (request.getPeriod() == null || request.getTerm() == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "missing Loan Period or Term");
        }
        else if (request.getLoanAmount() == null || request.getLoanAmount().compareTo(BigDecimal.ZERO) == 0)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "missing/Invalid Loan amount");
        }
        else
        {
            try (AccountHandler helper = new AccountHandler())
            {
                outcome = helper.loanApplication(request);
            }
        }
        return outcome;
    }


    @POST
    @Path("/repayLoan")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> loanRepayment(@HeaderParam("authcode") String authcode,
                                             @HeaderParam("authpass") String authpass, final LoanRepaymentRequest request)
    {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        }
        else if (authpass == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        }
        else if (request == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Invalid request data");
        }
        else if (request.getRepaymentDpAccountNo() == null || request.getPhoneNumber() == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "missing validation parameter");
        }
        else if (request.getRepaymentAmount() == null || request.getRepaymentAmount().compareTo(BigDecimal.ZERO) == 0)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "missing/Invalid Loan amount");
        }
        else
        {
            try (AccountHandler helper = new AccountHandler())
            {
                outcome = helper.loanRepayment(request);
            }
        }
        return outcome;
    }


    @POST
    @Path("/checkLoanBalance")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> checkRepaymentBalance(@HeaderParam("authcode") String authcode,
                                                     @HeaderParam("authpass") String authpass, final LoanRepaymentRequest request)
    {

        Map<String, Object> outcome = new HashMap<>();
        if (authcode == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header code {authcode}");
        }
        else if (authpass == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Missing authorization header password {authpass}");
        }
        else if (request == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "Invalid request data");
        }
        else if (request.getRepaymentDpAccountNo() == null || request.getPhoneNumber() == null)
        {
            outcome.put("responseCode", 12);
            outcome.put("responseTxt", "missing validation parameter");
        }
        else
        {
            try (AccountHandler helper = new AccountHandler())
            {
                outcome = helper.checkLoanBalance(request,true);
            }
        }
        return outcome;
    }
}