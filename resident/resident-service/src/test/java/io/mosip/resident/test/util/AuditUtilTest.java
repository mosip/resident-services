package io.mosip.resident.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.net.InetAddress;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.AuditRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.AuditResponseDto;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author Abubacker Siddik
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({SecurityContextHolder.class, InetAddress.class, DateUtils.class})
public class AuditUtilTest {

    @InjectMocks
    private AuditUtil auditUtil;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Environment environment;

    @Mock
    private IdentityServiceImpl identityService;

    @Captor
    ArgumentCaptor<HttpEntity> httpEntityCaptor;

    @Captor
    ArgumentCaptor<String> stringCaptor;

    private InetAddress host;

    private LocalDateTime localDateTime;

    private String auditUrl = "https://qa.mosip.net/v1/auditmanager/audits";

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(auditUtil, "auditUrl", auditUrl);

        PowerMockito.mockStatic(SecurityContextHolder.class);
        PowerMockito.mockStatic(InetAddress.class);
        PowerMockito.mockStatic(DateUtils.class);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user1", "password", null);
        when(SecurityContextHolder.getContext()).thenReturn(new SecurityContextImpl(token));

        host = InetAddress.getLocalHost();
        when(InetAddress.getLocalHost()).thenReturn(host);

        localDateTime = DateUtils.getUTCCurrentDateTime();
        when(DateUtils.getUTCCurrentDateTime()).thenReturn(localDateTime);
        when(identityService.getAvailableclaimValue(Mockito.anyString())).thenReturn("user1");
        when(environment.getProperty(Mockito.anyString())).thenReturn("user1");
    }

    @Test
    public void setAuditRequestDtoTest() throws Exception {
        EventEnum eventEnum = EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "get Rid status API");
        AuditResponseDto auditResponseDto = new AuditResponseDto();
        auditResponseDto.setStatus(true);
        ResponseWrapper<AuditResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(auditResponseDto);
        String responseString = "Response String";
        ResponseEntity<String> response = ResponseEntity.ok(responseString);

        AuditRequestDTO auditRequestDto = new AuditRequestDTO();
		auditRequestDto.setId("9054257143");
		auditRequestDto.setIdType("UIN");
        RequestWrapper<AuditRequestDTO> auditRequestWrapper = new RequestWrapper<>();
        auditRequestWrapper.setRequest(auditRequestDto);
        when(restTemplate.exchange(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class), Mockito.any(Object.class))).thenReturn(response);
        when(objectMapper.readValue(Mockito.anyString(), Mockito.any(TypeReference.class))).thenReturn(responseWrapper);
		String individualId = "9054257143";
		String refIdType = "UIN";
		Mockito.when(identityService.getResidentIndvidualIdFromSession()).thenReturn(individualId);
		Mockito.when(identityService.getIndividualIdType(individualId)).thenReturn(refIdType);
		
        auditUtil.setAuditRequestDto(eventEnum);

        verify(restTemplate, times(1)).exchange(stringCaptor.capture(), Mockito.any(), httpEntityCaptor.capture(), Mockito.any(Class.class), Mockito.any(Object.class));
        final HttpEntity<RequestWrapper<AuditRequestDTO>> httpEntity = httpEntityCaptor.getValue();
        final String auditUrlInput = stringCaptor.getValue();

        assertNotNull("Response Body is not null", httpEntity.getBody());

        assertEquals(eventEnum.getApplicationId(), httpEntity.getBody().getRequest().getApplicationId());
        assertEquals(eventEnum.getApplicationName(), httpEntity.getBody().getRequest().getApplicationName());
        assertEquals(eventEnum.getDescription(), httpEntity.getBody().getRequest().getDescription());
        assertEquals(eventEnum.getType(), httpEntity.getBody().getRequest().getEventType());
        assertEquals(eventEnum.getName(), httpEntity.getBody().getRequest().getEventName());
        assertEquals(eventEnum.getEventId(), httpEntity.getBody().getRequest().getEventId());
        assertEquals(eventEnum.getModuleId(), httpEntity.getBody().getRequest().getModuleId());
        assertEquals(eventEnum.getModuleName(), httpEntity.getBody().getRequest().getModuleName());
		assertEquals("07DDDD711B7311BAE05A09F36479BAF78EA4FF1B91603A9704A2D59206766308",
				httpEntity.getBody().getRequest().getId());
		assertEquals("UIN", httpEntity.getBody().getRequest().getIdType());

        assertEquals(host.getHostName(), httpEntity.getBody().getRequest().getHostName());
        assertEquals(host.getHostAddress(), httpEntity.getBody().getRequest().getHostIp());

        assertEquals("user1", httpEntity.getBody().getRequest().getSessionUserId());
        assertEquals("user1", httpEntity.getBody().getRequest().getSessionUserName());
        assertEquals("user1", httpEntity.getBody().getRequest().getCreatedBy());

        assertEquals(localDateTime, httpEntity.getBody().getRequest().getActionTimeStamp());

        assertEquals(auditUrlInput, auditUrl);

    }
    
	@Test
	public void testGetRefIdandTypeNoID() {
		Tuple2<String, String> response = auditUtil.getRefIdandType();
		assertEquals(Tuples.of(ResidentConstants.NO_ID, ResidentConstants.NO_ID_TYPE), response);
	}

	@Test
	public void testGetRefIdandType() throws ApisResourceAccessException {
		String individualId = "9054257143";
		String refIdType = "UIN";
		Mockito.when(identityService.getResidentIndvidualIdFromSession()).thenReturn(individualId);
		Mockito.when(identityService.getIndividualIdType(individualId)).thenReturn(refIdType);
		Tuple2<String, String> refIdandType = auditUtil.getRefIdandType();
		assertEquals(Tuples.of("07DDDD711B7311BAE05A09F36479BAF78EA4FF1B91603A9704A2D59206766308", "UIN"),
				refIdandType);
	}

}
