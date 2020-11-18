package io.mosip.resident.test.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentVidRequestDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.exception.VidRevocationException;
import io.mosip.resident.service.impl.IdAuthServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.service.impl.ResidentVidServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class ResidentVidControllerTest {

	private static final String JSON_STRING_RESPONSE = "";

	@MockBean
	private ResidentVidServiceImpl residentVidService;

	@MockBean
	private IdAuthServiceImpl idAuthService;

	@MockBean
	private ResidentServiceImpl residentService;

	@MockBean
	private ResidentServiceRestClient residentServiceRestClient;

	@MockBean
	private TokenGenerator tokenGenerator;

	@Mock
	private Environment env;

	@Autowired
	private MockMvc mockMvc;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@WithUserDetails("resident")
	public void vidCreationSuccessTest() throws Exception {

		VidResponseDto dto = new VidResponseDto();
		dto.setVid("12345");
		dto.setMessage("Successful");

		ResponseWrapper<VidResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);

		Mockito.when(residentVidService.generateVid(Mockito.any(VidRequestDto.class))).thenReturn(responseWrapper);

		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(getRequest());

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.vid", is("12345")));
	}

	@Test
	@WithUserDetails("resident")
	public void otpValidationFailureTest() throws Exception {

		Mockito.when(residentVidService.generateVid(Mockito.any(VidRequestDto.class)))
				.thenThrow(new OtpValidationFailedException());

		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(getRequest());

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-004")));
	}

	@Test
	@WithUserDetails("resident")
	public void vidCreationFailureTest() throws Exception {

		Mockito.when(residentVidService.generateVid(Mockito.any(VidRequestDto.class)))
				.thenThrow(new VidCreationException());

		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(getRequest());

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-007")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidId() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.setId(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidVersion() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.setVersion(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidRequest() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.setRequest(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidVidType() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.getRequest().setVidType(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidIndividualIdType() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.getRequest().setIndividualIdType(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidIndividualId() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.getRequest().setIndividualId(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidTransactionId() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.getRequest().setTransactionID(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("resident")
	public void invalidOtp() throws Exception {

		ResidentVidRequestDto request = getRequest();
		request.getRequest().setOtp(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("resident")
	public void vidRevokingSuccessTest() throws Exception {

		VidRevokeResponseDTO dto = new VidRevokeResponseDTO();
		dto.setMessage("Successful");

		ResponseWrapper<VidRevokeResponseDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);

		Mockito.when(residentVidService.revokeVid(Mockito.any(VidRevokeRequestDTO.class), Mockito.anyString()))
				.thenReturn(responseWrapper);

		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(getRevokeRequest());

		RequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540").content(json)
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk());
				//.andExpect(jsonPath("$.response.message", is("Successful")));

	}

	@Test
	@WithUserDetails("resident")
	public void vidRevokingFailureTest() throws Exception {

		Mockito.when(residentVidService.revokeVid(Mockito.any(VidRevokeRequestDTO.class), Mockito.anyString()))
				.thenThrow(new VidRevocationException());

		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(getRevokeRequest());

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-RID-005")));

	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidIdRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.setId(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidVersionRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.setVersion(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidRequestRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.setRequest(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
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
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidIndividualIdTypeRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.getRequest().setIndividualIdType(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidIndividualIdRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.getRequest().setIndividualId(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidTransactionIdRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.getRequest().setTransactionID(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	@Test
	@WithUserDetails("reg-admin")
	public void invalidOtpRevokeTest() throws Exception {

		RequestWrapper<VidRevokeRequestDTO> request = getRevokeRequest();
		request.getRequest().setOtp(null);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(request);

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/vid/{vid}", "2038096257310540")
				.content(json).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
				.characterEncoding("UTF-8");

		this.mockMvc.perform(builder).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("RES-SER-009")));
	}

	private static ResidentVidRequestDto getRequest() {
		VidRequestDto vidRequestDto = new VidRequestDto();
		vidRequestDto.setIndividualId("9072037081");
		vidRequestDto.setIndividualIdType(IdType.UIN.name());
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
		vidRevokeRequestDTO.setIndividualIdType(IdType.VID.name());
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

}
