package io.mosip.resident.controller;

import java.time.LocalDateTime;
import java.util.Map;

import io.mosip.resident.util.AuditUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.websub.model.Event;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.service.WebSubCredentialStatusUpdateService;
import io.mosip.resident.test.ResidentTestBootApplication;

/**
 * Web-Sub Credential Status Update Controller Test Note: This class is used to
 * test the Web-Sub Credential Status Update Controller
 * 
 * @author Ritik Jain
 */

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class WebSubCredentialStatusUpdateControllerTest {

	@InjectMocks
	WebSubCredentialStatusUpdateController webSubCredentialStatusUpdateController;

	@Mock
	WebSubCredentialStatusUpdateService webSubCredentialStatusUpdateService;

	@Autowired
	private MockMvc mockMvc;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private AuditUtil auditUtil;

	@Before
	public void setup() throws Exception {

		MockitoAnnotations.initMocks(this);

		this.mockMvc = MockMvcBuilders.standaloneSetup(webSubCredentialStatusUpdateController).build();
	}

	@Test
	public void testCredentialStatusUpdateCallback() throws Exception {

		EventModel eventModel = new EventModel();
		Event event = new Event();
		event.setTransactionId("1234");
		event.setId("8251649601");
		Map<String, Object> partnerIdMap = new java.util.HashMap<>();
		partnerIdMap.put("olv_partner_id", "mpartner-default-auth");
		event.setData(partnerIdMap);

		eventModel.setEvent(event);
		eventModel.setTopic("CREDENTIAL_STATUS_UPDATE_CALL_BACK");
		eventModel.setPublishedOn(String.valueOf(LocalDateTime.now()));
		eventModel.setPublisher("CREDENTIAL_STATUS_UPDATE_CALL_BACK");
		webSubCredentialStatusUpdateController
				.credentialStatusUpdateCallback(objectMapper.convertValue(eventModel, Map.class));

		mockMvc.perform((MockMvcRequestBuilders.post("/callback/credentialStatusUpdate"))
				.contentType(MediaType.APPLICATION_JSON).content(eventModel.toString())).andReturn();
	}

	@Test
	public void testCredentialStatusUpdateCallbackWithException() throws Exception {
		MockitoAnnotations.openMocks(this);
		EventModel eventModel = new EventModel();
		webSubCredentialStatusUpdateController
				.credentialStatusUpdateCallback(objectMapper.convertValue(eventModel, Map.class));
	}
}
