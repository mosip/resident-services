package io.mosip.resident.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.AcknowledgementService;
import io.mosip.resident.service.impl.AcknowledgementServiceImpl;
import io.mosip.resident.service.impl.ProxyMasterdataServiceImpl;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static io.mosip.resident.constant.RegistrationConstants.DATETIME_PATTERN;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

/**
 * This class is used to create service class test  for getting acknowledgement API.
 * @Author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class AcknowledgmentServiceTest {

    @InjectMocks
    private AcknowledgementService acknowledgementService = new AcknowledgementServiceImpl();

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private ProxyMasterdataServiceImpl proxyMasterdataServiceImpl;

    @Mock
    private TemplateUtil templateUtil;

    @InjectMocks
    private TemplateManagerBuilderImpl templateManagerBuilder;

    @Mock
    private PDFGenerator pdfGenerator;

    @Mock
    private Environment environment;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    private byte[] result;
    private String eventId;
    private String languageCode;
    private Optional<ResidentTransactionEntity> residentTransactionEntity;
    private Map<String, Object> templateResponse;
    private ResponseWrapper responseWrapper;
    private Map<String, String> templateVariables;

    @Mock
    private TemplateManager templateManager;
    private static final String CLASSPATH = "classpath";
    private static final String ENCODE_TYPE = "UTF-8";
    private Map<String, Object> values;

    @Before
    public void setup() throws Exception {
        templateResponse = new LinkedHashMap<>();
        templateVariables = new LinkedHashMap<>();
        values = new LinkedHashMap<>();
        values.put("test", String.class);
        templateVariables.put("eventId", eventId);
        responseWrapper = new ResponseWrapper<>();
        templateResponse.put("fileText", "test");
        responseWrapper.setResponse(templateResponse);
        result = "test".getBytes(StandardCharsets.UTF_8);
        eventId = "bf42d76e-b02e-48c8-a17a-6bb842d85ea9";
        languageCode = "eng";
        residentTransactionEntity = Optional.of(new ResidentTransactionEntity());
        residentTransactionEntity.get().setEventId(eventId);
        residentTransactionEntity.get().setRequestTypeCode(RequestType.SHARE_CRED_WITH_PARTNER.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        Mockito.when(proxyMasterdataServiceImpl.getAllTemplateBylangCodeAndTemplateTypeCode(Mockito.anyString(), Mockito.anyString())).thenReturn(responseWrapper);
        Mockito.when(RequestType.SHARE_CRED_WITH_PARTNER.getAckTemplateVariables(templateUtil, eventId)).thenReturn(templateVariables);
        ReflectionTestUtils.setField(acknowledgementService, "templateManagerBuilder", templateManagerBuilder);
        templateManagerBuilder.encodingType(ENCODE_TYPE).enableCache(false).resourceLoader(CLASSPATH).build();
        InputStream stream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
        Mockito.when(templateManager.merge(any(), Mockito.anyMap())).thenReturn(stream);
        OutputStream outputStream = new ByteArrayOutputStream(1024);
        outputStream.write("test".getBytes(StandardCharsets.UTF_8));
        Mockito.when(pdfGenerator.generate(stream)).thenReturn(outputStream);
        Mockito.when(environment.getProperty(ResidentConstants.LOWER_LEFT_X)).thenReturn("4");
        Mockito.when(environment.getProperty(ResidentConstants.LOWER_LEFT_Y)).thenReturn("4");
        Mockito.when(environment.getProperty(ResidentConstants.UPPER_RIGHT_X)).thenReturn("4");
        Mockito.when(environment.getProperty(ResidentConstants.UPPER_RIGHT_Y)).thenReturn("4");
        Mockito.when(environment.getProperty(ResidentConstants.REASON)).thenReturn("4");
        Mockito.when(environment.getProperty(DATETIME_PATTERN)).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Mockito.when(environment.getProperty(ApiName.PDFSIGN.name())).thenReturn("http://resident/v1");

        ReflectionTestUtils.setField(acknowledgementService, "shareCredWithPartnerTemplate", "acknowledgement-share-cred-with-partner");
        ReflectionTestUtils.setField(acknowledgementService, "manageMyVidTemplate", "manageMyVidTemplate");
        ReflectionTestUtils.setField(acknowledgementService, "orderAPhysicalCard", "orderAPhysicalCard");
        ReflectionTestUtils.setField(acknowledgementService, "updateDemographicData", "updateDemographicData");
        ReflectionTestUtils.setField(acknowledgementService, "verifyEmailIdOrPhoneNumber", "verifyEmailIdOrPhoneNumber");
        ReflectionTestUtils.setField(acknowledgementService, "secureMyId", "secureMyId");
        ReflectionTestUtils.setField(acknowledgementService, "downloadAPersonalizedCard", "downloadAPersonalizedCard");
        SignatureResponseDto signatureResponseDto = new SignatureResponseDto();
        signatureResponseDto.setData("data");
        ResponseWrapper<SignatureResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(signatureResponseDto);
        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("data");
        Mockito.when(objectMapper.readValue(Mockito.anyString(), (Class<Object>) any())).thenReturn(signatureResponseDto);
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(responseWrapper);
    }

    @Test
    public void testAcknowledgementServiceTest() throws ResidentServiceCheckedException, IOException {
          byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
          assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeGenerateVidTest() throws ResidentServiceCheckedException, IOException {
        residentTransactionEntity.get().setRequestTypeCode(RequestType.GENERATE_VID.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeRevokeVidTest() throws ResidentServiceCheckedException, IOException {
        residentTransactionEntity.get().setRequestTypeCode(RequestType.REVOKE_VID.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeOrderPhysicalCardTest() throws ResidentServiceCheckedException, IOException {
        residentTransactionEntity.get().setRequestTypeCode(RequestType.ORDER_PHYSICAL_CARD.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeDownloadPersonalizedCardTest() throws ResidentServiceCheckedException, IOException {
        residentTransactionEntity.get().setRequestTypeCode(RequestType.DOWNLOAD_PERSONALIZED_CARD.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeUpdateMyUinTest() throws ResidentServiceCheckedException, IOException {
        residentTransactionEntity.get().setRequestTypeCode(RequestType.UPDATE_MY_UIN.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeVerifyPhoneOrEmailTest() throws ResidentServiceCheckedException, IOException {
        residentTransactionEntity.get().setRequestTypeCode(RequestType.VERIFY_PHONE_EMAIL.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeSecureMyIdTest() throws ResidentServiceCheckedException, IOException {
        residentTransactionEntity.get().setRequestTypeCode(RequestType.AUTH_TYPE_LOCK_UNLOCK.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
        assertNotNull(actualResult);
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testAcknowledgmentBadEventIdTest() throws ResidentServiceCheckedException, IOException {
        residentTransactionEntity = Optional.empty();
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
        assertNotNull(actualResult);
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testAcknowledgmentBadRequestTypeTest() throws ResidentServiceCheckedException, IOException {
        residentTransactionEntity.get().setRequestTypeCode(RequestType.GET_MY_ID.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
        assertNotNull(actualResult);
    }
}