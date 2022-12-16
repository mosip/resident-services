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
    public static final String MOSIP_OIDC_JWT_SIGNED = "mosip.resident.oidc.userinfo.jwt.signed";
    public static final String MOSIP_OIDC_ENCRYPTION_ENABLED = "mosip.resident.oidc.userinfo.encryption.enabled";

    public static final String IDP_REFERENCE_ID = "mosip.resident.oidc.keymanager.reference.id";
    public static final String RESIDENT_APP_ID = "resident.appid";

    public static final String DATA_SHARE_URL = "mosip.datashare.url";
    public static final String DATA_SHARE_APPLICATION_ID = "mosip.datashare.application.id";
    public static final String DATA_SHARE_REFERENCE_ID = "mosip.datashare.reference.id";

    public static final String DOWNLOAD_UIN_CARD_ID = "mosip.resident.download.uin.card";
    public static final String LOWER_LEFT_X = "mosip.resident.service.uincard.lowerleftx";
    public static final String LOWER_LEFT_Y = "mosip.resident.service.uincard.lowerlefty";
    public static final String UPPER_RIGHT_X = "mosip.resident.service.uincard.upperrightx";
    public static final String UPPER_RIGHT_Y = "mosip.resident.service.uincard.upperrighty";
    public static final String REASON = "mosip.resident.service.uincard.signature.reason";

    public static final String SIGN_PDF_APPLICATION_ID = "mosip.resident.sign.pdf.application.id";
    public static final String SIGN_PDF_REFERENCE_ID = "mosip.resident.sign.pdf.reference.id";
    
    public static final String AUTHENTICATION_MODE_CLAIM_NAME = "mosip.resident.access_token.auth_mode.claim-name";

    public static final String MOSIP_RESIDENT_DOWNLOAD_PERSONALIZED_CARD = "mosip.resident.download.personalized.card";

    public static final String PASSWORD_ATTRIBUTE = "mosip.digitalcard.uincard.password";

    public static final String CREATE_PASSWORD_METHOD_NAME = "resident.create.password.method.name";

    public static final String DOWNLOAD_PERSONALIZED_CARD_NAMING_CONVENTION_PROPERTY = "mosip.resident.download.personalized.card.naming.convention";

    public static final String RESIDENT_IDENTITY_SCHEMATYPE = "resident.identity.schematype.with.photo";

    public static final String IS_PASSWORD_FLAG_ENABLED = "mosip.digitalcard.pdf.password.enable.flag";

    public static final String CREDENTIAL_TYPE_PROPERTY = "mosip.resident.request.credential.credentialType";
    public static final String CREDENTIAL_ISSUER = "mosip.credential.issuer";
    public static final String CREDENTIAL_ENCRYPTION_FLAG = "mosip.resident.request.credential.isEncrypt";
    public static final String CREDENTIAL_ENCRYPTION_KEY = "mosip.resident.request.credential.encryption.key";
    public static final String VID_DOWNLOAD_CARD_ID = "mosip.resident.request.vid.card.id";
    public static final String VID_DOWNLOAD_CARD_VERSION = "mosip.resident.request.vid.card.version";

    public static final String SERVICE_HISTORY_PROPERTY_TEMPLATE_TYPE_CODE = "mosip.resident.service.history.template.type.code";
    public static final String PHOTO_ATTRIBUTE_NAME = "mosip.resident.photo.attribute.name";
    public static final String APPLICANT_NAME_PROPERTY = "mosip.resident.applicant.name.property";

    public static final String INDIVIDUALID_CLAIM_NAME = "mosip.resident.individual.id.claim.name";
    public static final String MOSIP_CREDENTIAL_TYPE_PROPERTY="mosip.digital.card.credential.type";

    public static final String CREDENTIAL_REQUEST_SERVICE_ID = "mosip.credential.request.service.id";
    public static final String CREDENTIAL_REQUEST_SERVICE_VERSION = "mosip.credential.request.service.version";
    public static final String DOWNLOAD_REGISTRATION_CENTRE_FILE_NAME_CONVENTION_PROPERTY = "mosip.resident.download.registration.centre.file.name.convention";
    public static final String DOWNLOAD_NEAREST_REGISTRATION_CENTRE_FILE_NAME_CONVENTION_PROPERTY = "mosip.resident.download.nearest.registration.centre.file.name.convention";
    public static final String DOWNLOAD_SUPPORTING_DOCUMENT_FILE_NAME_CONVENTION_PROPERTY = "mosip.resident.download.supporting.document.file.name.convention";
    public static final String ACK_MANAGE_MY_VID_NAMING_CONVENTION_PROPERTY = "mosip.resident.ack.manage_my_vid.name.convention";
    public static final String ACK_SECURE_MY_ID_NAMING_CONVENTION_PROPERTY = "mosip.resident.ack.secure_my_id.name.convention";
    public static final String ACK_PERSONALIZED_CARD_NAMING_CONVENTION_PROPERTY = "mosip.resident.ack.personalised_card.name.convention";
    public static final String ACK_UPDATE_MY_DATA_NAMING_CONVENTION_PROPERTY = "mosip.resident.ack.update_my_data.name.convention";
    public static final String ACK_SHARE_CREDENTIAL_NAMING_CONVENTION_PROPERTY = "mosip.resident.ack.share_credential.name.convention";
    public static final String ACK_ORDER_PHYSICAL_CARD_NAMING_CONVENTION_PROPERTY = "mosip.resident.ack.order_physical_card.name.convention";
    public static final String ACK_NAMING_CONVENTION_PROPERTY = "mosip.resident.ack.name.convention";
    public static final String UIN_CARD_NAMING_CONVENTION_PROPERTY = "mosip.resident.uin.card.name.convention";
    public static final String VID_CARD_NAMING_CONVENTION_PROPERTY = "mosip.resident.vid.card.name.convention";
    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";
    public static final String REGISTRATION_CENTRE_TEMPLATE_PROPERTY = "resident.template.registration.centers.list";
    public static final String SUPPORTING_DOCS_TEMPLATE_PROPERTY = "resident.template.support-docs-list";
    public static final String FROM_DATE_TIME = "fromDateTime";
    public static final String TO_DATE_TIME = "toDateTime";
    public static final String DOWNLOAD_SERVICE_HISTORY_FILE_NAME_CONVENTION_PROPERTY = "mosip.resident.download.service.history.file.name.convention";
    public static final String GRIEVANCE_REQUEST_ID = "mosip.resident.grievance.ticket.request.id";
    public static final String GRIEVANCE_REQUEST_VERSION = "mosip.resident.grievance.ticket.request.version";

    public static final String NAME_FROM_PROFILE = "mosip.resident.name.token.claim-name";

    public static final String EMAIL_FROM_PROFILE = "mosip.resident.email.token.claim-email";
    public static final String PHONE_FROM_PROFILE = "mosip.resident.phone.token.claim-phone";

    public static final String MESSAGE_CODE_MAXIMUM_LENGTH = "mosip.resident.message.code.maximum.length";

}
