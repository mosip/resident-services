package io.mosip.resident.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;
import io.mosip.resident.dto.RegistrationCenterDto;
import io.mosip.resident.dto.WorkingDaysDto;
import io.mosip.resident.dto.WorkingDaysResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utility;

/**
 * This class is used to create service class test  for download master data service impl.
 * @Author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class DownloadmasterDataServiceImplTest {

    @InjectMocks
    private DownLoadMasterDataServiceImpl downLoadMasterDataService = new DownLoadMasterDataServiceImpl();

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private TemplateUtil templateUtil;

    @Mock
    private ProxyMasterdataService proxyMasterdataService;

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

    private String langCode;
    private Short hierarchyLevel;
    private String name;

    @Before
    public void setup() throws Exception {
        templateVariables = new LinkedHashMap<>();
        values = new LinkedHashMap<>();
        values.put("test", String.class);
        templateVariables.put("eventId", eventId);
        result = "test".getBytes(StandardCharsets.UTF_8);
        eventId = "bf42d76e-b02e-48c8-a17a-6bb842d85ea9";
        languageCode = "eng";

		Mockito.when(
				templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("file text template");
        ReflectionTestUtils.setField(downLoadMasterDataService, "templateManagerBuilder", templateManagerBuilder);
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
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("supporting-docs-list");
        langCode="eng";
        hierarchyLevel=4;
        name = "name1";
    }

    @Test
    public void testDownloadRegistrationCentersByHierarchyLevel() throws Exception {
    	ReflectionTestUtils.setField(downLoadMasterDataService, "maxRegistrationCenterPageSize", 10);
          byte[] actualResult = downLoadMasterDataService.downloadRegistrationCentersByHierarchyLevel(langCode, hierarchyLevel, name).readAllBytes();
          assertNotNull(actualResult);
    }

    @Test
    public void testGetNearestRegistrationcenters() throws Exception {
        byte[] actualResult = downLoadMasterDataService.getNearestRegistrationcenters(langCode, 4L, 4L,3).readAllBytes();
        assertNotNull(actualResult);
    }

    @Test
    public void testDownloadSupportingDocsByLanguage() throws Exception {
        byte[] actualResult = downLoadMasterDataService.downloadSupportingDocsByLanguage(langCode).readAllBytes();
        assertNotNull(actualResult);
    }

    @Test
    public void testgetTime() throws Exception {
        RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
        registrationCenterDto.setCenterTypeCode("Ind");
        WorkingDaysResponseDto workingDaysResponseDto = new WorkingDaysResponseDto();
        WorkingDaysDto workingDaysDto = new WorkingDaysDto();
        workingDaysDto.setCode("123");
        workingDaysResponseDto.setWorkingdays(List.of(workingDaysDto));
        ResponseWrapper responseWrapper1 = new ResponseWrapper<>();
        responseWrapper1.setResponse(workingDaysResponseDto);
        ReflectionTestUtils.invokeMethod(downLoadMasterDataService, "getTime",
                String.valueOf(LocalTime.of(12,2,2)));

    }

    @Test
    public void testgetTimeFailed() throws Exception {
        RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
        registrationCenterDto.setCenterTypeCode("Ind");
        WorkingDaysResponseDto workingDaysResponseDto = new WorkingDaysResponseDto();
        WorkingDaysDto workingDaysDto = new WorkingDaysDto();
        workingDaysDto.setCode("123");
        workingDaysResponseDto.setWorkingdays(List.of(workingDaysDto));
        ResponseWrapper responseWrapper1 = new ResponseWrapper<>();
        responseWrapper1.setResponse(workingDaysResponseDto);
        ReflectionTestUtils.invokeMethod(downLoadMasterDataService, "getTime", "123");

    }
}