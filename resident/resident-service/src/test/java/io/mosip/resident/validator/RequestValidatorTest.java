package io.mosip.resident.validator;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.impl.RemainingUpdateCountByIndividualId;
import io.mosip.resident.util.*;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestIdType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.AidStatusRequestDTO;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDtoV2;
import io.mosip.resident.dto.AuthTypeStatusDtoV2;
import io.mosip.resident.dto.AuthUnLockRequestDTO;
import io.mosip.resident.dto.BaseVidRevokeRequestDTO;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.GrievanceRequestDTO;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.dto.OtpRequestDTOV3;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResidentVidRequestDto;
import io.mosip.resident.dto.ResidentVidRequestDtoV2;
import io.mosip.resident.dto.SharableAttributesDTO;
import io.mosip.resident.dto.ShareCredentialRequestDto;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidRequestDtoV2;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeRequestDTOV2;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.BaseResidentUncheckedExceptionWithMetadata;
import io.mosip.resident.exception.EidNotBelongToSessionException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.ProxyPartnerManagementService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentConfigServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;

@RunWith(SpringRunner.class)
public class RequestValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private UinValidator<String> uinValidator;

    @Mock
    private EmailPhoneValidator emailPhoneValidator;

    @Mock
    private VidValidator<String> vidValidator;

    @Mock
    private RidValidator<String> ridValidator;

    @InjectMocks
    private RequestValidator requestValidator;

    @Mock
    private AuditUtil audit;

    @Mock
    private Utilities utilities;

    @Mock
    private Environment environment;

    @Mock
    private ResidentConfigServiceImpl residentConfigService;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private ProxyMasterdataService proxyMasterdataService;

    @Mock
    private IdentityServiceImpl identityService;

    @Mock
    private ProxyPartnerManagementService proxyPartnerManagementService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProxyIdRepoService idRepoService;

    @Mock
    private ValidateNewUpdateRequest validateNewUpdateRequest;

    @Mock
    private RemainingUpdateCountByIndividualId remainingUpdateCountByIndividualId;

    String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @InjectMocks
    private ResidentService residentService = new ResidentServiceImpl();

    private Object schema;
    private String schemaJson;

    private JSONObject idRepoJson;
    private JSONObject identity;

    @Mock
    private UinVidValidator uinVidValidator;

    @Mock
    private AvailableClaimUtility availableClaimUtility;

    @Mock
    private IdentityUtil identityUtil;

    @Mock
    private ValidateOtpCharLimit validateOtpCharLimit;

    @InjectMocks
    private ValidateSameData validateSameData;

    @Before
    public void setup() throws ParseException {
        schema = "{\\\"$schema\\\":\\\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\\\",\\\"description\\\":\\\"MOSIP Sample identity\\\",\\\"additionalProperties\\\":false,\\\"title\\\":\\\"MOSIP identity\\\",\\\"type\\\":\\\"object\\\",\\\"definitions\\\":{\\\"simpleType\\\":{\\\"uniqueItems\\\":true,\\\"additionalItems\\\":false,\\\"type\\\":\\\"array\\\",\\\"items\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"language\\\",\\\"value\\\"],\\\"properties\\\":{\\\"language\\\":{\\\"type\\\":\\\"string\\\"},\\\"value\\\":{\\\"type\\\":\\\"string\\\"}}}},\\\"documentType\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"format\\\":{\\\"type\\\":\\\"string\\\"},\\\"type\\\":{\\\"type\\\":\\\"string\\\"},\\\"value\\\":{\\\"type\\\":\\\"string\\\"},\\\"refNumber\\\":{\\\"type\\\":[\\\"string\\\",\\\"null\\\"]}}},\\\"biometricsType\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"format\\\":{\\\"type\\\":\\\"string\\\"},\\\"version\\\":{\\\"type\\\":\\\"number\\\",\\\"minimum\\\":0},\\\"value\\\":{\\\"type\\\":\\\"string\\\"}}}},\\\"properties\\\":{\\\"identity\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"IDSchemaVersion\\\",\\\"fullName\\\",\\\"dateOfBirth\\\",\\\"gender\\\",\\\"addressLine1\\\",\\\"addressLine2\\\",\\\"addressLine3\\\",\\\"region\\\",\\\"province\\\",\\\"city\\\",\\\"zone\\\",\\\"postalCode\\\",\\\"phone\\\",\\\"email\\\",\\\"proofOfIdentity\\\",\\\"individualBiometrics\\\"],\\\"properties\\\":{\\\"proofOfAddress\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"gender\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"city\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"postalCode\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[(?i)A-Z0-9]{5}$|^NA$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"proofOfException-1\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"referenceIdentityNumber\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^([0-9]{10,30})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"kyc\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"individualBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"province\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"zone\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"proofOfDateOfBirth\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"addressLine1\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"addressLine2\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"residenceStatus\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"kyc\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"addressLine3\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"email\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[A-Za-z0-9_\\\\\\\\-]+(\\\\\\\\.[A-Za-z0-9_]+)*@[A-Za-z0-9_-]+(\\\\\\\\.[A-Za-z0-9_]+)*(\\\\\\\\.[a-zA-Z]{2,})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerRID\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"fullName\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"dateOfBirth\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])\\/([0][1-9]|1[0-2])\\/([0][1-9]|[1-2][0-9]|3[01])$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"individualAuthBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"introducerUIN\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"proofOfIdentity\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"IDSchemaVersion\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"none\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"number\\\",\\\"fieldType\\\":\\\"default\\\",\\\"minimum\\\":0},\\\"proofOfException\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"phone\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[+]*([0-9]{1})([0-9]{9})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerName\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"proofOfRelationship\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"UIN\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"none\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"region\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"}}}}}";
        schemaJson = (String) schema;
        Map<RequestIdType, String> map = new HashMap<RequestIdType, String>();
        map.put(RequestIdType.RE_PRINT_ID, "mosip.resident.print");
        map.put(RequestIdType.AUTH_LOCK_ID, "mosip.resident.authlock");
        map.put(RequestIdType.AUTH_UNLOCK_ID, "mosip.resident.authunlock");
        map.put(RequestIdType.E_UIN_ID, "mosip.resident.euin");
        map.put(RequestIdType.AUTH_HISTORY_ID, "mosip.resident.authhistory");
        map.put(RequestIdType.RES_UPDATE, "mosip.resident.updateuin");
        map.put(RequestIdType.CHECK_STATUS, "mosip.resident.checkstatus");
        map.put(RequestIdType.AUTH_LOCK_UNLOCK, "mosip.resident.auth.lock.unlock");
        map.put(RequestIdType.SHARE_CREDENTIAL, "mosip.resident.share.credential");
        ReflectionTestUtils.setField(requestValidator, "checkStatusID", "mosip.resident.checkstatus");
        ReflectionTestUtils.setField(requestValidator, "authLockId", "mosip.resident.authlock");
        ReflectionTestUtils.setField(requestValidator, "euinId", "mosip.resident.euin");
        ReflectionTestUtils.setField(requestValidator, "authHstoryId", "mosip.resident.authhistory");
        ReflectionTestUtils.setField(requestValidator, "uinUpdateId", "mosip.resident.updateuin");
        ReflectionTestUtils.setField(requestValidator, "allowedAuthTypes", "bio-FIR,bio-IIR");
        ReflectionTestUtils.setField(requestValidator, "version", "v1");
        ReflectionTestUtils.setField(requestValidator, "map", map);
        ReflectionTestUtils.setField(requestValidator, "allowedAuthTypes", "otp,bio-FIR,bio-IIR,bio-FACE");
        ReflectionTestUtils.setField(residentService, "authTypes", "otp,bio-FIR,bio-IIR,bio-FACE");
        ReflectionTestUtils.setField(requestValidator, "mandatoryLanguages", "eng");
        ReflectionTestUtils.setField(requestValidator, "optionalLanguages", "ara");
        ReflectionTestUtils.setField(requestValidator, "reqResVersion", "1.0");
        ReflectionTestUtils.setField(requestValidator, "newVersion", "1.0");
        ReflectionTestUtils.setField(requestValidator, "vidLength", 16);
        ReflectionTestUtils.setField(requestValidator, "otpLength", 6);
        ReflectionTestUtils.setField(requestValidator, "searchTextLength", 16);
        ReflectionTestUtils.setField(requestValidator, "emailCharsLimit", 128);
        ReflectionTestUtils.setField(requestValidator, "phoneCharsLimit", 64);
        ReflectionTestUtils.setField(requestValidator, "messageCharsLimit", 1024);
        ReflectionTestUtils.setField(requestValidator, "purposeCharsLimit", 1024);
        ReflectionTestUtils.setField(requestValidator, "messageAllowedSpecialCharRegex", "[A-Za-z0-9 .,-]+");
        ReflectionTestUtils.setField(requestValidator, "purposeAllowedSpecialCharRegex", "[A-Za-z0-9 .,-]+");
        ReflectionTestUtils.setField(requestValidator, "idAllowedSpecialCharRegex", "[A-Za-z0-9 -]+");
        ReflectionTestUtils.setField(requestValidator, "numericDataRegex", "^[0-9]+$");
        ReflectionTestUtils.setField(requestValidator, "transactionIdRegex", "^[0-9]{10}$");
        ReflectionTestUtils.setField(requestValidator, "eventIdRegex", "^[0-9]{16}$");
        String[] valuesOfExcludedAttributeList = {"UIN", "IDSchemaVersion"};
        ReflectionTestUtils.setField(validateSameData,  "valuesOfExcludedAttributeList", valuesOfExcludedAttributeList);
        Mockito.when(uinValidator.validateId(Mockito.anyString())).thenReturn(true);
        Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
        Mockito.when(ridValidator.validateId(Mockito.anyString())).thenReturn(true);
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("property");
        JSONParser parser = new JSONParser();
        idRepoJson = (JSONObject) parser.parse("{\"gender\":[{\"language\":\"eng\",\"value\":\"Female\"},{\"language\":\"ara\",\"value\":\"أنثى\"},{\"language\":\"fra\",\"value\":\"Femelle\"},{\"language\":\"tam\",\"value\":\"பெண்\"},{\"language\":\"hin\",\"value\":\"महिला\"},{\"language\":\"kan\",\"value\":\"ಹೆಣ್ಣು\"}],\"city\":[{\"language\":\"eng\",\"value\":\"TEST_CITYeng\"},{\"language\":\"ara\",\"value\":\"TEST_CITYara\"},{\"language\":\"fra\",\"value\":\"TEST_CITYfra\"},{\"language\":\"tam\",\"value\":\"TEST_CITYtam\"},{\"language\":\"hin\",\"value\":\"TEST_CITYhin\"},{\"language\":\"kan\",\"value\":\"TEST_CITYkan\"}],\"postalCode\":\"14022\"}");
        identity = (JSONObject) parser.parse("{\"email\":\"kam@g1.com\",\"gender\":[{\"language\":\"eng\",\"value\":\"Female\"},{\"language\":\"ara\",\"value\":\"أنثى\"},{\"language\":\"fra\",\"value\":\"Femelle\"},{\"language\":\"tam\",\"value\":\"பெண்\"},{\"language\":\"hin\",\"value\":\"महिला\"},{\"language\":\"kan\",\"value\":\"ಹೆಣ್ಣು\"}]}");

    }

    @Test(expected = InvalidInputException.class)
    public void testValidId() throws Exception {
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authLockRequestDto);
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidUnlockId() throws Exception {
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authLockRequestDto);
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.UNLOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValideuinId() throws Exception {
        EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
        RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(euinRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
        requestValidator.validateEuinRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidAuthHistoryId() throws Exception {
        AuthHistoryRequestDTO authHistoryRequestDTO = new AuthHistoryRequestDTO();
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authHistoryRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authlock");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidVersion() throws Exception {
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setRequest(authLockRequestDto);
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidAuthHistoryVersion() throws Exception {
        AuthHistoryRequestDTO authHistoryRequestDTO = new AuthHistoryRequestDTO();
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authHistoryRequestDTO);
        requestWrapper.setVersion("v2");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testValideuinVersion() throws Exception {
        EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
        RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(euinRequestDTO);
        requestWrapper.setVersion("v2");
        requestWrapper.setId("mosip.resident.euin");
        requestValidator.validateEuinRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidRequest() throws Exception {

        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(null);
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidAuthHistoryRequest() throws Exception {

        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestWrapper.setRequest(null);
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testValideuinRequest() throws Exception {

        RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.euin");
        requestWrapper.setRequest(null);
        requestValidator.validateEuinRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidIndividualType() throws Exception {
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        authLockRequestDto.setTransactionID("12345");
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authLockRequestDto);
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidIndividualId() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.any())).thenReturn(false);
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        authLockRequestDto.setTransactionID("12345");
        authLockRequestDto.setIndividualId("12345");
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authLockRequestDto);
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test
    public void testValidateAuthLockOrUnlockRequestSuccess() throws Exception {
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        authLockRequestDto.setTransactionID("12345");
        authLockRequestDto.setIndividualId("12345");
        authLockRequestDto.setOtp("12345");
        List<String> authTypes = new ArrayList<>();
        authTypes.add("bio-FIR");
        authLockRequestDto.setAuthType(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authLockRequestDto);
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);
    }

    @Test
    public void testValidateAuthLockOrUnlockRequestSuccessForUnlock() throws Exception {
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        authLockRequestDto.setTransactionID("12345");
        authLockRequestDto.setIndividualId("12345");
        authLockRequestDto.setOtp("12345");
        List<String> authTypes = new ArrayList<>();
        authTypes.add("bio-FIR");
        authLockRequestDto.setAuthType(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authunlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authLockRequestDto);
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.UNLOCK);
    }

    @Test(expected = InvalidInputException.class)
    public void testeuinValidIndividualType() throws Exception {
        EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
        euinRequestDTO.setIndividualIdType(IdType.RID.name());
        RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(euinRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.euin");
        requestValidator.validateEuinRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidIndividualType() throws Exception {
        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidOtp() throws Exception {
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        authLockRequestDto.setTransactionID("12345");
        authLockRequestDto.setIndividualId("12344567");
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authLockRequestDto);
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidAuthTypes() throws Exception {
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        authLockRequestDto.setTransactionID("12345");
        authLockRequestDto.setOtp("1232354");
        authLockRequestDto.setIndividualId("12344567");
        List<String> authTypes = new ArrayList<String>();
        authTypes.add("bio-FMR");
        authLockRequestDto.setAuthType(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authLockRequestDto);
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidEmptyAuthTypes() throws Exception {
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        authLockRequestDto.setTransactionID("12345");
        authLockRequestDto.setOtp("1232354");
        authLockRequestDto.setIndividualId("12344567");
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authLockRequestDto);
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidPageFetch() throws Exception {
        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("123");
        authRequestDTO.setPageStart("1");
        authRequestDTO.setOtp("12345");
        authRequestDTO.setTransactionID("12345");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidPageStart() throws Exception {
        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("123");
        authRequestDTO.setPageFetch("1");
        authRequestDTO.setOtp("12345");
        authRequestDTO.setTransactionID("12345");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidIndividualId() throws Exception {
        Mockito.when(uinValidator.validateId(Mockito.anyString())).thenReturn(false);
        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("123");
        authRequestDTO.setPageFetch("1");
        authRequestDTO.setOtp("12345");
        authRequestDTO.setTransactionID("12345");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidpageFetch() throws Exception {

        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("123");
        authRequestDTO.setPageFetch("1Q");
        authRequestDTO.setPageStart("1");
        authRequestDTO.setOtp("12345");
        authRequestDTO.setTransactionID("12345");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidpageStart() throws Exception {

        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("123");
        authRequestDTO.setPageFetch("1");
        authRequestDTO.setPageStart("1Q");
        authRequestDTO.setOtp("12345");
        authRequestDTO.setTransactionID("12345");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidpageStartPageFetch() throws Exception {

        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("123");
        authRequestDTO.setPageFetch(" ");
        authRequestDTO.setOtp("12345");
        authRequestDTO.setTransactionID("12345");
        authRequestDTO.setPageStart(" ");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidPageStartnullPageFetch() throws Exception {

        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("123");
        authRequestDTO.setOtp("12345");
        authRequestDTO.setTransactionID("12345");
        authRequestDTO.setPageStart(" ");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidnullPageStartPageFetch() throws Exception {

        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("123");
        authRequestDTO.setPageFetch(" ");
        authRequestDTO.setOtp("12345");
        authRequestDTO.setTransactionID("12345");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryinValidpageStartPageFetch() throws Exception {

        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("123");
        authRequestDTO.setPageFetch("-10");
        authRequestDTO.setPageStart("-11");
        authRequestDTO.setOtp("12345");
        authRequestDTO.setTransactionID("12345");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testeuinValidIndividualId() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(false);
        EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
        euinRequestDTO.setIndividualIdType(IdType.VID.name());
        RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(euinRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.euin");
        requestValidator.validateEuinRequest(requestWrapper);

    }

    @Test
    public void testValidateRequest() {
        ResidentReprintRequestDto request = new ResidentReprintRequestDto();
        request.setIndividualId("3542102");
        request.setIndividualIdType(IdType.UIN.name());
        request.setOtp("1234");
        request.setTransactionID("9876543210");
        RequestWrapper<ResidentReprintRequestDto> reqWrapper = new RequestWrapper<>();
        reqWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        reqWrapper.setRequest(request);
        reqWrapper.setId("mosip.resident.print");
        reqWrapper.setVersion("v1");
        requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);

    }

    @Test(expected = InvalidInputException.class)
    public void testvalidateRequestInValidId() {

        RequestWrapper<ResidentReprintRequestDto> reqWrapper = new RequestWrapper<>();
        reqWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));

        requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);

        reqWrapper.setId("mosip.resident.print1");

        requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);

        reqWrapper.setVersion("v1");

        requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);

        reqWrapper.setId("mosip.resident.print");
        reqWrapper.setVersion("v2");
        requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);

    }

    @Test
    public void testInvalidDateTime() {
        boolean result = false;
        ResidentReprintRequestDto request = new ResidentReprintRequestDto();
        RequestWrapper<ResidentReprintRequestDto> reqWrapper = new RequestWrapper<>();
        reqWrapper.setRequest(request);
        reqWrapper.setId("mosip.resident.print");
        reqWrapper.setVersion("v1");
        try {
            requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);
        } catch (InvalidInputException e) {
            assertTrue(e.getMessage().contains("Invalid Input Parameter- requesttime"));
            result = true;
        }
        if (!result)
            fail();
    }

    @Test(expected = InvalidInputException.class)
    public void testeuinValidCardType() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
        EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
        euinRequestDTO.setIndividualIdType(IdType.VID.name());
        euinRequestDTO.setIndividualId("1234567");
        euinRequestDTO.setCardType("vid");
        RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(euinRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.euin");
        requestValidator.validateEuinRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testeuinValidOtp() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
        EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
        euinRequestDTO.setIndividualIdType(IdType.VID.name());
        euinRequestDTO.setIndividualId("1234567");
        euinRequestDTO.setCardType(CardType.MASKED_UIN.name());
        RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(euinRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.euin");
        requestValidator.validateEuinRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testeuinValidTransactionId() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
        EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
        euinRequestDTO.setIndividualIdType(IdType.VID.name());
        euinRequestDTO.setIndividualId("1234567");
        euinRequestDTO.setCardType(CardType.MASKED_UIN.name());
        euinRequestDTO.setOtp("12345");
        RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(euinRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.euin");
        requestValidator.validateEuinRequest(requestWrapper);

    }

    @Test
    public void testValidateEuinRequestSuccess() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
        euinRequestDTO.setIndividualIdType(IdType.VID.name());
        euinRequestDTO.setIndividualId("1234567");
        euinRequestDTO.setCardType(CardType.MASKED_UIN.name());
        euinRequestDTO.setOtp("12345");
        euinRequestDTO.setTransactionID("9876543210");
        RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(euinRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.euin");
        requestValidator.validateEuinRequest(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidTransactionId() throws Exception {
        AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
        authLockRequestDto.setIndividualId("12344567");
        authLockRequestDto.setOtp("12345");
        RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authLockRequestDto);
        requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidGetStatusId() throws Exception {
        RequestDTO requestDTO = new RequestDTO();
        RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testGetStatusValidIndividualType() throws Exception {
        RequestDTO requestDTO = new RequestDTO();
        RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();

        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.checkstatus");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRidCheckStatusRequestDTONullRequest() throws Exception {
        RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.checkstatus");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(null);
        requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRidCheckStatusRequestDTOInvalidIdType() throws Exception {
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setIndividualIdType("Invalid");
        RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.checkstatus");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRidCheckStatusRequestDTONullIndividualId() throws Exception {
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setIndividualIdType(IdType.RID.name());
        RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.checkstatus");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);
    }

    @Test
    public void testValidateRidCheckStatusRequestDTOSuccess() throws Exception {
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setIndividualIdType(IdType.RID.name());
        requestDTO.setIndividualId("1234567");
        RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.checkstatus");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testGetStatusValidIndividualId() throws Exception {
        Mockito.when(ridValidator.validateId(Mockito.anyString())).thenReturn(false);
        RequestDTO requestDTO = new RequestDTO();
        RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType("INVALID_RID");
        requestDTO.setIndividualId("123456");
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.checkstatus");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidUpdateId() throws Exception {
        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);

    }

    @Test(expected = InvalidInputException.class)
    public void testUpdateValidIndividualType() throws Exception {
        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();

        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);

    }

    @Test(expected = InvalidInputException.class)
    public void testUpdateValidIndividualId() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(false);
        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestDTO.setIndividualId("1234567");
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);

    }

    @Test(expected = InvalidInputException.class)
    public void testUpdateValidTransaction() throws Exception {

        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);

    }

    @Test(expected = InvalidInputException.class)
    public void testUpdateValidOtp() throws Exception {

        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestDTO.setIndividualId("1234567");
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);

    }

    @Test(expected = InvalidInputException.class)
    public void testUpdateValidIdentityJson() throws Exception {

        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestDTO.setTransactionID("1234567");
        requestDTO.setIdentityJson("");
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidReprintId() throws Exception {
        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidUpdateRequestNullRequest() throws Exception {
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("v1");
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidUpdateRequestIndividualIdNull() throws Exception {
        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        requestDTO.setIndividualIdType(IdType.VID.name());
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);
    }

    @Test
    public void testValidateUpdateRequest() throws Exception {
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestDTO.setTransactionID("1234567");
        requestDTO.setIdentityJson("abcdef");
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);
    }

    @Test
    public void testValidateUpdateRequestV2() throws Exception {
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        requestDTO.setIndividualIdType(IdType.UIN.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestDTO.setTransactionID("1234567");
        requestDTO.setIdentityJson("abcdef");
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, false, schemaJson);
    }

    @Test(expected = InvalidInputException.class)
    public void testReprintValidIndividualType() throws Exception {
        ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
        RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();

        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.print");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateReprintRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testReprintValidIndividualId() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(false);
        ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
        RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestDTO.setIndividualId("1234567");
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.print");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateReprintRequest(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testReprintNullIndividualId() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
        ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
        RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.print");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateReprintRequest(requestWrapper);
    }

    @Test
    public void testReprintSuccess() throws Exception {
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
        ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
        RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestDTO.setIndividualId("12345678");
        requestDTO.setCardType(CardType.MASKED_UIN.name());
        requestDTO.setOtp("111111");
        requestDTO.setTransactionID("11111");
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.print");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateReprintRequest(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testReprintValidTransaction() throws Exception {

        ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
        RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestDTO.setCardType(CardType.MASKED_UIN.name());
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.print");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateReprintRequest(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testReprintValidOtp() throws Exception {

        ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
        RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setCardType(CardType.MASKED_UIN.name());
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.print");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateReprintRequest(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testReprintValidCardType() throws Exception {

        ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
        RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
        requestDTO.setIndividualIdType(IdType.VID.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setCardType(IdType.VID.name());
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.print");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateReprintRequest(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateReprintRequestNullRequest() throws Exception {

        RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequest(null);
        requestValidator.validateReprintRequest(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidOtp() throws Exception {
        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("1234567");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test(expected = InvalidInputException.class)
    public void testAuthHistoryValidTransactionId() throws Exception {
        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("1234567");
        authRequestDTO.setOtp("1245");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);

    }

    @Test
    public void testValidateAuthHistoryRequestSuccess() throws Exception {
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
        authRequestDTO.setIndividualId("1234567");
        authRequestDTO.setOtp("1245");
        authRequestDTO.setTransactionID("1234567");
        RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setRequest(authRequestDTO);
        requestWrapper.setVersion("v1");
        requestWrapper.setId("mosip.resident.authhistory");
        requestValidator.validateAuthHistoryRequest(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testAuthUnlockRequestNull() throws Exception {
        RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authunlock");
        requestWrapper.setVersion("v1");
        requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidIndividualIdForAuthUnlockRequest() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.any())).thenReturn(false);
        AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
        authUnLockRequestDto.setTransactionID("12345");
        authUnLockRequestDto.setIndividualId("12345");
        RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authunlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authUnLockRequestDto);
        requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthUnlockRequestUnlockForSecondsNull() throws Exception {
        AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
        authUnLockRequestDto.setTransactionID("12345");
        authUnLockRequestDto.setIndividualId("12345");
        authUnLockRequestDto.setOtp("12345");
        authUnLockRequestDto.setUnlockForSeconds(null);
        List<String> authTypeList = new ArrayList<>();
        authTypeList.add("bio-FIR");
        authUnLockRequestDto.setAuthType(authTypeList);
        RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authunlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authUnLockRequestDto);
        requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);
    }

    @Test
    public void testValidateAuthUnlockRequestSuccess() throws Exception {
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
        authUnLockRequestDto.setTransactionID("12345");
        authUnLockRequestDto.setIndividualId("12345");
        authUnLockRequestDto.setOtp("12345");
        authUnLockRequestDto.setUnlockForSeconds("10");
        List<String> authTypeList = new ArrayList<>();
        authTypeList.add("bio-FIR");
        authUnLockRequestDto.setAuthType(authTypeList);
        RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authunlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authUnLockRequestDto);
        requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthUnlockRequestNullIndividualId() throws Exception {
        AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
        authUnLockRequestDto.setTransactionID("12345");
        RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authunlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authUnLockRequestDto);
        requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidOtpForAuthUnlockRequest() throws Exception {
        AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
        authUnLockRequestDto.setTransactionID("12345");
        authUnLockRequestDto.setIndividualId("12344567");
        RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authunlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authUnLockRequestDto);
        requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }

    @Test(expected = InvalidInputException.class)
    public void testValidTransactionIdForAuthUnlockRequest() throws Exception {
        AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
        authUnLockRequestDto.setIndividualId("12344567");
        authUnLockRequestDto.setOtp("12345");
        RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authunlock");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(authUnLockRequestDto);
        requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

    }


    @Test(expected = InvalidInputException.class)
    public void testValidunlockForMinutesLessThanZeroForAuthUnlockRequest() throws Exception {
        AuthUnLockRequestDTO authUnLockRequestDto1 = new AuthUnLockRequestDTO();
        authUnLockRequestDto1.setIndividualId("12344567");
        authUnLockRequestDto1.setOtp("12345");
        authUnLockRequestDto1.setTransactionID("12345");
        authUnLockRequestDto1.setUnlockForSeconds(String.valueOf(-1L));
        List<String> authTypes = new ArrayList<String>();
        authTypes.add("bio-FIR");
        authUnLockRequestDto1.setAuthType(authTypes);
        RequestWrapper<AuthUnLockRequestDTO> requestWrapper1 = new RequestWrapper<>();
        requestWrapper1.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper1.setId("mosip.resident.authunlock");
        requestWrapper1.setVersion("v1");
        requestWrapper1.setRequest(authUnLockRequestDto1);
        requestValidator.validateAuthUnlockRequest(requestWrapper1, AuthTypeStatus.LOCK);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2BadRequest() throws Exception {
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(null);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2InvalidIndividualIdBadIdType() throws Exception {
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2InvalidAuthType() throws Exception {
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        authLockOrUnLockRequestDtoV2.setAuthTypes(null);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2BadAuthType() throws Exception {
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("dummy");
        authTypeStatusDto.setLocked(true);
        authTypeStatusDto.setUnlockForSeconds(10L);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test
    public void testValidateAuthLockOrUnlockRequestV2() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.auth.lock.unlock");
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("bio-FIR");
        authTypeStatusDto.setLocked(true);
        //authTypeStatusDto.setUnlockForSeconds(10L);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.auth.lock.unlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2InvalidAuthTypeV2() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.auth.lock.unlock");
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.auth.lock.unlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2EmptyAuthType() throws Exception {
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthTypeUnlockRequestV2NegativeUnlockForSeconds() throws Exception {
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("bio-FIR");
        authTypeStatusDto.setLocked(false);
        authTypeStatusDto.setUnlockForSeconds(-1L);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test
    public void testValidateAuthLockOrUnlockRequestV2NullUnlockForSeconds() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.auth.lock.unlock");
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("bio-FIR");
        authTypeStatusDto.setLocked(true);
        authTypeStatusDto.setUnlockForSeconds(null);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.auth.lock.unlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
        //Should not throw exception
    }

    @Test
    public void testValidateAuthLockRequestV2NegativeUnlockForSeconds() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.auth.lock.unlock");
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("bio-FIR");
        authTypeStatusDto.setLocked(true);
        authTypeStatusDto.setUnlockForSeconds(1L);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.auth.lock.unlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        try {
            requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
        } catch (BaseResidentUncheckedExceptionWithMetadata e) {
            assertEquals(e.getErrorCode(),ResidentErrorCode.UNSUPPORTED_INPUT.getErrorCode());
            return;
        }
        fail();
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2EmptyAuthTypeV2() throws Exception {
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2BadUnlockForSeconds2() throws Exception {
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("bio-FIR");
        authTypeStatusDto.setLocked(true);
        authTypeStatusDto.setUnlockForSeconds(-10L);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }


    @Test(expected = InvalidInputException.class)
    public void testValidateAidStatusRequestDto() throws Exception {
        RequestWrapper<AidStatusRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.checkstatus");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(null);
        requestValidator.validateAidStatusRequestDto(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAidStatusRequestDtoAidNull() throws Exception {
        AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
        aidStatusRequestDTO.setIndividualId(null);
        RequestWrapper<AidStatusRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.checkstatus");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(aidStatusRequestDTO);
        requestValidator.validateAidStatusRequestDto(requestWrapper);
    }

    @Test
    public void testValidateAidStatusRequestDtoSuccess() throws Exception {
        AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
        aidStatusRequestDTO.setIndividualId("17");
        aidStatusRequestDTO.setTransactionId("1234567890");
        aidStatusRequestDTO.setOtp("111111");
        RequestWrapper<AidStatusRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.checkstatus");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(aidStatusRequestDTO);
        requestValidator.validateAidStatusRequestDto(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateChannelVerificationStatus() throws Exception {
        String channel = "";
        String individualId = "";
        requestValidator.validateChannelVerificationStatus(channel, individualId);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateChannelVerificationStatusNullChannel() throws Exception {
        String channel = "BadChannel";
        String individualId = "";
        requestValidator.validateChannelVerificationStatus(channel, individualId);
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateChannelVerificationStatusNullIndividualId() throws Exception {
        String channel = "PHONE";
        requestValidator.validateChannelVerificationStatus(channel, null);
    }

    @Test
    public void testValidateChannelVerificationStatusSuccess() throws Exception {
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        String channel = "PHONE";
        requestValidator.validateChannelVerificationStatus(channel, "12345678");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateServiceHistoryRequest() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String sortType = "";
        String serviceType = "";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, sortType, sortType, "eng", null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateServiceHistoryRequestBadServiceType() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String sortType = "";
        String serviceType = "BadServiceType";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, sortType, sortType, "eng", null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateServiceHistoryRequestBadSortType() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String sortType = "BadSortType";
        String serviceType = "DATA_SHARE_REQUEST";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, sortType, sortType, "eng", null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateServiceHistoryRequestNullSortType() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String serviceType = "DATA_SHARE_REQUEST";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, null, null, serviceType, "eng", null);
    }

    @Test
    public void testValidateServiceHistoryRequestDateCheck() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String sortType = "ASC";
        String serviceType = "DATA_SHARE_REQUEST";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null, "eng", null);
    }

    @Test
    public void testValidateServiceHistoryRequestServiceHistoryServiceRequest() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String sortType = "ASC";
        String serviceType = "SERVICE_REQUEST";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null, "eng", null);
    }

    @Test
    public void testValidateServiceHistoryRequestServiceHistoryID_MANAGEMENT_REQUEST() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String sortType = "ASC";
        String serviceType = "ID_MANAGEMENT_REQUEST";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null, "eng", null);
    }

    @Test
    public void testValidateServiceHistoryRequestServiceHistoryDATA_UPDATE_REQUEST() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String sortType = "ASC";
        String serviceType = "DATA_UPDATE_REQUEST";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null, "eng", null);
    }

    @Test
    public void testValidateServiceHistoryRequestServiceHistoryAUTHENTICATION_REQUEST() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String sortType = "ASC";
        String serviceType = "AUTHENTICATION_REQUEST";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null, "eng", null);
    }

    @Test
    public void testValidateServiceHistoryRequestServiceHistorySuccess() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String sortType = "DESC";
        String serviceType = "AUTHENTICATION_REQUEST";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null, "eng", null);
    }


    @Test(expected = ResidentServiceException.class)
    public void testValidateIndividualId() throws Exception {
        String individualId = "";
        requestValidator.validateEventId(individualId);
    }

    @Test
    public void testValidateIndividualIdSuccess() throws Exception {
        String individualId = "1234567897777777";
        requestValidator.validateEventId(individualId);
    }

    @Test
    public void testPhoneValidator() throws Exception {
        when(emailPhoneValidator.phoneValidator(Mockito.anyString())).thenReturn(true);
        String phone = "1234567890";
        emailPhoneValidator.phoneValidator(phone);
    }

    @Test
    public void testEmailValidator() throws Exception {
        when(emailPhoneValidator.emailValidator(Mockito.anyString())).thenReturn(true);
        String email = "abc@gmail.com";
        emailPhoneValidator.emailValidator(email);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRevokeVidRequestWrapperRevokeVidNull() throws Exception {
        BaseVidRevokeRequestDTO baseVidRevokeRequestDTO = new BaseVidRevokeRequestDTO();
        baseVidRevokeRequestDTO.setVidStatus("REVOKED");
        baseVidRevokeRequestDTO.setTransactionID("123456789");
        RequestWrapper<BaseVidRevokeRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime("2019-01-0");
        requestWrapper.setId("mosip.resident.revokevid");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(baseVidRevokeRequestDTO);
        requestValidator.validateRevokeVidRequestWrapper(requestWrapper, "v1");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRevokeVidRequestWrapper() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidId", "mosip.resident.vidstatus");
        BaseVidRevokeRequestDTO baseVidRevokeRequestDTO = new BaseVidRevokeRequestDTO();
        baseVidRevokeRequestDTO.setVidStatus("mosip.resident.vidstatus");
        baseVidRevokeRequestDTO.setTransactionID("123456789");
        RequestWrapper<BaseVidRevokeRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime("2019-01-0");
        requestWrapper.setId("mosip.resident.vidstatus");
        requestWrapper.setVersion("v1");
        requestWrapper.setRequest(baseVidRevokeRequestDTO);
        requestValidator.validateRevokeVidRequestWrapper(requestWrapper, "v1");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateId() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "transliterateId", "mosip.resident.transliteration.transliterate");
        MainRequestDTO<TransliterationRequestDTO> requestDTO = new MainRequestDTO<>();
        TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
        requestDTO.setId(null);
        requestValidator.validateId(requestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateTransliterationId() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "transliterateId", "mosip.resident.transliteration.transliterate");
        MainRequestDTO<TransliterationRequestDTO> requestDTO = new MainRequestDTO<>();
        TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
        requestDTO.setId("mosip");
        requestValidator.validateId(requestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRequestNewApi() throws Exception {
        RequestWrapper<?> request = new RequestWrapper<>();
        RequestIdType requestIdType = RequestIdType.RE_PRINT_ID;
        requestValidator.validateRequestNewApi(request, requestIdType);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRequestNewApiInvalidId() throws Exception {
        RequestWrapper<?> request = new RequestWrapper<>();
        RequestIdType requestIdType = RequestIdType.VERSION;
        requestValidator.validateRequestNewApi(request, requestIdType);
    }

    @Test
    public void testValidateDownloadCardVid() throws Exception {
        Mockito.when(uinVidValidator.validateVid(Mockito.anyString())).thenReturn(true);
        ReflectionTestUtils.setField(requestValidator, "reprintId", "mosip.resident.print");
        requestValidator.validateDownloadCardVid("12345");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadCardVidFailed() throws Exception {
        Mockito.when(vidValidator.validateId(Mockito.any())).thenReturn(false);
        ReflectionTestUtils.setField(requestValidator, "reprintId", "mosip.resident.print");
        requestValidator.validateDownloadCardVid("12345");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadPersonalizedCard() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
        mainRequestDTO.setId("id");
        mainRequestDTO.setVersion("1.0");
        DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
        mainRequestDTO.setRequest(downloadPersonalizedCardDto);
        requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadPersonalizedCardNullId() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
        mainRequestDTO.setId(null);
        mainRequestDTO.setVersion("1.0");
        DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
        mainRequestDTO.setRequest(downloadPersonalizedCardDto);
        requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadPersonalizedCardNullRequestTime() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
        mainRequestDTO.setId("property");
        mainRequestDTO.setVersion("1.0");
        DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
        mainRequestDTO.setRequest(downloadPersonalizedCardDto);
        requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadPersonalizedCardNullString() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
        mainRequestDTO.setId("property");
        mainRequestDTO.setVersion("1.0");
        mainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
        mainRequestDTO.setRequest(downloadPersonalizedCardDto);
        requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
    }

    @Test
    public void testValidateDownloadPersonalizedCardSuccess() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
        mainRequestDTO.setId("property");
        mainRequestDTO.setVersion("1.0");
        mainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
        downloadPersonalizedCardDto.setHtml("html");
        downloadPersonalizedCardDto.setAttributes(List.of(TemplateVariablesConstants.NAME));
        mainRequestDTO.setRequest(downloadPersonalizedCardDto);
        requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadPersonalizedCardBadHtml() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
        mainRequestDTO.setId("property");
        mainRequestDTO.setVersion("1.0");
        mainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
        downloadPersonalizedCardDto.setHtml("`1&`");
        mainRequestDTO.setRequest(downloadPersonalizedCardDto);
        requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadCardNullTransactionId() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTOMainRequestDTO.setId("property");
        downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadCardNonNumericTransactionId() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTO.setTransactionId("ab");
        downloadCardRequestDTOMainRequestDTO.setId("property");
        downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadCardLessThan10DigitTransactionId() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTO.setTransactionId("1234");
        downloadCardRequestDTOMainRequestDTO.setId("property");
        downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadCardNullOtp() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTO.setTransactionId("1234343434");
        downloadCardRequestDTOMainRequestDTO.setId("property");
        downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadCardNonNumericOtp() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTO.setTransactionId("1234343434");
        downloadCardRequestDTO.setOtp("abc");
        downloadCardRequestDTOMainRequestDTO.setId("property");
        downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateDownloadCardInvalidIndividualId() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTO.setTransactionId("1234343434");
        downloadCardRequestDTO.setOtp("111111");
        downloadCardRequestDTOMainRequestDTO.setId("property");
        downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateDownloadCardEmptyIndividualId() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTO.setTransactionId("1234343434");
        downloadCardRequestDTO.setOtp("111111");
        downloadCardRequestDTO.setIndividualId("");
        downloadCardRequestDTOMainRequestDTO.setId("property");
        downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
    }

    @Test
    public void testValidateDownloadCardSuccess() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTO.setTransactionId("1234343434");
        downloadCardRequestDTO.setOtp("111111");
        downloadCardRequestDTO.setIndividualId("123");
        downloadCardRequestDTOMainRequestDTO.setId("property");
        downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateProxySendOtpRequestInvalidUserId() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV2> userIdOtpRequest =
                new io.mosip.resident.dto.MainRequestDTO<>();
        OtpRequestDTOV2 otpRequestDTOV2 = new OtpRequestDTOV2();
        otpRequestDTOV2.setTransactionId("1232323232");
        userIdOtpRequest.setId("property");
        userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        userIdOtpRequest.setRequest(otpRequestDTOV2);
        requestValidator.validateProxySendOtpRequest(userIdOtpRequest, null);
    }

    @Test
    public void testValidateProxySendOtpRequestCorrectPhoneUserId() throws Exception {
        IdentityDTO identityDTO = new IdentityDTO();
        identityDTO.setFullName("kamesh");
        identityDTO.setEmail("kam@g.com");
        identityDTO.setPhone("8809393939");
        when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("1234567788");
        when(identityUtil.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
        when(emailPhoneValidator.phoneValidator(Mockito.anyString())).thenReturn(true);
        io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV2> userIdOtpRequest =
                new io.mosip.resident.dto.MainRequestDTO<>();
        OtpRequestDTOV2 otpRequestDTOV2 = new OtpRequestDTOV2();
        otpRequestDTOV2.setTransactionId("1232323232");
        userIdOtpRequest.setId("property");
        userIdOtpRequest.setVersion("1.0");
        otpRequestDTOV2.setUserId("8878787878");
        userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        userIdOtpRequest.setRequest(otpRequestDTOV2);
        requestValidator.validateProxySendOtpRequest(userIdOtpRequest, identityDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateUpdateDataRequestInvalidUserId() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest =
                new io.mosip.resident.dto.MainRequestDTO<>();
        OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
        otpRequestDTOV3.setOtp("111111");
        otpRequestDTOV3.setTransactionId("1232323232");
        userIdOtpRequest.setId("property");
        userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        userIdOtpRequest.setRequest(otpRequestDTOV3);
        requestValidator.validateUpdateDataRequest(userIdOtpRequest);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateUpdateDataRequestInvalidPhoneUserId() throws Exception {
        when(emailPhoneValidator.emailValidator(Mockito.anyString())).thenReturn(false);
        io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest =
                new io.mosip.resident.dto.MainRequestDTO<>();
        OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
        otpRequestDTOV3.setOtp("111111");
        otpRequestDTOV3.setTransactionId("1232323232");
        userIdOtpRequest.setId("property");
        otpRequestDTOV3.setUserId("k");
        userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        userIdOtpRequest.setRequest(otpRequestDTOV3);
        requestValidator.validateUpdateDataRequest(userIdOtpRequest);
    }

    @Test
    public void testValidateUpdateDataRequestCorrectPhoneUserId() throws Exception {
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("60");
        when(emailPhoneValidator.phoneValidator(Mockito.anyString())).thenReturn(true);
        io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest =
                new io.mosip.resident.dto.MainRequestDTO<>();
        OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
        otpRequestDTOV3.setOtp("111111");
        otpRequestDTOV3.setTransactionId("1232323232");
        userIdOtpRequest.setId("60");
        userIdOtpRequest.setVersion("1.0");
        otpRequestDTOV3.setUserId("8878787878");
        userIdOtpRequest.setRequesttime(Date.from(Instant.now()));
        userIdOtpRequest.setRequest(otpRequestDTOV3);
        requestValidator.validateUpdateDataRequest(userIdOtpRequest);
    }

    @Test
    public void testValidateUpdateDataRequestCorrectEmailId() throws Exception {
        when(emailPhoneValidator.emailValidator(Mockito.anyString())).thenReturn(true);
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("60");
        io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest =
                new io.mosip.resident.dto.MainRequestDTO<>();
        OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
        otpRequestDTOV3.setOtp("111111");
        otpRequestDTOV3.setTransactionId("1232323232");
        userIdOtpRequest.setId("60");
        userIdOtpRequest.setVersion("1.0");
        otpRequestDTOV3.setUserId("test@g.com");
        userIdOtpRequest.setRequesttime(Date.from(Instant.now()));
        userIdOtpRequest.setRequest(otpRequestDTOV3);
        requestValidator.validateUpdateDataRequest(userIdOtpRequest);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidCreateRequest() {
        requestValidator.validateVidCreateRequest(null, false, null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidCreateV2Request() {
        ResidentVidRequestDtoV2 requestDto = new ResidentVidRequestDtoV2();
        requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
        requestValidator.validateVidCreateV2Request(requestDto,
                false, null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidCreateV2RequestInvalidVersion() {
        ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
        ResidentVidRequestDtoV2 requestDto = new ResidentVidRequestDtoV2();
        requestDto.setId("generate");
        requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
        requestValidator.validateVidCreateV2Request(requestDto,
                false, null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidCreateV2RequestInvalidRequest() {
        ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
        ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
        ResidentVidRequestDtoV2 requestDto = new ResidentVidRequestDtoV2();
        requestDto.setId("generate");
        requestDto.setVersion("newVersion");
        requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
        requestValidator.validateVidCreateV2Request(requestDto,
                false, null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidCreateV2RequestInvalidIndividualId() {
        ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
        ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
        ResidentVidRequestDtoV2 requestDto = new ResidentVidRequestDtoV2();
        requestDto.setId("generate");
        requestDto.setVersion("newVersion");
        VidRequestDtoV2 vidRequestDtoV2 = new VidRequestDtoV2();
        vidRequestDtoV2.setVidType("PERPETUAL");
        requestDto.setRequest(vidRequestDtoV2);
        requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
        requestValidator.validateVidCreateV2Request(requestDto,
                false, null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidCreateV2RequestInvalidRequestDto() {
        ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
        ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
        ResidentVidRequestDto requestDto = new ResidentVidRequestDto();
        requestDto.setId("generate");
        requestDto.setVersion("newVersion");
        VidRequestDto vidRequestDtoV2 = new VidRequestDto();
        vidRequestDtoV2.setVidType("PERPETUAL");
        requestDto.setRequest(vidRequestDtoV2);
        requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
        requestValidator.validateVidCreateV2Request(requestDto,
                true, "123");
    }

    @Test
    public void testValidateVidCreateV2RequestSuccess() {
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
        ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
        ResidentVidRequestDto requestDto = new ResidentVidRequestDto();
        requestDto.setId("generate");
        requestDto.setVersion("newVersion");
        VidRequestDto vidRequestDtoV2 = new VidRequestDto();
        vidRequestDtoV2.setVidType("PERPETUAL");
        vidRequestDtoV2.setTransactionID("1232323232");
        requestDto.setRequest(vidRequestDtoV2);
        requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
        requestValidator.validateVidCreateV2Request(requestDto,
                false, "123");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidatePageFetchAndPageStartFormat() {
        RequestWrapper<AuthHistoryRequestDTO> requestDTO = new RequestWrapper<>();
        AuthHistoryRequestDTO authHistoryRequestDTO = new AuthHistoryRequestDTO();
        authHistoryRequestDTO.setPageStart(String.valueOf(0));
        authHistoryRequestDTO.setPageFetch("0");
        requestDTO.setRequest(authHistoryRequestDTO);
        requestValidator.validatePageFetchAndPageStartFormat(requestDTO, "request");
    }

    @Test
    public void testValidateUin() {
        Mockito.when(uinValidator.validateId(Mockito.any())).thenThrow(new InvalidIDException(ResidentErrorCode.INVALID_UIN.getErrorCode(),
                ResidentErrorCode.INVALID_UIN.getErrorMessage()));
        assertEquals(false, uinVidValidator.validateUin("123"));
    }

    @Test
    public void testValidateVid() {
        Mockito.when(vidValidator.validateId(Mockito.any())).thenThrow(new InvalidIDException(ResidentErrorCode.INVALID_VID.getErrorCode(),
                ResidentErrorCode.INVALID_VID.getErrorMessage()));
        assertEquals(false, uinVidValidator.validateVid("123"));
    }

    @Test(expected = InvalidInputException.class)
    public void testEmptyTransactionId() {
        requestValidator.validateTransactionId("");
    }

    @Test(expected = InvalidInputException.class)
    public void testEmptyUserIdAndTransactionId() {
        requestValidator.validateUserIdAndTransactionId("", "3232323232");
    }

    @Test(expected = InvalidInputException.class)
    public void testInvalidUserId() {
        when(emailPhoneValidator.emailValidator(Mockito.anyString())).thenReturn(false);
        requestValidator.validateUserIdAndTransactionId("shgasbieh", "3232323232");
    }

    @Test(expected = InvalidInputException.class)
    public void testNullUserIdAndTransactionId() {
        requestValidator.validateUserIdAndTransactionId(null, "3232323232");
    }

    @Test
    public void testValidateTransliterationIdSuccess() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "transliterateId", "mosip.resident.transliteration.transliterate");
        MainRequestDTO<TransliterationRequestDTO> requestDTO = new MainRequestDTO<>();
        TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
        transliterationRequestDTO.setFromFieldLang("eng");
        transliterationRequestDTO.setToFieldLang("fra");
        transliterationRequestDTO.setFromFieldValue("demo");
        requestDTO.setRequest(transliterationRequestDTO);
        requestDTO.setId("mosip.resident.transliteration.transliterate");
        requestValidator.validateId(requestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateOnlyLanguageCode() {
        requestValidator.validateLanguageCode(null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateOnlyEmptyLanguageCode() {
        requestValidator.validateLanguageCode("");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateOnlyInvalidLanguageCode() {
        requestValidator.validateLanguageCode("fra");
    }

    @Test
    public void testValidateOnlyValidLanguageCodeSuccess() {
        requestValidator.validateLanguageCode("eng");
    }

    @Test
    public void testValidateOnlyInvalidLanguageCodeSuccess() {
        requestValidator.validateLanguageCode("ara");
    }

    @Test
    public void testValidateEventIdLanguageCodeSuccess() {
        requestValidator.validateEventIdLanguageCode("3434343434777777", "ara");
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateEmptyEventIdLanguageCodeSuccess() {
        requestValidator.validateEventIdLanguageCode("", "ara");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateSortType() {
        ReflectionTestUtils.invokeMethod(requestValidator, "validateSortType", "D", "sortType");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateStatusFilter() {
        ReflectionTestUtils.invokeMethod(requestValidator, "validateStatusFilter", "", "sortType");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateFromDateTimeToDateTimeFromDateTimeNull() {
        requestValidator.validateFromDateTimeToDateTime(null, LocalDate.now(), "fromDate");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateFromDateTimeToDateTimeToDateTimeNull() {
        requestValidator.validateFromDateTimeToDateTime(LocalDate.MAX, null, "fromDate");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRequestNewApiInvalidDate() throws Exception {
        RequestWrapper<?> request = new RequestWrapper<>();
        request.setId("mosip.resident.print");
        RequestIdType requestIdType = RequestIdType.RE_PRINT_ID;
        requestValidator.validateRequestNewApi(request, requestIdType);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRequestNewApiEmptyVersion() throws Exception {
        RequestWrapper<?> request = new RequestWrapper<>();
        request.setId("mosip.resident.print");
        request.setRequesttime(String.valueOf(LocalDateTime.now()));
        RequestIdType requestIdType = RequestIdType.RE_PRINT_ID;
        requestValidator.validateRequestNewApi(request, requestIdType);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRequestNewApiInvalidVersion() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "reqResVersion", "1.0");
        RequestWrapper<?> request = new RequestWrapper<>();
        request.setId("mosip.resident.print");
        request.setRequesttime(String.valueOf(LocalDateTime.now()));
        RequestIdType requestIdType = RequestIdType.RE_PRINT_ID;
        requestValidator.validateRequestNewApi(request, requestIdType);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRequestNewApiInvalidRequest() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "reqResVersion", "1.0");
        RequestWrapper<?> request = new RequestWrapper<>();
        request.setId("mosip.resident.print");
        request.setVersion("1.0");
        request.setRequesttime(String.valueOf(LocalDateTime.now()));
        RequestIdType requestIdType = RequestIdType.RE_PRINT_ID;
        requestValidator.validateRequestNewApi(request, requestIdType);
    }

    @Test
    public void testValidateRequestNewApiSuccess() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "reqResVersion", "1.0");
        RequestWrapper<String> request = new RequestWrapper<>();
        request.setId("mosip.resident.print");
        request.setVersion("1.0");
        request.setRequest("d");
        request.setRequesttime(String.valueOf(LocalDateTime.now()));
        RequestIdType requestIdType = RequestIdType.RE_PRINT_ID;
        assertEquals(true, requestValidator.validateRequestNewApi(request, requestIdType));
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateNullIndividualId() throws Exception {
        requestValidator.validateEventId(null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2Request() throws Exception {
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestId() throws Exception {
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("");
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestIdInvalidId() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("v1");
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestIdInvalidVersion() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("1.0");
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestIdInvalidVersionV1() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("1.0");
        requestDto.setRequesttime(LocalDateTime.now().toString());
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestIdEmptyVersionV1() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("1.0");
        requestDto.setVersion("");
        requestDto.setRequesttime(LocalDateTime.now().toString());
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestIdIncorrectVersion() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        ReflectionTestUtils.setField(requestValidator, "revokeVidVersion", "1.0");
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("1.0");
        requestDto.setVersion("v1");
        requestDto.setRequesttime(LocalDateTime.now().toString());
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestIdInvalidRequest() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        ReflectionTestUtils.setField(requestValidator, "revokeVidVersion", "1.0");
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("1.0");
        requestDto.setVersion("1.0");
        requestDto.setRequesttime(LocalDateTime.now().toString());
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestIdNullVidStatus() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        ReflectionTestUtils.setField(requestValidator, "revokeVidVersion", "1.0");
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("1.0");
        requestDto.setVersion("1.0");
        VidRevokeRequestDTOV2 vidRevokeRequestDTO = new VidRevokeRequestDTOV2();
        requestDto.setRequest(vidRevokeRequestDTO);
        requestDto.setRequesttime(LocalDateTime.now().toString());
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestIdEmptyVidStatus() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        ReflectionTestUtils.setField(requestValidator, "revokeVidVersion", "1.0");
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("1.0");
        requestDto.setVersion("1.0");
        VidRevokeRequestDTOV2 vidRevokeRequestDTO = new VidRevokeRequestDTOV2();
        vidRevokeRequestDTO.setVidStatus("");
        requestDto.setRequest(vidRevokeRequestDTO);
        requestDto.setRequesttime(LocalDateTime.now().toString());
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test
    public void testValidateVidRevokeV2RequestEmptyRequestIdEmptyVidStatusSuccess() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        ReflectionTestUtils.setField(requestValidator, "revokeVidVersion", "1.0");
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("1.0");
        requestDto.setVersion("1.0");
        VidRevokeRequestDTOV2 vidRevokeRequestDTO = new VidRevokeRequestDTOV2();
        vidRevokeRequestDTO.setVidStatus("REVOKED");
        vidRevokeRequestDTO.setTransactionID("1212121212");
        requestDto.setRequest(vidRevokeRequestDTO);
        requestDto.setRequesttime(LocalDateTime.now().toString());
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateServiceHistoryRequestServiceHistoryAUTHENTICATION_REQUESTInvalidDate() throws Exception {
        LocalDate fromDate = LocalDate.of
                (-1, 4, 4);
        LocalDate toDate = LocalDate.now();
        String sortType = "ASC";
        String serviceType = "AUTHENTICATION_REQUEST";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null, "eng", null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateServiceHistoryRequestServiceHistoryAUTHENTICATION_REQUESTInvalidToDate() throws Exception {
        LocalDate toDate = LocalDate.of
                (-1, 4, 4);
        LocalDate fromDate = LocalDate.now();
        String sortType = "ASC";
        String serviceType = "AUTHENTICATION_REQUEST";
        requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null, "eng", null);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidCreateV2RequestFailure() {
        ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
        ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
        ResidentVidRequestDto requestDto = new ResidentVidRequestDto();
        requestDto.setId("generate");
        requestDto.setVersion("newVersion");
        VidRequestDto vidRequestDtoV2 = new VidRequestDto();
        vidRequestDtoV2.setVidType("PERPETUAL");
        vidRequestDtoV2.setTransactionID("1232323232");
        requestDto.setRequest(vidRequestDtoV2);
        requestDto.setRequesttime(null);
        requestValidator.validateVidCreateV2Request(requestDto,
                false, "123");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2InvalidAuthTypeV1() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.authlock");
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("bio-FIR");
        authTypeStatusDto.setLocked(true);
        authTypeStatusDto.setUnlockForSeconds(2L);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(null);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2EmptyAuthTypeV1() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.authlock");
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("");
        authTypeStatusDto.setLocked(true);
        authTypeStatusDto.setUnlockForSeconds(2L);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2InvalidUnlockForSeconds() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.authlock");
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("bio-FIR");
        authTypeStatusDto.setLocked(true);
        authTypeStatusDto.setUnlockForSeconds(-2L);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.authlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeRequest() {
        ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
        ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
        ReflectionTestUtils.setField(requestValidator, "revokeVidId", "mosip.resident.vidstatus");
        RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
        requestDto.setId("mosip.resident.vidstatus");
        requestDto.setVersion("v1");
        VidRevokeRequestDTOV2 vidRevokeRequestDTO = new VidRevokeRequestDTOV2();
        vidRevokeRequestDTO.setVidStatus("");
        vidRevokeRequestDTO.setTransactionID("1212121212");
        requestDto.setRequest(vidRevokeRequestDTO);
        requestDto.setRequesttime(LocalDateTime.now().toString());
        requestValidator.validateVidRevokeRequest(requestDto,
                false, "123");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateUnlockForSeconds() {
        ReflectionTestUtils.invokeMethod(requestValidator, "validateUnlockForSeconds", -1L, "validateUnlockForSeconds");
    }

    @Test
    public void testValidateGrievanceRequestDtoInvalidMessage() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_ID)).thenReturn("id");
        Mockito.when(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_VERSION)).thenReturn("version");
        io.mosip.resident.dto.MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        GrievanceRequestDTO grievanceRequestDTO = new GrievanceRequestDTO();
        grievanceRequestDTO.setMessage("message");
        grievanceRequestDTO.setEventId("1212121212121211");
        grievanceRequestDTOMainRequestDTO.setRequest(grievanceRequestDTO);
        grievanceRequestDTOMainRequestDTO.setId("id");
        grievanceRequestDTOMainRequestDTO.setVersion("version");
        grievanceRequestDTOMainRequestDTO.setRequesttime(DateTime.now().toDate());
        requestValidator.validateGrievanceRequestDto(grievanceRequestDTOMainRequestDTO);
    }

    @Test
    public void testValidateGrievanceRequestDtoSuccess() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_ID)).thenReturn("id");
        Mockito.when(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_VERSION)).thenReturn("version");
        io.mosip.resident.dto.MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        GrievanceRequestDTO grievanceRequestDTO = new GrievanceRequestDTO();
        grievanceRequestDTO.setMessage("message");
        grievanceRequestDTO.setEventId("1212121212121211");
        grievanceRequestDTOMainRequestDTO.setRequest(grievanceRequestDTO);
        grievanceRequestDTOMainRequestDTO.setId("id");
        grievanceRequestDTOMainRequestDTO.setVersion("version");
        grievanceRequestDTOMainRequestDTO.setRequesttime(DateTime.now().toDate());
        requestValidator.validateGrievanceRequestDto(grievanceRequestDTOMainRequestDTO);
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateGrievanceRequestDtoInvalidMessageLength() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_ID)).thenReturn("id");
        Mockito.when(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_VERSION)).thenReturn("version");
        ReflectionTestUtils.setField(requestValidator, "messageCharsLimit", 1);
        io.mosip.resident.dto.MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        GrievanceRequestDTO grievanceRequestDTO = new GrievanceRequestDTO();
        grievanceRequestDTO.setMessage("message");
        grievanceRequestDTO.setEventId("1212121212121211");
        grievanceRequestDTOMainRequestDTO.setRequest(grievanceRequestDTO);
        grievanceRequestDTOMainRequestDTO.setId("id");
        grievanceRequestDTOMainRequestDTO.setVersion("version");
        grievanceRequestDTOMainRequestDTO.setRequesttime(DateTime.now().toDate());
        requestValidator.validateGrievanceRequestDto(grievanceRequestDTOMainRequestDTO);
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateGrievanceRequestDtoInvalidAllowedChars() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_ID)).thenReturn("id");
        ReflectionTestUtils.setField(requestValidator, "messageAllowedSpecialCharRegex", "[A-Za-z .,-]+");
        Mockito.when(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_VERSION)).thenReturn("version");
        io.mosip.resident.dto.MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        GrievanceRequestDTO grievanceRequestDTO = new GrievanceRequestDTO();
        grievanceRequestDTO.setMessage("message1");
        grievanceRequestDTO.setEventId("1212121212121211");
        grievanceRequestDTOMainRequestDTO.setRequest(grievanceRequestDTO);
        grievanceRequestDTOMainRequestDTO.setId("id");
        grievanceRequestDTOMainRequestDTO.setVersion("version");
        grievanceRequestDTOMainRequestDTO.setRequesttime(DateTime.now().toDate());
        requestValidator.validateGrievanceRequestDto(grievanceRequestDTOMainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateEventIdNonNumeric() {
        requestValidator.validateEventId("a");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateEventIdLessLength() {
        requestValidator.validateEventId("3");
    }

    @Test(expected = EidNotBelongToSessionException.class)
    public void testValidateApiBelongToSameSession() throws ResidentServiceCheckedException, ApisResourceAccessException {
        ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setTokenId("123");
        Optional<ResidentTransactionEntity> residentTransactionEntity1 = Optional.of(residentTransactionEntity);
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity1);
        Mockito.when(availableClaimUtility.getResidentIdaToken()).thenReturn("2");
        ReflectionTestUtils.invokeMethod(requestValidator, "validateEventIdBelongToSameSession", "12");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateEmailId() throws ResidentServiceCheckedException, ApisResourceAccessException {
        when(emailPhoneValidator.emailValidator(Mockito.anyString())).thenReturn(false);
        ReflectionTestUtils.invokeMethod(requestValidator, "validateEmailId", "123");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidatePhone() throws ResidentServiceCheckedException, ApisResourceAccessException {
        when(emailPhoneValidator.phoneValidator(Mockito.anyString())).thenReturn(false);
        ReflectionTestUtils.invokeMethod(requestValidator, "validatePhoneNumber", "w");
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateSharableAttributes() {
        requestValidator.validateSharableAttributes(List.of());
    }

    @Test
    public void testValidateSharableAttributesSuccess() {
        requestValidator.validateSharableAttributes(List.of(new SharableAttributesDTO()));
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidatePurposeNull() {
        requestValidator.validatePurpose(null);
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidatePurposeEmpty() {
        requestValidator.validatePurpose("");
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidatePurposeWithMoreCharacter() {
        ReflectionTestUtils.setField(requestValidator, "purposeCharsLimit", 1);
        requestValidator.validatePurpose("HR");
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidatePurposeWithAllowedRegex() {
        ReflectionTestUtils.setField(requestValidator, "purposeCharsLimit", 10);
        ReflectionTestUtils.setField(requestValidator, "purposeAllowedSpecialCharRegex", "^[A-Za-z .,-]+$");
        requestValidator.validatePurpose("H2R");
    }

    @Test
    public void testValidatePurposeSuccess() {
        ReflectionTestUtils.setField(requestValidator, "purposeCharsLimit", 10);
        ReflectionTestUtils.setField(requestValidator, "purposeAllowedSpecialCharRegex", "^[A-Za-z .,-]+$");
        requestValidator.validatePurpose("HR");
    }

    @Test
    public void testValidateReqOtp() {
        IndividualIdOtpRequestDTO individualIdOtpRequestDTO = new IndividualIdOtpRequestDTO();
        individualIdOtpRequestDTO.setIndividualId("1234567890");
        individualIdOtpRequestDTO.setTransactionId("1234567890");
        requestValidator.validateReqOtp(individualIdOtpRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateRequestTimeFailure() {
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("60");
        requestValidator.validateRequestTime(Date.from(Instant.now().minusSeconds(100)));
    }

    @Test
    public void testValidateRequestTime() {
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("60");
        requestValidator.validateRequestTime(Date.from(Instant.now().minusSeconds(10)));
    }

    @Test
    public void testValidateNameWithValidName() throws ResidentServiceCheckedException {
        String name = "Kamesh Shekhar Prasad";
        RequestValidator requestValidator1 = Mockito.spy(new RequestValidator());
        requestValidator1.validateName(name);
        verify(requestValidator1).validateName(name);
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testValidateNameWithBlankName() throws ResidentServiceCheckedException {
        requestValidator.validateName("");
    }

    @Test
    public void testValidateGrievanceRequestDtoSuccessWithAlternateEmailID() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_ID)).thenReturn("id");
        Mockito.when(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_VERSION)).thenReturn("version");
        when(emailPhoneValidator.emailValidator(Mockito.anyString())).thenReturn(true);
        when(emailPhoneValidator.phoneValidator(Mockito.anyString())).thenReturn(true);
        io.mosip.resident.dto.MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO =
                new io.mosip.resident.dto.MainRequestDTO<>();
        GrievanceRequestDTO grievanceRequestDTO = new GrievanceRequestDTO();
        grievanceRequestDTO.setMessage("message");
        grievanceRequestDTO.setEventId("1212121212121211");
        grievanceRequestDTO.setAlternateEmailId("Ka@g.com");
        grievanceRequestDTO.setAlternatePhoneNo("8898787878");
        grievanceRequestDTOMainRequestDTO.setRequest(grievanceRequestDTO);
        grievanceRequestDTOMainRequestDTO.setId("id");
        grievanceRequestDTOMainRequestDTO.setVersion("version");
        grievanceRequestDTOMainRequestDTO.setRequesttime(DateTime.now().toDate());
        requestValidator.validateGrievanceRequestDto(grievanceRequestDTOMainRequestDTO);
    }

    @Test
    public void testValidateReqCredentialRequestSuccess() {
        Mockito.when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
        RequestWrapper<ResidentCredentialRequestDto> requestWrapper = new RequestWrapper<>();
        ResidentCredentialRequestDto residentCredentialRequestDto = new ResidentCredentialRequestDto();
        residentCredentialRequestDto.setIndividualId("1232323232");
        residentCredentialRequestDto.setCredentialType("Vercred");
        residentCredentialRequestDto.setIssuer("resident");
        residentCredentialRequestDto.setOtp("111111");
        residentCredentialRequestDto.setTransactionID("1232323232");
        requestWrapper.setRequest(residentCredentialRequestDto);
        requestValidator.validateReqCredentialRequest(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateReqCredentialRequestFailure() {
        RequestWrapper<ResidentCredentialRequestDto> requestWrapper = new RequestWrapper<>();
        ResidentCredentialRequestDto residentCredentialRequestDto = new ResidentCredentialRequestDto();
        residentCredentialRequestDto.setIndividualId("");
        requestWrapper.setRequest(residentCredentialRequestDto);
        requestValidator.validateReqCredentialRequest(requestWrapper);
    }

    @Test
    public void testValidateShareCredentialRequest() {
        RequestWrapper<ShareCredentialRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setId("42");
        ReflectionTestUtils.setField(requestValidator, "shareCredentialId", "io.mosip");
        SharableAttributesDTO sharableAttributesDTO = new SharableAttributesDTO();
        sharableAttributesDTO.setAttributeName("name");
        ShareCredentialRequestDto shareCredentialRequestDto = new ShareCredentialRequestDto();
        shareCredentialRequestDto.setSharableAttributes(List.of(sharableAttributesDTO));
        shareCredentialRequestDto.setConsent("Accepted");
        shareCredentialRequestDto.setPurpose("share");
        requestWrapper.setRequest(shareCredentialRequestDto);
        requestWrapper.setId("mosip.resident.share.credential");
        requestWrapper.setRequesttime(String.valueOf(LocalDateTime.now()));
        requestWrapper.setVersion("1.0");
        thrown.expect(ResidentServiceException.class);
        requestValidator.validateShareCredentialRequest(requestWrapper);
        verify(requestWrapper).getId();
        verify(requestWrapper).setId((String) any());
        verify(requestWrapper).setRequest((ShareCredentialRequestDto) any());
        verify(requestWrapper).setRequesttime((String) any());
        verify(requestWrapper).setVersion((String) any());
    }

    @Test
    public void testValidateShareCredentialRequestWithPartnerID() {
        RequestWrapper<ShareCredentialRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setId("42");
        ReflectionTestUtils.setField(requestValidator, "shareCredentialId", "io.mosip");
        when(environment.getProperty(Mockito.anyString())).thenReturn("env");
        when(proxyPartnerManagementService.getPartnerDetailFromPartnerIdAndPartnerType(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new HashMap<>());
        SharableAttributesDTO sharableAttributesDTO = new SharableAttributesDTO();
        sharableAttributesDTO.setAttributeName("name");
        ShareCredentialRequestDto shareCredentialRequestDto = new ShareCredentialRequestDto();
        shareCredentialRequestDto.setSharableAttributes(List.of(sharableAttributesDTO));
        shareCredentialRequestDto.setConsent("Accepted");
        shareCredentialRequestDto.setPurpose("share");
        shareCredentialRequestDto.setPartnerId("partnerId");
        requestWrapper.setRequest(shareCredentialRequestDto);
        requestWrapper.setId("mosip.resident.share.credential");
        requestWrapper.setRequesttime(String.valueOf(LocalDateTime.now()));
        requestWrapper.setVersion("1.0");
        thrown.expect(ResidentServiceException.class);
        requestValidator.validateShareCredentialRequest(requestWrapper);
        verify(requestWrapper).getId();
        verify(requestWrapper).setId((String) any());
        verify(requestWrapper).setRequest((ShareCredentialRequestDto) any());
        verify(requestWrapper).setRequesttime((String) any());
        verify(requestWrapper).setVersion((String) any());
    }
    @Test(expected = InvalidInputException.class)
    public void testValidateDownloadPersonalizedCardInvalidAttributeList() throws Exception {
        io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
        mainRequestDTO.setId("property");
        mainRequestDTO.setVersion("1.0");
        mainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
        downloadPersonalizedCardDto.setHtml("html");
        downloadPersonalizedCardDto.setAttributes(List.of());
        mainRequestDTO.setRequest(downloadPersonalizedCardDto);
        requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateProxySendOtpRequestInCorrectPhoneUserId() throws Exception {
        IdentityDTO identityDTO = new IdentityDTO();
        identityDTO.setFullName("kamesh");
        identityDTO.setEmail("kam@g.com");
        identityDTO.setPhone("8878787878");
        when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("1234567788");
        when(identityUtil.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
        io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV2> userIdOtpRequest =
                new io.mosip.resident.dto.MainRequestDTO<>();
        OtpRequestDTOV2 otpRequestDTOV2 = new OtpRequestDTOV2();
        otpRequestDTOV2.setTransactionId("1232323232");
        userIdOtpRequest.setId("property");
        userIdOtpRequest.setVersion("1.0");
        otpRequestDTOV2.setUserId("8878787878");
        userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        userIdOtpRequest.setRequest(otpRequestDTOV2);
        requestValidator.validateProxySendOtpRequest(userIdOtpRequest, identityDTO);
    }

    @Test
    public void testValidateProxySendOtpRequestInCorrectEmailUserId() throws Exception {
        IdentityDTO identityDTO = new IdentityDTO();
        identityDTO.setFullName("kamesh");
        identityDTO.setEmail("kam@g.com");
        identityDTO.setPhone("887878");
        when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("1234567788");
        when(emailPhoneValidator.phoneValidator(Mockito.anyString())).thenReturn(true);
        when(emailPhoneValidator.emailValidator(Mockito.anyString())).thenReturn(true);
        when(identityUtil.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
        io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV2> userIdOtpRequest =
                new io.mosip.resident.dto.MainRequestDTO<>();
        OtpRequestDTOV2 otpRequestDTOV2 = new OtpRequestDTOV2();
        otpRequestDTOV2.setTransactionId("1232323232");
        userIdOtpRequest.setId("property");
        userIdOtpRequest.setVersion("1.0");
        otpRequestDTOV2.setUserId("kam@g.com");
        userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        userIdOtpRequest.setRequest(otpRequestDTOV2);
        when(remainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(Mockito.anyList())).thenReturn(new ResponseWrapper<>());
        requestValidator.validateProxySendOtpRequest(userIdOtpRequest, identityDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateProxySendOtpRequestFailed() throws Exception {
        IdentityDTO identityDTO = new IdentityDTO();
        identityDTO.setFullName("kamesh");
        identityDTO.setEmail("kam@g.com");
        identityDTO.setPhone("887878");
        when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("1234567788");
        when(identityUtil.getIdentity(Mockito.anyString())).thenThrow(new ResidentServiceCheckedException());
        io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV2> userIdOtpRequest =
                new io.mosip.resident.dto.MainRequestDTO<>();
        OtpRequestDTOV2 otpRequestDTOV2 = new OtpRequestDTOV2();
        otpRequestDTOV2.setTransactionId("1232323232");
        userIdOtpRequest.setId("property");
        userIdOtpRequest.setVersion("1.0");
        otpRequestDTOV2.setUserId("kam@g.com");
        userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2, 2));
        userIdOtpRequest.setRequest(otpRequestDTOV2);
        requestValidator.validateProxySendOtpRequest(userIdOtpRequest, identityDTO);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateTransliterationIdLangFailed() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "transliterateId", "mosip.resident.transliteration.transliterate");
        MainRequestDTO<TransliterationRequestDTO> requestDTO = new MainRequestDTO<>();
        TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
        transliterationRequestDTO.setFromFieldLang("eng");
        transliterationRequestDTO.setToFieldLang("eng");
        transliterationRequestDTO.setFromFieldValue("demo");
        requestDTO.setRequest(transliterationRequestDTO);
        requestDTO.setId("mosip.resident.transliteration.transliterate");
        requestValidator.validateId(requestDTO);
    }

    @Test
    public void testValidateOtpCharLimit() {
        validateOtpCharLimit.validateOtpCharLimit("111111");
    }

    @Test
    public void testValidateOtpCharLimitFailed() {
        validateOtpCharLimit.validateOtpCharLimit("11111111");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateSchemaType() {
        requestValidator.validateSchemaType("ui");
    }

    @Test
    public void testValidateSchemaTypeSuccess() {
        requestValidator.validateSchemaType("update-demographics");
    }

    @Test
    public void testValidateSearchText() {
        requestValidator.validateSearchText(null);
    }

    @Test
    public void testValidateSearchTextNotNull() {
        requestValidator.validateSearchText("11");
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateSearchTextNotNullFailure() {
        requestValidator.validateSearchText("154654545454124545451");
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateChannelVerificationStatusInvalidVidLength() throws Exception {
        String channel = "PHONE";
        requestValidator.validateChannelVerificationStatus(channel, "454645787845124578");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAidStatusRequestDtoFailure() throws Exception {
        AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
        aidStatusRequestDTO.setIndividualId(null);
        aidStatusRequestDTO.setTransactionId("1234567890");
        RequestWrapper<AidStatusRequestDTO> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.checkstatus");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(aidStatusRequestDTO);
        requestValidator.validateAidStatusRequestDto(requestWrapper);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateUpdateRequestV2Failed() throws Exception {
        Mockito.when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("1212121212");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("IDSchemaVersion", "0.2");
        ResponseWrapper idSchemaResponse = new ResponseWrapper();
        JSONObject object = new JSONObject();
        object.put("schemaJson", schema);
        idSchemaResponse.setResponse(object);
        Map<String, ?> map = new HashMap<>();
        when(objectMapper.convertValue(object, Map.class)).thenReturn(object);

        when(proxyMasterdataService.getLatestIdSchema(0.2, null, null)).thenReturn(idSchemaResponse);
        Mockito.when(utilities.retrieveIdrepoJson(Mockito.anyString())).thenReturn(jsonObject);

        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        requestDTO.setIndividualIdType(IdType.UIN.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestDTO.setTransactionID("1234567");
        requestDTO.setIdentityJson("abcdef");
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, true, schemaJson);
    }

    @Test
    public void testValidateUpdateRequestV2Passed() throws Exception {
        Mockito.when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("1212121212");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("IDSchemaVersion", "0.2");
        ResponseWrapper idSchemaResponse = new ResponseWrapper();
        JSONObject object = new JSONObject();
        object.put("schemaJson", schema);
        idSchemaResponse.setResponse(object);
        Map<String, ?> map = new HashMap<>();
        when(objectMapper.convertValue(object, Map.class)).thenReturn(object);

        when(proxyMasterdataService.getLatestIdSchema(0.2, null, null)).thenReturn(idSchemaResponse);
        Mockito.when(utilities.retrieveIdrepoJson(Mockito.anyString())).thenReturn(jsonObject);

        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        requestDTO.setIndividualIdType(IdType.UIN.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestDTO.setTransactionID("1234567890");
        requestDTO.setIdentityJson("abcdef");
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("identity", "abc");
        requestDTO.setIdentity(jsonObject1);
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, true, schemaJson);
    }

    @Test
    public void testValidateUpdateRequestV2PassedWithLanguageCode() throws Exception {
        Mockito.when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("1212121212");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("IDSchemaVersion", "0.2");
        ResponseWrapper idSchemaResponse = new ResponseWrapper();
        JSONObject object = new JSONObject();
        object.put("schemaJson", schema);
        idSchemaResponse.setResponse(object);
        Map<String, ?> map = new HashMap<>();
        when(objectMapper.convertValue(object, Map.class)).thenReturn(object);

        when(proxyMasterdataService.getLatestIdSchema(0.2, null, null)).thenReturn(idSchemaResponse);
        Mockito.when(utilities.retrieveIdrepoJson(Mockito.anyString())).thenReturn(jsonObject);

        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        requestDTO.setIndividualIdType(IdType.UIN.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestDTO.setTransactionID("1234567890");
        requestDTO.setIdentityJson("abcdef");
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("identity", "abc");
        ArrayList<Map<String, String>> languageMap = new ArrayList<>();
        Map<String, String> language = new HashMap<>();
        language.put("language", "eng");
        language.put("value", "eng");
        languageMap.add(language);
        jsonObject1.put("value", languageMap);
        requestDTO.setIdentity(jsonObject1);
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, true, schemaJson);
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateUpdateRequestV2InvalidTransactionId() throws Exception {
        Mockito.when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("1212121212");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("IDSchemaVersion", "0.2");
        ResponseWrapper idSchemaResponse = new ResponseWrapper();
        JSONObject object = new JSONObject();
        object.put("schemaJson", schema);
        idSchemaResponse.setResponse(object);
        Map<String, ?> map = new HashMap<>();
        when(objectMapper.convertValue(object, Map.class)).thenReturn(object);

        when(proxyMasterdataService.getLatestIdSchema(0.2, null, null)).thenReturn(idSchemaResponse);
        Mockito.when(utilities.retrieveIdrepoJson(Mockito.anyString())).thenReturn(jsonObject);

        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        requestDTO.setIndividualIdType(IdType.UIN.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestDTO.setTransactionID("");
        requestDTO.setIdentityJson("abcdef");
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("identity", "abc");
        ArrayList<Map<String, String>> languageMap = new ArrayList<>();
        Map<String, String> language = new HashMap<>();
        language.put("language", "eng");
        language.put("value", "eng");
        languageMap.add(language);
        jsonObject1.put("value", languageMap);
        requestDTO.setIdentity(jsonObject1);
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, true, schemaJson);
    }

    @Test
    public void testValidateUpdateRequestV2FailedWithLanguageCode() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "attributeNamesWithoutDocumentsRequired", "email");
        Mockito.when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("1212121212");
        Map<String, Object> identityMappingMap = new HashMap<>();
        Map<String, String> identityValue = new HashMap<>();
        identityValue.put("value", "email");
        identityMappingMap.put("email", identityValue);
        Mockito.when(residentConfigService.getIdentityMappingMap()).thenReturn(identityMappingMap);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("IDSchemaVersion", "0.2");
        ResponseWrapper idSchemaResponse = new ResponseWrapper();
        JSONObject object = new JSONObject();
        object.put("schemaJson", schema);
        idSchemaResponse.setResponse(object);
        Map<String, ?> map = new HashMap<>();
        when(objectMapper.convertValue(object, Map.class)).thenReturn(object);

        when(proxyMasterdataService.getLatestIdSchema(0.2, null, null)).thenReturn(idSchemaResponse);
        Mockito.when(utilities.retrieveIdrepoJson(Mockito.anyString())).thenReturn(jsonObject);

        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        requestDTO.setIndividualIdType(IdType.UIN.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestDTO.setTransactionID("1234567890");
        requestDTO.setIdentityJson("abcdef");
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("identity", "abc");
        ArrayList<Map<String, String>> languageMap = new ArrayList<>();
        Map<String, String> language = new HashMap<>();
        language.put("language", "eng");
        language.put("value", "eng");
        languageMap.add(language);
        jsonObject1.put("value", languageMap);
        requestDTO.setIdentity(jsonObject1);
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, true, schemaJson);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateUpdateRequestV2FailedWithRunTimeException() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "attributeNamesWithoutDocumentsRequired", "email");
        Mockito.when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("1212121212");
        Map<String, Object> identityMappingMap = new HashMap<>();
        Map<String, String> identityValue = new HashMap<>();
        identityValue.put("value", "email");
        identityMappingMap.put("email", identityValue);
        Mockito.when(residentConfigService.getIdentityMappingMap()).thenThrow(new IOException());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("IDSchemaVersion", "0.2");
        ResponseWrapper idSchemaResponse = new ResponseWrapper();
        JSONObject object = new JSONObject();
        object.put("schemaJson", schema);
        idSchemaResponse.setResponse(object);
        Map<String, ?> map = new HashMap<>();
        when(objectMapper.convertValue(object, Map.class)).thenReturn(object);

        when(proxyMasterdataService.getLatestIdSchema(0.2, null, null)).thenReturn(idSchemaResponse);
        Mockito.when(utilities.retrieveIdrepoJson(Mockito.anyString())).thenReturn(jsonObject);

        ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
        requestDTO.setIndividualIdType(IdType.UIN.name());
        requestDTO.setIndividualId("1234567");
        requestDTO.setOtp("1234567");
        requestDTO.setTransactionID("1234567");
        requestDTO.setIdentityJson("abcdef");
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("identity", "abc");
        ArrayList<Map<String, String>> languageMap = new ArrayList<>();
        Map<String, String> language = new HashMap<>();
        language.put("language", "eng");
        language.put("value", "eng");
        languageMap.add(language);
        jsonObject1.put("value", languageMap);
        requestDTO.setIdentity(jsonObject1);
        RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.updateuin");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(requestDTO);
        requestValidator.validateUpdateRequest(requestWrapper, true, schemaJson);
    }

    @Test
    public void testEmailsCharValidator() {
        requestValidator.emailCharsValidator("ka@g.com");
    }

    @Test(expected = ResidentServiceException.class)
    public void testEmailsCharValidatorFailed() {
        requestValidator.emailCharsValidator("45454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545");
    }

    @Test(expected = ResidentServiceException.class)
    public void testPhoneCharsValidator() {
        UUID uuid = UUID.randomUUID();
        requestValidator.phoneCharsValidator("45454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545454545");
    }

    @Test
    public void testValidateRid() {
        Mockito.when(ridValidator.validateId(Mockito.anyString())).thenReturn(true);
        assertTrue(requestValidator.validateRid("11345678"));
    }

    @Test
    public void testValidateRidFailed() {
        Mockito.when(ridValidator.validateId(Mockito.anyString())).thenThrow(new InvalidIDException(ResidentErrorCode.INVALID_RID.getErrorCode(),
                ResidentErrorCode.INVALID_RID.getErrorMessage()));
        assertFalse(requestValidator.validateRid("11345678"));
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestIdEmptyVidStatusFailure() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        ReflectionTestUtils.setField(requestValidator, "revokeVidVersion", "1.0");
        RequestWrapper<VidRevokeRequestDTO> requestDto = new RequestWrapper<>();
        requestDto.setId("1.0");
        requestDto.setVersion("1.0");
        VidRevokeRequestDTO vidRevokeRequestDTO = new VidRevokeRequestDTO();
        vidRevokeRequestDTO.setVidStatus("REVOKED");
        vidRevokeRequestDTO.setTransactionID("1212121212");
        requestDto.setRequest(vidRevokeRequestDTO);
        requestDto.setRequesttime(LocalDateTime.now().toString());
        requestValidator.validateVidRevokeV2Request(requestDto, false, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateVidRevokeV2RequestEmptyRequestIdEmptyOtpFailure() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "revokeVidIdNew", "1.0");
        ReflectionTestUtils.setField(requestValidator, "revokeVidVersion", "1.0");
        RequestWrapper<VidRevokeRequestDTO> requestDto = new RequestWrapper<>();
        requestDto.setId("1.0");
        requestDto.setVersion("1.0");
        VidRevokeRequestDTO vidRevokeRequestDTO = new VidRevokeRequestDTO();
        vidRevokeRequestDTO.setVidStatus("REVOKED");
        vidRevokeRequestDTO.setTransactionID("1212121212");
        vidRevokeRequestDTO.setIndividualId("123");
        requestDto.setRequest(vidRevokeRequestDTO);
        requestDto.setRequesttime(LocalDateTime.now().toString());
        requestValidator.validateVidRevokeV2Request(requestDto, true, "3956038419");
    }

    @Test(expected = InvalidInputException.class)
    public void testValidateAuthLockOrUnlockRequestV2Failed() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.auth.lock.unlock");
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("bio-IR");
        authTypeStatusDto.setLocked(true);
        authTypeStatusDto.setUnlockForSeconds(10L);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.auth.lock.unlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
    }

    @Test
    public void testValidateAuthLockOrUnlockRequestV2FailedUnlockSeconds() throws Exception {
        ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.auth.lock.unlock");
        AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
        List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
        AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
        authTypeStatusDto.setAuthType("bio-FIR");
        authTypeStatusDto.setLocked(true);
        authTypeStatusDto.setUnlockForSeconds(-10L);
        authTypes.add(authTypeStatusDto);
        authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
        RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
        requestWrapper.setId("mosip.resident.auth.lock.unlock");
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
        try {
            requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
        } catch (BaseResidentUncheckedExceptionWithMetadata e) {
            assertEquals(e.getErrorCode(),ResidentErrorCode.UNSUPPORTED_INPUT.getErrorCode());
            return;
        }
        fail();
    }

    @Test
    public void testValidateSameData_SameStringAttribute() {
        identity.put("email", "existing@example.com");
        idRepoJson.put("email", "existing@example.com");

        Exception exception = assertThrows(ResidentServiceCheckedException.class, () -> {
            validateSameData.validateSameData(idRepoJson, identity);
        });

        String expectedMessage = "Same data already present for Attribute:- gender and Language code: eng";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testValidateSameData_SameJsonObjectAttribute() {
        JSONArray genderArray = new JSONArray();
        JSONObject genderObject = new JSONObject();
        genderObject.put(ResidentConstants.LANGUAGE, "eng");
        genderObject.put(ResidentConstants.VALUE, "Female");
        genderArray.add(genderObject);

        identity.put("gender", genderArray);

        Exception exception = assertThrows(ResidentServiceCheckedException.class, () -> {
            validateSameData.validateSameData(idRepoJson, identity);
        });

        String expectedMessage = "Same data already present for Attribute:- gender and Language code: eng";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testValidateSameData_NoSameData() {
        identity.put("email", "new@example.com");
        JSONArray newGenderArray = new JSONArray();
        JSONObject newGenderObject = new JSONObject();
        newGenderObject.put(ResidentConstants.LANGUAGE, "spa");
        newGenderObject.put(ResidentConstants.VALUE, "Mujer");
        newGenderArray.add(newGenderObject);

        identity.put("gender", newGenderArray);

        assertDoesNotThrow(() -> validateSameData.validateSameData(idRepoJson, identity));
    }
}