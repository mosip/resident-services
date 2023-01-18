package io.mosip.resident.test.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.dto.EventStatusResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.EidNotBelongToSessionException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.validator.RequestValidator;
import reactor.util.function.Tuples;

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
    
    @Mock
	private ResidentServiceRestClient restClient;
    
    @Mock
    private ProxyMasterdataService proxyMasterdataService;

    @Mock
    private Environment environment;

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
        residentTransactionEntity.get().setTokenId("123456789");
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        templateVariables.put("eventId", eventId);
        templateVariables.put("authenticationMode", "OTP");
        templateVariables.put("partnerName", "partnerName");
        templateVariables.put("purpose", "authentication");
        Mockito.when(requestType.getAckTemplateVariables(templateUtil, Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(Tuples.of(templateVariables, ""));
        Mockito.when(identityServiceImpl.getResidentIndvidualId()).thenReturn("123456789");
        Mockito.when(identityServiceImpl.getResidentIdaToken()).thenReturn("123456789");
        Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
        Mockito.when(templateUtil.getPurposeTemplateTypeCode(Mockito.any(), Mockito.any())).thenReturn("template-type-code");
        Mockito.when(templateUtil.getSummaryTemplateTypeCode(Mockito.any(), Mockito.any())).thenReturn("template-type-code");
        ResponseWrapper primaryLangResp = new ResponseWrapper<>();
		primaryLangResp.setResponse(Map.of("filtext","Authentication is successful"));
		Mockito.when(proxyMasterdataService
				.getAllTemplateBylangCodeAndTemplateTypeCode(Mockito.anyString(), Mockito.anyString())).thenReturn(primaryLangResp);
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("property");
    }

    @Test
    public void getEventStatusTest() throws ResidentServiceCheckedException {
        ResponseWrapper<EventStatusResponseDTO> resultResponseWrapper =residentService.getEventStatus(eventId, langCode, 0);
        assert resultResponseWrapper.getResponse().getEventId().equals(eventId);
    }

    @Test
    public void getEventStatusTestVid() throws ResidentServiceCheckedException {
        Mockito.when(validator.validateVid(Mockito.anyString())).thenReturn(true);
        ResponseWrapper<EventStatusResponseDTO> resultResponseWrapper =residentService.getEventStatus(eventId, langCode, 0);
        assert resultResponseWrapper.getResponse().getEventId().equals(eventId);
    }

    @Test
    public void getEventStatusTestUIN() throws ResidentServiceCheckedException {
        Mockito.when(validator.validateUin(Mockito.anyString())).thenReturn(true);
        ResponseWrapper<EventStatusResponseDTO> resultResponseWrapper =residentService.getEventStatus(eventId, langCode, 0);
        assert resultResponseWrapper.getResponse().getEventId().equals(eventId);
    }

    @Test
    public void getEventStatusTestEventStatusFailure() throws ResidentServiceCheckedException {
        residentTransactionEntity.get().setStatusCode(EventStatusFailure.AUTHENTICATION_FAILED.name());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        ResponseWrapper<EventStatusResponseDTO> resultResponseWrapper =residentService.getEventStatus(eventId, langCode, 0);
        assert resultResponseWrapper.getResponse().getEventId().equals(eventId);
    }

    @Test
    public void getEventStatusTestEventStatusPending() throws ResidentServiceCheckedException{
        residentTransactionEntity.get().setStatusCode("in-progress");
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        ResponseWrapper<EventStatusResponseDTO> resultResponseWrapper =residentService.getEventStatus(eventId, langCode, 0);
        assert resultResponseWrapper.getResponse().getEventId().equals(eventId);
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void getEventStatusTestException() throws ResidentServiceCheckedException {
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
        residentService.getEventStatus(eventId, langCode, 0);
    }
    
    @Test(expected = EidNotBelongToSessionException.class)
    public void getEventStatusNestedIfTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
    	Mockito.when(identityServiceImpl.getResidentIdaToken()).thenReturn("abcd");
        residentService.getEventStatus(eventId, langCode, 0);
    }
    
    @Test(expected = ResidentServiceCheckedException.class)
    public void getEventStatusThrowsExceptionTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
    	Mockito.when(identityServiceImpl.getResidentIdaToken()).thenThrow(new ApisResourceAccessException());
        residentService.getEventStatus(eventId, langCode, 0);
    }
    
    @Test
    public void getEventStatusServiceTypeNotMappedTest() throws ResidentServiceCheckedException {
        Mockito.when(RequestType.SEND_OTP.getAckTemplateVariables(templateUtil, eventId, "eng", 0)).
                thenReturn(Tuples.of(templateVariables, "acknowledgement-order-a-physical-card"));
    	residentTransactionEntity.get().setRequestTypeCode(RequestType.SEND_OTP.name());
        ResponseWrapper<EventStatusResponseDTO> resultResponseWrapper =residentService.getEventStatus(eventId, langCode, 0);
        assert resultResponseWrapper.getResponse().getEventId().equals(eventId);
    }
}
