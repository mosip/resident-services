package io.mosip.resident.service.impl;

import static io.mosip.resident.constant.RegistrationConstants.SUCCESS;

import java.util.HashMap;
import java.util.UUID;

import io.mosip.resident.exception.NoSuchAlgorithmException;
import io.mosip.resident.util.AvailableClaimValueUtility;
import io.mosip.resident.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.GrievanceRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.entity.ResidentGrievanceEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.repository.ResidentGrievanceRepository;
import io.mosip.resident.service.GrievanceService;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to implement service class of grievance api.
 */
@Service
public class GrievanceServiceImpl implements GrievanceService {

    private static final String TICKET_ID = "ticketId";
    @Autowired
    private IdentityServiceImpl identityService;

    @Autowired
    private Environment environment;

    @Autowired
    private ResidentGrievanceRepository residentGrievanceRepository;

    private static final Logger logger = LoggerConfiguration.logConfig(GrievanceServiceImpl.class);

    @Autowired
    private AvailableClaimValueUtility availableClaimValueUtility;

    @Autowired
    private Utility utility;

    @Override
    public ResponseWrapper<Object> getGrievanceTicket(MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO) throws ApisResourceAccessException, NoSuchAlgorithmException {
    	logger.debug("GrievanceServiceImpl::getGrievanceTicket()::entry");
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(grievanceRequestDTOMainRequestDTO.getId());
        responseWrapper.setVersion(grievanceRequestDTOMainRequestDTO.getVersion());
        responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTime());
        try {
            grievanceRequestDTOMainRequestDTO = fillDefaultValueFromProfile(grievanceRequestDTOMainRequestDTO);
            String ticketId = UUID.randomUUID().toString();
            insertDataInGrievanceTable(ticketId, grievanceRequestDTOMainRequestDTO);
            HashMap<String, String> response = new HashMap<>();
            response.put(TICKET_ID, ticketId);
            responseWrapper.setResponse(response);
        } catch (ApisResourceAccessException e) {
        	logger.error("%s - %s", ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e.getMessage());
            throw new ApisResourceAccessException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(), e.getMessage(), e);
        } catch (NoSuchAlgorithmException | java.security.NoSuchAlgorithmException e) {
            logger.error("%s - %s", ResidentErrorCode.NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e.getMessage());
            throw new NoSuchAlgorithmException(ResidentErrorCode.NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(), e.getMessage(), e);
        }
        logger.debug("GrievanceServiceImpl::getGrievanceTicket()::exit");
        return responseWrapper;
    }

    private void insertDataInGrievanceTable(String ticketId, MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO) throws NoSuchAlgorithmException, java.security.NoSuchAlgorithmException {
        ResidentGrievanceEntity residentGrievanceEntity = new ResidentGrievanceEntity();
        residentGrievanceEntity.setId(ticketId);
        residentGrievanceEntity.setEventId(grievanceRequestDTOMainRequestDTO.getRequest().getEventId());
        residentGrievanceEntity.setName(grievanceRequestDTOMainRequestDTO.getRequest().getName());
        residentGrievanceEntity.setEmailId(utility.getRefIdHash(grievanceRequestDTOMainRequestDTO.getRequest().getEmailId()));
        residentGrievanceEntity.setAlternateEmailId(utility.getRefIdHash(grievanceRequestDTOMainRequestDTO.getRequest().getAlternateEmailId()));
        residentGrievanceEntity.setPhoneNo(utility.getRefIdHash(grievanceRequestDTOMainRequestDTO.getRequest().getPhoneNo()));
        residentGrievanceEntity.setAlternatePhoneNo(utility.getRefIdHash(grievanceRequestDTOMainRequestDTO.getRequest().getAlternatePhoneNo()));
        residentGrievanceEntity.setMessage(grievanceRequestDTOMainRequestDTO.getRequest().getMessage());
        residentGrievanceEntity.setStatus(SUCCESS);
        residentGrievanceEntity.setCrBy(this.environment.getProperty(ResidentConstants.RESIDENT_APP_ID));
        residentGrievanceEntity.setCrDtimes(DateUtils.getUTCCurrentDateTime());
        residentGrievanceRepository.save(residentGrievanceEntity);
    }

    private MainRequestDTO<GrievanceRequestDTO> fillDefaultValueFromProfile(MainRequestDTO<GrievanceRequestDTO>
                                                                                    grievanceRequestDTOMainRequestDTO)
            throws ApisResourceAccessException {
        if (grievanceRequestDTOMainRequestDTO.getRequest().getName() == null) {
            grievanceRequestDTOMainRequestDTO.getRequest().setName(availableClaimValueUtility.getAvailableClaimValue(
                    this.environment.getProperty(ResidentConstants.NAME_FROM_PROFILE)));
        }
        if(grievanceRequestDTOMainRequestDTO.getRequest().getPhoneNo() == null){
            grievanceRequestDTOMainRequestDTO.getRequest().setPhoneNo(availableClaimValueUtility.getAvailableClaimValue(
                    this.environment.getProperty(ResidentConstants.PHONE_FROM_PROFILE)
            ));
        }
        if(grievanceRequestDTOMainRequestDTO.getRequest().getEmailId() == null){
            grievanceRequestDTOMainRequestDTO.getRequest().setEmailId(availableClaimValueUtility.getAvailableClaimValue(
                    this.environment.getProperty(ResidentConstants.EMAIL_FROM_PROFILE)));
        }
        return grievanceRequestDTOMainRequestDTO;
    }

}
