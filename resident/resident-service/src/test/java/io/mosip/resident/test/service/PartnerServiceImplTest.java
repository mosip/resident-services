package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.PartnerServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;

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

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PublisherClient<String, Object, HttpHeaders> publisher;

    @Mock
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    private ResponseWrapper<Map<String, Object>> responseWrapper;

    @Before
    public void setup() {
        responseWrapper = new ResponseWrapper<Map<String, Object>>();
        responseWrapper.setVersion("v1");
        responseWrapper.setId("1");
        Map<String, Object> partners = new HashMap<>();
        ArrayList<Object> partnerList = new ArrayList<>();
        Map<String, Object> individualPartner = new HashMap<>();
        individualPartner.put("partnerID", "1");
        partnerList.add(individualPartner);
        partners.put("partners", partnerList);
        responseWrapper.setResponse(partners);
    }

    @Test
    public void testPartnerService() throws ResidentServiceCheckedException, ApisResourceAccessException, URISyntaxException {
        String partnerId = "Online_Verification_Partner";
        ArrayList<String> partnerIds;

        ReflectionTestUtils.setField(partnerService, "partnerServiceUrl", "https://dev.mosip.net/v1/partnermanager/partners?partnerType=Online_Verification_Partner");
        URI uri = new URI("https://dev.mosip.net/v1/partnermanager/partners?partnerType=Online_Verification_Partner");
        when(residentServiceRestClient.getApi(uri, ResponseWrapper.class))
                .thenReturn(responseWrapper);
        partnerIds=partnerService.getPartnerDetails(partnerId);
        assertEquals(1, partnerIds.size());
    }
}