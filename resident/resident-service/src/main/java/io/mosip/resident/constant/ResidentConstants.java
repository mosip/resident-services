package io.mosip.resident.constant;

/**
 * It contains all the constants used in the Resident Service
 * 
 * @author Manoj SP
 */
public class ResidentConstants {

    public static final String DOWNLOAD_UIN_CARD_ID = "mosip.resident.download.uin.card";


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
    
    public static final String PUBLIC_URL = "mosipbox.public.url";
    
    public static final String NOTIFICATION_ZONE = "mosip.notification.timezone";
    public static final String NOTIFICATION_DATE_PATTERN = "mosip.notification.date.pattern";
    public static final String NOTIFICATION_TIME_PATTERN = "mosip.notification.time.pattern";
    
    public static final String EVENT_ID = "eventId";
    
    public static final String EVENT_DETAILS = "eventDetails";
    
    public static final String NAME = "name";
    
    public static final String DATE = "date";
    
    public static final String TIME = "time";

    public static final String DOWNLOAD_CARD = "/download/card/";

    public static final String STATUS_CODE = "statusCode";

    public static final String URL = "url";
    
    public static final String DOWNLOAD_LINK = "downloadLink";
    
    public static final String TRACK_SERVICE_REQUEST_LINK = "trackServiceRequestLink";

    public static final String TXN_ID = "txnId";
    
    public static final String RESIDENT = "RESIDENT";

    public static final String RESIDENT_CONTACT_DETAILS_UPDATE_ID="resident.contact.details.update.id";

    public static final String RESIDENT_CONTACT_DETAILS_SEND_OTP_ID="resident.contact.details.send.otp.id";


    public static final String IDA_TOKEN_CLAIM_NAME = "mosip.resident.oidc.id_token.ida_token.claim-name";
    public static final String MOSIP_OIDC_ENCRYPTION_ENABLED = "mosip.resident.oidc.userinfo.encryption.enabled";
    public static final String IDP_REFERENCE_ID = "mosip.resident.oidc.keymanager.reference.id";
    public static final String RESIDENT_APP_ID = "resident.appid";

    public static final String DATA_SHARE_URL = "mosip.datashare.url";
    public static final String DATA_SHARE_APPLICATION_ID = "mosip.datashare.application.id";
    public static final String DATA_SHARE_REFERENCE_ID = "mosip.datashare.reference.id";


}
