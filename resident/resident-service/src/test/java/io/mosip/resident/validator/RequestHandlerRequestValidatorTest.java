package io.mosip.resident.validator;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.dto.LogDescription;
import io.mosip.resident.dto.MachineDto;
import io.mosip.resident.dto.MachineResponseDto;
import io.mosip.resident.dto.RegistrationCenterDto;
import io.mosip.resident.dto.RegistrationCenterResponseDto;
import io.mosip.resident.dto.RegistrationType;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.RequestHandlerValidationException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;

@RunWith(SpringRunner.class)
public class RequestHandlerRequestValidatorTest {

    @InjectMocks
    private RequestHandlerRequestValidator requestHandlerRequestValidator;

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

    @Before
    public void setup() throws ApisResourceAccessException, IOException {
		Mockito.when(env.getProperty("mosip.mandatory-languages")).thenReturn("eng");
        Mockito.when(restClientService.getApi(any(), any(), anyString(), anyString(), any(Class.class))).thenReturn(new ResponseWrapper<>());
        Mockito.when(mapper.writeValueAsString(any())).thenReturn("String");
    }

    @Test(expected = RequestHandlerValidationException.class)
    public void testValidateWithRequestHandlerValidationException() throws RequestHandlerValidationException {
        requestHandlerRequestValidator.validate(ID);
    }

    @Test(expected = RequestHandlerValidationException.class)
    public void testValidateNullId() throws RequestHandlerValidationException {
        requestHandlerRequestValidator.validate(null);
    }

