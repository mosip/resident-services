package io.mosip.resident.util;

import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AvailableClaimUtility
 */
@RunWith(MockitoJUnitRunner.class)
public class AvailableClaimUtilityTest {

    private AvailableClaimUtility availableClaimUtility;

    @Mock
    private TokenIDGenerator tokenIDGenerator;

    @Mock
    private UinForIndividualId uinForIndividualId;

    @Mock
    private ClaimValueUtility claimValueUtility;

    @Before
    public void setUp() {
        availableClaimUtility = new AvailableClaimUtility();

        // inject mocks and @Value field
        ReflectionTestUtils.setField(availableClaimUtility, "tokenIDGenerator", tokenIDGenerator);
        ReflectionTestUtils.setField(availableClaimUtility, "uinForIndividualId", uinForIndividualId);
        ReflectionTestUtils.setField(availableClaimUtility, "claimValueUtility", claimValueUtility);
        ReflectionTestUtils.setField(availableClaimUtility, "onlineVerificationPartnerId", "DEFAULT_OLV");
    }

    @Test
    public void testGetIDAToken_usesDefaultPartner() {
        String uin = "UIN123";
        String expected = "TOKEN-DEF";

        when(tokenIDGenerator.generateTokenID(eq(uin), eq("DEFAULT_OLV"))).thenReturn(expected);

        String actual = availableClaimUtility.getIDAToken(uin);

        assertEquals(expected, actual);
        verify(tokenIDGenerator, times(1)).generateTokenID(eq(uin), eq("DEFAULT_OLV"));
    }

    @Test
    public void testGetIDAToken_withExplicitPartner() {
        String uin = "UIN-EX";
        String partner = "OLV-X";
        String expected = "TOKEN-X";

        when(tokenIDGenerator.generateTokenID(eq(uin), eq(partner))).thenReturn(expected);

        String actual = availableClaimUtility.getIDAToken(uin, partner);

        assertEquals(expected, actual);
        verify(tokenIDGenerator, times(1)).generateTokenID(eq(uin), eq(partner));
    }

    @Test
    public void testGetIDATokenForIndividualId_success() throws ResidentServiceCheckedException {
        String individualId = "IND-1";
        String uin = "UIN-1";
        String token = "TOKEN-1";

        when(uinForIndividualId.getUinForIndividualId(eq(individualId))).thenReturn(uin);
        when(tokenIDGenerator.generateTokenID(eq(uin), eq("DEFAULT_OLV"))).thenReturn(token);

        String actual = availableClaimUtility.getIDATokenForIndividualId(individualId);

        assertEquals(token, actual);
        verify(uinForIndividualId, times(1)).getUinForIndividualId(eq(individualId));
        verify(tokenIDGenerator, times(1)).generateTokenID(eq(uin), eq("DEFAULT_OLV"));
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetIDATokenForIndividualId_whenUinServiceThrows_shouldPropagate() throws ResidentServiceCheckedException {
        String individualId = "IND-ERR";

        when(uinForIndividualId.getUinForIndividualId(eq(individualId)))
                .thenThrow(new ResidentServiceCheckedException("ERR_CODE", "msg"));

        // should propagate ResidentServiceCheckedException
        availableClaimUtility.getIDATokenForIndividualId(individualId);
    }

    @Test
    public void testGetResidentIdaToken_success() throws ApisResourceAccessException, ResidentServiceCheckedException {
        String sessionIndividualId = "IND-SESSION";
        String uin = "UIN-S";
        String token = "TOKEN-S";

        when(claimValueUtility.getClaimValue(eq("individual_id"))).thenReturn(sessionIndividualId);
        when(uinForIndividualId.getUinForIndividualId(eq(sessionIndividualId))).thenReturn(uin);
        when(tokenIDGenerator.generateTokenID(eq(uin), eq("DEFAULT_OLV"))).thenReturn(token);

        String actual = availableClaimUtility.getResidentIdaToken();

        assertEquals(token, actual);

        verify(claimValueUtility, times(1)).getClaimValue(eq("individual_id"));
        verify(uinForIndividualId, times(1)).getUinForIndividualId(eq(sessionIndividualId));
        verify(tokenIDGenerator, times(1)).generateTokenID(eq(uin), eq("DEFAULT_OLV"));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testGetResidentIdaToken_whenClaimThrowsApisException_shouldPropagate() throws ApisResourceAccessException, ResidentServiceCheckedException {
        when(claimValueUtility.getClaimValue(eq("individual_id")))
                .thenThrow(new ApisResourceAccessException("claim error"));

        availableClaimUtility.getResidentIdaToken();
    }

    @Test
    public void testGetResidentIndvidualIdFromSession_success() throws ApisResourceAccessException {
        when(claimValueUtility.getClaimValue(eq("individual_id"))).thenReturn("IND-XYZ");

        String actual = availableClaimUtility.getResidentIndvidualIdFromSession();

        assertEquals("IND-XYZ", actual);
        verify(claimValueUtility, times(1)).getClaimValue(eq("individual_id"));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testGetResidentIndvidualIdFromSession_whenClaimThrows_shouldPropagate() throws ApisResourceAccessException {
        when(claimValueUtility.getClaimValue(eq("individual_id"))).thenThrow(new ApisResourceAccessException("err"));

        availableClaimUtility.getResidentIndvidualIdFromSession();
    }
}
