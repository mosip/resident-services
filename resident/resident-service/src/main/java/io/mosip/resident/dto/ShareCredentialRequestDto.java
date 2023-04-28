package io.mosip.resident.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ritik Jain
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareCredentialRequestDto {

	private String partnerId;

	private List<SharableAttributesDTO> sharableAttributes;

	private String purpose;

	private String consent;

}
