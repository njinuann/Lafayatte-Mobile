package org.redlamp.interfaces;

public interface ISO {

	String RESPONSE_KEY = "10";
	String RESOURCE_KEY = "02";
	String REQUEST_KEY = "00";

	String XAPI_APPROVED = "00";
	String SYSTEM_ERROR = "91";

	String OFFLINE_ACTIVATED = "S105";

	String UNABLE_TO_LOCATE_RECORD = "25";
	String NO_CUSTOMER_RECORD = "48";

	String MISSING_TXN_AMOUNT = "13";
	String INVALID_TXN_AMOUNT = "13";
	String NO_XAPI_CONNECTION = "91";
	String INVALID_PROCESSING_CODE = "12";
	String MISSING_ACCT_NUMBER = "53";
	String MISSING_CARD_NUMBER = "56";
	String MISSING_TERMINAL_ID = "58";
	String MISSING_TXN_CURRENCY = "12";
	String INVALID_TXN_CURRENCY = "12";
	String UNKNOWN_MESSAGE_TYPE = "58";
	String MISSING_TXN_ACQUIRER_CODE = "15";
	String MISSING_TRANSMISSION_TIME = "12";
	String MISSING_TRACE_AUDIT_NUMBER = "12";
	String MISSING_SECOND_ACCT_NUMBER = "53";
	String UNSUPPORTED_TXN_TYPE = "58";
	String XAPI_CONNECTION_TERMINATED = "91";
	String TRANSFER_TO_SAME_ACCOUNT = "12";
	String MISSING_ORIGINAL_TXN_REFERENCE = "25";
	String TRANSACTION_TIMED_OUT = "91";
	String INVALID_SERVICE_ID = "12";
	String SERVICE_DEACTIVATED = "40";
	String UNKNOWN_OWN_TERMINAL = "15";
	String TERMINAL_DEACTIVATED = "58";
	String INVALID_CARD_NO = "14";
	String CARD_DEACTIVATED = "54";
	String INVALID_CARD_ACCOUNT = "56";
	String INACTIVE_CARD_ACCOUNT = "56";
	String CROSS_CURRENCY_TXN_NOT_ALLOWED = "40";
	String TXN_CURRENCY_DEACTIVATED = "40";
	String DAILY_TXN_AMOUNT_LIMIT_EXCEEDED = "61";
	String UNABLE_TO_FORWARD_TXN_TO_HOST = "96";
	String MISSING_ACCOUNT_ID_FOR_DEPOSIT = "39";
	String UNMATCHED_ACCOUNT_ID_FOR_DEPOSIT = "25";
	String TRANSACTION_NOT_ALLOWED_FOR_ACCOUNT = "57";
	String MISSING_CHARGE_ACCOUNT_FOR_LOAN_TXN = "52";
	String UNABLE_TO_FETCH_LOAN_REPAYMENT_ACCOUNT = "25";
	String UNSUPPORTED_ACCOUNT_CATEGORY = "45";
	String INVALID_ACCOUNT_FOR_REGISTRATION = "14";
	String ACCOUNT_ALREADY_REGISTERED = "26";
	String TRANSACTION_NOT_REVERSIBLE = "21";

	String NO_ACTION_TAKEN = "21";

	String UNABLE_TO_FETCH_TERMINAL_CONTRA_ACCOUNT = "25";
	String INVALID_DEVICE = "15";
	String TRANSACTION_NOT_PERMITTED_ON_TERMINAL = "58";
	String DUPLICATE_REFERENCE = "26";
	String TRY_LATER = "91";
	String INVALID_ACCOUNT_STATUS = "45";
	String ACCOUNT_CANNOT_TRANSACT_1 = "53";
	String INVALID_ACCOUNT = "53";
	String UNKNOWN_ACCOUNT = "14";
	String INSUFFICIENT_FUNDS_1 = "51";
	String ACCOUNT_CANNOT_TRANSACT_2 = "52";
	String INVALID_OPERATOR = "03";
	String EXCEEDS_CASH_LIMIT = "98";
	String EXCEEDS_WITHDRAWAL_LIMIT = "61";
	String OPERATOR_CANNOT_POST = "87";
	String MULTIPLE_SIGNATORIES = "A61";
	String ACCOUNT_NOT_PERMITTED = "106";
	

}
