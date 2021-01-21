package io.mosip.resident.dto;

import org.json.JSONObject;

import lombok.Data;

@Data
public class PartnerCredentialTypePolicyDto {

	private String partnerId;

	private String credentialType;

	private String policyId;

	private String policyName;

	private String policyDesc;

	private String policyType;

	private String publishDate;

	private String validTill;

	private String status;

	private String version;

	private String schema;

	private Boolean is_Active;

	private String cr_by;

	private String cr_dtimes;

	private String up_by;

	private String upd_dtimes;

	private JSONObject policies;

}
