package io.mosip.resident.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * This dto is used for conversion of an object.
 * 
 * @author Ritik Jain
 */
@Data
public class AuthLockStatusResponseDtoV2 implements Serializable {

	private static final long serialVersionUID = 5863264708458118491L;
	private List<AuthLockTypeStatusDtoV2> authTypes;

}
