package io.mosip.resident.test.handler;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.exception.PacketCreatorException;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.RegistrationType;
import io.mosip.resident.dto.ResidentIndividialIDType;
import io.mosip.resident.dto.ResidentUpdateDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.handler.service.ResidentUpdateService;
import io.mosip.resident.handler.service.SyncAndUploadService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.IdSchemaUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.validator.RequestHandlerRequestValidator;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({ IOUtils.class, File.class, FileInputStream.class})
public class ResidentUpdateServiceTest {

    private ResidentUpdateDto residentUpdateDto;

    @InjectMocks
    private ResidentUpdateService residentUpdateService;

    @Mock
    private RequestHandlerRequestValidator validator;

    @Mock
    private ResidentServiceRestClient restClientService;

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
    private FileInputStream fileInputStream;
    
	@Mock
	private AuditUtil audit;

    private static final String rid = "10001100770000320200720092256";

    @Before
    public void setup() throws Exception {
        ReflectionTestUtils.setField(residentUpdateService, "defaultIdSchemaVersion", "0.1");

        residentUpdateDto = new ResidentUpdateDto();
        residentUpdateDto.setIdValue("5984924027");
        residentUpdateDto.setCenterId("10001");
        residentUpdateDto.setIdentityJson("eyJpZGVudGl0eSI6IHsiYWRkcmVzc0xpbmUxIjogW3sibGFuZ3VhZ2UiOiAiZW5nIiwidmFsdWUiOiAiTVkgd29yayBhZHJlc3MifSx7Imxhbmd1YWdlIjogImFyYSIsInZhbHVlIjogIkZGRiJ9XSwicHJvb2ZPZkFkZHJlc3MiIDogeyJ2YWx1ZSIgOiAicHJvb2ZPZkFkZHJlc3MiLCJ0eXBlIiA6ICJET0MwMDQiLCJmb3JtYXQiIDogImpwZyJ9LCJJRFNjaGVtYVZlcnNpb24iOiAwLjEsIlVJTiI6ICI1OTg0OTI0MDI3In19");
        residentUpdateDto.setIdType(ResidentIndividialIDType.UIN);
        residentUpdateDto.setMachineId("10001");
        residentUpdateDto.setRequestType(RegistrationType.RES_UPDATE);
        residentUpdateDto.setProofOfAddress("address");
        residentUpdateDto.setProofOfDateOfBirth("birth");
        residentUpdateDto.setProofOfRelationship("relationship");
        residentUpdateDto.setProofOfIdentity("identity");

        Mockito.when(validator.isValidCenter(anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(validator.isValidMachine(anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(validator.isValidRegistrationTypeAndUin(anyString(), anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(validator.isValidVid(anyString())).thenReturn(Boolean.TRUE);

        Mockito.when(utilities.getDefaultSource()).thenReturn("source");
        Mockito.when(idSchemaUtil.getIdSchema(anyDouble())).thenReturn("idschema");
        Mockito.when(utilities.generateAudit(any())).thenReturn(new ArrayList<>());

        JSONObject ridJson = new JSONObject();
        ridJson.put("rid", rid);

        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setId(rid);
        packetInfo.setSource("source");
		Mockito.when(packetWriter.createPacket(any())).thenReturn(Lists.newArrayList(packetInfo));

        Mockito.when(mapper.writeValueAsString(any())).thenReturn("String");
        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(ridJson);
        Mockito.when(restClientService.getApi(any(), any(), anyString(), anyString(), any(Class.class))).thenReturn(new ResponseWrapper<>());

        Mockito.when(env.getProperty(any())).thenReturn("property");

        File file = PowerMockito.mock(File.class);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(file);
        PowerMockito.whenNew(File.class).withArguments(anyString()).thenReturn(file);
        fileInputStream =PowerMockito.mock(FileInputStream.class);
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments().thenReturn(fileInputStream);
        PowerMockito.whenNew(FileInputStream.class).withArguments(anyString()).thenReturn(fileInputStream);

        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.when(IOUtils.class, "toByteArray", fileInputStream).thenReturn("packet".getBytes());

        PacketGeneratorResDto resDto = new PacketGeneratorResDto();
        resDto.setRegistrationId(rid);
        resDto.setMessage("packet uploaded");
        resDto.setStatus("PROCESSING");
        Mockito.when(syncUploadEncryptionService.uploadUinPacket(any(), any(), any(), any())).thenReturn(resDto);
        Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
    }

    @Test(expected = BaseCheckedException.class)
    public void testCreatePacket() throws IOException, BaseCheckedException {

        PacketGeneratorResDto result = residentUpdateService.createPacket(residentUpdateDto);

        assertTrue(result.getRegistrationId().equalsIgnoreCase(rid));

    }
    
    @Test(expected = BaseCheckedException.class)
    public void testApiResourceException() throws IOException, BaseCheckedException {

        Mockito.when(restClientService.getApi(any(), any(), anyString(), anyString(),
                any(Class.class))).thenThrow(new ApisResourceAccessException("Error",new HttpClientErrorException(HttpStatus.OK, "message")));

        residentUpdateService.createPacket(residentUpdateDto);
    }

    
    @Test(expected = BaseCheckedException.class)
    public void testPacketCreatorException() throws IOException, BaseCheckedException {

        Mockito.when(restClientService.getApi(any(), any(), anyString(), anyString(),
                any(Class.class))).thenThrow(new PacketCreatorException("code", "message"));

        residentUpdateService.createPacket(residentUpdateDto);
    }
}
