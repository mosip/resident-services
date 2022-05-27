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
	
}