package io.mosip.resident.filter;

import io.mosip.kernel.websub.api.verifier.AuthenticatedContentVerifier;

public class TestSignatureVeirification {

	public static void main(String[] args) {
		String body = "{\"publisher\":\"IDA\", \"topic\":\"AUTH_TYPE_STATUS_UPDATE_ACK\", \"publishedOn\":\"2023-04-10T14:31:09.009Z\", \"event\":{\"id\":\"bea74f15-65a8-45bd-bd80-a98e4d7d2099\", \"requestId\":\"5fd12039-bc6a-44fc-87d6-83ecd25d7dd2\", \"timestamp\":null, \"data\":{\"authTypes\":[{\"authType\":\"otp\", \"authSubType\":\"email\", \"locked\":false, \"unlockForSeconds\":null, \"requestId\":\"5fd12039-bc6a-44fc-87d6-83ecd25d7dd2\", \"metadata\":null}, {\"authType\":\"bio\", \"authSubType\":\"FACE\", \"locked\":false, \"unlockForSeconds\":null, \"requestId\":\"5fd12039-bc6a-44fc-87d6-83ecd25d7dd2\", \"metadata\":null}, {\"authType\":\"bio\", \"authSubType\":\"FINGER\", \"locked\":false, \"unlockForSeconds\":null, \"requestId\":\"5fd12039-bc6a-44fc-87d6-83ecd25d7dd2\", \"metadata\":null}, {\"authType\":\"demo\", \"authSubType\":null, \"locked\":false, \"unlockForSeconds\":null, \"requestId\":\"5fd12039-bc6a-44fc-87d6-83ecd25d7dd2\", \"metadata\":null}, {\"authType\":\"otp\", \"authSubType\":\"phone\", \"locked\":false, \"unlockForSeconds\":null, \"requestId\":\"5fd12039-bc6a-44fc-87d6-83ecd25d7dd2\", \"metadata\":null}, {\"authType\":\"bio\", \"authSubType\":\"IRIS\", \"locked\":false, \"unlockForSeconds\":null, \"requestId\":\"5fd12039-bc6a-44fc-87d6-83ecd25d7dd2\", \"metadata\":null}], \"olv_partner_id\":\"mpartner-default-auth\"}}}";
		String signatureHeader = "sha256=17a5e0d0d884a30674be359ee6cd2335289e6abaaf86dc88eb71c8a0ba74952f";
		String secret = "EPpZWwqJqPdm9gV0";
		AuthenticatedContentVerifier authenticatedContentVerifier = new AuthenticatedContentVerifier();
		boolean contentVerified = authenticatedContentVerifier.isContentVerified(secret, body, signatureHeader);
		System.out.println("content verified : " + contentVerified);	
	}

}