    @Test
    public void testIsValidCenter() throws BaseCheckedException, IOException {
        RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
        RegistrationCenterResponseDto rcpdto = new RegistrationCenterResponseDto();
        rcpdto.setRegistrationCenters(Lists.newArrayList(registrationCenterDto));

        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(rcpdto);

        boolean result = requestHandlerRequestValidator.isValidCenter(ID);
        assertTrue(result);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidCenterNestedElse() throws BaseCheckedException, IOException {
    	RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
    	ServiceError error=new ServiceError();
    	error.setErrorCode("invalid-101");
    	error.setMessage("invalid center");
    	List<ServiceError> errorList=new ArrayList<ServiceError>();
    	errorList.add(error);
    	ResponseWrapper<?> wrapper=new ResponseWrapper<>();
    	wrapper.setErrors(errorList);
        RegistrationCenterResponseDto rcpdto = new RegistrationCenterResponseDto();
        rcpdto.setRegistrationCenters(Lists.newArrayList(registrationCenterDto));
        when(restClientService.getApi(any(), any(), anyString(), anyString(), any())).thenReturn(wrapper);
        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(rcpdto);
    	
        requestHandlerRequestValidator.isValidCenter(ID);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidCenterElse() throws BaseCheckedException, IOException {
        requestHandlerRequestValidator.isValidCenter("");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidCenterElse2() throws BaseCheckedException, IOException {
        requestHandlerRequestValidator.isValidCenter(null);
    }
    
    @Test
    public void testIsValidCenterWithApisResourceAccessException() throws BaseCheckedException, IOException {
    	RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
    	ServiceError error=new ServiceError();
    	error.setErrorCode("invalid-101");
    	error.setMessage("invalid center");
    	List<ServiceError> errorList=new ArrayList<ServiceError>();
    	errorList.add(error);
    	ResponseWrapper<?> wrapper=new ResponseWrapper<>();
    	wrapper.setErrors(errorList);
        RegistrationCenterResponseDto rcpdto = new RegistrationCenterResponseDto();
        rcpdto.setRegistrationCenters(Lists.newArrayList(registrationCenterDto));
        when(restClientService.getApi(any(), any(), anyString(), anyString(), any())).thenThrow(new ApisResourceAccessException("error"));
//        when(restClientService.getApi(any(), any(), anyString(), anyString(), any())).thenThrow(new ApisResourceAccessException("error", new HttpClientErrorException(HttpStatus.OK)));
//        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenThrow(ApisResourceAccessException.class);
    	
        requestHandlerRequestValidator.isValidCenter(ID);
    }

    @Test
    public void testIsValidMachine() throws BaseCheckedException, IOException {
        MachineDto registrationCenterDto = new MachineDto();
        MachineResponseDto machinedto = new MachineResponseDto();
        machinedto.setMachines(Lists.newArrayList(registrationCenterDto));

        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(machinedto);

        boolean result = requestHandlerRequestValidator.isValidMachine(ID);
        assertTrue(result);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidMachineNestedElse() throws BaseCheckedException, IOException{
    	MachineDto registrationCenterDto = new MachineDto();
    	ServiceError error=new ServiceError();
    	error.setErrorCode("invalid-101");
    	error.setMessage("invalid center");
    	List<ServiceError> errorList=new ArrayList<ServiceError>();
    	errorList.add(error);
    	ResponseWrapper<?> wrapper=new ResponseWrapper<>();
    	wrapper.setErrors(errorList);
        MachineResponseDto machinedto = new MachineResponseDto();
        machinedto.setMachines(Lists.newArrayList(registrationCenterDto));

        when(restClientService.getApi(any(), any(), anyString(), anyString(), any())).thenReturn(wrapper);
        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(machinedto);

        requestHandlerRequestValidator.isValidMachine(ID);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidMachineElse() throws BaseCheckedException, IOException{
    	requestHandlerRequestValidator.isValidMachine(null);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidMachineElse2() throws BaseCheckedException, IOException{
    	requestHandlerRequestValidator.isValidMachine("");
    }
    
    @Test
    public void testIsValidMachineWithApisResourceAccessException() throws BaseCheckedException, IOException{
    	MachineDto registrationCenterDto = new MachineDto();
    	ServiceError error=new ServiceError();
    	error.setErrorCode("invalid-101");
    	error.setMessage("invalid center");
    	List<ServiceError> errorList=new ArrayList<ServiceError>();
    	errorList.add(error);
    	ResponseWrapper<?> wrapper=new ResponseWrapper<>();
    	wrapper.setErrors(errorList);
        MachineResponseDto machinedto = new MachineResponseDto();
        machinedto.setMachines(Lists.newArrayList(registrationCenterDto));

        when(restClientService.getApi(any(), any(), anyString(), anyString(), any())).thenThrow(new ApisResourceAccessException("error"));
//        when(restClientService.getApi(any(), any(), anyString(), anyString(), any())).thenThrow(new ApisResourceAccessException("error", new HttpClientErrorException(HttpStatus.OK)));
//        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenThrow(ApisResourceAccessException.class);

        requestHandlerRequestValidator.isValidMachine(ID);
    }

    @Test
    public void testIsValidUin() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        Mockito.when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        Mockito.when(utilities.retrieveIdrepoJson(anyString())).thenReturn(jsonObject);

        boolean result = requestHandlerRequestValidator.isValidUin("1234");
        assertTrue(result);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidUinElse1() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        when(utilities.retrieveIdrepoJson(anyString())).thenReturn(null);

        requestHandlerRequestValidator.isValidUin("1234");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidUinElse2() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        when(uinValidatorImpl.validateId(anyString())).thenReturn(false);
        when(utilities.retrieveIdrepoJson(anyString())).thenReturn(jsonObject);

        requestHandlerRequestValidator.isValidUin("1234");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidUinWithInvalidIDException() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        Mockito.when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        Mockito.when(utilities.retrieveIdrepoJson(anyString())).thenThrow(new InvalidIDException("errorcode", "message"));

        requestHandlerRequestValidator.isValidUin("1234");
    }

    @Test(expected = BaseCheckedException.class)
    public void testIsValidUinWithIdRepoAppException() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        Mockito.when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        Mockito.when(utilities.retrieveIdrepoJson(anyString())).thenThrow(new IdRepoAppException("errorcode", "message"));

        requestHandlerRequestValidator.isValidUin("1234");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidUinWithNumberFormatException() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        Mockito.when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        Mockito.when(utilities.retrieveIdrepoJson(anyString())).thenThrow(new NumberFormatException("errorcode"));

        requestHandlerRequestValidator.isValidUin("1234");
    }

    @Test(expected = BaseCheckedException.class)
    public void testIsValidUinWithApisResourceAccessException() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        Mockito.when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        Mockito.when(utilities.retrieveIdrepoJson(anyString())).thenThrow(new ApisResourceAccessException("errorcode"));

        requestHandlerRequestValidator.isValidUin("1234");
    }

    @Test(expected = BaseCheckedException.class)
    public void testIsValidUinWithIOException() throws BaseCheckedException, IOException {
        JSONObject jsonObject = new JSONObject();
        Mockito.when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
        Mockito.when(utilities.retrieveIdrepoJson(anyString())).thenThrow(new IOException("errorcode"));

        requestHandlerRequestValidator.isValidUin("1234");
    }
    
    @Test
    public void testIsValidRePrintRegistrationType() throws BaseCheckedException, IOException {
        boolean result = requestHandlerRequestValidator.isValidRePrintRegistrationType(RegistrationType.RES_REPRINT.name());
        assertTrue(result);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidRePrintRegistrationTypeElse() throws BaseCheckedException {
        requestHandlerRequestValidator.isValidRePrintRegistrationType("LOST");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidRePrintRegistrationTypeElse2() throws BaseCheckedException {
        requestHandlerRequestValidator.isValidRePrintRegistrationType(null);
    }
    
    @Test
    public void testIsValidRegistrationTypeAndUinNestedNestedIf11() throws BaseCheckedException, IOException{
    	when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.retrieveIdrepoJsonStatus(anyString())).thenReturn("ACTIVATED");
    	JSONObject idObject=new JSONObject();
    	when(utilities.retrieveIdrepoJson(anyString())).thenReturn(idObject);
    	boolean result=requestHandlerRequestValidator.isValidRegistrationTypeAndUin(RegistrationType.RES_UPDATE.name(), "1234");
    	assertTrue(result);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidRegistrationTypeAndUinNestedNestedIf12() throws BaseCheckedException, IOException{
    	when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.retrieveIdrepoJsonStatus(anyString())).thenReturn("ACTIVATED");
    	when(utilities.retrieveIdrepoJson(anyString())).thenReturn(null);
    	requestHandlerRequestValidator.isValidRegistrationTypeAndUin(RegistrationType.RES_UPDATE.name(), "1234");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidRegistrationTypeAndUinNestedNestedIf13() throws BaseCheckedException, IOException{
    	when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.retrieveIdrepoJsonStatus(anyString())).thenReturn("any status");
    	when(utilities.retrieveIdrepoJson(anyString())).thenReturn(new JSONObject());
    	requestHandlerRequestValidator.isValidRegistrationTypeAndUin(RegistrationType.RES_UPDATE.name(), "1234");
    }
    
    @Test
    public void testIsValidRegistrationTypeAndUinNestedNestedIf21() throws BaseCheckedException, IOException{
    	when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.retrieveIdrepoJsonStatus(anyString())).thenReturn("ACTIVATED");
    	boolean result=requestHandlerRequestValidator.isValidRegistrationTypeAndUin(RegistrationType.DEACTIVATED.name(), "1234");
    	assertTrue(result);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidRegistrationTypeAndUinNestedNestedElse21() throws BaseCheckedException, IOException{
    	when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.retrieveIdrepoJsonStatus(anyString())).thenReturn("ACTIVATED");
    	requestHandlerRequestValidator.isValidRegistrationTypeAndUin(RegistrationType.ACTIVATED.name(), "1234");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidRegistrationTypeAndUinNestedElse() throws BaseCheckedException, IOException{
    	when(uinValidatorImpl.validateId(anyString())).thenReturn(false);
    	
    	requestHandlerRequestValidator.isValidRegistrationTypeAndUin(RegistrationType.ACTIVATED.name(), "1234");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidRegistrationTypeAndUinElse1() throws BaseCheckedException, IOException{
    	requestHandlerRequestValidator.isValidRegistrationTypeAndUin(null, "1234");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidRegistrationTypeAndUinElse2() throws BaseCheckedException, IOException{
    	requestHandlerRequestValidator.isValidRegistrationTypeAndUin("any registration", "1234");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidRegistrationTypeAndUinWithInvalidIDException() throws BaseCheckedException, IOException{
    	when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.retrieveIdrepoJsonStatus(anyString())).thenThrow(new InvalidIDException("errorcode", "message"));
    	requestHandlerRequestValidator.isValidRegistrationTypeAndUin(RegistrationType.RES_UPDATE.name(), "1234");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidRegistrationTypeAndUinWithNumberFormatException() throws BaseCheckedException, IOException{
    	when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.retrieveIdrepoJsonStatus(anyString())).thenThrow(new NumberFormatException("message"));
    	requestHandlerRequestValidator.isValidRegistrationTypeAndUin(RegistrationType.RES_UPDATE.name(), "1234");
    }
    
    @Test
    public void testIsValidVid() throws BaseCheckedException, IOException{
    	String str="vid";
    	when(vidValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.getUinByVid(anyString())).thenReturn(str);
    	boolean result=requestHandlerRequestValidator.isValidVid(ID);
    	assertTrue(result);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidVidElse() throws BaseCheckedException, IOException{
    	String str="vid";
    	when(vidValidatorImpl.validateId(anyString())).thenReturn(false);
    	when(utilities.getUinByVid(anyString())).thenReturn(str);
    	requestHandlerRequestValidator.isValidVid(ID);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidVidElse2() throws BaseCheckedException, IOException{
    	when(vidValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.getUinByVid(anyString())).thenReturn(null);
    	requestHandlerRequestValidator.isValidVid(ID);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidVidWithInvalidIDException() throws BaseCheckedException, IOException{
    	when(vidValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.getUinByVid(anyString())).thenThrow(new InvalidIDException("errorcode", "message"));
    	requestHandlerRequestValidator.isValidVid(ID);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidVidWithIdRepoAppException() throws BaseCheckedException, IOException{
    	when(vidValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.getUinByVid(anyString())).thenThrow(new IdRepoAppException("errorcode", "message"));
    	requestHandlerRequestValidator.isValidVid(ID);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidVidWithNumberFormatException() throws BaseCheckedException, IOException{
    	when(vidValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.getUinByVid(anyString())).thenThrow(new NumberFormatException("message"));
    	requestHandlerRequestValidator.isValidVid(ID);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidVidWithApisResourceAccessException() throws BaseCheckedException, IOException{
    	when(vidValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.getUinByVid(anyString())).thenThrow(new ApisResourceAccessException("message"));
    	requestHandlerRequestValidator.isValidVid(ID);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidVidWithVidCreationException() throws BaseCheckedException, IOException{
    	when(vidValidatorImpl.validateId(anyString())).thenReturn(true);
    	when(utilities.getUinByVid(anyString())).thenThrow(new VidCreationException("message"));
    	requestHandlerRequestValidator.isValidVid(ID);
    }
    
    @Test
    public void testIsValidIdType() throws Exception{
    	boolean result=requestHandlerRequestValidator.isValidIdType("UIN");
    	assertTrue(result);
    }
    
    @Test
    public void testIsValidIdType2() throws BaseCheckedException {
        boolean result = requestHandlerRequestValidator.isValidIdType("VID");
        assertTrue(result);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidIdTypeElse() throws BaseCheckedException {
        requestHandlerRequestValidator.isValidIdType(null);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidIdTypeElse2() throws BaseCheckedException {
        requestHandlerRequestValidator.isValidIdType("");
    }

    @Test
    public void testIsValidCardType() throws BaseCheckedException, IOException {
        boolean result = requestHandlerRequestValidator.isValidCardType(CardType.MASKED_UIN.name());
        assertTrue(result);
    }
    
    @Test
    public void testIsValidCardType2() throws BaseCheckedException, IOException {
        boolean result = requestHandlerRequestValidator.isValidCardType(CardType.UIN.name());
        assertTrue(result);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidCardTypeElse() throws BaseCheckedException {
        requestHandlerRequestValidator.isValidCardType("voter-id");
    }

    @Test(expected = BaseCheckedException.class)
    public void testIsValidCardTypeElse2() throws BaseCheckedException {
        requestHandlerRequestValidator.isValidCardType("");
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidCardTypeElse3() throws BaseCheckedException {
        requestHandlerRequestValidator.isValidCardType(null);
    }

    @Test
    public void testIsValidContact() throws BaseCheckedException, IOException {
        String EMAIL = "Email";
        boolean result = requestHandlerRequestValidator.isValidContactType(EMAIL, new LogDescription());
        assertTrue(result);
    }
    
    @Test
    public void testIsValidContact2() throws BaseCheckedException, IOException {
        String PHONE = "Phone";
        boolean result = requestHandlerRequestValidator.isValidContactType(PHONE, new LogDescription());
        assertTrue(result);
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidContactElse() throws BaseCheckedException {
        requestHandlerRequestValidator.isValidContactType("whatsapp", new LogDescription());
    }
    
    @Test(expected = BaseCheckedException.class)
    public void testIsValidContactElse2() throws BaseCheckedException {
        requestHandlerRequestValidator.isValidContactType(null, new LogDescription());
    }
}
