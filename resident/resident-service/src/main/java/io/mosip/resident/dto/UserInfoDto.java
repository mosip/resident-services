package io.mosip.resident.dto;

import java.time.LocalDateTime;
import java.util.List;
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
	
	private LocalDateTime lastLogin;
	
	private String ip;
	
	private String host;
	
	private String machineType;
	
	private Map<String, Object> photo;
	
	private String email;
	
	private String  phone; 

}
