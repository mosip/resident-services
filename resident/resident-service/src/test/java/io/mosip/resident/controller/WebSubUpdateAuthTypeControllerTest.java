package io.mosip.resident.controller;

import java.time.LocalDateTime;
import java.util.Map;

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
import io.mosip.resident.service.WebSubUpdateAuthTypeService;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;

/**
 * Web-Sub Update Controller Test Note: This class is used to test the Web-Sub
 * Update Controller
 * 
 * @author Kamesh Shekhar Prasad
 */

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class WebSubUpdateAuthTypeControllerTest {

	@Mock
	private AuditUtil audit;

	@InjectMocks
	WebSubUpdateAuthTypeController webSubUpdateAuthTypeController;

	@Mock
	WebSubUpdateAuthTypeService webSubUpdateAuthTypeService;

	@Autowired
	private MockMvc mockMvc;

	@Mock
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

	@Test
	public void testAuthTypeCallbackWithException() throws Exception {
		EventModel eventModel = new EventModel();
		webSubUpdateAuthTypeController.authTypeCallback(objectMapper.convertValue(eventModel, Map.class));
	}
}
