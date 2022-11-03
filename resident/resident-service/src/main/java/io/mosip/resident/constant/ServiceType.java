package io.mosip.resident.constant;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The Enum ServiceType.
 * @author Kamesh Shekhar Prasad
 */

public enum ServiceType {
    AUTHENTICATION_REQUEST(List.of(RequestType.AUTHENTICATION_REQUEST)),
	SERVICE_REQUEST(List.of(RequestType.DOWNLOAD_PERSONALIZED_CARD, RequestType.ORDER_PHYSICAL_CARD,
			RequestType.GET_MY_ID, RequestType.BOOK_AN_APPOINTMENT, RequestType.SEND_OTP, RequestType.VALIDATE_OTP)),
    DATA_UPDATE_REQUEST(List.of(RequestType.UPDATE_MY_UIN)),
    ID_MANAGEMENT_REQUEST(List.of(RequestType.GENERATE_VID, RequestType.REVOKE_VID,
            RequestType.VERIFY_PHONE_EMAIL, RequestType.AUTH_TYPE_LOCK_UNLOCK)),
    DATA_SHARE_REQUEST(List.of(RequestType.SHARE_CRED_WITH_PARTNER)),
    ALL(List.of(RequestType.values()));
	
    private List<RequestType> subTypes;

    ServiceType(List<RequestType> subTypes) {
        this.subTypes = Collections.unmodifiableList(subTypes);
    }
    
    public List<RequestType> getRequestType() {
        return subTypes;
    }
    
    public static Optional<ServiceType> getServiceTypeFromString(String serviceTypeString) {
        for (ServiceType serviceType : values()) {
            if (serviceType.name().equalsIgnoreCase(serviceTypeString)) {
                return Optional.of(serviceType);
            }
        }
        return Optional.empty();
    }
    
    public static Optional<String> getServiceTypeFromRequestType(RequestType requestType) {
        for (ServiceType serviceType : values()) {
        	List<RequestType> requestTypesList = serviceType.getRequestType();
            if (requestTypesList.contains(requestType)) {
                return Optional.of(serviceType.name());
            }
        }
        return Optional.empty();
    }
}
