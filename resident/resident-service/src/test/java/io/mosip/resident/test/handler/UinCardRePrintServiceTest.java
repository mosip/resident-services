package io.mosip.resident.test.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.exception.PacketCreatorException;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.handler.service.SyncAndUploadService;
import io.mosip.resident.handler.service.UinCardRePrintService;
import io.mosip.resident.validator.RequestHandlerRequestValidator;
import io.mosip.resident.util.*;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Lists;
import org.json.simple.JSONObject;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({ IOUtils.class, JsonUtil.class, File.class, FileInputStream.class, UinCardRePrintService.class})
public class UinCardRePrintServiceTest {

    @InjectMocks
    private UinCardRePrintService uinCardRePrintService;

    @Mock
    private RequestHandlerRequestValidator validator;

    @Mock
    private ResidentServiceRestClient restClientService;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private SyncAndUploadService syncUploadEncryptionService;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Environment env;

    @Mock
    private PacketWriter packetWriter;

    @Mock
    private IdSchemaUtil idSchemaUtil;

    @Mock
    private Utilities utilities;

    @Mock
    private JSONObject jsonObject;

    @Mock
    private FileInputStream fileInputStream;

    private static final String rid = "10001100770000320200720092256";

    private RegProcRePrintRequestDto regProcRePrintRequestDto;

