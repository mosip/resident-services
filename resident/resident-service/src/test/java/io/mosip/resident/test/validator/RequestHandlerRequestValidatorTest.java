package io.mosip.resident.test.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.RequestHandlerValidationException;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.validator.RequestHandlerRequestValidator;
import org.assertj.core.util.Lists;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(SpringRunner.class)
public class RequestHandlerRequestValidatorTest {

    @InjectMocks
    private RequestHandlerRequestValidator requestHandlerRequestValidator;

    @Mock
    private TokenGenerator tokenGenerator;

    /** The rest client service. */
    @Mock
    private ResidentServiceRestClient restClientService;

    /** The mapper. */
    @Mock
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private UinValidator<String> uinValidatorImpl;

    /** The vid validator impl. */
    @Mock
    private VidValidator<String> vidValidatorImpl;

    /** The utilities. */
    @Mock
    private Utilities utilities;

    @Mock
    private Environment env;

    private static final String ID = "110011";
    private static final String PROPERTY = "property";

    @Before
    public void setup() throws ApisResourceAccessException, IOException {
        ReflectionTestUtils.setField(requestHandlerRequestValidator, "gracePeriod", 120);
        ReflectionTestUtils.setField(requestHandlerRequestValidator, "primaryLanguagecode", "eng");
        Mockito.when(env.getProperty(any())).thenReturn(PROPERTY);
        Mockito.when(restClientService.getApi(any(), any(), anyString(), anyString(), any(Class.class), any())).thenReturn(new ResponseWrapper<>());
        Mockito.when(mapper.writeValueAsString(any())).thenReturn("String");
    }

    @Test(expected = RequestHandlerValidationException.class)
    public void testRequestHandlerValidationException() throws RequestHandlerValidationException {
        requestHandlerRequestValidator.validate(ID);
    }

    @Test(expected = RequestHandlerValidationException.class)
    public void testValidateNullId() throws RequestHandlerValidationException {
        requestHandlerRequestValidator.validate(null);
    }

    @Test
    public void testValidCenter() throws BaseCheckedException, IOException {
        RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
        RegistrationCenterResponseDto rcpdto = new RegistrationCenterResponseDto();
        rcpdto.setRegistrationCenters(Lists.newArrayList(registrationCenterDto));

        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(rcpdto);

        boolean result = requestHandlerRequestValidator.isValidCenter(ID);
        assertTrue(result);
    }

    @Test(expected = BaseCheckedException.class)
    @Ignore
    public void testApisResourceAccessExceptionForCenter() throws BaseCheckedException, IOException {
        Mockito.when(restClientService.getApi(any(), any(), anyString(), anyString(),
                any(Class.class), any())).thenThrow(new ApisResourceAccessException("Error",new HttpClientErrorException(HttpStatus.OK, "message")));

        boolean result = requestHandlerRequestValidator.isValidCenter(ID);
        assertTrue(result);
    }

    @Test
    public void testValidMachine() throws BaseCheckedException, IOException {
        MachineDto registrationCenterDto = new MachineDto();
        MachineResponseDto machinedto = new MachineResponseDto();
        machinedto.setMachines(Lists.newArrayList(registrationCenterDto));

        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(machinedto);

        boolean result = requestHandlerRequestValidator.isValidMachine(ID);
        assertTrue(result);
    }

    @Test(expected = BaseCheckedException.class)
    public void testInValidContact() throws BaseCheckedException {
        boolean result = requestHandlerRequestValidator.isValidContactType("whatsapp", new LogDescription());
    }

    @Test
    public void testValidContact() throws BaseCheckedException, IOException {
        String EMAIL = "Email";
        boolean result = requestHandlerRequestValidator.isValidContactType(EMAIL, new LogDescription());
        assertTrue(result);
    }

    @Test(expected = BaseCheckedException.class)
    public void testInValidCardType() throws BaseCheckedException {
        boolean result = requestHandlerRequestValidator.isValidCardType("wedding card");
    }

    @Test
    public void testValidCardType() throws BaseCheckedException, IOException {
        boolean result = requestHandlerRequestValidator.isValidCardType(CardType.MASKED_UIN.name());
        assertTrue(result);
    }

    @Test(expected = BaseCheckedException.class)
    public void testInValidIdType() throws BaseCheckedException {
        boolean result = requestHandlerRequestValidator.isValidCardType("voter id");
    }

    @Test
    public void testValidIdType() throws BaseCheckedException, IOException {
        String UIN = "UIN";
        boolean result = requestHandlerRequestValidator.isValidCardType(UIN);
        assertTrue(result);
    }

    @Test
    public void testValidUin() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        Mockito.when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        Mockito.when(utilities.retrieveIdrepoJson(anyString())).thenReturn(jsonObject);

        boolean result = requestHandlerRequestValidator.isValidUin("1234");
        assertTrue(result);
    }

    @Test(expected = BaseCheckedException.class)
    public void testIdRepoAppException() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        Mockito.when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        Mockito.when(utilities.retrieveIdrepoJson(anyString())).thenThrow(new IdRepoAppException("errorcode", "message"));

        boolean result = requestHandlerRequestValidator.isValidUin("1234");
        assertTrue(result);
    }

    @Test(expected = BaseCheckedException.class)
    public void testApisResourceAccessException() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        Mockito.when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        Mockito.when(utilities.retrieveIdrepoJson(anyString())).thenThrow(new ApisResourceAccessException("errorcode"));

        boolean result = requestHandlerRequestValidator.isValidUin("1234");
        assertTrue(result);
    }

    @Test(expected = BaseCheckedException.class)
    public void testIOException() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        Mockito.when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        Mockito.when(utilities.retrieveIdrepoJson(anyString())).thenThrow(new IOException("errorcode"));

        boolean result = requestHandlerRequestValidator.isValidUin("1234");
        assertTrue(result);
    }

    @Test(expected = BaseCheckedException.class)
    public void testInValidRePrintRegistrationType() throws BaseCheckedException {
        boolean result = requestHandlerRequestValidator.isValidRePrintRegistrationType("LOST");
    }

    @Test
    public void testValidRePrintRegistrationType() throws BaseCheckedException, IOException {
        String UIN = "UIN";
        boolean result = requestHandlerRequestValidator.isValidRePrintRegistrationType(RegistrationType.RES_REPRINT.name());
        assertTrue(result);
    }
}
