package io.mosip.resident.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.resident.dto.JsonValue;
import io.mosip.resident.util.JsonUtil;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({})
public class JsonUtilTest {
	private JSONObject jsonObject;
	private String jsonString = "{\"identity\":{\"fullName\":[{\"language\":\"eng\",\"value\":\"firstName\"},{\"language\":\"ara\",\"value\":\"lastName\"}],\"dateOfBirth\":\"1996/01/01\",\"referenceIdentityNumber\":\"2323232323232323\",\"proofOfIdentity\":{\"value\":\"POI_Passport\",\"type\":\"DOC001\",\"format\":\"jpg\"},\"IDSchemaVersion\":1,\"phone\":\"9898989899\",\"age\":23,\"email\":\"sdf@sdf.co\"}}\r\n"
			+ "";

	@Before
	public void setUp() throws IOException {

		jsonObject = JsonUtil.readValue(jsonString, JSONObject.class);
	}

	@Test
	public void getJSONObjectTest() throws IOException {

		JSONObject result = JsonUtil.getJSONObject(jsonObject, "identity");
		assertEquals("9898989899", result.get("phone"));

	}

	@Test
	public void getJSONArrayTest() {
		JSONArray jsonArray = JsonUtil.getJSONArray(JsonUtil.getJSONObject(jsonObject, "identity"), "fullName");
		JSONObject result = JsonUtil.getJSONObjectFromArray(jsonArray, 0);
		String firstName = JsonUtil.getJSONValue(result, "value");
		assertEquals("firstName", firstName);
	}

	@Test
	public void testJsonUtilWriteValue() throws IOException {
		String result = JsonUtil.writeValueAsString(jsonObject);
		assertTrue(jsonString.trim().equals(result));
	}

	@Test
	public void getJsonValuesTest() throws ReflectiveOperationException {
		JsonValue[] jsonvalues = JsonUtil.getJsonValues(JsonUtil.getJSONObject(jsonObject, "identity"), "fullName");
		assertEquals(2, jsonvalues.length);

	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void convertValueTest() {
		Map<String, Object> convertedValue = JsonUtil.convertValue(jsonObject, Map.class);
		Map<String, Object> identityMap = JsonUtil.convertValue(convertedValue.get("identity"), Map.class);
		assertEquals("2323232323232323", identityMap.get("referenceIdentityNumber"));
	}
	
	@Test
	public void convertValueTypeReferenceTest() {
		Map<String, Object> convertedValue = JsonUtil.convertValue(jsonObject,
				new TypeReference<Map<String, Object>>() {
				});
		Map<String, Object> identityMap = JsonUtil.convertValue(convertedValue.get("identity"),
				new TypeReference<Map<String, Object>>() {
				});
		assertEquals("2323232323232323", identityMap.get("referenceIdentityNumber"));
	}
	
	@Test
	public void readValueTypeReferenceTest() throws IOException {
		Map<String, Map<String, Object>> readValue = JsonUtil.readValue(jsonString, new TypeReference<Map<String, Map<String, Object>>>() {
				});
		assertEquals("2323232323232323", readValue.get("identity").get("referenceIdentityNumber"));
	}

}
