package io.mosip.testrig.apirig.resident.utils;

import java.time.Instant;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.resident.testrunner.MosipTestRunner;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.RestClient;
import io.mosip.testrig.apirig.utils.SkipTestCaseHandler;
import io.restassured.response.Response;

public class ResidentUtil extends AdminTestUtil {

	private static final Logger logger = Logger.getLogger(ResidentUtil.class);
	
	protected static final String ESIGNET_PAYLOAD = "config/esignetPayload.json";
	
	public static void setLogLevel() {
		if (ResidentConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}
	
	public static String isTestCaseValidForExecution(TestCaseDTO testCaseDTO) {
		String testCaseName = testCaseDTO.getTestCaseName();
		
		int indexof = testCaseName.indexOf("_");
		String modifiedTestCaseName = testCaseName.substring(indexof + 1);

		addTestCaseDetailsToMap(modifiedTestCaseName, testCaseDTO.getUniqueIdentifier());
		
		if (MosipTestRunner.skipAll == true) {
			throw new SkipException(GlobalConstants.PRE_REQUISITE_FAILED_MESSAGE);
		}
		
		if (SkipTestCaseHandler.isTestCaseInSkippedList(testCaseName)) {
			throw new SkipException(GlobalConstants.KNOWN_ISSUES);
		}
		
		if ((ResidentConfigManager.isInServiceNotDeployedList(GlobalConstants.ESIGNET))
				&& BaseTestCase.currentModule.equalsIgnoreCase("resident") && testCaseName.contains("_SignJWT_")) {
			throw new SkipException("esignet module is not deployed");
		}

		if ((ResidentConfigManager.isInServiceNotDeployedList(GlobalConstants.ESIGNET))
				&& BaseTestCase.currentModule.equalsIgnoreCase("resident")
				&& (testCaseDTO.getRole() != null && (testCaseDTO.getRole().equalsIgnoreCase("residentNew")
						|| testCaseDTO.getRole().equalsIgnoreCase("residentNewVid")))) {
			throw new SkipException("esignet module is not deployed");
		}
		if (BaseTestCase.currentModule.equalsIgnoreCase(GlobalConstants.RESIDENT)) {
			if (testCaseDTO.getRole() != null && (testCaseDTO.getRole().equalsIgnoreCase(GlobalConstants.RESIDENTNEW)
					|| testCaseDTO.isValidityCheckRequired())) {
				if (testCaseName.contains("uin") || testCaseName.contains("UIN") || testCaseName.contains("Uin")) {
					if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
							&& BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {
						throw new SkipException("Idtype UIN not supported skipping the testcase");
					}
				}
			} else if (testCaseDTO.getRole() != null && (testCaseDTO.getRole().equalsIgnoreCase("residentNewVid")
					|| testCaseDTO.isValidityCheckRequired())) {
				if (testCaseName.contains("vid") || testCaseName.contains("VID") || testCaseName.contains("Vid")) {
					if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
							&& BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
						throw new SkipException("Idtype VID not supported skipping the testcase");
					}
				}
			}
		}
		
		return testCaseName;
	}
	
	public static String inputstringKeyWordHandeler(String jsonString, String testCaseName) {
		if (jsonString.contains(GlobalConstants.TIMESTAMP)) {
			jsonString = replaceKeywordValue(jsonString, GlobalConstants.TIMESTAMP, generateCurrentUTCTimeStamp());
		}
		
		if (jsonString.contains("$CLAIMSFROMCONFIG$")) {
			jsonString = replaceKeywordValue(jsonString, "$CLAIMSFROMCONFIG$", getValueFromConfigActuator());
		}
		
		if (jsonString.contains("$OIDCCLIENT$")) {
			jsonString = replaceKeywordValue(jsonString, "$OIDCCLIENT$",
					getValueFromActuator(GlobalConstants.RESIDENT_DEFAULT_PROPERTIES, "mosip.iam.module.clientID"));
		}
		
		if (jsonString.contains("$IDPCLIENTPAYLOAD$")) {
			String clientId = getValueFromActuator(GlobalConstants.RESIDENT_DEFAULT_PROPERTIES,
					"mosip.iam.module.clientID");
			String esignetBaseURI = getValueFromActuator(GlobalConstants.RESIDENT_DEFAULT_PROPERTIES,
					"mosip.iam.token_endpoint");
			int idTokenExpirySecs = Integer
					.parseInt(getValueFromEsignetActuator(ResidentConfigManager.getEsignetActuatorPropertySection(),
							GlobalConstants.MOSIP_ESIGNET_ID_TOKEN_EXPIRE_SECONDS));

			Instant instant = Instant.now();

			logger.info("Current Instant: " + instant);

			long epochValue = instant.getEpochSecond();

			org.json.simple.JSONObject payloadBody = getRequestJson(ESIGNET_PAYLOAD);
			payloadBody.put("sub", clientId);
			payloadBody.put("iss", clientId);
			payloadBody.put("aud", esignetBaseURI);
			payloadBody.put("exp", epochValue + idTokenExpirySecs);
			payloadBody.put("iat", epochValue);

			jsonString = replaceKeywordValue(jsonString, "$IDPCLIENTPAYLOAD$",
					encodeBase64(payloadBody.toString()));
		}
		
		
		return jsonString;
		
	}
	
