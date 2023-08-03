package io.mosip.resident.service;

import java.io.IOException;

import io.mosip.resident.exception.ResidentServiceCheckedException;
import reactor.util.function.Tuple2;

/**
 * This class is used to create service class for getting acknowledgement API.
 * @Author Kamesh Shekhar Prasad
 */
public interface AcknowledgementService {
    Tuple2<byte[], String> getAcknowledgementPDF(String eventId, String languageCode, int timeZoneOffset, String locale) throws ResidentServiceCheckedException, IOException;
}
