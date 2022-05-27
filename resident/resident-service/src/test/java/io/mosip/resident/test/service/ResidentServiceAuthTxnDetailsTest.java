package io.mosip.resident.test.service;

import io.mosip.resident.controller.ResidentController;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.impl.PartnerServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.validator.RequestValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentServiceAuthTxnDetailsTest {

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    Environment env;

    @Mock
    private AuditUtil audit;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private ResidentServiceImpl residentServiceImpl;

    @InjectMocks
    ResidentController residentController;

    @Mock
    private RequestValidator validator;

    @Mock
    private PartnerServiceImpl partnerServiceImpl;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(residentServiceImpl).build();
    }

    @Test
    public void TestgetTxnDetails() throws ResidentServiceCheckedException {
        String individualId = "8251649601";
        Integer pageStart = 1;
        Integer pageSize = 1;

        assertEquals(0, residentServiceImpl.getAuthTxnDetails(individualId, pageStart, pageSize, "UIN").size());
    }


}
