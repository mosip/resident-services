package io.mosip.resident.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentReprintRequestDto implements Serializable {
	private static final long serialVersionUID = -4492209826585681216L;
	
	private String transactionID;
	
	private String individualId;

	private String individualIdType;
	
	private String cardType;

	private String otp;
}
