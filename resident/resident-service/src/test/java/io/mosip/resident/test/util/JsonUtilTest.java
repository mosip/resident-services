package io.mosip.resident.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import io.mosip.resident.util.JsonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.mosip.resident.dto.JsonValue;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({})
public class JsonUtilTest {
	private String jsonString;
	private JSONObject jsonObject;

	@Before
	public void setUp() throws IOException {
		jsonString = "{\"identity\":{\"fullName\":[{\"language\":\"eng\",\"value\":\"firstName\"},{\"language\":\"ara\",\"value\":\"lastName\"}],\"dateOfBirth\":\"1996/01/01\",\"referenceIdentityNumber\":\"2323232323232323\",\"proofOfIdentity\":{\"value\":\"POI_Passport\",\"type\":\"DOC001\",\"format\":\"jpg\"},\"IDSchemaVersion\":1,\"phone\":\"9898989899\",\"age\":23,\"email\":\"sdf@sdf.co\"}}\r\n"
				+ "";

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
		System.out.println(result);
		assertTrue(jsonString.trim().equals(result));
	}

	@Test
	public void getJsonValuesTest() throws ReflectiveOperationException {
		JsonValue[] jsonvalues = JsonUtil.getJsonValues(JsonUtil.getJSONObject(jsonObject, "identity"), "fullName");
		assertEquals(2, jsonvalues.length);

	}

}
