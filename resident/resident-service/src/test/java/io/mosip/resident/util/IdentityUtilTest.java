package io.mosip.resident.util;

import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.dto.ResponseDTO1;
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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private IdResponseDTO1 idResponseDTO1;

    @Before
    public void setup() throws ApisResourceAccessException {
        Map identityMap = new LinkedHashMap();
        identityMap.put("UIN", "8251649601");
        identityMap.put("email", "manojvsp12@gmail.com");
        identityMap.put("phone", "9395910872");
        identityMap.put("dateOfBirth", "1970/11/16");
        idResponseDTO1 = new IdResponseDTO1();
        ResponseDTO1 responseDTO1 = new ResponseDTO1();
        responseDTO1.setIdentity(identityMap);
        idResponseDTO1.setResponse(responseDTO1);
        when(environment.getProperty("resident.additional.identity.attribute.to.fetch"))
                .thenReturn("UIN,email,phone,dateOfBirth,fullName");
        when(accessTokenUtility.getAccessToken()).thenReturn("token");
        when(cachedIdentityDataUtil.getCachedIdentityData(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(idResponseDTO1);
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetIdentityAttributesFailure() throws Exception {
        getAuthUserDetailsFromAuthentication();
        when(cachedIdentityDataUtil.getCachedIdentityData(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenThrow(new ApisResourceAccessException());
        identityUtil.getIdentityAttributes("6", "personalized-card");
    }

    @Test
    public void testGetIdentityAttributesCachedIdentityData() throws ApisResourceAccessException, ResidentServiceCheckedException, IOException {
        getAuthUserDetailsFromAuthentication();
        identityUtil.getIdentityAttributes("6", "personalized-card");
    }
}