	public static String replaceKeywordValue(String jsonString, String keyword, String value) {
		if (value != null && !value.isEmpty())
			return jsonString.replace(keyword, value);
		else
			throw new SkipException("Marking testcase as skipped as required fields are empty " + keyword);
	}
	
	public static JSONArray esignetActuatorResponseArray = null;

	public static String getValueFromEsignetActuator(String section, String key) {
		String url = ResidentConfigManager.getEsignetBaseUrl() + ResidentConfigManager.getproperty("actuatorEsignetEndpoint");
		String actuatorCacheKey = url + section + key;
		String value = actuatorValueCache.get(actuatorCacheKey);
		if (value != null && !value.isEmpty())
			return value;

		try {
			if (esignetActuatorResponseArray == null) {
				Response response = null;
				JSONObject responseJson = null;
				response = RestClient.getRequest(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
				responseJson = new JSONObject(response.getBody().asString());
				esignetActuatorResponseArray = responseJson.getJSONArray("propertySources");
			}

			for (int i = 0, size = esignetActuatorResponseArray.length(); i < size; i++) {
				JSONObject eachJson = esignetActuatorResponseArray.getJSONObject(i);
				if (eachJson.get("name").toString().contains(section)) {
					value = eachJson.getJSONObject(GlobalConstants.PROPERTIES).getJSONObject(key)
							.get(GlobalConstants.VALUE).toString();
					if (ResidentConfigManager.IsDebugEnabled())
						logger.info("Actuator: " + url + " key: " + key + " value: " + value);
					break;
				}
			}
			actuatorValueCache.put(actuatorCacheKey, value);

			return value;
		} catch (Exception e) {
			logger.error(GlobalConstants.EXCEPTION_STRING_2 + e);
			return value;
		}

	}
	
	public static JSONArray configActuatorResponseArray = null;
	
	public static String getValueFromConfigActuator() {

		String url = ApplnURI + ResidentConfigManager.getproperty("actuatorEndpoint");

		String actuatorCacheKey = url + "mosip.iam.module.login_flow.claims";

		String claims = actuatorValueCache.get(actuatorCacheKey);

		if (claims != null && !claims.isEmpty())
			return claims;

		try {
			if (configActuatorResponseArray == null) {
				Response response = null;
				JSONObject responseJson = null;
				response = RestClient.getRequest(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
				GlobalMethods.reportResponse(response.getHeaders().asList().toString(), url, response);

				responseJson = new JSONObject(response.getBody().asString());
				configActuatorResponseArray = responseJson.getJSONArray("propertySources");
			}

			for (int i = 0, size = configActuatorResponseArray.length(); i < size; i++) {
				JSONObject eachJson = configActuatorResponseArray.getJSONObject(i);
				if (eachJson.get("name").toString().contains(GlobalConstants.RESIDENT_DEFAULT_PROPERTIES)) {
					String claimVal = eachJson.getJSONObject(GlobalConstants.PROPERTIES)
							.getJSONObject("mosip.iam.module.login_flow.claims").getString(GlobalConstants.VALUE);
					JSONObject claimJson = new JSONObject(claimVal);
					claims = claimJson.getJSONObject("userinfo").toString();
					break;
				}
			}

			actuatorValueCache.put(actuatorCacheKey, claims);

			return claims;
		} catch (Exception e) {
			logger.error(GlobalConstants.EXCEPTION_STRING_2 + e);
			return claims;
		}

	}
	
}