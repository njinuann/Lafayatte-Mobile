/**
 * ProcessInterBankTransferRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package NIPClient.Channel;

@SuppressWarnings("serial")
public class ProcessInterBankTransferRequest implements java.io.Serializable {
	private String sessionID;

	private String nameEnquiryRef;

	private String destinationInstitutionCode;

	private int channelCode;

	private String beneficiaryAccountName;

	private String beneficiaryAccountNumber;

	private String beneficiaryBankVerificationNumber;

	private int beneficiaryKYCLevel;

	private String originatorAccountName;

	private String originatorAccountNumber;

	private String originatorBankVerificationNumber;

	private int originatorKYCLevel;

	private String transactionLocation;

	private String narration;

	private org.apache.axis.types.NMToken paymentReference;

	private java.math.BigDecimal amount;

	public ProcessInterBankTransferRequest() {
	}

	public ProcessInterBankTransferRequest(String sessionID, String nameEnquiryRef,
                                           String destinationInstitutionCode, int channelCode, String beneficiaryAccountName,
                                           String beneficiaryAccountNumber, String beneficiaryBankVerificationNumber,
                                           int beneficiaryKYCLevel, String originatorAccountName, String originatorAccountNumber,
                                           String originatorBankVerificationNumber, int originatorKYCLevel,
                                           String transactionLocation, String narration,
                                           org.apache.axis.types.NMToken paymentReference, java.math.BigDecimal amount) {
		this.sessionID = sessionID;
		this.nameEnquiryRef = nameEnquiryRef;
		this.destinationInstitutionCode = destinationInstitutionCode;
		this.channelCode = channelCode;
		this.beneficiaryAccountName = beneficiaryAccountName;
		this.beneficiaryAccountNumber = beneficiaryAccountNumber;
		this.beneficiaryBankVerificationNumber = beneficiaryBankVerificationNumber;
		this.beneficiaryKYCLevel = beneficiaryKYCLevel;
		this.originatorAccountName = originatorAccountName;
		this.originatorAccountNumber = originatorAccountNumber;
		this.originatorBankVerificationNumber = originatorBankVerificationNumber;
		this.originatorKYCLevel = originatorKYCLevel;
		this.transactionLocation = transactionLocation;
		this.narration = narration;
		this.paymentReference = paymentReference;
		this.amount = amount;
	}

	/**
	 * Gets the sessionID value for this ProcessInterBankTransferRequest.
	 * 
	 * @return sessionID
	 */
	public String getSessionID() {
		return sessionID;
	}

	/**
	 * Sets the sessionID value for this ProcessInterBankTransferRequest.
	 * 
	 * @param sessionID
	 */
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	/**
	 * Gets the nameEnquiryRef value for this ProcessInterBankTransferRequest.
	 * 
	 * @return nameEnquiryRef
	 */
	public String getNameEnquiryRef() {
		return nameEnquiryRef;
	}

	/**
	 * Sets the nameEnquiryRef value for this ProcessInterBankTransferRequest.
	 * 
	 * @param nameEnquiryRef
	 */
	public void setNameEnquiryRef(String nameEnquiryRef) {
		this.nameEnquiryRef = nameEnquiryRef;
	}

	/**
	 * Gets the destinationInstitutionCode value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @return destinationInstitutionCode
	 */
	public String getDestinationInstitutionCode() {
		return destinationInstitutionCode;
	}

	/**
	 * Sets the destinationInstitutionCode value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @param destinationInstitutionCode
	 */
	public void setDestinationInstitutionCode(String destinationInstitutionCode) {
		this.destinationInstitutionCode = destinationInstitutionCode;
	}

	/**
	 * Gets the channelCode value for this ProcessInterBankTransferRequest.
	 * 
	 * @return channelCode
	 */
	public int getChannelCode() {
		return channelCode;
	}

	/**
	 * Sets the channelCode value for this ProcessInterBankTransferRequest.
	 * 
	 * @param channelCode
	 */
	public void setChannelCode(int channelCode) {
		this.channelCode = channelCode;
	}

	/**
	 * Gets the beneficiaryAccountName value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @return beneficiaryAccountName
	 */
	public String getBeneficiaryAccountName() {
		return beneficiaryAccountName;
	}

	/**
	 * Sets the beneficiaryAccountName value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @param beneficiaryAccountName
	 */
	public void setBeneficiaryAccountName(String beneficiaryAccountName) {
		this.beneficiaryAccountName = beneficiaryAccountName;
	}

	/**
	 * Gets the beneficiaryAccountNumber value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @return beneficiaryAccountNumber
	 */
	public String getBeneficiaryAccountNumber() {
		return beneficiaryAccountNumber;
	}

	/**
	 * Sets the beneficiaryAccountNumber value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @param beneficiaryAccountNumber
	 */
	public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
		this.beneficiaryAccountNumber = beneficiaryAccountNumber;
	}

	/**
	 * Gets the beneficiaryBankVerificationNumber value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @return beneficiaryBankVerificationNumber
	 */
	public String getBeneficiaryBankVerificationNumber() {
		return beneficiaryBankVerificationNumber;
	}

	/**
	 * Sets the beneficiaryBankVerificationNumber value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @param beneficiaryBankVerificationNumber
	 */
	public void setBeneficiaryBankVerificationNumber(String beneficiaryBankVerificationNumber) {
		this.beneficiaryBankVerificationNumber = beneficiaryBankVerificationNumber;
	}

	/**
	 * Gets the beneficiaryKYCLevel value for this ProcessInterBankTransferRequest.
	 * 
	 * @return beneficiaryKYCLevel
	 */
	public int getBeneficiaryKYCLevel() {
		return beneficiaryKYCLevel;
	}

	/**
	 * Sets the beneficiaryKYCLevel value for this ProcessInterBankTransferRequest.
	 * 
	 * @param beneficiaryKYCLevel
	 */
	public void setBeneficiaryKYCLevel(int beneficiaryKYCLevel) {
		this.beneficiaryKYCLevel = beneficiaryKYCLevel;
	}

	/**
	 * Gets the originatorAccountName value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @return originatorAccountName
	 */
	public String getOriginatorAccountName() {
		return originatorAccountName;
	}

	/**
	 * Sets the originatorAccountName value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @param originatorAccountName
	 */
	public void setOriginatorAccountName(String originatorAccountName) {
		this.originatorAccountName = originatorAccountName;
	}

	/**
	 * Gets the originatorAccountNumber value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @return originatorAccountNumber
	 */
	public String getOriginatorAccountNumber() {
		return originatorAccountNumber;
	}

	/**
	 * Sets the originatorAccountNumber value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @param originatorAccountNumber
	 */
	public void setOriginatorAccountNumber(String originatorAccountNumber) {
		this.originatorAccountNumber = originatorAccountNumber;
	}

	/**
	 * Gets the originatorBankVerificationNumber value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @return originatorBankVerificationNumber
	 */
	public String getOriginatorBankVerificationNumber() {
		return originatorBankVerificationNumber;
	}

	/**
	 * Sets the originatorBankVerificationNumber value for this
	 * ProcessInterBankTransferRequest.
	 * 
	 * @param originatorBankVerificationNumber
	 */
	public void setOriginatorBankVerificationNumber(String originatorBankVerificationNumber) {
		this.originatorBankVerificationNumber = originatorBankVerificationNumber;
	}

	/**
	 * Gets the originatorKYCLevel value for this ProcessInterBankTransferRequest.
	 * 
	 * @return originatorKYCLevel
	 */
	public int getOriginatorKYCLevel() {
		return originatorKYCLevel;
	}

	/**
	 * Sets the originatorKYCLevel value for this ProcessInterBankTransferRequest.
	 * 
	 * @param originatorKYCLevel
	 */
	public void setOriginatorKYCLevel(int originatorKYCLevel) {
		this.originatorKYCLevel = originatorKYCLevel;
	}

	/**
	 * Gets the transactionLocation value for this ProcessInterBankTransferRequest.
	 * 
	 * @return transactionLocation
	 */
	public String getTransactionLocation() {
		return transactionLocation;
	}

	/**
	 * Sets the transactionLocation value for this ProcessInterBankTransferRequest.
	 * 
	 * @param transactionLocation
	 */
	public void setTransactionLocation(String transactionLocation) {
		this.transactionLocation = transactionLocation;
	}

	/**
	 * Gets the narration value for this ProcessInterBankTransferRequest.
	 * 
	 * @return narration
	 */
	public String getNarration() {
		return narration;
	}

	/**
	 * Sets the narration value for this ProcessInterBankTransferRequest.
	 * 
	 * @param narration
	 */
	public void setNarration(String narration) {
		this.narration = narration;
	}

	/**
	 * Gets the paymentReference value for this ProcessInterBankTransferRequest.
	 * 
	 * @return paymentReference
	 */
	public org.apache.axis.types.NMToken getPaymentReference() {
		return paymentReference;
	}

	/**
	 * Sets the paymentReference value for this ProcessInterBankTransferRequest.
	 * 
	 * @param paymentReference
	 */
	public void setPaymentReference(org.apache.axis.types.NMToken paymentReference) {
		this.paymentReference = paymentReference;
	}

	/**
	 * Gets the amount value for this ProcessInterBankTransferRequest.
	 * 
	 * @return amount
	 */
	public java.math.BigDecimal getAmount() {
		return amount;
	}

	/**
	 * Sets the amount value for this ProcessInterBankTransferRequest.
	 * 
	 * @param amount
	 */
	public void setAmount(java.math.BigDecimal amount) {
		this.amount = amount;
	}

	private Object __equalsCalc = null;

	@SuppressWarnings("unused")
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof ProcessInterBankTransferRequest))
			return false;
		ProcessInterBankTransferRequest other = (ProcessInterBankTransferRequest) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& ((this.sessionID == null && other.getSessionID() == null)
						|| (this.sessionID != null && this.sessionID.equals(other.getSessionID())))
				&& ((this.nameEnquiryRef == null && other.getNameEnquiryRef() == null)
						|| (this.nameEnquiryRef != null && this.nameEnquiryRef.equals(other.getNameEnquiryRef())))
				&& ((this.destinationInstitutionCode == null && other.getDestinationInstitutionCode() == null)
						|| (this.destinationInstitutionCode != null
								&& this.destinationInstitutionCode.equals(other.getDestinationInstitutionCode())))
				&& this.channelCode == other.getChannelCode()
				&& ((this.beneficiaryAccountName == null && other.getBeneficiaryAccountName() == null)
						|| (this.beneficiaryAccountName != null
								&& this.beneficiaryAccountName.equals(other.getBeneficiaryAccountName())))
				&& ((this.beneficiaryAccountNumber == null && other.getBeneficiaryAccountNumber() == null)
						|| (this.beneficiaryAccountNumber != null
								&& this.beneficiaryAccountNumber.equals(other.getBeneficiaryAccountNumber())))
				&& ((this.beneficiaryBankVerificationNumber == null
						&& other.getBeneficiaryBankVerificationNumber() == null)
						|| (this.beneficiaryBankVerificationNumber != null && this.beneficiaryBankVerificationNumber
								.equals(other.getBeneficiaryBankVerificationNumber())))
				&& this.beneficiaryKYCLevel == other.getBeneficiaryKYCLevel()
				&& ((this.originatorAccountName == null && other.getOriginatorAccountName() == null)
						|| (this.originatorAccountName != null
								&& this.originatorAccountName.equals(other.getOriginatorAccountName())))
				&& ((this.originatorAccountNumber == null && other.getOriginatorAccountNumber() == null)
						|| (this.originatorAccountNumber != null
								&& this.originatorAccountNumber.equals(other.getOriginatorAccountNumber())))
				&& ((this.originatorBankVerificationNumber == null
						&& other.getOriginatorBankVerificationNumber() == null)
						|| (this.originatorBankVerificationNumber != null && this.originatorBankVerificationNumber
								.equals(other.getOriginatorBankVerificationNumber())))
				&& this.originatorKYCLevel == other.getOriginatorKYCLevel()
				&& ((this.transactionLocation == null && other.getTransactionLocation() == null)
						|| (this.transactionLocation != null
								&& this.transactionLocation.equals(other.getTransactionLocation())))
				&& ((this.narration == null && other.getNarration() == null)
						|| (this.narration != null && this.narration.equals(other.getNarration())))
				&& ((this.paymentReference == null && other.getPaymentReference() == null)
						|| (this.paymentReference != null && this.paymentReference.equals(other.getPaymentReference())))
				&& ((this.amount == null && other.getAmount() == null)
						|| (this.amount != null && this.amount.equals(other.getAmount())));
		__equalsCalc = null;
		return _equals;
	}

	private boolean __hashCodeCalc = false;

	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1;
		if (getSessionID() != null) {
			_hashCode += getSessionID().hashCode();
		}
		if (getNameEnquiryRef() != null) {
			_hashCode += getNameEnquiryRef().hashCode();
		}
		if (getDestinationInstitutionCode() != null) {
			_hashCode += getDestinationInstitutionCode().hashCode();
		}
		_hashCode += getChannelCode();
		if (getBeneficiaryAccountName() != null) {
			_hashCode += getBeneficiaryAccountName().hashCode();
		}
		if (getBeneficiaryAccountNumber() != null) {
			_hashCode += getBeneficiaryAccountNumber().hashCode();
		}
		if (getBeneficiaryBankVerificationNumber() != null) {
			_hashCode += getBeneficiaryBankVerificationNumber().hashCode();
		}
		_hashCode += getBeneficiaryKYCLevel();
		if (getOriginatorAccountName() != null) {
			_hashCode += getOriginatorAccountName().hashCode();
		}
		if (getOriginatorAccountNumber() != null) {
			_hashCode += getOriginatorAccountNumber().hashCode();
		}
		if (getOriginatorBankVerificationNumber() != null) {
			_hashCode += getOriginatorBankVerificationNumber().hashCode();
		}
		_hashCode += getOriginatorKYCLevel();
		if (getTransactionLocation() != null) {
			_hashCode += getTransactionLocation().hashCode();
		}
		if (getNarration() != null) {
			_hashCode += getNarration().hashCode();
		}
		if (getPaymentReference() != null) {
			_hashCode += getPaymentReference().hashCode();
		}
		if (getAmount() != null) {
			_hashCode += getAmount().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			ProcessInterBankTransferRequest.class, true);

	static {
		typeDesc.setXmlType(
				new javax.xml.namespace.QName("http://Channel.NIPClient/", ">processInterBankTransfer>request"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("sessionID");
		elemField.setXmlName(new javax.xml.namespace.QName("", "SessionID"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("nameEnquiryRef");
		elemField.setXmlName(new javax.xml.namespace.QName("", "NameEnquiryRef"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("destinationInstitutionCode");
		elemField.setXmlName(new javax.xml.namespace.QName("", "DestinationInstitutionCode"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("channelCode");
		elemField.setXmlName(new javax.xml.namespace.QName("", "ChannelCode"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("beneficiaryAccountName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "BeneficiaryAccountName"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("beneficiaryAccountNumber");
		elemField.setXmlName(new javax.xml.namespace.QName("", "BeneficiaryAccountNumber"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("beneficiaryBankVerificationNumber");
		elemField.setXmlName(new javax.xml.namespace.QName("", "BeneficiaryBankVerificationNumber"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("beneficiaryKYCLevel");
		elemField.setXmlName(new javax.xml.namespace.QName("", "BeneficiaryKYCLevel"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("originatorAccountName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "OriginatorAccountName"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("originatorAccountNumber");
		elemField.setXmlName(new javax.xml.namespace.QName("", "OriginatorAccountNumber"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("originatorBankVerificationNumber");
		elemField.setXmlName(new javax.xml.namespace.QName("", "OriginatorBankVerificationNumber"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("originatorKYCLevel");
		elemField.setXmlName(new javax.xml.namespace.QName("", "OriginatorKYCLevel"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("transactionLocation");
		elemField.setXmlName(new javax.xml.namespace.QName("", "TransactionLocation"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("narration");
		elemField.setXmlName(new javax.xml.namespace.QName("", "Narration"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("paymentReference");
		elemField.setXmlName(new javax.xml.namespace.QName("", "PaymentReference"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "NMTOKEN"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("amount");
		elemField.setXmlName(new javax.xml.namespace.QName("", "Amount"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc() {
		return typeDesc;
	}

	/**
	 * Get Custom Serializer
	 */
	public static org.apache.axis.encoding.Serializer getSerializer(String mechType,
                                                                    Class _javaType, javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(String mechType,
                                                                        Class _javaType, javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, typeDesc);
	}

	@Override
	public String toString() {
		return "ProcessInterBankTransferRequest [sessionID=" + sessionID + ", nameEnquiryRef=" + nameEnquiryRef
				+ ", destinationInstitutionCode=" + destinationInstitutionCode + ", channelCode=" + channelCode
				+ ", beneficiaryAccountName=" + beneficiaryAccountName + ", beneficiaryAccountNumber="
				+ beneficiaryAccountNumber + ", beneficiaryBankVerificationNumber=" + beneficiaryBankVerificationNumber
				+ ", beneficiaryKYCLevel=" + beneficiaryKYCLevel + ", originatorAccountName=" + originatorAccountName
				+ ", originatorAccountNumber=" + originatorAccountNumber + ", originatorBankVerificationNumber="
				+ originatorBankVerificationNumber + ", originatorKYCLevel=" + originatorKYCLevel
				+ ", transactionLocation=" + transactionLocation + ", narration=" + narration + ", paymentReference="
				+ paymentReference + ", amount=" + amount + ", __equalsCalc=" + __equalsCalc + ", __hashCodeCalc="
				+ __hashCodeCalc + "]";
	}

}
