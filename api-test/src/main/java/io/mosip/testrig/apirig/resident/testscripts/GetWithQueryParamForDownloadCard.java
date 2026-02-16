package io.mosip.testrig.apirig.resident.testscripts;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import io.mosip.testrig.apirig.dto.OutputValidationDto;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.resident.utils.ResidentConfigManager;
import io.mosip.testrig.apirig.resident.utils.ResidentUtil;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.ReportUtil;
import io.mosip.testrig.apirig.utils.RestClient;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.restassured.response.Response;

public class GetWithQueryParamForDownloadCard extends ResidentUtil implements ITest {
	private static final Logger logger = Logger.getLogger(GetWithQueryParamForDownloadCard.class);
	protected String testCaseName = "";
	public Response response = null;
	public byte[] pdf=null;
	public String pdfAsText =null;
	public boolean sendEsignetToken = false;
	
	@BeforeClass
	public static void setLogLevel() {
		if (ResidentConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}
	
	/**
	 * get current testcaseName
	 */
	@Override
	public String getTestName() {
		return testCaseName;
	}

	/**
	 * Data provider class provides test case list
	 * 
	 * @return object of data provider
	 */
	@DataProvider(name = "testcaselist")
	public Object[] getTestCaseList(ITestContext context) {
		String ymlFile = context.getCurrentXmlTest().getLocalParameters().get("ymlFile");
		sendEsignetToken = context.getCurrentXmlTest().getLocalParameters().containsKey("sendEsignetToken");
		logger.info("Started executing yml: "+ymlFile);
		return getYmlTestData(ymlFile);
	}
	
	

	/**
	 * Test method for OTP Generation execution
	 * 
	 * @param objTestParameters
	 * @param testScenario
	 * @param testcaseName
	 * @throws Exception 
	 */
	@Test(dataProvider = "testcaselist")
	public void test(TestCaseDTO testCaseDTO) throws Exception, SecurityXSSException {
		testCaseName = testCaseDTO.getTestCaseName();
		testCaseName = ResidentUtil.isTestCaseValidForExecution(testCaseDTO);
		boolean isNegative = testCaseName.toLowerCase().contains("_neg");
		if (HealthChecker.signalTerminateExecution) {
			throw new SkipException(GlobalConstants.TARGET_ENV_HEALTH_CHECK_FAILED + HealthChecker.healthCheckFailureMapS);
		}
		
		if (testCaseDTO.getTestCaseName().contains("VID") || testCaseDTO.getTestCaseName().contains("Vid")) {
			if (!BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
					&& !BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
				throw new SkipException(GlobalConstants.VID_FEATURE_NOT_SUPPORTED);
			}
		}
		
		String[] templateFields = testCaseDTO.getTemplateFields();

		if (testCaseDTO.getTemplateFields() != null && templateFields.length > 0) {
			ArrayList<JSONObject> inputtestCases = AdminTestUtil.getInputTestCase(testCaseDTO);
			for (int i = 0; i < languageList.size(); i++) {
				pdf = getWithQueryParamAndCookieForPdf(ApplnURI + testCaseDTO.getEndPoint(),
						getJsonFromTemplate(inputtestCases.get(i).toString(), testCaseDTO.getInputTemplate()),
						COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName());
				Response response = RestClient.getPdfDownloadResponse();
				validatePdfOrError(response, pdf, isNegative, testCaseDTO);
			}
		}  
		
		else {
			pdf = getWithQueryParamAndCookieForPdf(ApplnURI + testCaseDTO.getEndPoint(), getJsonFromTemplate(testCaseDTO.getInput(), testCaseDTO.getInputTemplate()), COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName(), sendEsignetToken);
			Response response = RestClient.getPdfDownloadResponse();
			validatePdfOrError(response, pdf, isNegative, testCaseDTO);
		}
		
	}
	
	private void validatePdfOrError(Response response, byte[] pdf, boolean isNegative, TestCaseDTO testCaseDTO) throws AdminTestException {

	    String contentType = response != null ? response.getHeader("Content-Type") : null;
	    String rawResponse = pdf != null ? new String(pdf) : null;

	    if (!isNegative) {

	        if (contentType != null && contentType.contains("application/pdf")) {
	            try {
	            	PdfReader reader;
	            	try {
	                    //First opening pdf without password
	                    reader = new PdfReader(new ByteArrayInputStream(pdf));

	                    if (reader.isEncrypted()) {
	                        reader.close();
	                        throw new Exception("Encrypted PDF");
	                    }

	                    logger.info("Opened non-encrypted PDF");

	                } catch (Exception e) {

	                    // If encrypted, try with password
	                    String password = getPdfPassword();

	                    reader = new PdfReader(
	                            new ByteArrayInputStream(pdf),
	                            password.getBytes());

	                    logger.info("Opened password protected PDF");
	                }

	            	pdfAsText = PdfTextExtractor.getTextFromPage(reader, 1);

	                GlobalMethods.reportResponse(null, ApplnURI + testCaseDTO.getEndPoint(),
	                        "PDF Content:\n" + pdfAsText);

	                Map<String, List<OutputValidationDto>> outputValid =
	                        OutputValidationUtil.doJsonOutputValidation(
	                                "{\"Content-Type\":\"" + contentType + "\"}",
	                                getJsonFromTemplate(testCaseDTO.getOutput(), testCaseDTO.getOutputTemplate()),
	                                testCaseDTO,
	                                response.getStatusCode());

	                Reporter.log(ReportUtil.getOutputValidationReport(outputValid));

	            } catch (Exception e) {
	                Assert.fail("Invalid PDF received: " + e.getMessage());
	            }
	        } else {
	            Assert.fail("Expected PDF but received: " + contentType);
	        }
	    }

	    else {

	        if (rawResponse == null)
	            Assert.fail("Response is null");

	        GlobalMethods.reportResponse(null, ApplnURI + testCaseDTO.getEndPoint(),
	                ResidentUtil.formatJsonIfPossible(rawResponse));

	        Map<String, List<OutputValidationDto>> outputValid =
	                OutputValidationUtil.doJsonOutputValidation(
	                        rawResponse,
	                        getJsonFromTemplate(testCaseDTO.getOutput(), testCaseDTO.getOutputTemplate()),
	                        testCaseDTO,
	                        response.getStatusCode());

	        Reporter.log(ReportUtil.getOutputValidationReport(outputValid));

	        if (!OutputValidationUtil.publishOutputResult(outputValid))
	            throw new AdminTestException("Failed at output validation");
	    }
	}

	/**
	 * The method ser current test name to result
	 * 
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		result.setAttribute("TestCaseName", testCaseName);
	}	
}
