package io.mosip.resident.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import io.mosip.resident.util.GetTemplateValueFromTemplateTypeCodeAndLangCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.AcknowledgementService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuples;

/**
 * This class is used to create service class test  for getting acknowledgement API.
 * @Author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class AcknowledgmentServiceTest {

    private static final String LOCALE_EN_US = "en-US";

	@InjectMocks
    private AcknowledgementService acknowledgementService = new AcknowledgementServiceImpl();

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private TemplateUtil templateUtil;

    @Mock
    private GetTemplateValueFromTemplateTypeCodeAndLangCode getTemplateValueFromTemplateTypeCodeAndLangCode;

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

    @Mock
    private Utility utility;

    private byte[] result;
    private String eventId;
    private String languageCode;
    private Optional<ResidentTransactionEntity> residentTransactionEntity;
    private Map<String, String> templateVariables;

    @Mock
    private TemplateManager templateManager;
    private static final String CLASSPATH = "classpath";
    private static final String ENCODE_TYPE = "UTF-8";
    private Map<String, Object> values;

    @Before
    public void setup() throws Exception {
        templateVariables = new LinkedHashMap<>();
        values = new LinkedHashMap<>();
        values.put("test", String.class);
        templateVariables.put(TemplateVariablesConstants.EVENT_ID, eventId);
        templateVariables.put(TemplateVariablesConstants.EVENT_TYPE, RequestType.SHARE_CRED_WITH_PARTNER.name());
        result = "test".getBytes(StandardCharsets.UTF_8);
        eventId = "bf42d76e-b02e-48c8-a17a-6bb842d85ea9";
        languageCode = "eng";
        residentTransactionEntity = Optional.of(new ResidentTransactionEntity());
        residentTransactionEntity.get().setEventId(eventId);
        residentTransactionEntity.get().setRequestTypeCode(RequestType.SHARE_CRED_WITH_PARTNER.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        Mockito.when(RequestType.SHARE_CRED_WITH_PARTNER.getAckTemplateVariables(templateUtil, residentTransactionEntity.get(), languageCode, 0, LOCALE_EN_US)).
                thenReturn(Tuples.of(templateVariables, "acknowledgement-order-a-physical-card"));
        ReflectionTestUtils.setField(acknowledgementService, "templateManagerBuilder", templateManagerBuilder);
        templateManagerBuilder.encodingType(ENCODE_TYPE).enableCache(false).resourceLoader(CLASSPATH).build();
        InputStream stream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
        Mockito.when(templateManager.merge(any(), Mockito.anyMap())).thenReturn(stream);
        OutputStream outputStream = new ByteArrayOutputStream(1024);
        outputStream.write("test".getBytes(StandardCharsets.UTF_8));
        SignatureResponseDto signatureResponseDto = new SignatureResponseDto();
        signatureResponseDto.setData("data");
        ResponseWrapper<SignatureResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(signatureResponseDto);
        Mockito.when(utility.signPdf(Mockito.any(), Mockito.any())).thenReturn("data".getBytes());
        Mockito.when(
				getTemplateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("file text template");
    }

    @Test
    public void testAcknowledgementServiceTest() throws ResidentServiceCheckedException, IOException {
          byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode, 0, LOCALE_EN_US).getT1();
          assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeGenerateVidTest() throws ResidentServiceCheckedException, IOException {
    	templateVariables.put(TemplateVariablesConstants.EVENT_TYPE, RequestType.GENERATE_VID.name());
        Mockito.when(RequestType.GENERATE_VID.getAckTemplateVariables(templateUtil, residentTransactionEntity.get(), languageCode, 0, LOCALE_EN_US)).
                thenReturn(Tuples.of(templateVariables, "acknowledgement-order-a-physical-card"));
        residentTransactionEntity.get().setRequestTypeCode(RequestType.GENERATE_VID.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode, 0, LOCALE_EN_US).getT1();
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeRevokeVidTest() throws ResidentServiceCheckedException, IOException {
    	templateVariables.put(TemplateVariablesConstants.EVENT_TYPE, RequestType.REVOKE_VID.name());
        Mockito.when(RequestType.REVOKE_VID.getAckTemplateVariables(templateUtil, residentTransactionEntity.get(), languageCode, 0, LOCALE_EN_US)).
                thenReturn(Tuples.of(templateVariables, "acknowledgement-order-a-physical-card"));
        residentTransactionEntity.get().setRequestTypeCode(RequestType.REVOKE_VID.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode, 0, LOCALE_EN_US).getT1();
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeOrderPhysicalCardTest() throws ResidentServiceCheckedException, IOException {
    	templateVariables.put(TemplateVariablesConstants.EVENT_TYPE, RequestType.ORDER_PHYSICAL_CARD.name());
        Mockito.when(RequestType.ORDER_PHYSICAL_CARD.getAckTemplateVariables(templateUtil, residentTransactionEntity.get(), languageCode, 0, LOCALE_EN_US)).
                thenReturn(Tuples.of(templateVariables, "acknowledgement-order-a-physical-card"));
        residentTransactionEntity.get().setRequestTypeCode(RequestType.ORDER_PHYSICAL_CARD.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode, 0, LOCALE_EN_US).getT1();
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeDownloadPersonalizedCardTest() throws ResidentServiceCheckedException, IOException {
    	templateVariables.put(TemplateVariablesConstants.EVENT_TYPE, RequestType.DOWNLOAD_PERSONALIZED_CARD.name());
        Mockito.when(RequestType.DOWNLOAD_PERSONALIZED_CARD.getAckTemplateVariables(templateUtil, residentTransactionEntity.get(), languageCode, 0, LOCALE_EN_US)).
                thenReturn(Tuples.of(templateVariables, "acknowledgement-order-a-physical-card"));
        residentTransactionEntity.get().setRequestTypeCode(RequestType.DOWNLOAD_PERSONALIZED_CARD.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode, 0, LOCALE_EN_US).getT1();
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeUpdateMyUinTest() throws ResidentServiceCheckedException, IOException {
    	templateVariables.put(TemplateVariablesConstants.EVENT_TYPE, RequestType.UPDATE_MY_UIN.name());
        Mockito.when(RequestType.UPDATE_MY_UIN.getAckTemplateVariables(templateUtil, residentTransactionEntity.get(), languageCode, 0, LOCALE_EN_US)).
                thenReturn(Tuples.of(templateVariables, "acknowledgement-order-a-physical-card"));
        residentTransactionEntity.get().setRequestTypeCode(RequestType.UPDATE_MY_UIN.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode, 0, LOCALE_EN_US).getT1();
        assertNotNull(actualResult);
    }

    @Test
    public void testAcknowledgementServiceRequestTypeSecureMyIdTest() throws ResidentServiceCheckedException, IOException {
    	templateVariables.put(TemplateVariablesConstants.EVENT_TYPE, RequestType.AUTH_TYPE_LOCK_UNLOCK.name());
        Mockito.when(RequestType.AUTH_TYPE_LOCK_UNLOCK.getAckTemplateVariables(templateUtil, residentTransactionEntity.get(), languageCode, 0, LOCALE_EN_US)).
                thenReturn(Tuples.of(templateVariables, "acknowledgement-order-a-physical-card"));
        residentTransactionEntity.get().setRequestTypeCode(RequestType.AUTH_TYPE_LOCK_UNLOCK.toString());
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] actualResult = acknowledgementService.getAcknowledgementPDF(eventId, languageCode, 0, LOCALE_EN_US).getT1();
        assertNotNull(actualResult);
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testAcknowledgmentBadEventIdTest() throws ResidentServiceCheckedException, IOException {
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
        acknowledgementService.getAcknowledgementPDF(eventId, languageCode, 0, LOCALE_EN_US).getT1();
    }
}