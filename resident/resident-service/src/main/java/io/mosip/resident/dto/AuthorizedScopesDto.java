package io.mosip.resident.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component("authorizedScopes")
@ConfigurationProperties(prefix = "mosip.scope.resident")
@Getter
@Setter
public class AuthorizedScopesDto {

	private List<String> getinputattributevalues;

	private List<String> patchrevokevid;

	private List<String> postgeneratevid;

	private List<String> getvids;

	private List<String> postAuthTypeStatus;

	private List<String> postAuthTypeUnlock;

	private List<String> getAuthLockStatus;

	private List<String> patchUpdateUin;

	private List<String> postUploadDocuments;

	private List<String> getUploadedDocuments;

	private List<String> getUploadedDocument;

	private List<String> getServiceAuthHistoryRoles;

	private List<String> getServiceRequestUpdate;

	private List<String> getEventIdStatus;

	private List<String> getPartnersByPartnerType;

	private List<String> getDownloadCard;

	private List<String> postSendPhysicalCard;

	private List<String> deleteUploadDocuments;

	private List<String> getAllTemplateBylangCodeAndTemplateTypeCode;

	private List<String> postAuditLog;

	private List<String> getUnreadServiceList;

	private List<String> getNotificationCount;

	private List<String> getNotificationClick;

	private List<String> getupdatedttimes;

	private List<String> postPinStatus;

	private List<String> postUnPinStatus;

	private List<String> getAcknowledgement;
	
	private List<String> postRequestDownloadPersonalizedCard;

	private List<String> postRequestShareCredWithPartner;

	private List<String> postTransliteration;


}