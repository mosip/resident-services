package io.mosip.resident.util;

import static io.mosip.resident.util.IdSchemaUtil.RESPONSE;
import static io.mosip.resident.util.IdSchemaUtil.SCHEMA_JSON;
import static io.mosip.resident.util.IdSchemaUtil.SCHEMA_VERSION_QUERY_PARAM;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.exception.ApisResourceAccessException;

/**
 * @author Abubacker Siddik
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*" })
@PrepareForTest({ DateUtils.class })
public class IdSchemaUtilTest {

	@InjectMocks
	private IdSchemaUtil idSchemaUtil;

	@Mock
	private Environment env;

	@Mock
	private ResidentServiceRestClient restClientService;

	private String responseJson;

	@Before
	public void setUp() throws Exception {
		responseJson = "{\n" + "    \"id\": null,\n" + "    \"version\": null,\n"
				+ "    \"responsetime\": \"2022-01-11T06:08:02.307Z\",\n" + "    \"metadata\": null,\n"
				+ "    \"response\": {\n"
				+ "        \"schemaJson\": \"{\\\"$schema\\\":\\\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\\\",\\\"description\\\":\\\"MOSIP Sample identity\\\",\\\"additionalProperties\\\":false,\\\"title\\\":\\\"MOSIP identity\\\",\\\"type\\\":\\\"object\\\",\\\"definitions\\\":{\\\"simpleType\\\":{\\\"uniqueItems\\\":true,\\\"additionalItems\\\":false,\\\"type\\\":\\\"array\\\",\\\"items\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"language\\\",\\\"value\\\"],\\\"properties\\\":{\\\"language\\\":{\\\"type\\\":\\\"string\\\"},\\\"value\\\":{\\\"type\\\":\\\"string\\\"}}}},\\\"documentType\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"format\\\":{\\\"type\\\":\\\"string\\\"},\\\"type\\\":{\\\"type\\\":\\\"string\\\"},\\\"value\\\":{\\\"type\\\":\\\"string\\\"},\\\"refNumber\\\":{\\\"type\\\":\\\"string\\\"}}},\\\"biometricsType\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"format\\\":{\\\"type\\\":\\\"string\\\"},\\\"version\\\":{\\\"type\\\":\\\"number\\\",\\\"minimum\\\":0},\\\"value\\\":{\\\"type\\\":\\\"string\\\"}}}},\\\"properties\\\":{\\\"identity\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"IDSchemaVersion\\\",\\\"fullName\\\",\\\"dateOfBirth\\\",\\\"gender\\\",\\\"addressLine1\\\",\\\"addressLine2\\\",\\\"addressLine3\\\",\\\"region\\\",\\\"province\\\",\\\"city\\\",\\\"zone\\\",\\\"postalCode\\\",\\\"phone\\\",\\\"email\\\",\\\"proofOfIdentity\\\",\\\"individualBiometrics\\\"],\\\"properties\\\":{\\\"proofOfAddress\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"gender\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"city\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"postalCode\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[(?i)A-Z0-9]{5}$|^NA$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"proofOfException-1\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"referenceIdentityNumber\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^([0-9]{10,30})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"kyc\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"individualBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"province\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"zone\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"proofOfDateOfBirth\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"addressLine1\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"addressLine2\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"residenceStatus\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"kyc\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"addressLine3\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"email\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[A-Za-z0-9_\\\\\\\\-]+(\\\\\\\\.[A-Za-z0-9_]+)*@[A-Za-z0-9_-]+(\\\\\\\\.[A-Za-z0-9_]+)*(\\\\\\\\.[a-zA-Z]{2,})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerRID\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"fullName\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"dateOfBirth\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])\\/([0][1-9]|1[0-2])\\/([0][1-9]|[1-2][0-9]|3[01])$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"individualAuthBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"introducerUIN\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"proofOfIdentity\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"IDSchemaVersion\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"none\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"number\\\",\\\"fieldType\\\":\\\"default\\\",\\\"minimum\\\":0},\\\"proofOfException\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"phone\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[+]*([0-9]{1})([0-9]{9})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerName\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"proofOfRelationship\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"UIN\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"none\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"region\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"}}}}}\",\n"
				+ "        \"id\": \"1001\",\n" + "        \"effectiveFrom\": \"2021-12-22T13:45:46.303Z\",\n"
				+ "        \"status\": \"PUBLISHED\"\n" + "    },\n" + "    \"errors\": null\n" + "}";
	}

	@Test
	public void loadDefaultFieldsTest() throws Exception {
		Double schemaVersion = 0.1d;
		when(restClientService.getApi(ApiName.MIDSCHEMAURL, (List) null, Lists.newArrayList(SCHEMA_VERSION_QUERY_PARAM),
				Lists.newArrayList(schemaVersion), String.class)).thenReturn(responseJson);

		List<Map<String, String>> schemaMap = idSchemaUtil.loadDefaultFields(schemaVersion);
		assertEquals(schemaMap.get(0).get("id"), "proofOfAddress");
		assertEquals(schemaMap.get(0).get("type"), "#/definitions/documentType");
		assertEquals(schemaMap.get(1).get("id"), "gender");
		assertEquals(schemaMap.get(1).get("type"), "#/definitions/simpleType");
		assertEquals(schemaMap.get(2).get("id"), "city");
		assertEquals(schemaMap.get(2).get("type"), "#/definitions/simpleType");
		assertEquals(schemaMap.get(10).get("id"), "addressLine1");
		assertEquals(schemaMap.get(10).get("type"), "#/definitions/simpleType");
		assertEquals(schemaMap.get(13).get("id"), "addressLine3");
		assertEquals(schemaMap.get(13).get("type"), "#/definitions/simpleType");
		assertEquals(schemaMap.get(16).get("id"), "dateOfBirth");
		assertEquals(schemaMap.get(16).get("type"), "string");
		assertEquals(schemaMap.get(17).get("id"), "individualAuthBiometrics");
		assertEquals(schemaMap.get(17).get("type"), "#/definitions/biometricsType");
		assertEquals(schemaMap.get(20).get("id"), "phone");
		assertEquals(schemaMap.get(20).get("type"), "string");
		assertEquals(schemaMap.get(23).get("id"), "region");
		assertEquals(schemaMap.get(23).get("type"), "#/definitions/simpleType");

		verify(restClientService, times(1)).getApi(ApiName.MIDSCHEMAURL, (List) null,
				Lists.newArrayList(SCHEMA_VERSION_QUERY_PARAM), Lists.newArrayList(schemaVersion), String.class);
	}

	@Test(expected = ApisResourceAccessException.class)
	public void throwApiResourceAccessIfResponseIsNull() throws Exception {
		Double schemaVersion = 0.1d;
		when(restClientService.getApi(ApiName.MIDSCHEMAURL, (List) null, Lists.newArrayList(SCHEMA_VERSION_QUERY_PARAM),
				Lists.newArrayList(schemaVersion), String.class)).thenReturn(null);
		idSchemaUtil.loadDefaultFields(schemaVersion);
	}

	@Test
	public void testIfIdSchemaMapIsUsedAndRestClientNeverInvoked()
			throws JSONException, ApisResourceAccessException, IOException {
		Double schemaVersion = 0.1d;
		JSONObject jsonObject = new JSONObject(responseJson);
		JSONObject respObj = (JSONObject) jsonObject.get(RESPONSE);
		String responseString = respObj != null ? (String) respObj.get(SCHEMA_JSON) : null;
		Map<Double, String> idschema = new HashMap<>();
		idschema.put(schemaVersion, responseString);
		ReflectionTestUtils.setField(idSchemaUtil, "idschema", idschema);

		idSchemaUtil.loadDefaultFields(schemaVersion);
		verify(restClientService, never()).getApi(ApiName.MIDSCHEMAURL, (List) null,
				Lists.newArrayList(SCHEMA_VERSION_QUERY_PARAM), Lists.newArrayList(schemaVersion), String.class);
	}

}
