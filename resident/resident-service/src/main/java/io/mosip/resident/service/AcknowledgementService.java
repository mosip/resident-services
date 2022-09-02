package io.mosip.resident.service;

import io.mosip.resident.exception.ResidentServiceCheckedException;

public interface AcknowledgementService {
    byte[] getAcknowledgementPDF(String eventId, String languageCode) throws ResidentServiceCheckedException;
}
