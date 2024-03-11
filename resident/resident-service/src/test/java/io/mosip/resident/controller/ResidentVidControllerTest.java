package io.mosip.resident.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import io.mosip.resident.util.Utility;
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
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.dto.BaseVidRequestDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentVidRequestDto;
import io.mosip.resident.dto.ResidentVidRequestDtoV2;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidRequestDtoV2;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeRequestDTOV2;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.exception.VidRevocationException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.impl.IdAuthServiceImpl;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.service.impl.ResidentVidServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.validator.RequestValidator;
import reactor.util.function.Tuples;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class ResidentVidControllerTest {
	
    @MockBean
    private ProxyIdRepoService proxyIdRepoService;

	@MockBean
	private ResidentVidServiceImpl residentVidService;

	@MockBean
	private Utility utilityBean;

	@InjectMocks
	private ResidentVidController residentVidController;

	@MockBean
	private IdAuthServiceImpl idAuthService;
	
	@MockBean
	private IdentityServiceImpl identityServiceImpl;

	@MockBean
	private ResidentServiceImpl residentService;

	@MockBean
	@Qualifier("restClientWithPlainRestTemplate")
	private ResidentServiceRestClient residentServiceRestClient;
	
	@MockBean
	private DocumentService docService;
	
	@MockBean
	private ObjectStoreHelper objectStore;

	@MockBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@Mock
	private Environment env;

	@Autowired
	private MockMvc mockMvc;

	@Mock
	private AuditUtil audit;

	@Mock
	private RequestValidator validator;

	@Before
	public void setup() throws ApisResourceAccessException {
		MockitoAnnotations.initMocks(this);
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
		Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn(null);
	}

	@Test
	@WithUserDetails("resident")
	public void vidCreationSuccessTest() throws Exception {

		VidResponseDto dto = new VidResponseDto();
		dto.setVid("12345");
		dto.setMessage("Successful");

		ResponseWrapper<VidResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);

		Mockito.when(residentVidService.generateVid(Mockito.any(), Mockito.anyString())).thenReturn(responseWrapper);

		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(getRequest());

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());// .andExpect(jsonPath("$.response.vid", is("12345")));
	}

	@Test
	@WithUserDetails("resident")
	public void otpValidationFailureTest() throws Exception {

		Mockito.when(residentVidService.generateVid(Mockito.any(), Mockito.anyString()))
				.thenThrow(new OtpValidationFailedException());

		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(getRequest());

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-422")));
	}

	@Test
	@WithUserDetails("resident")
	public void vidCreationFailureTest() throws Exception {

		Mockito.when(residentVidService.generateVid(Mockito.any(), Mockito.anyString()))
				.thenThrow(new VidCreationException());

		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(getRequest());

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-406")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidId() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.setId(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidVersion() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.setVersion(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidRequest() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.setRequest(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidIndividualId() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.getRequest().setIndividualId(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidTransactionId() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.getRequest().setTransactionID(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidOtp() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.getRequest().setOtp(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("resident")
	public void vidRevokingSuccessTest() throws Exception {
		VidRevokeResponseDTO dto = new VidRevokeResponseDTO();
		dto.setMessage("Successful");
		ResponseWrapper<VidRevokeResponseDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);

		Mockito.when(residentVidService.revokeVid(Mockito.any(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(responseWrapper);

		residentVidController.revokeVid(getRevokeRequest(), "123457987765422");
	}

	@Test
	@WithUserDetails("resident")
	public void vidRevokingFailureTest2() throws Exception {

		Mockito.when(residentVidService.revokeVid(Mockito.any(VidRevokeRequestDTO.class), Mockito.anyString(), Mockito.anyString()))
				.thenThrow(new VidRevocationException());

		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(getRevokeRequest());

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310541")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-407")));

	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidIdRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.setId(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310541")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidVersionRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.setVersion(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310541")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidRequestRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.setRequest(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310541")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidVidStatusRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.getRequest().setVidStatus(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-471")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidIndividualIdTypeRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidIndividualIdRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.getRequest().setIndividualId(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310541")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidTransactionIdRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.getRequest().setTransactionID(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310541")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidOtpRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.getRequest().setOtp(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310541")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-410")));
	}

	private static ResidentVidRequestDto getRequest() {
		VidRequestDto vidRequestDto = new VidRequestDto();
		vidRequestDto.setIndividualId("9072037081");
		vidRequestDto.setOtp("974436");
		vidRequestDto.setTransactionID("1111122222");
		vidRequestDto.setVidType("Temporary");

		ResidentVidRequestDto request = new ResidentVidRequestDto();
		request.setId("mosip.resident.vid");
		request.setVersion("v1");

		request.setRequesttime(DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
		request.setRequest(vidRequestDto);
		return request;
	}

	private static RequestWrapper<VidRevokeRequestDTO> getRevokeRequest() {
		VidRevokeRequestDTO vidRevokeRequestDTO = new VidRevokeRequestDTO();
		vidRevokeRequestDTO.setIndividualId("2038096257310540");
		vidRevokeRequestDTO.setOtp("974436");
		vidRevokeRequestDTO.setTransactionID("1111122222");
		vidRevokeRequestDTO.setVidStatus("REVOKED");

		RequestWrapper request = new RequestWrapper();
		request.setId("mosip.resident.vidstatus");
		request.setVersion("v1");
		request.setRequesttime(DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
		request.setRequest(vidRevokeRequestDTO);
		return request;
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testGetVidPolicy() throws Exception {
		when(residentVidService.getVidPolicy()).thenReturn("policy");
		this.mockMvc.perform(get("/vid/policy")).andExpect(status().isOk()).andDo(print());
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testGetVidPolicyFailed() throws Exception {
		when(residentVidService.getVidPolicy()).thenThrow(new ResidentServiceCheckedException());
		this.mockMvc.perform(get("/vid/policy")).andExpect(status().isOk()).andDo(print())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-426")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void vidCreationV2SuccessTest() throws Exception {

		VidResponseDto dto = new VidResponseDto();
		dto.setVid("12345");
		dto.setMessage("Successful");

		ResponseWrapper<VidResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);

		Mockito.when(residentVidService.generateVid(Mockito.any(BaseVidRequestDto.class), any())).thenReturn(responseWrapper);

		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(getRequest());

		this.mockMvc.perform(post("/generate-vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());// .andExpect(jsonPath("$.response.vid", is("12345")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void vidRevokingV2SuccessTest() throws Exception {
		Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("12345678");
		VidRevokeResponseDTO dto = new VidRevokeResponseDTO();
		dto.setMessage("Successful");

		ResponseWrapper<VidRevokeResponseDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);

		Mockito.when(residentVidService.revokeVidV2(Mockito.any(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Tuples.of(responseWrapper, "12345"));
		
		VidRevokeRequestDTOV2 vidRevokeRequestDTOV2 = new VidRevokeRequestDTOV2();
		vidRevokeRequestDTOV2.setTransactionID("1234567890");
		vidRevokeRequestDTOV2.setVidStatus("revoked");
		RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
		requestDto.setRequest(vidRevokeRequestDTOV2);
		residentVidController.revokeVidV2(requestDto, "1234567432456");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	@WithUserDetails("reg-admin")
	public void testRevokeVidV2() throws Exception {
		Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("1234567432456");
		VidRevokeRequestDTOV2 vidRevokeRequestDTOV2 = new VidRevokeRequestDTOV2();
		vidRevokeRequestDTOV2.setTransactionID("1234567890");
		vidRevokeRequestDTOV2.setVidStatus("revoked");
		RequestWrapper<VidRevokeRequestDTOV2> requestDto = new RequestWrapper<>();
		requestDto.setRequest(vidRevokeRequestDTOV2);
		residentVidController.revokeVidV2(requestDto, "1234567432456");
	}

	@Test
	@WithUserDetails("reg-admin")
	public void testRetrieveVids() throws Exception {
		ResponseWrapper<List<Map<String, ?>>> responseWrapper = new ResponseWrapper<>();
		Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("12345678");
		Mockito.when(residentVidService.retrieveVids(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
				.thenReturn(responseWrapper);
		residentVidController.retrieveVids(0, "En-us");
	}

	@Test(expected = Exception.class)
	@WithUserDetails("reg-admin")
	public void testRetrieveVidsWithException() throws Exception {
		Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("12345678");
		Mockito.when(residentVidService.retrieveVids(Mockito.anyString(), Mockito.anyInt(), Mockito.nullable(String.class)))
				.thenThrow(new ApisResourceAccessException());
		residentVidController.retrieveVids(0, "En-us");
	}

	@Test
	@WithUserDetails("resident")
	public void testGenerateVidV2() throws Exception {
		Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("12345678");
		VidResponseDto dto = new VidResponseDto();
		dto.setVid("12345");
		dto.setMessage("Successful");
		ResponseWrapper<VidResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);

		Mockito.when(residentVidService.generateVidV2(Mockito.any(), Mockito.anyString())).thenReturn(Tuples.of(responseWrapper, "12345"));

		VidRequestDtoV2 vidRequestDtoV2 = new VidRequestDtoV2();
		vidRequestDtoV2.setTransactionID("1234567890");
		vidRequestDtoV2.setVidType("perpetual");
		vidRequestDtoV2.setChannels(List.of("email"));
		ResidentVidRequestDtoV2 requestDto = new ResidentVidRequestDtoV2();
		requestDto.setRequest(vidRequestDtoV2);
		residentVidController.generateVidV2(requestDto);
	}
}
