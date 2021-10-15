/**
 * EnquireNameResponseReturn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package NIPClient.Channel;

@SuppressWarnings("serial")
public class EnquireNameResponseReturn implements java.io.Serializable {
	private String sessionID;

	private String destinationInstitutionCode;

	private int channelCode;

	private String accountNumber;

	private String accountName;

	private String bankVerificationNumber;

	private int KYCLevel;

	private String responseCode;

	public EnquireNameResponseReturn() {
	}

	public EnquireNameResponseReturn(String sessionID, String destinationInstitutionCode,
                                     int channelCode, String accountNumber, String accountName,
                                     String bankVerificationNumber, int KYCLevel, String responseCode) {
		this.sessionID = sessionID;
		this.destinationInstitutionCode = destinationInstitutionCode;
		this.channelCode = channelCode;
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.bankVerificationNumber = bankVerificationNumber;
		this.KYCLevel = KYCLevel;
		this.responseCode = responseCode;
	}

	/**
	 * Gets the sessionID value for this EnquireNameResponseReturn.
	 * 
	 * @return sessionID
	 */
	public String getSessionID() {
		return sessionID;
	}

	/**
	 * Sets the sessionID value for this EnquireNameResponseReturn.
	 * 
	 * @param sessionID
	 */
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	/**
	 * Gets the destinationInstitutionCode value for this EnquireNameResponseReturn.
	 * 
	 * @return destinationInstitutionCode
	 */
	public String getDestinationInstitutionCode() {
		return destinationInstitutionCode;
	}

	/**
	 * Sets the destinationInstitutionCode value for this EnquireNameResponseReturn.
	 * 
	 * @param destinationInstitutionCode
	 */
	public void setDestinationInstitutionCode(String destinationInstitutionCode) {
		this.destinationInstitutionCode = destinationInstitutionCode;
	}

	/**
	 * Gets the channelCode value for this EnquireNameResponseReturn.
	 * 
	 * @return channelCode
	 */
	public int getChannelCode() {
		return channelCode;
	}

	/**
	 * Sets the channelCode value for this EnquireNameResponseReturn.
	 * 
	 * @param channelCode
	 */
	public void setChannelCode(int channelCode) {
		this.channelCode = channelCode;
	}

	/**
	 * Gets the accountNumber value for this EnquireNameResponseReturn.
	 * 
	 * @return accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * Sets the accountNumber value for this EnquireNameResponseReturn.
	 * 
	 * @param accountNumber
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	/**
	 * Gets the accountName value for this EnquireNameResponseReturn.
	 * 
	 * @return accountName
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * Sets the accountName value for this EnquireNameResponseReturn.
	 * 
	 * @param accountName
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	/**
	 * Gets the bankVerificationNumber value for this EnquireNameResponseReturn.
	 * 
	 * @return bankVerificationNumber
	 */
	public String getBankVerificationNumber() {
		return bankVerificationNumber;
	}

	/**
	 * Sets the bankVerificationNumber value for this EnquireNameResponseReturn.
	 * 
	 * @param bankVerificationNumber
	 */
	public void setBankVerificationNumber(String bankVerificationNumber) {
		this.bankVerificationNumber = bankVerificationNumber;
	}

	/**
	 * Gets the KYCLevel value for this EnquireNameResponseReturn.
	 * 
	 * @return KYCLevel
	 */
	public int getKYCLevel() {
		return KYCLevel;
	}

	/**
	 * Sets the KYCLevel value for this EnquireNameResponseReturn.
	 * 
	 * @param KYCLevel
	 */
	public void setKYCLevel(int KYCLevel) {
		this.KYCLevel = KYCLevel;
	}

	/**
	 * Gets the responseCode value for this EnquireNameResponseReturn.
	 * 
	 * @return responseCode
	 */
	public String getResponseCode() {
		return responseCode;
	}

	/**
	 * Sets the responseCode value for this EnquireNameResponseReturn.
	 * 
	 * @param responseCode
	 */
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	private Object __equalsCalc = null;

	@SuppressWarnings("unused")
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof EnquireNameResponseReturn))
			return false;
		EnquireNameResponseReturn other = (EnquireNameResponseReturn) obj;
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
				&& ((this.destinationInstitutionCode == null && other.getDestinationInstitutionCode() == null)
						|| (this.destinationInstitutionCode != null
								&& this.destinationInstitutionCode.equals(other.getDestinationInstitutionCode())))
				&& this.channelCode == other.getChannelCode()
				&& ((this.accountNumber == null && other.getAccountNumber() == null)
						|| (this.accountNumber != null && this.accountNumber.equals(other.getAccountNumber())))
				&& ((this.accountName == null && other.getAccountName() == null)
						|| (this.accountName != null && this.accountName.equals(other.getAccountName())))
				&& ((this.bankVerificationNumber == null && other.getBankVerificationNumber() == null)
						|| (this.bankVerificationNumber != null
								&& this.bankVerificationNumber.equals(other.getBankVerificationNumber())))
				&& this.KYCLevel == other.getKYCLevel()
				&& ((this.responseCode == null && other.getResponseCode() == null)
						|| (this.responseCode != null && this.responseCode.equals(other.getResponseCode())));
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
		if (getDestinationInstitutionCode() != null) {
			_hashCode += getDestinationInstitutionCode().hashCode();
		}
		_hashCode += getChannelCode();
		if (getAccountNumber() != null) {
			_hashCode += getAccountNumber().hashCode();
		}
		if (getAccountName() != null) {
			_hashCode += getAccountName().hashCode();
		}
		if (getBankVerificationNumber() != null) {
			_hashCode += getBankVerificationNumber().hashCode();
		}
		_hashCode += getKYCLevel();
		if (getResponseCode() != null) {
			_hashCode += getResponseCode().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			EnquireNameResponseReturn.class, true);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("http://Channel.NIPClient/", ">enquireNameResponse>return"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("sessionID");
		elemField.setXmlName(new javax.xml.namespace.QName("", "SessionID"));
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
		elemField.setFieldName("accountNumber");
		elemField.setXmlName(new javax.xml.namespace.QName("", "AccountNumber"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("accountName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "AccountName"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("bankVerificationNumber");
		elemField.setXmlName(new javax.xml.namespace.QName("", "BankVerificationNumber"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("KYCLevel");
		elemField.setXmlName(new javax.xml.namespace.QName("", "KYCLevel"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("responseCode");
		elemField.setXmlName(new javax.xml.namespace.QName("", "ResponseCode"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
		return "EnquireNameResponseReturn [sessionID=" + sessionID + ", destinationInstitutionCode="
				+ destinationInstitutionCode + ", channelCode=" + channelCode + ", accountNumber=" + accountNumber
				+ ", accountName=" + accountName + ", bankVerificationNumber=" + bankVerificationNumber + ", KYCLevel="
				+ KYCLevel + ", responseCode=" + responseCode + ", __equalsCalc=" + __equalsCalc + ", __hashCodeCalc="
				+ __hashCodeCalc + "]";
	}

}