    @Before
    public void setup() throws Exception {
        ReflectionTestUtils.setField(uinCardRePrintService, "idschemaVersion", "0.1");

        regProcRePrintRequestDto = new RegProcRePrintRequestDto();
        regProcRePrintRequestDto.setId("5984924027");
        regProcRePrintRequestDto.setIdType("UIN");
        regProcRePrintRequestDto.setCardType("UIN");
        regProcRePrintRequestDto.setCenterId("10001");
        regProcRePrintRequestDto.setMachineId("10001");
        regProcRePrintRequestDto.setRegistrationType(RegistrationType.RES_REPRINT.name());

        Mockito.when(validator.isValidCenter(anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(validator.isValidMachine(anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(validator.isValidRePrintRegistrationType(anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(validator.isValidCardType(any())).thenReturn(Boolean.TRUE);
        Mockito.when(validator.isValidIdType(any())).thenReturn(Boolean.TRUE);
        Mockito.when(validator.isValidUin(any())).thenReturn(Boolean.TRUE);
        Mockito.when(validator.isValidVid(any())).thenReturn(Boolean.TRUE);

        Mockito.when(utilities.getDefaultSource()).thenReturn("source");
        Mockito.when(idSchemaUtil.getIdSchema(anyDouble())).thenReturn("idschema");
        Mockito.when(utilities.generateAudit(any())).thenReturn(new ArrayList<>());

        JSONObject ridJson = new JSONObject();
        ridJson.put("rid", rid);

        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setId(rid);
        packetInfo.setSource("source");
        Mockito.when(packetWriter.createPacket(any(), anyBoolean())).thenReturn(Lists.newArrayList(packetInfo));

        Mockito.when(tokenGenerator.getToken()).thenReturn("token");
        Mockito.when(mapper.writeValueAsString(any())).thenReturn("String");
        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(ridJson);
        Mockito.when(restClientService.getApi(any(), any(), anyString(), anyString(), any(Class.class), any())).thenReturn(new ResponseWrapper<>());

        Mockito.when(env.getProperty(any())).thenReturn("property");

        File file = PowerMockito.mock(File.class);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(file);
        PowerMockito.whenNew(File.class).withArguments(anyString()).thenReturn(file);
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments().thenReturn(fileInputStream);

        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.when(IOUtils.class, "toByteArray", fileInputStream).thenReturn("packet".getBytes());

        PowerMockito.mockStatic(JsonUtil.class);
        PowerMockito.when(JsonUtil.class, "getJSONObject", jsonObject, MappingJsonConstants.IDSCHEMA_VERSION).thenReturn(jsonObject);
        PowerMockito.when(JsonUtil.class, "getJSONValue", jsonObject, MappingJsonConstants.VALUE).thenReturn("value");

        PacketGeneratorResDto resDto = new PacketGeneratorResDto();
        resDto.setRegistrationId(rid);
        resDto.setMessage("packet uploaded");
        resDto.setStatus("PROCESSING");
        Mockito.when(syncUploadEncryptionService.uploadUinPacket(any(), any(), any(), any())).thenReturn(resDto);
        Mockito.when(utilities.getRegistrationProcessorMappingJson()).thenReturn(jsonObject);
        Mockito.when(utilities.linkRegIdWrtUin(any(), any())).thenReturn(true);

    }

    @Test
    public void testCreatePacket() throws IOException, BaseCheckedException {

        PacketGeneratorResDto result = uinCardRePrintService.createPacket(regProcRePrintRequestDto);

        assertTrue(result.getRegistrationId().equalsIgnoreCase(rid));
    }

    @Test(expected = BaseCheckedException.class)
    public void testApiResourceException() throws IOException, BaseCheckedException {

        Mockito.when(restClientService.getApi(any(), any(), anyString(), anyString(),
                any(Class.class), any())).thenThrow(new ApisResourceAccessException("Error",new HttpClientErrorException(HttpStatus.OK, "message")));

        PacketGeneratorResDto result = uinCardRePrintService.createPacket(regProcRePrintRequestDto);
    }

    @Test(expected = PacketCreatorException.class)
    public void testPacketCreatorException() throws IOException, BaseCheckedException {

        Mockito.when(restClientService.getApi(any(), any(), anyString(), anyString(),
                any(Class.class), any())).thenThrow(new PacketCreatorException("code", "message"));

        PacketGeneratorResDto result = uinCardRePrintService.createPacket(regProcRePrintRequestDto);
    }

    @Test
    public void testVid() throws IOException, BaseCheckedException {
        regProcRePrintRequestDto.setCardType(CardType.MASKED_UIN.name());
        /*regProcRePrintRequestDto.setIdType("VID");
        regProcRePrintRequestDto.setId("1234");*/

        VidResponseDTO1 vidResponseDTO1 = new VidResponseDTO1();
        VidResDTO vidResDTO = new VidResDTO();
        vidResDTO.setVid("2345");
        vidResponseDTO1.setResponse(vidResDTO);

        Mockito.when(restClientService.postApi(any(), any(), any(), any(Class.class), any())).thenReturn(vidResponseDTO1);
        Mockito.when(utilities.getUinByVid(any())).thenReturn("12345");

        PacketGeneratorResDto result = uinCardRePrintService.createPacket(regProcRePrintRequestDto);

        assertTrue(result.getRegistrationId().equalsIgnoreCase(rid));
    }

    @Test
    public void testVidWithNoUin() throws IOException, BaseCheckedException {
        regProcRePrintRequestDto.setCardType(CardType.MASKED_UIN.name());
        regProcRePrintRequestDto.setIdType("VID");
        regProcRePrintRequestDto.setId("1234");

        VidResponseDTO1 vidResponseDTO1 = new VidResponseDTO1();
        VidResDTO vidResDTO = new VidResDTO();
        vidResDTO.setVid("2345");
        vidResponseDTO1.setResponse(vidResDTO);

        Mockito.when(restClientService.postApi(any(), any(), any(), any(Class.class), any())).thenReturn(vidResponseDTO1);
        Mockito.when(utilities.getUinByVid(any())).thenReturn("12345");

        PacketGeneratorResDto result = uinCardRePrintService.createPacket(regProcRePrintRequestDto);

        assertTrue(result.getRegistrationId().equalsIgnoreCase(rid));
    }

    @Test(expected = BaseCheckedException.class)
    public void testVidCreationException() throws IOException, BaseCheckedException {
        regProcRePrintRequestDto.setCardType(CardType.MASKED_UIN.name());

        VidResponseDTO1 vidResponseDTO1 = new VidResponseDTO1();
        vidResponseDTO1.setResponse(null);
        ErrorDTO errorDTO = new ErrorDTO("", "");
        vidResponseDTO1.setErrors(Lists.newArrayList(errorDTO));

        Mockito.when(restClientService.postApi(any(), any(), any(), any(Class.class), any())).thenReturn(vidResponseDTO1);
        Mockito.when(utilities.getUinByVid(any())).thenReturn("12345");

        PacketGeneratorResDto result = uinCardRePrintService.createPacket(regProcRePrintRequestDto);

        assertTrue(result.getRegistrationId().equalsIgnoreCase(rid));
    }
}
