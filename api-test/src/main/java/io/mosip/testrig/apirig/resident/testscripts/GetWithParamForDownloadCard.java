package io.mosip.testrig.apirig.resident.testscripts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.resident.utils.ResidentConfigManager;
import io.mosip.testrig.apirig.resident.utils.ResidentUtil;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.restassured.response.Response;

public class GetWithParamForDownloadCard extends ResidentUtil implements ITest {
	private static final Logger logger = Logger.getLogger(GetWithParamForDownloadCard.class);
	protected String testCaseName = "";
	public Response response = null;
	public byte[] pdf=null;
	public String pdfAsText =null;
	public boolean sendEsignetToken = false;
	public boolean auditLogCheck = false;
	
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
	public void test(TestCaseDTO testCaseDTO) throws AdminTestException, SecurityXSSException {	
		testCaseName = testCaseDTO.getTestCaseName();
		testCaseName = ResidentUtil.isTestCaseValidForExecution(testCaseDTO);
		if (HealthChecker.signalTerminateExecution) {
			throw new SkipException(GlobalConstants.TARGET_ENV_HEALTH_CHECK_FAILED + HealthChecker.healthCheckFailureMapS);
		}
		
		if (testCaseDTO.getTestCaseName().contains("VID") || testCaseDTO.getTestCaseName().contains("Vid")) {
			if (!BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
					&& !BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
				throw new SkipException(GlobalConstants.VID_FEATURE_NOT_SUPPORTED);
			}
		}
		auditLogCheck = testCaseDTO.isAuditLogCheck();
		pdf = getWithPathParamAndCookieForPdf(ApplnURI + testCaseDTO.getEndPoint(), getJsonFromTemplate(testCaseDTO.getInput(), testCaseDTO.getInputTemplate()), auditLogCheck, COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName(), sendEsignetToken);
		PdfReader pdfReader = null;
		ByteArrayInputStream bIS = null;
		try {
			bIS = new ByteArrayInputStream(pdf);
			pdfReader = new PdfReader(bIS);
			pdfAsText = PdfTextExtractor.getTextFromPage(pdfReader, 1);
		} catch (IOException e) {
			Reporter.log("Exception : " + e.getMessage());
		} finally {
			AdminTestUtil.closeByteArrayInputStream(bIS);
			AdminTestUtil.closePdfReader(pdfReader);
		}
		 
		 if(pdf!=null && (new String(pdf).contains("errors")|| pdfAsText == null)) {
			 GlobalMethods.reportResponse(null, ApplnURI + testCaseDTO.getEndPoint(), "Not able to download UIN Card");
		 }
		 else {
			 GlobalMethods.reportResponse(null, ApplnURI + testCaseDTO.getEndPoint(), pdfAsText);
		 }
				
		
       
	}

	/**
	 * The method ser current test name to result
	 * 
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		try {
			Field method = TestResult.class.getDeclaredField("m_method");
			method.setAccessible(true);
			method.set(result, result.getMethod().clone());
			BaseTestMethod baseTestMethod = (BaseTestMethod) result.getMethod();
			Field f = baseTestMethod.getClass().getSuperclass().getDeclaredField("m_methodName");
			f.setAccessible(true);
			f.set(baseTestMethod, testCaseName);
		} catch (Exception e) {
			Reporter.log("Exception : " + e.getMessage());
		}
	}	
}
