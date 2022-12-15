package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.GrievanceRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.GrievanceService;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.mosip.resident.constant.MappingJsonConstants.EMAIL;
import static io.mosip.resident.constant.MappingJsonConstants.PHONE;

@Service
public class GrievanceServiceImpl implements GrievanceService {

    private static final String LANGUAGE = "language";
    private static final String VALUE = "value";
    @Autowired
    private IdentityService identityService;

    @Autowired
    private Environment environment;

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private Utilities utilities;

    private static final Logger logger = LoggerConfiguration.logConfig(GrievanceServiceImpl.class);

    @Override
    public ResponseWrapper<Object> getGrievanceTicket(MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO) throws IOException, ApisResourceAccessException {
        try {
            grievanceRequestDTOMainRequestDTO = fillDefaultValueFromProfile(grievanceRequestDTOMainRequestDTO);
            String ticketId = UUID.randomUUID().toString();
            insertDataInGrievanceTable(ticketId, grievanceRequestDTOMainRequestDTO);
        }
        catch (ResidentServiceCheckedException e) {
            auditUtil.setAuditRequestDto(EventEnum.DOWNLOAD_PERSONALIZED_CARD);
            logger.error("Unable to get attributes- "+e);
            throw new ResidentServiceException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD, e);
        } catch (IOException e) {
            auditUtil.setAuditRequestDto(EventEnum.DOWNLOAD_PERSONALIZED_CARD);
            logger.error("Unable to get attributes- "+e);
            throw new IOException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD.getErrorCode(), e);
        } catch (ApisResourceAccessException e) {
            throw new ApisResourceAccessException(ResidentErrorCode.GRIEVANCE_TICKET_GENERATION_FAILED.getErrorCode(), e);
        }
        return null;
    }

    private void insertDataInGrievanceTable(String ticketId, MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO) {

    }

    private MainRequestDTO<GrievanceRequestDTO> fillDefaultValueFromProfile(MainRequestDTO<GrievanceRequestDTO>
                                                                                    grievanceRequestDTOMainRequestDTO)
            throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        if (grievanceRequestDTOMainRequestDTO.getRequest().getName() == null) {
            grievanceRequestDTOMainRequestDTO.getRequest().setName(getFullNameFromProfile(
                    this.environment.getProperty(ResidentConstants.FULL_NAME_ATTRIBUTE_NAME)));
        }
        if(grievanceRequestDTOMainRequestDTO.getRequest().getPhoneNo() == null){
            grievanceRequestDTOMainRequestDTO.getRequest().setPhoneNo(getFullNameFromProfile(PHONE));
        }
        if(grievanceRequestDTOMainRequestDTO.getRequest().getEmailId() == null){
            grievanceRequestDTOMainRequestDTO.getRequest().setEmailId(getFullNameFromProfile(EMAIL));
        }
        return grievanceRequestDTOMainRequestDTO;
    }

    private String getFullNameFromProfile(String attribute) throws ApisResourceAccessException, ResidentServiceCheckedException, IOException {
        Map<String, Object> identityAttributes = null;
        String name="";
        identityAttributes = (Map<String, Object>) identityService.getIdentityAttributes(
                    identityService.getResidentIndvidualId(), this.environment.getProperty(ResidentConstants.RESIDENT_IDENTITY_SCHEMATYPE));
        Object attributeObject = identityAttributes.get(attribute);
        if (attributeObject instanceof List) {
            List<Map<String, Object>> attributeMapObject = (List<Map<String, Object>>) attributeObject;
            for (Map<String, Object> attributeInLanguage : attributeMapObject) {
                /**
                 * 1st language code is taken from mandatory/optional languages properties
                 */
                String languageCode = utilities.getLanguageCode();
                if (attributeInLanguage.containsKey(LANGUAGE) &&
                        attributeInLanguage.get(LANGUAGE).toString().equalsIgnoreCase(languageCode)) {
                    name = ((String) attributeInLanguage.get(VALUE));
                }
            }
        } else{
            name = ((String) attributeObject);
        }
        return name;
    }
}
