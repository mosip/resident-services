
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

    public static final String PUBLIC_URL = "mosipbox.public.url";
    
    public static final String NOTIFICATION_ZONE = "mosip.notification.timezone";
    public static final String NOTIFICATION_DATE_PATTERN = "mosip.notification.date.pattern";
    public static final String NOTIFICATION_TIME_PATTERN = "mosip.notification.time.pattern";
    
    public static final String EVENT_ID = "eventId";

    public static final String DOWNLOAD_CARD = "/download/card/";

    public static final String STATUS_CODE = "statusCode";
    public static final String RESIDENT = "RESIDENT";

    public static final String RESIDENT_CONTACT_DETAILS_UPDATE_ID="resident.contact.details.update.id";

    public static final String RESIDENT_CONTACT_DETAILS_SEND_OTP_ID="resident.contact.details.send.otp.id";


    public static final String MOSIP_OIDC_JWT_SIGNED = "mosip.resident.oidc.userinfo.jwt.signed";
    public static final String MOSIP_OIDC_JWT_VERIFY_ENABLED = "mosip.resident.oidc.userinfo.jwt.verify.enabled";
    public static final String MOSIP_OIDC_ENCRYPTION_ENABLED = "mosip.resident.oidc.userinfo.encryption.enabled";

    public static final String IDP_REFERENCE_ID = "mosip.resident.oidc.keymanager.reference.id";
    public static final String RESIDENT_APP_ID = "resident.appid";

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
    public static final String SUBJECT_CLAIM_NAME = "mosip.resident.access_token.subject.claim-name";

    public static final String MOSIP_RESIDENT_DOWNLOAD_PERSONALIZED_CARD_ID = "mosip.resident.download.personalized.card.id";

    public static final String PASSWORD_ATTRIBUTE = "mosip.digitalcard.uincard.password";

    public static final String CREATE_PASSWORD_METHOD_NAME = "resident.create.password.method.name";

    public static final String DOWNLOAD_PERSONALIZED_CARD_NAMING_CONVENTION_PROPERTY = "mosip.resident.download.personalized.card.naming.convention";

    public static final String IS_PASSWORD_FLAG_ENABLED = "mosip.digitalcard.pdf.password.enable.flag";

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
    public static final String NOT_AVAILABLE = "NA";
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

    public static final String TRANSACTION_TYPE_CODE = "transactionTypeCode";
    public static final String AID_STATUS = "aidStatus";

    public static final String CHECK_STATUS_ID = "resident.checkstatus.id";

    public static final String CHECK_STATUS_INDIVIDUAL_ID = "mosip.resident.checkstatus.individualid.id";
    public static final String CHECKSTATUS_INDIVIDUALID_VERSION = "mosip.resident.checkstatus.individualid.version";
    
    public static final String RID = "rid";

    public static final String ADDITIONAL_ATTRIBUTE_TO_FETCH = "resident.additional.identity.attribute.to.fetch";
    public static final String COMMA = ",";
    public static final String COLON = ":";

    public static final String FILE_TEXT = "fileText";
    public static final String DOLLAR = "$";
    public static final String AUTH_TYPE = "authType";

    public static final String CHANNEL = "channel";

    public static final String MASK_PREFIX = "masked_";
    public static final String MASK_PERPETUAL_VID = "masked_perpetualVID";

    public static final String HTTP_STATUS_CODE = "httpStatusCode";
    public static final String REQ_RES_ID = "reqResId";
    public static final String REQ_RES_VERSION = "mosip.resident.request.response.version";
    public static final String RESIDENT_USER_PROFILE_ID = "mosip.resident.user.profile.id";

    public static final String ACK_SHARE_CREDENTIAL_TEMPLATE_PROPERTY = "resident.template.ack.share-cred-with-partner";
    public static final String ACK_AUTHENTICATION_REQUEST_TEMPLATE_PROPERTY = "resident.template.ack.authentication.request";
    public static final String ACK_DOWNLOAD_PERSONALIZED_CARD_TEMPLATE_PROPERTY = "resident.template.ack.download-a-personalized-card";
    public static final String ACK_ORDER_PHYSICAL_CARD_TEMPLATE_PROPERTY = "resident.template.ack.order-a-physical-card";
    public static final String ACK_GET_MY_ID_TEMPLATE_PROPERTY = "resident.template.ack.get.my.id";
    public static final String ACK_UPDATE_MY_UIN_TEMPLATE_PROPERTY = "resident.template.ack.update-demographic-data";
    public static final String ACK_MANAGE_MY_VID_TEMPLATE_PROPERTY = "resident.template.ack.manage-my-vid";
    public static final String ACK_AUTH_TYPE_LOCK_UNLOCK_TEMPLATE_PROPERTY = "resident.template.ack.secure-my-id";
    public static final String ACK_VID_CARD_DOWNLOAD_TEMPLATE_PROPERTY = "resident.template.ack.vid.card.download";
    public static final String ACK_VERIFY_PHONE_EMAIL_TEMPLATE_PROPERTY = "resident.template.ack.verify-email-id-or-phone-number";
    public static final String ACTION_PERFORMED = "actionPerformed";
    public static final String VID_TYPE = "vidType";
    public static final String MASKED_VID = "*masked VID*";
    public static final String UI_DATE_TIME_PATTERN = "resident.ui.datetime.pattern";
    public static final String FILENAME_DATETIME_PATTERN = "resident.filename.datetime.pattern";
    
    public static final String NO_ID = "NO_ID";
    public static final String NO_ID_TYPE = "NO_ID_TYPE";

    public static final String OTP_EXPIRED_ERR_CODE = "IDA-OTA-003"; 
    public static final String OTP_INVALID_ERR_CODE = "IDA-OTA-004";
    public static final String INVALID_ID_ERR_CODE = "IDA-MLC-009";
    public static final String OTP_AUTH_LOCKED_ERR_CODE = "IDA-MLC-019";
    public static final String PHONE = "PHONE";
    public static final String EMAIL = "EMAIL";
    
    // IP headers constants start---
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_REAL_IP = "x-real-ip";
    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
    public static final String HTTP_X_FORWARDED = "HTTP_X_FORWARDED";
    public static final String HTTP_X_CLUSTER_CLIENT_IP = "HTTP_X_CLUSTER_CLIENT_IP";
    public static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    public static final String HTTP_FORWARDED_FOR = "HTTP_FORWARDED_FOR";
    public static final String HTTP_FORWARDED = "HTTP_FORWARDED";
    public static final String HTTP_VIA = "HTTP_VIA";
    public static final String REMOTE_ADDR = "REMOTE_ADDR";
    // IP headers constants end---

    public static final String VID_POLICIES = "vidPolicies";
    public static final String VID_POLICY = "vidPolicy";
    public static final String PERPETUAL = "Perpetual";

    public static final String VID_ACTIVE_STATUS = "mosip.idrepo.vid.reactive-status";
    public static final int UTC_TIMEZONE_OFFSET = 0;
    public static final String INDIVIDUAL_ID = "individualId";
    public static final String MUST_NOT_BE_EMPTY = " must not be empty.";

    public static final String ID = "id";
    public static final String REVOKE_VID = "revokeVidId";
    public static final String SCHEMA_TYPE = "schemaType";
    public static final String ALLOWED_FILE_TYPE = "mosip.allowed.extension";
    public static final  String MANDATORY_LANGUAGE="mosip.mandatory-languages";
    public static final CharSequence ATTRIBUTES = "attributes";
}
