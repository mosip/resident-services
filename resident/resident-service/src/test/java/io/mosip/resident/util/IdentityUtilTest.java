package io.mosip.resident.util;

import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import static io.mosip.resident.service.impl.IdentityServiceTest.getAuthUserDetailsFromAuthentication;
import static org.mockito.Mockito.when;

/**
 * @author Kamesh Shekhar Prasad
 */

@RunWith(MockitoJUnitRunner.class)
public class IdentityUtilTest {

    @InjectMocks
    private IdentityUtil identityUtil = new IdentityUtil();

    @Mock
    private CachedIdentityDataUtil cachedIdentityDataUtil;

    @Mock
    private Environment environment;

    @Mock
    private AccessTokenUtility accessTokenUtility;

    @Before
    public void setup(){
        when(environment.getProperty("resident.additional.identity.attribute.to.fetch"))
                .thenReturn("UIN,email,phone,dateOfBirth,fullName");
        when(accessTokenUtility.getAccessToken()).thenReturn("token");
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetIdentityAttributesFailure() throws Exception {
        getAuthUserDetailsFromAuthentication();
        when(cachedIdentityDataUtil.getCachedIdentityData(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenThrow(new ApisResourceAccessException());
        identityUtil.getIdentityAttributes("6", "personalized-card");
    }
}
