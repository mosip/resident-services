package io.mosip.resident.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.GrievanceRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;

/**
 * This class is used to create service class for getting Grievance Service API.
 * @Author Kamesh Shekhar Prasad
 */
public interface GrievanceService {
    ResponseWrapper<Object> getGrievanceTicket(MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO) throws IOException, ApisResourceAccessException, NoSuchAlgorithmException, io.mosip.resident.exception.NoSuchAlgorithmException;
}
