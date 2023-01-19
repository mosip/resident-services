package io.mosip.resident.service;

import io.mosip.resident.exception.ResidentServiceCheckedException;

import java.io.IOException;

/**
 * This class is used to create service class for getting acknowledgement API.
 * @Author Kamesh Shekhar Prasad
 */
public interface AcknowledgementService {
    byte[] getAcknowledgementPDF(String eventId, String languageCode, int timeZoneOffset) throws ResidentServiceCheckedException, IOException;
}
