package io.mosip.resident.test.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.AutnTxnRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.PartnerServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
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
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class PartnerServiceImplTest {

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    Environment env;

    @Mock
    private AuditUtil audit;

    @InjectMocks
    private PartnerServiceImpl partnerService;

    @Mock
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    private AutnTxnRepository autnTxnRepository;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PublisherClient<String, Object, HttpHeaders> publisher;

    @Mock
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    private ResponseWrapper<?> responseWrapper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(partnerService).build();
        responseWrapper = new ResponseWrapper<>();
        responseWrapper.setVersion("v1");
        responseWrapper.setId("1");
    }

    @Test
    public void testPartnerService() throws ResidentServiceCheckedException, ApisResourceAccessException {
        String partnerId = "Online_Verification_Partner";
        ArrayList<String> partnerIds = new ArrayList<>();
        URI uri = URI.create("http://localhost:8080/v1/partner/");
        responseWrapper = residentServiceRestClient.getApi(uri, ResponseWrapper.class);

        partnerService.getPartnerDetails(partnerId);
        assertEquals(0, partnerIds.size());
    }
}