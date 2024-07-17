package io.mosip.resident.controller;

import io.mosip.idrepository.core.util.EnvUtil;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.LocationImmediateChildrenResponseDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.AcknowledgementServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Resident proxy masterdata controller test class.
 *
 * @author Ritik Jain
 */
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(EnvUtil.class)
@ActiveProfiles("test")
public class ProxyMasterDataControllerTest {

    @MockBean
    private ProxyIdRepoService proxyIdRepoService;

    @MockBean
    private ProxyMasterdataService proxyMasterdataService;

    @Mock
    private AuditUtil auditUtil;

    @MockBean
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @MockBean
    private ResidentVidService vidService;

    @MockBean
    private AcknowledgementController acknowledgementController;

    @MockBean
    private AcknowledgementServiceImpl acknowledgementService;

    @MockBean
    private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

    @InjectMocks
    private ProxyMasterdataController proxyMasterdataController;

    @MockBean
    private DocumentService docService;

    @MockBean
    private ObjectStoreHelper objectStore;

    @MockBean
    private ResidentServiceImpl residentService;

    @Autowired
    private MockMvc mockMvc;

    private ResponseWrapper responseWrapper;
    @MockBean
    private Utility utility;

    @MockBean
    private Utilities utilities;

    @Mock
    private IdentityDataUtil identityDataUtil;

    @Mock
    private ProxyMasterDataServiceUtility proxyMasterDataServiceUtility;

    @Mock
    private ValidDocumentByLangCodeCache validDocumentByLangCodeCache;

