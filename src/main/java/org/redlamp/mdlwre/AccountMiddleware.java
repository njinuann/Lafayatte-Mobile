package org.redlamp.mdlwre;

import java.util.HashMap;
import java.util.Map;

import org.redlamp.extras.AccountHandler;
import org.redlamp.extras.CustomerHandler;
import org.redlamp.interfaces.ISO;
import org.redlamp.logger.ApiLogger;
import org.redlamp.model.AccountRequest;
import org.redlamp.model.CustomerRequest;
import org.redlamp.model.NameInquiryRequest;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

public class AccountMiddleware implements AutoCloseable, ISO
{

    private StringBuilder builder;

    public AccountMiddleware()
    {
        setBuilder(new StringBuilder());
    }

    @Override
    public void close()
    {
        getBuilder().setLength(0);
    }

    public Map<String, Object>  processNameLookup(NameInquiryRequest inquiryRequest)
    {
        Map<String, Object> response = new HashMap<>();
        try (AccountHandler handler = new AccountHandler())
        {
            if (XapiCodes.BANK_CODE.equals(inquiryRequest.getBank_code()))
            {
                return handler.internalNameLookup(inquiryRequest.getAccount_no());
            }
            if (!XapiPool.nibss_allowed)
            {
                response.put("responseCode", "58");
                response.put("responseTxt", XapiCodes.getErrorDesc(String.valueOf(response.get("responseCode"))));
                return response;
            }
            response = handler.processNibssNameEnquiry(inquiryRequest.getAccount_no(), inquiryRequest.getBank_code());
            return response;
        } finally
        {
            if (!response.containsKey("responseTxt"))
                response.put("responseTxt", XapiCodes.getErrorDesc(String.valueOf(response.get("responseCode"))));
        }
    }

    public Map<String, Object> getDepAcctList(String acct_no)
    {
        Map<String, Object> local = new HashMap<String, Object>();
        try (AccountHandler handler = new AccountHandler())
        {
            local = handler.depositAccountList(acct_no);
        } catch (Exception ex)
        {
            ApiLogger.getLogger().error(ex);
        }
        return local;
    }

    public Map<String, Object> getLoanBillSchedule(String acct_no)
    {
        Map<String, Object> local = new HashMap<String, Object>();
        try (AccountHandler handler = new AccountHandler())
        {
            local = handler.loanBillSchedule(acct_no);
        } finally
        {
            local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
        }
        return local;
    }

    public Map<String, Object> getLoanAcctList(Integer cust_no)
    {
        Map<String, Object> local = new HashMap<String, Object>();
        try (AccountHandler handler = new AccountHandler())
        {
            local = handler.loanAccounts(cust_no);
        } finally
        {
            local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
        }
        return local;
    }

    public Map<String, Object> getBankList()
    {
        Map<String, Object> local = new HashMap<String, Object>();
        try (AccountHandler handler = new AccountHandler())
        {
            local = handler.getBankList();
        } finally
        {
            local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
        }
        return local;
    }

    public Map<String, Object> loanAccountInquiry(String acct_no, String acct_type)
    {
        Map<String, Object> local = new HashMap<String, Object>();
        if (XapiPool.isInOfflineMode())
        {
            local.put("responseCode", OFFLINE_ACTIVATED);
            local.put("responseTxt", XapiCodes.getErrorDesc(OFFLINE_ACTIVATED));
            return local;
        }
        try (AccountHandler handler = new AccountHandler())
        {
            local = handler.loanAccountInquiry(acct_no, acct_type);
        } finally
        {
            local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
        }
        return local;
    }

    public Map<String, Object> getAccountStatement(String acct_no)
    {
        Map<String, Object> local = new HashMap<String, Object>();
        if (XapiPool.isInOfflineMode())
        {
            local.put("responseCode", OFFLINE_ACTIVATED);
            local.put("responseTxt", XapiCodes.getErrorDesc(OFFLINE_ACTIVATED));
            return local;
        }

        try (AccountHandler handler = new AccountHandler())
        {
            local = handler.accountStatement(acct_no);
        } finally
        {
            local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
        }
        return local;
    }

    public Map<String, Object> findByRim(Integer rim)
    {
        Map<String, Object> local = new HashMap<String, Object>();

        try (AccountHandler handler = new AccountHandler())
        {
            local = handler.findByRim(rim);
        } finally
        {
            local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
        }
        return local;
    }

    public Map<String, Object> findByAccount(String account)
    {
        Map<String, Object> local = new HashMap<String, Object>();

        try (AccountHandler handler = new AccountHandler())
        {
            local = handler.findByAccount(account);
        } finally
        {
            local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
        }
        return local;
    }

    public Map<String, Object> createAccount(AccountRequest request)
    {
        Map<String, Object> local = new HashMap<String, Object>();
        if (XapiPool.isInOfflineMode())
        {
            local.put("responseCode", OFFLINE_ACTIVATED);
            local.put("responseTxt", XapiCodes.getErrorDesc(OFFLINE_ACTIVATED));
            return local;
        }

        try (AccountHandler handler = new AccountHandler())
        {
            local = handler.createAccount(request);
        } finally
        {
            local.put("responseTxt", XapiCodes.getErrorDesc(local.get("responseCode")));
        }
        return local;
    }

    public StringBuilder getBuilder(boolean reset)
    {
        if (reset)
            builder.setLength(0);
        return builder;
    }

    public StringBuilder getBuilder()
    {
        return builder;
    }

    public void setBuilder(StringBuilder builder)
    {
        this.builder = builder;
    }

}
