package io.mosip.resident.test.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.GrievanceRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.repository.ResidentGrievanceRepository;
import io.mosip.resident.service.impl.GrievanceServiceImpl;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.AuditUtil;

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

import static org.junit.Assert.assertNotNull;

/**
 * This class is used to create service class test  for grievance API.
 * @Author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class GrievanceServiceImplTest {

    @InjectMocks
    private GrievanceServiceImpl grievanceService = new GrievanceServiceImpl();

    @Mock
    private Environment environment;

    @Mock
    private IdentityServiceImpl identityService;

    @Mock
    private ResidentGrievanceRepository residentGrievanceRepository;
    
    @Mock
    private AuditUtil auditUtil;

    private MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO;

    @Before
    public void setup() throws Exception {
        grievanceRequestDTOMainRequestDTO = new MainRequestDTO<>();
        GrievanceRequestDTO grievanceRequestDTO = new GrievanceRequestDTO();
        grievanceRequestDTO.setEventId("12121212121212");
        grievanceRequestDTO.setMessage("message");
        grievanceRequestDTOMainRequestDTO.setRequest(grievanceRequestDTO);
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("Kamesh");
        Mockito.when(identityService.getAvailableclaimValue(Mockito.anyString())).thenReturn("kamesh");
    }

    @Test
    public void testGetGrievanceTicket() throws ApisResourceAccessException {
        ResponseWrapper<Object> actualResult = grievanceService.getGrievanceTicket(grievanceRequestDTOMainRequestDTO);
        assertNotNull(actualResult);
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testGetGrievanceTicketFailed() throws ApisResourceAccessException {
        Mockito.when(identityService.getAvailableclaimValue(Mockito.anyString())).thenThrow(new ApisResourceAccessException());
        ResponseWrapper<Object> actualResult = grievanceService.getGrievanceTicket(grievanceRequestDTOMainRequestDTO);
        assertNotNull(actualResult);
    }
}