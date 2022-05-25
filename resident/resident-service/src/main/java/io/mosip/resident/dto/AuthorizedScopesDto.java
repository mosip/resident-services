package io.mosip.resident.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
	
	public static boolean hasAllScopes(List<String> scopes) {
		List<? extends String> authorities = SecurityContextHolder
												.getContext()
												.getAuthentication()
												.getAuthorities()
												.stream()
												.map(GrantedAuthority::getAuthority)
												.collect(Collectors.toList());
		return scopes.stream()
				.map(scope -> "SCOPE_" + scope)
				.allMatch(authorities::contains);
	}
	


}