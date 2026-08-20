package io.mosip.resident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthHistoryRequestDTO {

	@NotBlank
	private String transactionID;

	@NotBlank
	@Size(min = 12, max = 36, message = "Individual ID must be between 12 and 36 characters")
	private String individualId;

	@NotBlank
	@Size(min = 6, max = 6, message = "OTP must be 6 digits")
	private String otp;

	@Pattern(regexp = "^[0-9]*$", message = "Pagination fields must be numeric")
	private String pageStart;

	@Pattern(regexp = "^[0-9]*$", message = "Pagination fields must be numeric")
	private String pageFetch;

}
