package io.mosip.resident.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.preregistration.application.dto.TransliterationResponseDTO;
import io.mosip.preregistration.application.service.TransliterationService;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentVidServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;

/**
 * Transliteration Controller Test
 * Note: This class is used to test the Transliteration Controller
 * @author Kamesh Shekhar Prasad
 */

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class TransliterationControllerTest {
    @InjectMocks
    private TransliterationController transliterationController;

    @Mock
    private AuditUtil auditUtil;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private TransliterationService transliterationService;

    @Mock
    private IdentityServiceImpl identityService;

    @Mock
    private ObjectStoreHelper objectStore;

    @Mock
    private ResidentVidServiceImpl residentVidService;

    @MockBean
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @Mock
    private IdAuthService idAuthService;

    private ResponseEntity<MainResponseDTO<TransliterationResponseDTO>> response;

    @Test
    public void testCreateRequestGenerationSuccess() throws Exception {
        MainResponseDTO<TransliterationResponseDTO> response = new MainResponseDTO<>();
        TransliterationResponseDTO dto = new TransliterationResponseDTO();
        response.setResponse(dto);
        MainRequestDTO<TransliterationRequestDTO> requestDTO = new MainRequestDTO<>();
        TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
        transliterationRequestDTO.setFromFieldLang("eng");
        transliterationRequestDTO.setFromFieldValue("cat");
        transliterationRequestDTO.setToFieldLang("hin");
        requestDTO.setRequest(transliterationRequestDTO);
        Mockito.when(transliterationService.translitratorService(Mockito.any())).thenReturn(response);

        ResponseEntity<MainResponseDTO<TransliterationResponseDTO>> responseEntity = transliterationController.translitrator(requestDTO);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test(expected = InvalidInputException.class)
    public void testWithInvalidInputException() throws Exception {
        MainRequestDTO<TransliterationRequestDTO> requestDTO = new MainRequestDTO<>();
        TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
        transliterationRequestDTO.setFromFieldLang("eng");
        transliterationRequestDTO.setFromFieldValue("cat");
        transliterationRequestDTO.setToFieldLang("hin");
        requestDTO.setRequest(transliterationRequestDTO);
        doThrow(InvalidInputException.class).when(requestValidator).validateId(Mockito.any());
        transliterationController.translitrator(requestDTO);
    }
}
