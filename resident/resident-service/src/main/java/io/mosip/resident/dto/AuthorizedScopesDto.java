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
	
	private List<String> getAuthTransactions;
	
	private List<String> postAuthTypeStatus;
	
	private List<String> postAuthTypeUnlock;
	
	private List<String> getAuthLockStatus;
	
	private List<String> patchUpdateUin;
	
	private List<String> postUploadDocuments;
	
	private List<String> getUploadedDocuments;

	private List<String> getServiceAuthHistoryRoles;

	private List<String> getServiceRequestUpdate;
	
	private List<String> getPartnersByPartnerType;
	
}