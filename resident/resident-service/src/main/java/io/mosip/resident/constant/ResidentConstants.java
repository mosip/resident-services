package io.mosip.resident.constant;

/**
 * It contains all the constants used in the Resident Service
 * 
 * @author Manoj SP
 */
public class ResidentConstants {
	
	private ResidentConstants() {
	}

    public static final String OBJECT_STORE_ACCOUNT_NAME = "mosip.resident.object.store.account-name";

    public static final String OBJECT_STORE_BUCKET_NAME = "mosip.resident.object.store.bucket-name";

    public static final String OBJECT_STORE_ADAPTER_NAME = "mosip.resident.object.store.adapter-name";

    public static final String CRYPTO_APPLICATION_NAME = "mosip.resident.keymanager.application-name";

    public static final String CRYPTO_REFERENCE_ID = "mosip.resident.keymanager.reference-id";

    public static final String CRYPTO_ENCRYPT_URI = "mosip.resident.keymanager.encrypt-uri";

    public static final String CRYPTO_DECRYPT_URI = "mosip.resident.keymanager.decrypt-uri";

    public static final String VIRUS_SCANNER_ENABLED = "mosip.resident.virus-scanner.enabled";

    public static final String SUBSCRIPTIONS_DELAY_ON_STARTUP = "subscriptions-delay-on-startup_millisecs";
    
    public static final String CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY = "mosip.resident.update.service.status.job.initial-delay";
    
    public static final String CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY_DEFAULT = "60000";
    
    public static final String CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL = "mosip.resident.update.service.status.job.interval.millisecs";
    
    public static final String CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL_DEFAULT = "60000";
    
    public static final String IS_CREDENTIAL_STATUS_UPDATE_JOB_ENABLED = "mosip.resident.update.service.status.job.enabled";

    public static final String ID_TYPE = "idType";

}
