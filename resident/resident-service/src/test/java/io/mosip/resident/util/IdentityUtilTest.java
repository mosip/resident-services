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
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.mosip.resident.service.impl.IdentityServiceTest.getAuthUserDetailsFromAuthentication;
import static org.junit.Assert.assertEquals;
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

    @Mock
    private MaskDataUtility maskDataUtility;

    @Mock
    private Utility utility;

    @Mock
    private ClaimValueUtility claimValueUtility;

    private IdResponseDTO1 idResponseDTO1;
    private String uin;

    @Before
    public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException {
        Map identityMap = new LinkedHashMap();
        uin = "8251649601";
        identityMap.put("UIN", uin);
        identityMap.put("email", "manojvsp12@gmail.com");
        identityMap.put("phone", "9395910872");
        identityMap.put("dateOfBirth", "1970/11/16");
        identityMap.put("masked", "1970/11/16");
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
        assertEquals(uin, identityUtil.getIdentityAttributes("6", "personalized-card").get("UIN"));
    }

    @Test
    public void testGetIdentityAttributesCachedIdentityDataSuccess() throws ResidentServiceCheckedException, IOException {
        assertEquals(uin, identityUtil.getIdentityAttributes("6", "personalized-card").get("UIN"));
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
        assertEquals(uin, identityUtil.getIdentityAttributes("6", null).get("UIN"));
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
        assertEquals(uin, identityUtil.getIdentityAttributes("6", null).get("UIN"));
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetIdentityAttributesCachedIdentityDataFailureWithPhoto() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        when(environment.getProperty("resident.additional.identity.attribute.to.fetch"))
                .thenReturn("UIN,email,phone,dateOfBirth,fullName,perpetualVID,photo");
        when(environment.getProperty("mosip.resident.photo.attribute.name")).thenReturn("photo");
        when(environment.getProperty("mosip.resident.photo.token.claim-photo")).thenReturn("picture");
        when(availableClaimValueUtility.getAvailableClaimValue(Mockito.anyString())).thenThrow(new ApisResourceAccessException());
        getAuthUserDetailsFromAuthentication();
        identityUtil.getIdentityAttributes("6", null).get("UIN");
    }

    @Test
    public void testGetIdentityAttributesCachedIdentityDataSuccessWithMaskedAttribute() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        when(environment.getProperty("resident.additional.identity.attribute.to.fetch"))
                .thenReturn("UIN,email,phone,dateOfBirth,fullName,perpetualVID,photo,masked_email");
        when(maskDataUtility.convertToMaskData(Mockito.anyString())).thenReturn("8**3");
        getAuthUserDetailsFromAuthentication();
        assertEquals(uin, identityUtil.getIdentityAttributes("6", null).get("UIN"));
    }

    @Test
    public void testGetIdentity() throws ResidentServiceCheckedException, IOException {
        when(utility.getMappingValue(Mockito.anyMap(), Mockito.anyString())).thenReturn("2016/02/02");
        ReflectionTestUtils.setField(identityUtil, "dateFormat", "yyyy/MM/dd");
        assertEquals("1970/11/16", identityUtil.getIdentity(uin).getDateOfBirth());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetIdentityFailure() throws ResidentServiceCheckedException, IOException {
        when(utility.getMappingValue(Mockito.anyMap(), Mockito.anyString())).thenThrow(new IOException());
        identityUtil.getIdentity(uin).getDateOfBirth();
    }

    @Test
    public void testGetIdentityWithFetchFace() throws ResidentServiceCheckedException, IOException {
        when(utility.getMappingValue(Mockito.anyMap(), Mockito.anyString())).thenReturn("2016/02/02");
        ReflectionTestUtils.setField(identityUtil, "dateFormat", "yyyy/MM/dd");
        assertEquals("1970/11/16", identityUtil.getIdentity(uin, true, "eng").getDateOfBirth());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetIdentityWithFetchFaceFailure() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        when(utility.getMappingValue(Mockito.anyMap(), Mockito.anyString())).thenReturn("2016/02/02");
        when(environment.getProperty("mosip.resident.photo.token.claim-photo")).thenReturn("photo");
        ReflectionTestUtils.setField(identityUtil, "dateFormat", "yyyy/MM/dd");
        when(claimValueUtility.getClaimValue(Mockito.anyString())).thenThrow(new ApisResourceAccessException());
        assertEquals("1970/11/16", identityUtil.getIdentity(uin, true, "eng").getDateOfBirth());
    }
}
