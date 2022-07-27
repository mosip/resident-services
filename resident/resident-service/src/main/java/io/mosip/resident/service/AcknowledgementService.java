package io.mosip.resident.service;

public interface AcknowledgementService {
    byte[] getAcknowledgementPDF(String eventId, String languageCode);
}
