package io.mosip.resident.test.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.handler.service.SyncAndUploadService;
import io.mosip.resident.util.*;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({ JsonUtils.class, Gson.class, DateUtils.class,
        ByteArrayResource.class, SyncAndUploadService.class, GsonBuilder.class })
public class SyncAndUploadServiceTest {

    @InjectMocks
    private SyncAndUploadService syncAndUploadService;

    @Mock
    private Environment env;

    @Mock
    private IdSchemaUtil idSchemaUtil;

    @Mock
    private Utilities utilities;

    @Mock
    private ResidentServiceRestClient restClientService;

    @Mock
    private EncryptorUtil encryptorUtil;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    Gson gson;

    String registartionId;
    String creationTime;
    String regType;
    byte[] packetZipBytes;
    private static final String status = "PROCESSING";
    private static final String FAILURE = "FAILURE";

    @Before
    public void setup() throws Exception {
        registartionId = "10001100770000320200720092256";
        creationTime = DateUtils.getCurrentDateTimeString();
        regType = RegistrationType.NEW.name();
        packetZipBytes = "packet".getBytes();

        PowerMockito.mockStatic(DateUtils.class);
        PowerMockito.when(DateUtils.getUTCCurrentDateTimeString(any())).thenReturn("datetime");

        ReflectionTestUtils.setField(syncAndUploadService, "centerIdLength", 5);
        ReflectionTestUtils.setField(syncAndUploadService, "machineIdLength", 5);

        GsonBuilder gsonBuilder = PowerMockito.mock(GsonBuilder.class);
        //Gson gson = PowerMockito.mock(Gson.class);
        ByteArrayResource byteArrayResource = PowerMockito.mock(ByteArrayResource.class);
        PowerMockito.whenNew(ByteArrayResource.class).withAnyArguments().thenReturn(byteArrayResource);
        PowerMockito.whenNew(GsonBuilder.class).withNoArguments().thenReturn(gsonBuilder);
        Mockito.when(gsonBuilder.create()).thenReturn(gson);
        //PowerMockito.whenNew(Gson.class).withNoArguments().thenReturn(gson);
        Mockito.when(env.getProperty(any())).thenReturn("property");
        Mockito.when(restClientService.postApi(any(), any(), any(), any(Class.class), any())).thenReturn(new String(""));
        Mockito.when(encryptorUtil.encrypt(any(), any())).thenReturn("encrypted request String");
        Mockito.when(tokenGenerator.getToken()).thenReturn("sbfdsafuadfkbdsf");

        PacketReceiverSubResponseDTO packetReceiverSubResponseDTO = new PacketReceiverSubResponseDTO();
        packetReceiverSubResponseDTO.setStatus(status);
        PacketReceiverResponseDTO packetReceiverResponseDTO = new PacketReceiverResponseDTO();
        packetReceiverResponseDTO.setErrors(null);
        packetReceiverResponseDTO.setResponse(packetReceiverSubResponseDTO);
        SyncResponseDto syncResponseDto = new SyncResponseDto();
        syncResponseDto.setStatus("SUCCESS");
        RegSyncResponseDTO regSyncResponseDTO = new RegSyncResponseDTO();
        regSyncResponseDTO.setErrors(null);
        regSyncResponseDTO.setResponse(Lists.newArrayList(syncResponseDto));
        Mockito.when(gson.fromJson(anyString(), any(Class.class))).thenReturn(regSyncResponseDTO).thenReturn(packetReceiverResponseDTO);

    }

    @Test
    public void testSyncAndUpload() throws BaseCheckedException {
        PacketGeneratorResDto response = syncAndUploadService.uploadUinPacket(registartionId, creationTime, regType, packetZipBytes);

        assertTrue(response.getStatus().equals(status));
    }

    @Test
    public void testSyncFail() throws BaseCheckedException {
        SyncResponseDto syncResponseDto = new SyncResponseDto();
        syncResponseDto.setStatus(FAILURE);
        RegSyncResponseDTO regSyncResponseDTO = new RegSyncResponseDTO();
        regSyncResponseDTO.setErrors(null);
        regSyncResponseDTO.setResponse(Lists.newArrayList(syncResponseDto));
        Mockito.when(gson.fromJson(anyString(), any(Class.class))).thenReturn(regSyncResponseDTO);
        PacketGeneratorResDto response = syncAndUploadService.uploadUinPacket(registartionId, creationTime, regType, packetZipBytes);

        assertTrue(response.getStatus().equals(FAILURE));
    }

    @Test(expected = BaseCheckedException.class)
    public void testApisResourceAccessException() throws BaseCheckedException {
        Mockito.when(restClientService.postApi(any(), any(), any(), any(Class.class), any())).thenThrow(new ApisResourceAccessException("ApisResourceAccessException"));

        PacketGeneratorResDto response = syncAndUploadService.uploadUinPacket(registartionId, creationTime, regType, packetZipBytes);
    }

    @Test(expected = BaseCheckedException.class)
    public void testIOException() throws BaseCheckedException, IOException {
        Mockito.when(tokenGenerator.getToken()).thenThrow(new IOException("io exception"));

        PacketGeneratorResDto response = syncAndUploadService.uploadUinPacket(registartionId, creationTime, regType, packetZipBytes);
    }

    @Test(expected = BaseCheckedException.class)
    public void testJsonProcessingException() throws BaseCheckedException {
        PowerMockito.mockStatic(JsonUtils.class);
        PowerMockito.when(JsonUtils.javaObjectToJsonString(any())).thenThrow(new JsonProcessingException("io exception"));

        PacketGeneratorResDto response = syncAndUploadService.uploadUinPacket(registartionId, creationTime, regType, packetZipBytes);
    }

    @Test(expected = BaseCheckedException.class)
    public void testIOExceptionForPacketUpload() throws BaseCheckedException, IOException {
        Mockito.when(tokenGenerator.getToken()).thenReturn("token").thenThrow(new IOException("io exception"));

        PacketGeneratorResDto response = syncAndUploadService.uploadUinPacket(registartionId, creationTime, regType, packetZipBytes);
    }

}
