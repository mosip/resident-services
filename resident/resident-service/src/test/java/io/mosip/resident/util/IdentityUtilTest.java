package io.mosip.resident.util;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.dto.ResponseDTO1;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentConfigService;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.mosip.resident.service.impl.IdentityServiceTest.getAuthUserDetailsFromAuthentication;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private ResidentConfigService residentConfigService;

    @Mock
    private PerpetualVidUtil perpetualVidUtil;

    @Mock
    private AvailableClaimValueUtility availableClaimValueUtility;

    private IdResponseDTO1 idResponseDTO1;

    @Before
    public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException {
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
        when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
                .thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName", "middleName", "lastName", "perpetualVID"));
        Optional<String> perpVid = Optional.of("8251649601");
        when(perpetualVidUtil.getPerpatualVid(anyString())).thenReturn(perpVid);
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetIdentityAttributesFailure() throws Exception {
        getAuthUserDetailsFromAuthentication();
        when(cachedIdentityDataUtil.getCachedIdentityData(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenThrow(new ApisResourceAccessException());
        identityUtil.getIdentityAttributes("6", "personalized-card");
    }

    @Test
    public void testGetIdentityAttributesCachedIdentityDataSuccessSecureSession() throws ResidentServiceCheckedException, IOException {
        getAuthUserDetailsFromAuthentication();
        identityUtil.getIdentityAttributes("6", "personalized-card");
    }

    @Test
    public void testGetIdentityAttributesCachedIdentityDataSuccess() throws ResidentServiceCheckedException, IOException {
        identityUtil.getIdentityAttributes("6", "personalized-card");
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetIdentityAttributesCachedIdentityDataFailure() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        idResponseDTO1.setErrors(List.of(new ServiceError(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode()
        , ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage())));
        identityUtil.getIdentityAttributes("6", "personalized-card");
    }

    @Test
    public void testGetIdentityAttributesCachedIdentityDataSuccessSchemaTypeNull() throws ResidentServiceCheckedException, IOException {
        when(environment.getProperty("resident.additional.identity.attribute.to.fetch"))
                .thenReturn("UIN,email,phone,dateOfBirth,fullName,perpetualVID");
        identityUtil.getIdentityAttributes("6", null);
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetIdentityAttributesCachedIdentityDataFailurePerpetualVid() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        when(environment.getProperty("resident.additional.identity.attribute.to.fetch"))
                .thenReturn("UIN,email,phone,dateOfBirth,fullName,perpetualVID");
        when(perpetualVidUtil.getPerpatualVid(anyString())).thenThrow(new ApisResourceAccessException());
        identityUtil.getIdentityAttributes("6", null);
    }

    @Test
    public void testGetIdentityAttributesCachedIdentityDataSuccessWithPhoto() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        when(environment.getProperty("resident.additional.identity.attribute.to.fetch"))
                .thenReturn("UIN,email,phone,dateOfBirth,fullName,perpetualVID,photo");
        when(environment.getProperty("mosip.resident.photo.attribute.name")).thenReturn("photo");
        when(environment.getProperty("mosip.resident.photo.token.claim-photo")).thenReturn("picture");
        when(availableClaimValueUtility.getAvailableClaimValue(Mockito.anyString())).thenReturn("photo");
        getAuthUserDetailsFromAuthentication();
        identityUtil.getIdentityAttributes("6", null);
    }
}
