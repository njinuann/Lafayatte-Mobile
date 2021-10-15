/**
 * CNService_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package NIPClient.Channel;

public interface CNService_PortType extends java.rmi.Remote {
	public EnquireNameResponseReturn enquireName(EnquireNameRequest request)
			throws java.rmi.RemoteException;

	public String generateSessionId() throws java.rmi.RemoteException;

	public ProcessInterBankTransferResponseReturn processInterBankTransfer(
			ProcessInterBankTransferRequest request) throws java.rmi.RemoteException;
}
