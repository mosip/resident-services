package io.mosip.resident.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.resident")
@Getter
@Setter
public class AuthorizedRolesDto {

	private List<String> getinputattributevalues;
	
	private List<String> patchrevokevid;
	
	private List<String> postgeneratevid;
	
	private List<String> getvids;


}