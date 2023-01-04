package io.mosip.resident.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.GrievanceRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;

import java.io.IOException;

/**
 * This class is used to create service class for getting Grievance Service API.
 * @Author Kamesh Shekhar Prasad
 */
public interface GrievanceService {
    ResponseWrapper<Object> getGrievanceTicket(MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO) throws IOException, ApisResourceAccessException;
}
