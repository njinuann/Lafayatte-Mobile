/**
 * CNService_Service.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package NIPClient.Channel;

public interface CNService_Service extends javax.xml.rpc.Service {
	public String getCNServicePortAddress();

	public CNService_PortType getCNServicePort() throws javax.xml.rpc.ServiceException;

	public CNService_PortType getCNServicePort(java.net.URL portAddress)
			throws javax.xml.rpc.ServiceException;
}
