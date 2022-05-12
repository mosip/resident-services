package io.mosip.resident.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.resident")
@Getter
@Setter
public class AuthorizedRolesDto {

  //Idrepo controller
	
	private List<String> getinputattributevalues;


}