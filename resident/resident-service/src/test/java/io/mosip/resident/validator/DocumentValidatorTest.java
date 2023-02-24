package io.mosip.resident.validator;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.DocumentRequestDTO;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ProxyMasterdataService;
import reactor.util.function.Tuples;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.mosip.resident.constant.ResidentErrorCode.INVALID_INPUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Manoj SP
 *
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class DocumentValidatorTest {
	
	@InjectMocks
	private DocumentValidator validator;

	@Mock
	private RequestValidator requestValidator;

	@Mock
	private ProxyMasterdataService proxyMasterdataService;

	@Mock
	private VirusScanner<Boolean, InputStream> virusScanner;

	private MockEnvironment env = new MockEnvironment();
	
	@Before
	public void init() throws Exception {
		ReflectionTestUtils.setField(validator, "env", env);
		ReflectionTestUtils.setField(validator, "transactionIdRegex", "^[0-9]{10}$");
		ReflectionTestUtils.setField(validator, "documentIdRegex", "^[A-Za-z0-9-]{20,}$");
		when(proxyMasterdataService.getValidDocCatAndTypeList(any()))
				.thenReturn(Tuples.of(List.of("poi", "poa"), Map.of("poi", List.of("cob"), "poa", List.of("coa"))));
	}
	
	@Test
	public void testValidateRequestSuccess() throws ResidentServiceCheckedException {
		DocumentRequestDTO request = new DocumentRequestDTO();
		request.setDocCatCode("poi");
		request.setDocTypCode("cob");
		request.setLangCode("c");
		validator.validateRequest("1234567890", request.getDocCatCode(),request.getDocTypCode(),request.getLangCode());
	}

	@Test
	public void testNullDocCatCode() throws ResidentServiceCheckedException {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode(null);
			request.setDocTypCode("poi12");
			request.setLangCode("eng");
			validator.validateRequest("1234567890", request.getDocCatCode(),request.getDocTypCode(),request.getLangCode());
		} catch (InvalidInputException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorCode() + " --> " + INVALID_INPUT.getErrorMessage() + "docCatCode"), e.getMessage());
		}
	}
	
	@Test
	public void testBlankDocCatCode() throws ResidentServiceCheckedException {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("");
			validator.validateRequest("1234567890", request.getDocCatCode(),request.getDocTypCode(),request.getLangCode());
		} catch (InvalidInputException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorCode() + " --> " + INVALID_INPUT.getErrorMessage() + "docCatCode"), e.getMessage());		}
	}

	@Test
	public void testInvalidDocCatCode() throws ResidentServiceCheckedException {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("pop");
			request.setDocTypCode("cor");
			validator.validateRequest("1234567890", request.getDocCatCode(),request.getDocTypCode(),request.getLangCode());
		} catch (InvalidInputException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorCode() + " --> " + INVALID_INPUT.getErrorMessage() + "docCatCode"), e.getMessage());
		}
	}

	@Test
	public void testNullDocTypCode() throws ResidentServiceCheckedException {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("a");
			request.setDocTypCode(null);
			validator.validateRequest("1234567890", request.getDocCatCode(),request.getDocTypCode(),request.getLangCode());
		} catch (InvalidInputException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorCode() + " --> " + INVALID_INPUT.getErrorMessage() + "docTypCode"), e.getMessage());		
		}
	}
	
	@Test
	public void testBlankDocTypCode() throws ResidentServiceCheckedException {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("a");
			request.setDocTypCode("");
			validator.validateRequest("1234567890", request.getDocCatCode(),request.getDocTypCode(),request.getLangCode());
		} catch (InvalidInputException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorCode() + " --> " + INVALID_INPUT.getErrorMessage() + "docTypCode"), e.getMessage());
		}
	}

	@Test
	public void testInvalidDocTypCode() throws ResidentServiceCheckedException {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("poa");
			request.setDocTypCode("cor");
			validator.validateRequest("1234567890", request.getDocCatCode(),request.getDocTypCode(),request.getLangCode());
		} catch (InvalidInputException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorCode() + " --> " + INVALID_INPUT.getErrorMessage() + "docTypCode"), e.getMessage());
		}
	}

	@Test
	public void testNullLangCode() throws ResidentServiceCheckedException {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("poa");
			request.setDocTypCode("coa");
			request.setLangCode(null);
			validator.validateRequest("1234567890", request.getDocCatCode(),request.getDocTypCode(),request.getLangCode());
		} catch (InvalidInputException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorCode() + " --> " + INVALID_INPUT.getErrorMessage() + "langCode"), e.getMessage());
		}
	}
	
	@Test
	public void testBlankLangCode() throws ResidentServiceCheckedException {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("poi");
			request.setDocTypCode("cob");
			request.setLangCode(" ");
			validator.validateRequest("1234567890", request.getDocCatCode(),request.getDocTypCode(),request.getLangCode());
		} catch (ResidentServiceException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorCode() + " --> " + INVALID_INPUT.getErrorMessage() + "langCode"), e.getMessage());
		}
	}
	
	@Test
	public void testScanForVirusesSuccess() {
		env.setProperty(ResidentConstants.VIRUS_SCANNER_ENABLED, "true");
		when(virusScanner.scanFile(any(InputStream.class))).thenReturn(true);
		validator.scanForViruses(new MockMultipartFile("name", "a".getBytes()));
	}
	
	@Test
	public void testScanForVirusesDisabledSuccess() {
		env.setProperty(ResidentConstants.VIRUS_SCANNER_ENABLED, "false");
		validator.scanForViruses(new MockMultipartFile("name", "a".getBytes()));
	}
	
	@Test(expected = ResidentServiceException.class)
	public void testScanForVirusesFailed() {
		env.setProperty(ResidentConstants.VIRUS_SCANNER_ENABLED, "true");
		when(virusScanner.scanFile(any(InputStream.class))).thenThrow(new VirusScannerException());
		validator.scanForViruses(new MockMultipartFile("name", "a".getBytes()));
	}
	
	@Test
	public void testSupportsSuccess() {
		assertTrue(validator.supports(RequestWrapper.class));
	}
	
	@Test
	public void testSupportsFailed() {
		assertFalse(validator.supports(MockMultipartFile.class));
	}

	@Test
	public void testValidateGetDocumentByDocumentIdInputSuccess() {
		String transactionId = "1234567891";
		validator.validateTransactionIdForDocument(transactionId);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateGetDocumentByDocumentIdFailed() {
		String transactionId = "123a";
		validator.validateTransactionIdForDocument(transactionId);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDocumentIdAndTransactionId() {
		validator.validateDocumentIdAndTransactionId("d", "1a");
	}

	@Test
	public void testValidateDocumentIdAndTransactionIDSuccess(){
		validator.validateDocumentIdAndTransactionId(UUID.randomUUID().toString(), "1232323232");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDocumentId() {
		validator.validateDocumentId(null);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDocumentIdLessCharacterDocumentId() {
		validator.validateDocumentId("12");
	}
}