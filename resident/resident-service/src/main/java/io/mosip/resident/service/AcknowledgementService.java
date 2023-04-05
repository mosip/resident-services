package io.mosip.resident.service;

import java.io.IOException;

import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * This class is used to create service class for getting acknowledgement API.
 * @Author Kamesh Shekhar Prasad
 */
public interface AcknowledgementService {
    byte[] getAcknowledgementPDF(String eventId, String languageCode, int timeZoneOffset) throws ResidentServiceCheckedException, IOException;
}
