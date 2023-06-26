package io.mosip.resident.controller;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.websub.model.Event;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.VerificationService;
import io.mosip.resident.service.WebSubUpdateAuthTypeService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;

/**
 * Web-Sub Update Controller Test Note: This class is used to test the Web-Sub
 * Update Controller
 * 
 * @author Kamesh Shekhar Prasad
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class WebSubUpdateAuthTypeControllerTest {

	@MockBean
	private ProxyIdRepoService proxyIdRepoService;

	@MockBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@Mock
	private AuditUtil audit;

	@InjectMocks
	WebSubUpdateAuthTypeController webSubUpdateAuthTypeController;

	@MockBean
	WebSubUpdateAuthTypeService webSubUpdateAuthTypeService;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VerificationService verificationService;

	@MockBean
	private IdAuthService idAuthService;

	@MockBean
	private ResidentVidService vidService;

	@MockBean
	private DocumentService docService;

	@MockBean
	private ObjectStoreHelper objectStore;

	@MockBean
	VerificationController verificationController;

	@MockBean
	private ResidentServiceImpl residentService;

	@Autowired
	private ObjectMapper objectMapper;

	@Before
	public void setup() throws Exception {

		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(webSubUpdateAuthTypeController).build();
	}

	@Test
	public void testCreateRequestGenerationSuccess() throws Exception {

		EventModel eventModel = new EventModel();
		Event event = new Event();
		event.setTransactionId("1234");
		event.setId("8251649601");
		Map<String, Object> partnerIdMap = new java.util.HashMap<>();
		partnerIdMap.put("olv_partner_id", "mpartner-default-auth");
		event.setData(partnerIdMap);

		eventModel.setEvent(event);
		eventModel.setTopic("AUTH_TYPE_STATUS_UPDATE_ACK");
		eventModel.setPublishedOn(String.valueOf(LocalDateTime.now()));
		eventModel.setPublisher("AUTH_TYPE_STATUS_UPDATE_ACK");
		webSubUpdateAuthTypeController.authTypeCallback(objectMapper.convertValue(eventModel, Map.class));

		mockMvc.perform((MockMvcRequestBuilders.post("/callback/authTypeCallback"))
				.contentType(MediaType.APPLICATION_JSON).content(eventModel.toString())).andReturn();
	}

	@Test(expected = ResidentServiceException.class)
	public void testAuthTypeCallbackWithException() throws Exception {
		EventModel eventModel = new EventModel();
		doThrow(new ResidentServiceCheckedException()).when(webSubUpdateAuthTypeService).updateAuthTypeStatus(anyMap());
		webSubUpdateAuthTypeController.authTypeCallback(objectMapper.convertValue(eventModel, Map.class));
	}
}
