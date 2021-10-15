package NIPClient.Channel;

public class CNServiceProxy implements CNService_PortType {
	private String _endpoint = null;
	private CNService_PortType cNService_PortType = null;

	public CNServiceProxy() {
		_initCNServiceProxy();
	}

	public CNServiceProxy(String endpoint) {
		_endpoint = endpoint;
		_initCNServiceProxy();
	}

	private void _initCNServiceProxy() {
		try {
			cNService_PortType = (new CNService_ServiceLocator()).getCNServicePort();
			if (cNService_PortType != null) {
				if (_endpoint != null)
					((javax.xml.rpc.Stub) cNService_PortType)._setProperty("javax.xml.rpc.service.endpoint.address",
							_endpoint);
				else
					_endpoint = (String) ((javax.xml.rpc.Stub) cNService_PortType)
							._getProperty("javax.xml.rpc.service.endpoint.address");
			}

		} catch (javax.xml.rpc.ServiceException serviceException) {
		}
	}

	public String getEndpoint() {
		return _endpoint;
	}

	public void setEndpoint(String endpoint) {
		_endpoint = endpoint;
		if (cNService_PortType != null)
			((javax.xml.rpc.Stub) cNService_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);

	}

	public CNService_PortType getCNService_PortType() {
		if (cNService_PortType == null)
			_initCNServiceProxy();
		return cNService_PortType;
	}

	public EnquireNameResponseReturn enquireName(EnquireNameRequest request)
			throws java.rmi.RemoteException {
		if (cNService_PortType == null)
			_initCNServiceProxy();
		return cNService_PortType.enquireName(request);
	}

	public String generateSessionId() throws java.rmi.RemoteException {
		if (cNService_PortType == null)
			_initCNServiceProxy();
		return cNService_PortType.generateSessionId();
	}

	public ProcessInterBankTransferResponseReturn processInterBankTransfer(
			ProcessInterBankTransferRequest request) throws java.rmi.RemoteException {
		if (cNService_PortType == null)
			_initCNServiceProxy();
		return cNService_PortType.processInterBankTransfer(request);
	}

}