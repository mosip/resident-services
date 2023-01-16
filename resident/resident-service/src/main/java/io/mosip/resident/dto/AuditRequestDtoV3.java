package io.mosip.resident.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The audit request dto v3.
 * 
 * @author Ritik Jain
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class AuditRequestDtoV3 extends AuditRequestDtoV2 {
	
	/** The id. */
	@NotNull
	@Size(min = 1, max = 64)
	private String id;
	
}
