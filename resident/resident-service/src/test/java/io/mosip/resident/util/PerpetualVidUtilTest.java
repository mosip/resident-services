package io.mosip.resident.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PerpetualVidUtilTest {

    @InjectMocks
    private PerpetualVidUtil perpetualVidUtil;

    @Mock
    private Environment env;

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    private MaskDataUtility maskDataUtility;

    @Mock
    private Utility utility;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(perpetualVidUtil, "perpatualVidType", "PERPETUAL");
    }

    @Test
    public void testGetPerpetualVid_Success() throws ResidentServiceCheckedException, ApisResourceAccessException {
        String uin = "123456789012";
        String vid = "1234567890123456";
        String vidType = "PERPETUAL";

        Map<String, Object> vidMap = new HashMap<>();
        vidMap.put("vid", vid);
        vidMap.put("vidType", vidType);

        List<Map<String, ?>> vidList = Collections.singletonList(vidMap);

        ResponseWrapper<List<Map<String, ?>>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(vidList);

        when(residentServiceRestClient.getApi(anyString(), eq(ResponseWrapper.class))).thenReturn(responseWrapper);

        Optional<String> result = perpetualVidUtil.getPerpatualVid(uin);

        assertTrue(result.isPresent());
        assertEquals(vid, result.get());
    }

    @Test
    public void testGetPerpetualVid_Empty() throws ResidentServiceCheckedException, ApisResourceAccessException {
        String uin = "123456789012";

        ResponseWrapper<List<Map<String, ?>>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(Collections.emptyList());

        when(residentServiceRestClient.getApi(anyString(), eq(ResponseWrapper.class))).thenReturn(responseWrapper);

        Optional<String> result = perpetualVidUtil.getPerpatualVid(uin);

        assertFalse(result.isPresent());
    }

    @Test
    public void testRetrieveVidsfromUin_Success() throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
        String uin = "123456789012";
        int timeZoneOffset = 0;
        String locale = "en";

        Map<String, Object> vidMap = new HashMap<>();
        vidMap.put("vid", "1234567890123456");
        vidMap.put("transactionLimit", 5);

        List<Map<String, ?>> vidList = Collections.singletonList(vidMap);

        ResponseWrapper<List<Map<String, ?>>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(vidList);

        when(residentServiceRestClient.getApi(anyString(), eq(ResponseWrapper.class))).thenReturn(responseWrapper);
        when(utility.getRefIdHash(anyString())).thenReturn("hashedVid");
        when(residentTransactionRepository.findByRefIdAndAuthTypeCodeLike(anyString(), anyString())).thenReturn(2);
        when(maskDataUtility.convertToMaskData(anyString())).thenReturn("maskedVid");

        ResponseWrapper<List<Map<String, ?>>> result = perpetualVidUtil.retrieveVidsfromUin(uin, timeZoneOffset, locale);

        assertNotNull(result);
        assertEquals(1, result.getResponse().size());
        assertEquals("maskedVid", result.getResponse().get(0).get("maskedVid"));
    }

    @Test
    public void testRetrieveVidsfromUin_Exception() throws ResidentServiceCheckedException, ApisResourceAccessException {
        String uin = "123456789012";
        int timeZoneOffset = 0;
        String locale = "en";

        when(residentServiceRestClient.getApi(anyString(), eq(ResponseWrapper.class)))
                .thenThrow(new RuntimeException("API error"));

        assertThrows(ApisResourceAccessException.class, () -> {
            perpetualVidUtil.retrieveVidsfromUin(uin, timeZoneOffset, locale);
        });
    }
}

