/**
 * EnquireNameRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package NIPClient.Channel;

@SuppressWarnings("serial")
public class EnquireNameRequest implements java.io.Serializable {
	private String sessionID;

	private String destinationInstitutionCode;

	private int channelCode;

	private String accountNumber;

	public EnquireNameRequest() {
	}

	public EnquireNameRequest(String sessionID, String destinationInstitutionCode, int channelCode,
                              String accountNumber) {
		this.sessionID = sessionID;
		this.destinationInstitutionCode = destinationInstitutionCode;
		this.channelCode = channelCode;
		this.accountNumber = accountNumber;
	}

	/**
	 * Gets the sessionID value for this EnquireNameRequest.
	 * 
	 * @return sessionID
	 */
	public String getSessionID() {
		return sessionID;
	}

	/**
	 * Sets the sessionID value for this EnquireNameRequest.
	 * 
	 * @param sessionID
	 */
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	/**
	 * Gets the destinationInstitutionCode value for this EnquireNameRequest.
	 * 
	 * @return destinationInstitutionCode
	 */
	public String getDestinationInstitutionCode() {
		return destinationInstitutionCode;
	}

	/**
	 * Sets the destinationInstitutionCode value for this EnquireNameRequest.
	 * 
	 * @param destinationInstitutionCode
	 */
	public void setDestinationInstitutionCode(String destinationInstitutionCode) {
		this.destinationInstitutionCode = destinationInstitutionCode;
	}

	/**
	 * Gets the channelCode value for this EnquireNameRequest.
	 * 
	 * @return channelCode
	 */
	public int getChannelCode() {
		return channelCode;
	}

	/**
	 * Sets the channelCode value for this EnquireNameRequest.
	 * 
	 * @param channelCode
	 */
	public void setChannelCode(int channelCode) {
		this.channelCode = channelCode;
	}

	/**
	 * Gets the accountNumber value for this EnquireNameRequest.
	 * 
	 * @return accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * Sets the accountNumber value for this EnquireNameRequest.
	 * 
	 * @param accountNumber
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	private Object __equalsCalc = null;

	@SuppressWarnings("unused")
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof EnquireNameRequest))
			return false;
		EnquireNameRequest other = (EnquireNameRequest) obj;
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
						|| (this.accountNumber != null && this.accountNumber.equals(other.getAccountNumber())));
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
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			EnquireNameRequest.class, true);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("http://Channel.NIPClient/", ">enquireName>request"));
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

}
