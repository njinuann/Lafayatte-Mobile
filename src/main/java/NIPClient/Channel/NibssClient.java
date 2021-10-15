package NIPClient.Channel;

import java.math.BigDecimal;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.NMToken;
import org.redlamp.logger.ApiLogger;
import org.redlamp.util.XapiCodes;
import org.redlamp.util.XapiPool;

public class NibssClient
{

    public String postNIPTransfer(String originatorAccountName, String originatorAccountNumber,
                                  String transactionLocation, String narration, String destAcctNo, String bankCode, double txnAmount,
                                  String reference) throws RemoteException, ServiceException
    {

        CNService_Service service = new CNService_ServiceLocator();
        String nameSessionId = service.getCNServicePort().generateSessionId();
        if (nameSessionId == null)
        {
            throw new NullPointerException("Unable to acquire a session ID. Session id was null");
        }

        EnquireNameRequest request = new EnquireNameRequest();
        request.setAccountNumber(stripDashes(destAcctNo));
        request.setChannelCode(3);
        request.setDestinationInstitutionCode(bankCode);
        request.setSessionID(nameSessionId);
        log("Getting name...on " + destAcctNo + " @" + bankCode);

        EnquireNameResponseReturn enquireNameResponse = service.getCNServicePort().enquireName(request);
        log(enquireNameResponse);

        if (enquireNameResponse != null && !"00".equals(enquireNameResponse.getResponseCode()))
        {
            return enquireNameResponse.getResponseCode();
        }

        String txnSessionId = service.getCNServicePort().generateSessionId();
        NMToken paymentReference = new NMToken(
                reference != null ? reference : String.valueOf(System.currentTimeMillis()));

        log("Building Transfer Request");
        ProcessInterBankTransferRequest transfer = new ProcessInterBankTransferRequest();

        transfer.setAmount(BigDecimal.valueOf(txnAmount));

        transfer.setBeneficiaryAccountName(enquireNameResponse.getAccountName());

        transfer.setBeneficiaryBankVerificationNumber(enquireNameResponse.getBankVerificationNumber());

        transfer.setBeneficiaryAccountNumber(enquireNameResponse.getAccountNumber());

        transfer.setBeneficiaryKYCLevel(enquireNameResponse.getKYCLevel());

        transfer.setChannelCode(enquireNameResponse.getChannelCode());

        transfer.setDestinationInstitutionCode(enquireNameResponse.getDestinationInstitutionCode());

        transfer.setNameEnquiryRef(nameSessionId);

        transfer.setNarration(narration.length() > 75 ? narration.substring(0, 75) : narration); // max 75 xters

        transfer.setOriginatorAccountName(originatorAccountName);

        transfer.setOriginatorBankVerificationNumber("");

        transfer.setOriginatorKYCLevel(0);

        transfer.setOriginatorAccountNumber(stripDashes(originatorAccountNumber));

        transfer.setPaymentReference(paymentReference);

        transfer.setSessionID(txnSessionId);

        transfer.setTransactionLocation(transactionLocation);

        log(transfer);
        return service.getCNServicePort().processInterBankTransfer(transfer).getResponseCode();
    }

    public String requestSession() throws RemoteException, ServiceException
    {
        CNService_ServiceLocator service = new CNService_ServiceLocator();
        service.setCNServicePortEndpointAddress(XapiCodes.NIBSS_WSDL);
        return service.getCNServicePort().generateSessionId();
    }

    public String stripDashes(String acctNo)
    {
        if (acctNo != null && acctNo.contains("-"))
        {
            acctNo = acctNo.replaceAll("-", "").trim();
        }
        return acctNo;
    }

    public EnquireNameResponseReturn nameEnquiry(String accountNo, String bankCode)
            throws RemoteException, ServiceException
    {
        CNService_Service service = new CNService_ServiceLocator();
        String nameSessionId = service.getCNServicePort().generateSessionId();
        if (nameSessionId == null)
        {
            throw new NullPointerException("Failed to retrieve session ID for name enquiry from Nibss API");
        }
        EnquireNameRequest request = new EnquireNameRequest();
        request.setAccountNumber(stripDashes(accountNo));
        request.setChannelCode(3);
        request.setDestinationInstitutionCode(bankCode);
        request.setSessionID(nameSessionId);
        EnquireNameResponseReturn enquireName = service.getCNServicePort().enquireName(request);
        return enquireName;
    }

    private void log(Object data)
    {
        if (XapiPool.DEBUG_ENABLED)
            ApiLogger.getLogger().info(data);
    }

    public String postTransfer(String originatorAccountNumber, String transactionLocation, String narration,
                               String destAcctNo, String bankCode, double txnAmount) throws RemoteException, ServiceException
    {

        CNService_Service service = new CNService_ServiceLocator();
        String nameSessionId = service.getCNServicePort().generateSessionId();
        if (nameSessionId == null)
            return "101";

        EnquireNameRequest request = new EnquireNameRequest();
        request.setAccountNumber(destAcctNo);
        request.setChannelCode(XapiPool.NIBSS_CHANNEL);
        request.setDestinationInstitutionCode(bankCode);
        request.setSessionID(nameSessionId);

        EnquireNameResponseReturn enquireName = service.getCNServicePort().enquireName(request);
        if (!"00".equals(enquireName.getResponseCode()))
            return enquireName.getResponseCode() != null ? enquireName.getResponseCode() : "98";

        String txnSessionId = service.getCNServicePort().generateSessionId();
        NMToken paymentReference = new NMToken(String.valueOf(System.currentTimeMillis()));
        ProcessInterBankTransferRequest transfer = new ProcessInterBankTransferRequest(txnSessionId, nameSessionId,
                enquireName.getDestinationInstitutionCode(), enquireName.getChannelCode(), enquireName.getAccountName(),
                enquireName.getAccountNumber(), enquireName.getBankVerificationNumber(), enquireName.getKYCLevel(), "",
                originatorAccountNumber, "", 0, transactionLocation, narration, paymentReference,
                BigDecimal.valueOf(txnAmount));
        ProcessInterBankTransferResponseReturn processInterBankTransfer = service.getCNServicePort().processInterBankTransfer(transfer);
        log(processInterBankTransfer);
        if (!"00".equals(processInterBankTransfer.getResponseCode()))
        {
            return enquireName.getResponseCode() != null ? enquireName.getResponseCode() : "99";
        }
        return processInterBankTransfer.getResponseCode();
    }

}
