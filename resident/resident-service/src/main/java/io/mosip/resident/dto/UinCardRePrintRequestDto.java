package io.mosip.resident.dto;

import java.io.Serializable;

import javax.validation.Valid;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Instantiates a new resident service re-print request dto.
 * 
 * @author Ranjitha
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UinCardRePrintRequestDto extends BaseRequestDTO implements Serializable{
	
	private static final long serialVersionUID = -7951024873226775006L;
	@Valid
	private UinCardRequestDto1 request;

}
