package io.mosip.resident.constant;

/**
 * The Enum RestServiceContants - defines modules for which rest calls are made
 * from id repository. The value of constant is used to build the rest call
 * request.
 *
 * @author Neha
 */
public enum RestServicesConstants {

	SHARE_CREDENTIAL_SERVICE("mosip.resident.credential.request.rest.uri");
	
	

	/** The service name. */
	private final String serviceName;

	/**
	 * Instantiates a new rest service contants.
	 *
	 * @param serviceName the service name
	 */
	private RestServicesConstants(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Gets the service name.
	 *
	 * @return the service name
	 */
	public String getServiceName() {
		return serviceName;
	}
}