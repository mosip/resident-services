package io.mosip.resident.test.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.dto.EventStatusResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.validator.RequestValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used to test the get Status service
 * @author Kamesh Shekhar Prasad
 */

@RunWith(SpringRunner.class)
public class ResidentServiceGetEventStatusTest {

    @Mock
    private AuditUtil audit;

    @InjectMocks
    private ResidentService residentService = new ResidentServiceImpl();

    @Mock
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private RequestValidator validator;

    @Mock
    private TemplateUtil templateUtil;

    private String eventId;
    private String langCode;
    private ResponseWrapper<EventStatusResponseDTO> responseWrapper;
    private EventStatusResponseDTO eventStatusResponseDTO;
    private Optional<ResidentTransactionEntity> residentTransactionEntity;
    private RequestType requestType;
    Map<String, String> templateVariables;

    @Before
    public void setup() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        eventId = "123456789";
        requestType = RequestType.AUTHENTICATION_REQUEST;
        langCode = "eng";
        responseWrapper = new ResponseWrapper<>();
        templateVariables = new java.util.HashMap<>();
        eventStatusResponseDTO = new EventStatusResponseDTO();
        residentTransactionEntity = Optional.of(new ResidentTransactionEntity());
        eventStatusResponseDTO.setEventId("123456789");
        eventStatusResponseDTO.setEventStatus("COMPLETED");
        responseWrapper.setResponse(eventStatusResponseDTO);
        residentTransactionEntity.get().setRequestTypeCode(RequestType.AUTHENTICATION_REQUEST.name());
        residentTransactionEntity.get().setStatusCode(EventStatusSuccess.AUTHENTICATION_SUCCESSFUL.name());
        residentTransactionEntity.get().setEventId(eventId);
        residentTransactionEntity.get().setLangCode(langCode);
        residentTransactionEntity.get().setRequestSummary("requestSummary");
        residentTransactionEntity.get().setRequestTypeCode(requestType.name());
        residentTransactionEntity.get().setCrDtimes(LocalDateTime.now());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        templateVariables.put("eventId", eventId);
        templateVariables.put("authenticationMode", "OTP");
        templateVariables.put("partnerName", "partnerName");
        templateVariables.put("purpose", "authentication");
        Mockito.when(requestType.getAckTemplateVariables(templateUtil, Mockito.anyString())).thenReturn(templateVariables);
        Mockito.when(identityServiceImpl.getResidentIndvidualId()).thenReturn("123456789");
        Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
    }

    @Test
    public void getEventStatusTest() throws ResidentServiceCheckedException {
        ResponseWrapper<EventStatusResponseDTO> resultResponseWrapper =residentService.getEventStatus(eventId, langCode);
        assert resultResponseWrapper.getResponse().getEventId().equals(eventId);
    }


}
