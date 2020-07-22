/**
 * CNService_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package NIPClient.Channel;

@SuppressWarnings("serial")
public class CNService_ServiceLocator extends org.apache.axis.client.Service
		implements CNService_Service {

	public CNService_ServiceLocator() {
	}

	public CNService_ServiceLocator(org.apache.axis.EngineConfiguration config) {
		super(config);
	}

	public CNService_ServiceLocator(String wsdlLoc, javax.xml.namespace.QName sName)
			throws javax.xml.rpc.ServiceException {
		super(wsdlLoc, sName);
	}

	// Use to get a proxy class for CNServicePort
	private String CNServicePort_address = "http://192.168.4.23:5588/NIPClient/CNService";

	public String getCNServicePortAddress() {
		return CNServicePort_address;
	}

	// The WSDD service name defaults to the port name.
	private String CNServicePortWSDDServiceName = "CNServicePort";

	public String getCNServicePortWSDDServiceName() {
		return CNServicePortWSDDServiceName;
	}

	public void setCNServicePortWSDDServiceName(String name) {
		CNServicePortWSDDServiceName = name;
	}

	public CNService_PortType getCNServicePort() throws javax.xml.rpc.ServiceException {
		java.net.URL endpoint;
		try {
			endpoint = new java.net.URL(CNServicePort_address);
		} catch (java.net.MalformedURLException e) {
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getCNServicePort(endpoint);
	}

	public CNService_PortType getCNServicePort(java.net.URL portAddress)
			throws javax.xml.rpc.ServiceException {
		try {
			CNServiceBindingStub _stub = new CNServiceBindingStub(portAddress,
					this);
			_stub.setPortName(getCNServicePortWSDDServiceName());
			return _stub;
		} catch (org.apache.axis.AxisFault e) {
			return null;
		}
	}

	public void setCNServicePortEndpointAddress(String address) {
		CNServicePort_address = address;
	}

	/**
	 * For the given interface, get the stub implementation. If this service has no
	 * port for the given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
		try {
			if (CNService_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
				CNServiceBindingStub _stub = new CNServiceBindingStub(
						new java.net.URL(CNServicePort_address), this);
				_stub.setPortName(getCNServicePortWSDDServiceName());
				return _stub;
			}
		} catch (Throwable t) {
			throw new javax.xml.rpc.ServiceException(t);
		}
		throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  "
				+ (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
	}

	/**
	 * For the given interface, get the stub implementation. If this service has no
	 * port for the given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface)
			throws javax.xml.rpc.ServiceException {
		if (portName == null) {
			return getPort(serviceEndpointInterface);
		}
		String inputPortName = portName.getLocalPart();
		if ("CNServicePort".equals(inputPortName)) {
			return getCNServicePort();
		} else {
			java.rmi.Remote _stub = getPort(serviceEndpointInterface);
			((org.apache.axis.client.Stub) _stub).setPortName(portName);
			return _stub;
		}
	}

	public javax.xml.namespace.QName getServiceName() {
		return new javax.xml.namespace.QName("http://Channel.NIPClient/", "CNService");
	}

	private java.util.HashSet ports = null;

	@SuppressWarnings("unchecked")
	public java.util.Iterator getPorts() {
		if (ports == null) {
			ports = new java.util.HashSet();
			ports.add(new javax.xml.namespace.QName("http://Channel.NIPClient/", "CNServicePort"));
		}
		return ports.iterator();
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(String portName, String address)
			throws javax.xml.rpc.ServiceException {

		if ("CNServicePort".equals(portName)) {
			setCNServicePortEndpointAddress(address);
		} else { // Unknown Port Name
			throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
		}
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(javax.xml.namespace.QName portName, String address)
			throws javax.xml.rpc.ServiceException {
		setEndpointAddress(portName.getLocalPart(), address);
	}

}
