package io.mosip.resident.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The unauthenticated audit request dto.
 * 
 * @author Ritik Jain
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class UnauthenticatedAuditRequestDto extends AuthenticatedAuditRequestDto {

	/** The id. */
	@NotNull
	@Size(min = 1, max = 64)
	private String id;

}
