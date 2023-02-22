package io.mosip.resident.test.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.openid.bridge.api.service.validator.ScopeValidator;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.controller.ResidentController;
import io.mosip.resident.dto.AidStatusRequestDTO;
import io.mosip.resident.dto.AidStatusResponseDTO;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthHistoryResponseDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDtoV2;
import io.mosip.resident.dto.AuthTypeStatusDtoV2;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.PageDto;
import io.mosip.resident.dto.RegStatusCheckResponseDTO;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentDemographicUpdateRequestDTO;
import io.mosip.resident.dto.ResidentDocuments;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentReprintResponseDto;
import io.mosip.resident.dto.ResidentServiceHistoryResponseDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResidentUpdateResponseDTO;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
import io.mosip.resident.dto.SortType;
import io.mosip.resident.dto.UserInfoDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdAuthServiceImpl;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.validator.RequestValidator;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Sowmya Ujjappa Banakar
 * @author Jyoti Prakash Nayak
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class ResidentControllerTest {

	@MockBean
	private ProxyIdRepoService proxyIdRepoService;

	@MockBean
	private ResidentServiceImpl residentService;

	@Mock
	CbeffImpl cbeff;

	@MockBean
	private RequestValidator validator;

	@MockBean
	private ResidentVidService vidService;

	@MockBean
	private IdAuthServiceImpl idAuthServiceImpl;

	@MockBean
	private IdentityServiceImpl identityServiceImpl;

	@MockBean
	private DocumentService docService;

	@MockBean
	private ScopeValidator scopeValidator;

	@MockBean
	private ObjectStoreHelper objectStore;

	@Mock
	private AuditUtil audit;

	@MockBean
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

	@MockBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@InjectMocks
	ResidentController residentController;

	RequestWrapper<AuthLockOrUnLockRequestDto> authLockRequest;
	RequestWrapper<EuinRequestDTO> euinRequest;
	RequestWrapper<AuthHistoryRequestDTO> authHistoryRequest;
	RequestWrapper<AuthLockOrUnLockRequestDtoV2> authTypeStatusRequest;

	/** The array to json. */
	private String authLockRequestToJson;
	private String euinRequestToJson;
	private String historyRequestToJson;
	private String authStatusRequestToJson;
	private Gson gson;

	/** The mock mvc. */
	@Autowired
	private MockMvc mockMvc;

	@Before
	public void setUp() throws ApisResourceAccessException {
		MockitoAnnotations.initMocks(this);
		authLockRequest = new RequestWrapper<AuthLockOrUnLockRequestDto>();

		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setIndividualId("5734728510");
		authLockRequestDto.setOtp("111111");
		authLockRequestDto.setTransactionID("1234567898");
		List<String> authTypes = new ArrayList<>();
		authTypes.add("bio-FIR");
		authLockRequestDto.setAuthType(authTypes);
		authLockRequest.setRequest(authLockRequestDto);
		euinRequest = new RequestWrapper<EuinRequestDTO>();
		euinRequest.setRequest(new EuinRequestDTO("5734728510", "1234567890", IdType.UIN.name(), "UIN", "111111"));

		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("bio-FIR");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(1L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		authTypeStatusRequest = new RequestWrapper<>();
		authTypeStatusRequest.setRequest(authLockOrUnLockRequestDtoV2);
		authTypeStatusRequest.setRequesttime(LocalDateTime.now().toString());
		authTypeStatusRequest.setVersion("v1");
		authTypeStatusRequest.setId("io.mosip.resident.authHistory");

		gson = new GsonBuilder().serializeNulls().create();
		authLockRequestToJson = gson.toJson(authLockRequest);
		euinRequestToJson = gson.toJson(euinRequest);

		authStatusRequestToJson = gson.toJson(authTypeStatusRequest);
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());

		when(identityServiceImpl.getResidentIndvidualId()).thenReturn("5734728510");
	}

	@Test
	@WithUserDetails("resident")
	public void testGetRidStatusSuccess() throws Exception {
		RegStatusCheckResponseDTO dto = new RegStatusCheckResponseDTO();
		dto.setRidStatus("PROCESSED");
		Mockito.doReturn(dto).when(residentService).getRidStatus((RequestDTO) Mockito.any());
		this.mockMvc
				.perform(post("/rid/check-status").contentType(MediaType.APPLICATION_JSON)
						.content(authLockRequestToJson))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.ridStatus", is("PROCESSED")));
	}

	@Test
	@WithUserDetails("resident")
	public void testRequestAuthLockSuccess() throws Exception {
		ResponseDTO responseDto = new ResponseDTO();
		responseDto.setStatus("success");
		doNothing().when(validator).validateAuthLockOrUnlockRequest(Mockito.any(), Mockito.any());
		Mockito.doReturn(responseDto).when(residentService).reqAauthTypeStatusUpdate(Mockito.any(), Mockito.any());

		this.mockMvc
				.perform(post("/req/auth-lock").contentType(MediaType.APPLICATION_JSON).content(authLockRequestToJson))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.status", is("success")));
	}

	@Test
	@WithUserDetails("resident")
	public void testReqAuthTypeLock() throws Exception {
		ResponseDTO responseDto = new ResponseDTO();
		responseDto.setStatus("success");
		doNothing().when(validator).validateAuthLockOrUnlockRequestV2(Mockito.any());
		Mockito.doReturn(Tuples.of(responseDto, "12345")).when(residentService).reqAauthTypeStatusUpdateV2(Mockito.any());
		residentController.reqAauthTypeStatusUpdateV2(authTypeStatusRequest);
		validator.validateAuthLockOrUnlockRequestV2(authTypeStatusRequest);
		this.mockMvc.perform(
				post("/auth-lock-unlock").contentType(MediaType.APPLICATION_JSON).content(authStatusRequestToJson))
				.andExpect(status().isOk()).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("resident")
	public void testReqAuthTypeLockBadRequest() throws Exception {
		ResponseDTO responseDto = new ResponseDTO();
		doNothing().when(validator).validateAuthLockOrUnlockRequest(Mockito.any(), Mockito.any());
		Mockito.doReturn(Tuples.of(responseDto, "12345")).when(residentService).reqAauthTypeStatusUpdateV2(Mockito.any());

		MvcResult result = this.mockMvc
				.perform(post("/auth-lock-unlock").contentType(MediaType.APPLICATION_JSON).content(""))
				.andExpect(status().isOk()).andReturn();
		assertTrue(result.getResponse().getContentAsString().contains("RES-SER-418"));
	}

	@Test
	@WithUserDetails("resident")
	public void testRequestAuthLockBadRequest() throws Exception {
		ResponseDTO responseDto = new ResponseDTO();
		doNothing().when(validator).validateAuthLockOrUnlockRequest(Mockito.any(), Mockito.any());
		Mockito.doReturn(responseDto).when(residentService).reqAauthTypeStatusUpdate(Mockito.any(), Mockito.any());

		MvcResult result = this.mockMvc
				.perform(post("/req/auth-lock").contentType(MediaType.APPLICATION_JSON).content(""))
				.andExpect(status().isOk()).andReturn();
		assertTrue(result.getResponse().getContentAsString().contains("RES-SER-418"));
	}

	@Test
	@WithUserDetails("resident")
	public void testRequestEuinSuccess() throws Exception {
		doNothing().when(validator).validateEuinRequest(Mockito.any());
		Mockito.doReturn(new byte[10]).when(residentService).reqEuin(Mockito.any());

		MvcResult result = this.mockMvc
				.perform(post("/req/euin").contentType(MediaType.APPLICATION_JSON).content(euinRequestToJson))
				.andExpect(status().isOk()).andReturn();
		assertEquals("application/pdf", result.getResponse().getContentType());
	}

	@Test
	@WithUserDetails("resident")
	public void testRequestEuinBadRequest() throws Exception {

		MvcResult result = this.mockMvc.perform(post("/req/euin").contentType(MediaType.APPLICATION_JSON).content(""))
				.andExpect(status().isOk()).andReturn();
		assertTrue(result.getResponse().getContentAsString().contains("RES-SER-418"));
	}

	@Test
	@WithUserDetails("resident")
	public void testReprintUINSuccess() throws Exception {
		ResidentReprintResponseDto reprintResp = new ResidentReprintResponseDto();
		reprintResp.setRegistrationId("123456789");
		reprintResp.setMessage("Notification sent");
		ResponseWrapper<ResidentReprintResponseDto> response = new ResponseWrapper<>();
		response.setResponse(reprintResp);

		Mockito.when(residentService.reqPrintUin(Mockito.any())).thenReturn(reprintResp);

		RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
		ResidentReprintRequestDto request = new ResidentReprintRequestDto();
		request.setIndividualId("3527812406");
		request.setIndividualIdType(IdType.UIN.name());
		request.setOtp("1234");
		request.setTransactionID("9876543210");
		requestWrapper.setRequest(request);
		requestWrapper.setId(",osip.resident.reprint");
		requestWrapper.setVersion("1.0");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());

		Gson gson = new GsonBuilder().serializeNulls().create();
		String requestAsString = gson.toJson(requestWrapper);

		this.mockMvc.perform(post("/req/print-uin").contentType(MediaType.APPLICATION_JSON).content(requestAsString))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testRequestAuthUnLockSuccess() throws Exception {
		ResponseDTO responseDto = new ResponseDTO();
		responseDto.setStatus("success");
		doNothing().when(validator).validateAuthLockOrUnlockRequest(Mockito.any(), Mockito.any());
		Mockito.doReturn(responseDto).when(residentService).reqAauthTypeStatusUpdate(Mockito.any(), Mockito.any());

		this.mockMvc
				.perform(
						post("/req/auth-unlock").contentType(MediaType.APPLICATION_JSON).content(authLockRequestToJson))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.status", is("success")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testRequestAuthUnLockBadRequest() throws Exception {
		ResponseDTO responseDto = new ResponseDTO();
		doNothing().when(validator).validateAuthLockOrUnlockRequest(Mockito.any(), Mockito.any());
		Mockito.doReturn(responseDto).when(residentService).reqAauthTypeStatusUpdate(Mockito.any(), Mockito.any());

		MvcResult result = this.mockMvc
				.perform(post("/req/auth-unlock").contentType(MediaType.APPLICATION_JSON).content(""))
				.andExpect(status().isOk()).andReturn();
		assertTrue(result.getResponse().getContentAsString().contains("RES-SER-418"));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testGetServiceHistorySuccess() throws Exception {
		ResponseWrapper<PageDto<ServiceHistoryResponseDto>> response = new ResponseWrapper<>();
		Mockito.when(residentService.getServiceHistory(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt())).thenReturn(response);
		residentController.getServiceHistory("eng", 1, 12, LocalDate.parse("2022-06-10"),
				LocalDate.parse("2022-06-10"), SortType.ASC.toString(),
				ServiceType.AUTHENTICATION_REQUEST.name(), null, null, 0);
		mockMvc.perform(MockMvcRequestBuilders.get("/service-history/eng").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testRequestAuthHistorySuccess() throws Exception {
		authHistoryRequest = new RequestWrapper<AuthHistoryRequestDTO>();
		AuthHistoryRequestDTO hisdto = new AuthHistoryRequestDTO();
		hisdto.setIndividualId("1234");
		hisdto.setOtp("1234");
		hisdto.setTransactionID("1234");
		authHistoryRequest.setRequest(hisdto);
		authHistoryRequest.setId("id");
		authHistoryRequest.setRequesttime("12-12-2009");
		authHistoryRequest.setVersion("v1");
		historyRequestToJson = gson.toJson(authHistoryRequest);
		AuthHistoryResponseDTO responseDto = new AuthHistoryResponseDTO();
		responseDto.setMessage("success");
		doNothing().when(validator).validateAuthHistoryRequest(Mockito.any());
		Mockito.doReturn(responseDto).when(residentService).reqAuthHistory(Mockito.any());

		this.mockMvc
				.perform(
						post("/req/auth-history").contentType(MediaType.APPLICATION_JSON).content(historyRequestToJson))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.message", is("success")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testRequestAuthHistoryBadRequest() throws Exception {

		MvcResult result = this.mockMvc
				.perform(post("/req/auth-history").contentType(MediaType.APPLICATION_JSON).content(""))
				.andExpect(status().isOk()).andReturn();
		assertTrue(result.getResponse().getContentAsString().contains("RES-SER-418"));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testRequestUINUpdate() throws Exception {
		ResidentUpdateRequestDto dto = new ResidentUpdateRequestDto();
		ResidentDocuments document = new ResidentDocuments();
		document.setName("POA");
		document.setValue("abecfsgdsdg");
		List<ResidentDocuments> list = new ArrayList<>();
		list.add(document);
		dto.setDocuments(list);
		dto.setIdentityJson("sdgfdgsfhfh");
		dto.setIndividualId("9876543210");
		dto.setIndividualIdType(IdType.UIN.name());
		dto.setOtp("1234");
		dto.setTransactionID("12345");
		RequestWrapper<ResidentUpdateRequestDto> reqWrapper = new RequestWrapper<>();
		reqWrapper.setRequest(dto);
		reqWrapper.setId("mosip.resident.uin");
		reqWrapper.setVersion("v1");
		Mockito.when(residentService.reqUinUpdate(Mockito.any())).thenReturn(Tuples.of(new Object(), "123"));
		String requestAsString = gson.toJson(reqWrapper);
		this.mockMvc.perform(post("/req/update-uin").contentType(MediaType.APPLICATION_JSON).content(requestAsString))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("reg-admin")
	public void testUpdateUinDemographics() throws Exception {
		ResidentDemographicUpdateRequestDTO request = new ResidentDemographicUpdateRequestDTO();
		request.setTransactionID("12345");
		request.setIdentity(JsonUtil.readValue("{\"name\":\"My Name\"}", JSONObject.class));

		RequestWrapper<ResidentDemographicUpdateRequestDTO> requestDTO = new RequestWrapper<>();
		requestDTO.setRequest(request);
		requestDTO.setId("mosip.resident.demographic");
		requestDTO.setVersion("v1");

		when(identityServiceImpl.getResidentIndvidualId()).thenReturn("9876543210");
		when(residentService.reqUinUpdate(Mockito.any(), Mockito.any())).thenReturn(Tuples.of(new ResidentUpdateResponseDTO(), "12345"));
		ResponseEntity<Object> responseEntity = residentController
				.updateUinDemographics(requestDTO);
		assertEquals(new ResidentUpdateResponseDTO(), ((ResponseWrapper<Object>)responseEntity.getBody()).getResponse());
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testAuthLockStatus() throws Exception {
		ResponseWrapper<AuthLockOrUnLockRequestDtoV2> responseWrapper = new ResponseWrapper<>();
		when(identityServiceImpl.getResidentIndvidualId()).thenReturn("9876543210");
		when(residentService.getAuthLockStatus(Mockito.any())).thenReturn(responseWrapper);
		ResponseWrapper<AuthLockOrUnLockRequestDtoV2> resultRequestWrapper = residentController.getAuthLockStatus();
		assertEquals(responseWrapper, resultRequestWrapper);
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testAuthLockStatusFailed() throws Exception {
		ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.AUTH_LOCK_STATUS_FAILED.getErrorCode(),
				ResidentErrorCode.AUTH_LOCK_STATUS_FAILED.getErrorMessage())));
		responseWrapper.setResponsetime(null);

		when(identityServiceImpl.getResidentIndvidualId()).thenReturn("9876543210");
		when(residentService.getAuthLockStatus(Mockito.any()))
				.thenThrow(new ResidentServiceCheckedException("error", "error"));
		ResponseWrapper<AuthLockOrUnLockRequestDtoV2> resultRequestWrapper = residentController.getAuthLockStatus();
		resultRequestWrapper.setResponsetime(null);
		assertEquals(responseWrapper, resultRequestWrapper);
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testDownloadCardIndividualId() throws Exception {
		ResponseEntity<Object> responseEntity;
		byte[] pdfBytes = "test".getBytes(StandardCharsets.UTF_8);
		InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
		responseEntity = ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition", "attachment; filename=\"" +
						"abc" + ".pdf\"")
				.body(resource);
		ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(null);
		ResponseWrapper<Object> objectResponseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(objectResponseWrapper);
		ResponseWrapper<List<ResidentServiceHistoryResponseDto>> resultResponseWrapper = new ResponseWrapper<>();

		List<ResidentServiceHistoryResponseDto> list = new ArrayList<>();
		ResidentServiceHistoryResponseDto dto = new ResidentServiceHistoryResponseDto();
		dto.setId("12345");
		dto.setCardUrl("http://localhost:8080/mosip/resident/download-card/12345");
		dto.setRequestId("12345");
		dto.setStatusCode("200");
		list.add(dto);
		resultResponseWrapper.setResponse(list);
		resultResponseWrapper.setResponsetime(null);
		byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
		when(residentService.downloadCard(Mockito.anyString(), Mockito.anyString())).thenReturn(bytes);
		ResponseEntity<?> resultRequestWrapper = residentController
				.downloadCard("9876543210", 0);
		assertEquals(responseEntity.getStatusCode(), resultRequestWrapper.getStatusCode());
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testCheckAidStatus() throws Exception {
		AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
		aidStatusRequestDTO.setIndividualId("8251649601");
		aidStatusRequestDTO.setOtp("111111");
		aidStatusRequestDTO.setTransactionId("1234567890");
		RequestWrapper<AidStatusRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequest(aidStatusRequestDTO);
		requestWrapper.setId("mosip.resident.uin");
		requestWrapper.setVersion("1.0");
		Mockito.when(residentService.getAidStatus(Mockito.any())).thenReturn(new AidStatusResponseDTO());
		String requestAsString = gson.toJson(requestWrapper);
		this.mockMvc
				.perform(
						post("/aid/status").contentType(MediaType.APPLICATION_JSON).content(requestAsString))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testGetCredentialRequestStatusSuccess() throws Exception {
		residentController.checkAidStatus("17", "eng", 0);
		when(residentService.checkAidStatus("17")).thenReturn("PROCESSED");
		this.mockMvc.perform(get("/events/86c2ad43-e2a4-4952-bafc-d97ad1e5e453/?langCode=eng"))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("reg-admin")
	public void testGetUserInfo() throws Exception {
		UserInfoDto user = new UserInfoDto();
		user.setFullName("name");
		ResponseWrapper<UserInfoDto> response = new ResponseWrapper<>();
		response.setResponse(user);
		residentController.userinfo(0);
		Mockito.when(residentService.getUserinfo(Mockito.any(), Mockito.anyInt())).thenReturn(response);
		this.mockMvc.perform(get("/profile"))
				.andExpect(status().isOk());
	}
}
