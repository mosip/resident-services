package io.mosip.resident.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.packet.dto.packet.DecryptResponseDto;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.dto.CryptomanagerRequestDto;

/**
 * @author Abubacker Siddik
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({DateUtils.class})
public class EncryptorUtilTest {

    @InjectMocks
    private EncryptorUtil encryptorUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Environment env;

    @Mock
    private ResidentServiceRestClient restClientService;

    @Mock
    private Utilities utilities;

    @Captor
    ArgumentCaptor<RequestWrapper<CryptomanagerRequestDto>> requestCaptor;

    @Captor
    ArgumentCaptor<String> stringCaptor;

    private LocalDateTime localDateTime;
    private String encryptAPIUrl;

    @Before
    public void setUp() throws Exception {
        encryptAPIUrl = "http://kernel-keymanager-service/v1/keymanager/encrypt";

        PowerMockito.mockStatic(DateUtils.class);

        localDateTime = DateUtils.getUTCCurrentDateTime();
        when(DateUtils.getUTCCurrentDateTime()).thenReturn(localDateTime);
        when(utilities.getSecureRandom()).thenReturn(new SecureRandom());
    }

    @Test
    public void encryptTest() throws Exception {
        String encryptedData = "ew0KCSJpZGVudGl0eSI6IHsNCgkJImRhdGVPZkJpcnRoIjogIjE5NzgvMDEvMDEiLA0KCQkiSURTY2hlbWFWZXJzaW9uIjogMC4xLA0KCQkiVUlOIjogIjkzNDIzNjEwMzgiDQoJfQ0KfQ";
        DecryptResponseDto decryptResponseDto = new DecryptResponseDto();
        decryptResponseDto.setData(encryptedData);
        io.mosip.resident.dto.ResponseWrapper<DecryptResponseDto> responseWrapper = new io.mosip.resident.dto.ResponseWrapper<>();
        responseWrapper.setResponse(decryptResponseDto);
        String encryptedJsonString = "{\n" +
                "\"data\": " + encryptedData +
                "\n}";

        when(env.getProperty(Mockito.anyString())).thenReturn(encryptAPIUrl);
        when(restClientService.postApi(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(Class.class))).thenReturn(responseWrapper);
        when(objectMapper.readValue(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(decryptResponseDto);
        when(objectMapper.writeValueAsString(responseWrapper.getResponse())).thenReturn(encryptedJsonString);

        byte[] bytes = "Encrypt String".getBytes();
        String refId = "CenterID_MachineID";
        encryptorUtil.encrypt(bytes, refId);

        verify(env, times(1)).getProperty(Mockito.anyString());
        verify(restClientService, times(1)).postApi(stringCaptor.capture(), Mockito.any(), requestCaptor.capture(), Mockito.any(Class.class));

        final String envUrl = stringCaptor.getValue();
        assertEquals(encryptAPIUrl, envUrl);

        final RequestWrapper<CryptomanagerRequestDto> requestDtoRequestWrapper = requestCaptor.getValue();
        assertNotNull("Request is not null", requestDtoRequestWrapper);
        assertNotNull("Request body is not null", requestDtoRequestWrapper.getRequest());
        assertEquals("REGISTRATION", requestDtoRequestWrapper.getRequest().getApplicationId());
        assertEquals(localDateTime, requestDtoRequestWrapper.getRequest().getTimeStamp());

        verify(objectMapper, times(1)).readValue(stringCaptor.capture(), Mockito.any(Class.class));
        String jsonString = stringCaptor.getValue();
        assertEquals(encryptedJsonString, jsonString);

        verify(objectMapper, times(1)).writeValueAsString(responseWrapper.getResponse());
    }

    @Test(expected = PacketDecryptionFailureException.class)
    public void throwPacketDecryptionFailureExceptionTest() throws Exception {
        List<ServiceError> errors = new ArrayList<>();
        errors.add(new ServiceError());
        io.mosip.resident.dto.ResponseWrapper<DecryptResponseDto> responseWrapper = new io.mosip.resident.dto.ResponseWrapper<>();
        responseWrapper.setErrors(errors);

        when(env.getProperty(Mockito.anyString())).thenReturn(encryptAPIUrl);
        when(restClientService.postApi(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(Class.class))).thenReturn(responseWrapper);

        byte[] bytes = "Encrypt String".getBytes();
        String refId = "CenterID_MachineID";
        encryptorUtil.encrypt(bytes, refId);
    }

}