    @Before
    public void setUp() throws Exception {
        responseWrapper = new ResponseWrapper<>();
        responseWrapper.setVersion("v1");
        responseWrapper.setId("1");
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(proxyMasterdataController).build();
        Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any());
    }

    @Test
    public void testGetValidDocumentByLangCode() throws Exception {
        Mockito.when(validDocumentByLangCodeCache.getValidDocumentByLangCode(Mockito.anyString()))
                .thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/validdocuments/langCode"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetValidDocumentByLangCodeWithResidentServiceCheckedException() throws Exception {
        Mockito.when(validDocumentByLangCodeCache.getValidDocumentByLangCode(Mockito.anyString()))
                .thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/validdocuments/langCode"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetLocationHierarchyLevelByLangCode() throws Exception {
        Mockito.when(proxyMasterdataService.getLocationHierarchyLevelByLangCode(Mockito.anyString()))
                .thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/locationHierarchyLevels/langcode"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetLocationHierarchyLevelByLangCodeWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterdataService.getLocationHierarchyLevelByLangCode(Mockito.anyString()))
                .thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/locationHierarchyLevels/langcode"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetImmediateChildrenByLocCodeAndLangCode() throws Exception {
        Mockito.when(proxyMasterdataService.getImmediateChildrenByLocCodeAndLangCode(Mockito.anyString(),
                Mockito.anyString())).thenReturn(responseWrapper);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/proxy/masterdata/locations/immediatechildren/locationcode/langcode"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetImmediateChildrenByLocCodeAndLangCodeWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterdataService.getImmediateChildrenByLocCodeAndLangCode(Mockito.anyString(),
                Mockito.anyString())).thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/proxy/masterdata/locations/immediatechildren/locationcode/langcode"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetLocationDetailsByLocCodeAndLangCode() throws Exception {
        Mockito.when(
                        proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/locations/info/locationcode/langcode"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetLocationDetailsByLocCodeAndLangCodeWithResidentServiceCheckedException() throws Exception {
        Mockito.when(
                        proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/locations/info/locationcode/langcode"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetCoordinateSpecificRegistrationCenters() throws Exception {
        Mockito.when(proxyMasterdataService.getCoordinateSpecificRegistrationCenters(Mockito.anyString(),
                Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyInt())).thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/proxy/masterdata/getcoordinatespecificregistrationcenters/langcode/33.4/43.5/200"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetCoordinateSpecificRegistrationCentersWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterdataService.getCoordinateSpecificRegistrationCenters(Mockito.anyString(),
                Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyInt())).thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/proxy/masterdata/getcoordinatespecificregistrationcenters/langcode/33.4/43.5/200"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetApplicantValidDocument() throws Exception {
        Mockito.when(proxyMasterdataService.getApplicantValidDocument(Mockito.anyString(), Mockito.anyList()))
                .thenReturn(responseWrapper);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/proxy/masterdata/applicanttype/applicantId/languages?languages=eng"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetApplicantValidDocumentWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterdataService.getApplicantValidDocument(Mockito.anyString(), Mockito.anyList()))
                .thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/proxy/masterdata/applicanttype/applicantId/languages?languages=eng"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetRegistrationCentersByHierarchyLevel() throws Exception {
        Mockito.when(proxyMasterdataService.getRegistrationCentersByHierarchyLevel(Mockito.anyString(),
                Mockito.anyShort(), Mockito.anyList())).thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/registrationcenters/langcode/5/names?name=14110"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetRegistrationCentersByHierarchyLevelWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterdataService.getRegistrationCentersByHierarchyLevel(Mockito.anyString(),
                Mockito.anyShort(), Mockito.anyList())).thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/registrationcenters/langcode/5/names?name=14110"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetRegistrationCenterByHierarchyLevelAndTextPaginated() throws Exception {
        Mockito.when(proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated(Mockito.anyString(),
                Mockito.anyShort(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(),
                Mockito.anyString())).thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get(
                        "/proxy/masterdata/registrationcenters/page/langcode/5/name?pageNumber=0&pageSize=10&orderBy=desc&sortBy=createdDateTime"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetRegistrationCenterByHierarchyLevelAndTextPaginatedWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated(Mockito.anyString(),
                Mockito.anyShort(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(),
                Mockito.anyString())).thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get(
                        "/proxy/masterdata/registrationcenters/page/langcode/5/name?pageNumber=0&pageSize=10&orderBy=desc&sortBy=createdDateTime"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetRegistrationCenterWorkingDays() throws Exception {
        Mockito.when(proxyMasterdataService.getRegistrationCenterWorkingDays(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/workingdays/registrationCenterID/langCode"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetRegistrationCenterWorkingDaysWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterdataService.getRegistrationCenterWorkingDays(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/workingdays/registrationCenterID/langCode"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetLatestIdSchema() throws Exception {
        Mockito.when(
                        proxyMasterdataService.getLatestIdSchema(Mockito.anyDouble(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/idschema/latest?schemaVersion=&domain=&type="))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetLatestIdSchemaWithResidentServiceCheckedException() throws Exception {
        Mockito.when(
                        proxyMasterdataService.getLatestIdSchema(Mockito.anyDouble(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/idschema/latest?schemaVersion=&domain=&type="))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAllTemplateBylangCodeAndTemplateTypeCode() throws Exception {
        Mockito.when(proxyMasterdataService.getAllTemplateBylangCodeAndTemplateTypeCode(Mockito.anyString(),
                Mockito.anyString())).thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/auth-proxy/masterdata/templates/eng/OTP-sms-template"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetAllTemplateBylangCodeAndTemplateTypeCodeWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterdataService.getAllTemplateBylangCodeAndTemplateTypeCode(Mockito.anyString(),
                Mockito.anyString())).thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/auth-proxy/masterdata/templates/eng/OTP-sms-template"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetGenderTypesByLangCode() throws Exception {
        Mockito.when(proxyMasterDataServiceUtility.getDynamicFieldBasedOnLangCodeAndFieldName(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/auth-proxy/masterdata/dynamicfields/gender/eng?withValue=true"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetGenderTypesByLangCodeWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterDataServiceUtility.getDynamicFieldBasedOnLangCodeAndFieldName(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean())).thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/auth-proxy/masterdata/dynamicfields/gender/eng?withValue=true"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetDocumentTypesByDocumentCategoryCodeAndLangCode() throws Exception {
        Mockito.when(proxyMasterdataService.getDocumentTypesByDocumentCategoryAndLangCode(Mockito.anyString(), Mockito.anyString())).thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/documenttypes/documentcategorycode/langcode"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetDocumentTypesByDocumentCategoryCodeAndLangCodeWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterdataService.getDocumentTypesByDocumentCategoryAndLangCode(Mockito.anyString(), Mockito.anyString())).thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/documenttypes/documentcategorycode/langcode"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetGenderCodeByGenderTypeAndLangCode() throws Exception {
        Mockito.when(proxyMasterdataService.getGenderCodeByGenderTypeAndLangCode(Mockito.anyString(), Mockito.anyString())).thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/gendercode/gendertype/langcode"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetGenderCodeByGenderTypeAndLangCodeWithResidentServiceCheckedException() throws Exception {
        Mockito.when(proxyMasterdataService.getGenderCodeByGenderTypeAndLangCode(Mockito.anyString(), Mockito.anyString())).thenThrow(ResidentServiceCheckedException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/gendercode/gendertype/langcode"))
                .andExpect(status().isOk());
    }


    @Test
    public void testGetLocationHierarchyLevel() throws Exception {
        Mockito.when(proxyMasterdataService.getLocationHierarchyLevels(Mockito.anyString()))
                .thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/locationHierarchyLevels"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetLocationHierarchyLevel2() throws Exception {
        Mockito.when(proxyMasterdataService.getLocationHierarchyLevels(null))
                .thenThrow(new ResidentServiceCheckedException());
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/locationHierarchyLevels"))
                .andExpect(status().isOk());
    }

    @Test
    @Ignore
    public void testGetAllDynamicField() throws Exception {
        Mockito.when(proxyMasterdataService.getAllDynamicFieldByName("gender")).thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/dynamicfields/gender"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    @Ignore
    public void testGetAllDynamicFieldFailure() throws Exception {
        Mockito.when(proxyMasterdataService.getAllDynamicFieldByName("gender")).thenThrow(new ResidentServiceCheckedException());
        mockMvc.perform(MockMvcRequestBuilders.get("/proxy/masterdata/dynamicfields/gender"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetImmediateChildrenByLocCode() throws Exception {
        ResponseWrapper<LocationImmediateChildrenResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(new LocationImmediateChildrenResponseDto());
        Mockito.when(proxyMasterdataService.getImmediateChildrenByLocCode(Mockito.anyString(), Mockito.anyList())).thenReturn(responseWrapper.getResponse());
        mockMvc.perform(MockMvcRequestBuilders.get("/auth-proxy/masterdata/locations/immediatechildren/KNT?languageCodes=eng"))
                .andExpect(status().isOk());
    }
}
