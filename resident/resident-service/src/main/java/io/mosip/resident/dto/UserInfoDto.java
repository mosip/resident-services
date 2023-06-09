package io.mosip.resident.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

/**
 * This class is used to provide response for user profile
 * 
 * @author Neha Farheen
 *
 */
@Data
public class UserInfoDto {
	
	private String fullName;
	
	private String lastLogin;
	
	private Map<String, Object> photo;
	
	private String email;
	
	private String  phone; 

}
