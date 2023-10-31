package io.mosip.resident.dto;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper = true)
public class IdentityDTO extends LinkedHashMap<String, Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6419255998877306908L;
	
	public static final String UIN ="UIN";
	public static final String EMAIL ="email";
	public static final String PHONE ="phone";
	public static final String YEAR_OF_BIRTH ="yearOfBirth";
	public static final String FULL_NAME ="fullName";
	public static final String FACE ="face";
	public static final String DATE_OF_BIRTH ="dateOfBirth";
	private Map<String, String> attributes = new HashMap<>(10);
	
	public String getFullName() {
		return this.getAttribute(FULL_NAME);
	}
	
	public String getEmail() {
		return this.getAttribute(EMAIL);
	}
	
	public String getPhone() {
		return this.getAttribute(PHONE);
	}
	
	public String getYearOfBirth() {
		return this.getAttribute(YEAR_OF_BIRTH);
	}
	
	public String getUIN() {
		return this.getAttribute(UIN);
	}
	
	public String getFace() {
		return this.getAttribute(FACE);
	}
	
	public String getDateOfBirth() {
		return this.getAttribute(DATE_OF_BIRTH);
	}
	
	public void setFullName(String value) {
		this.put(FULL_NAME, value);
	}
	
	public void setEmail(String value) {
		this.put(EMAIL, value);
	}
	
	
	public void setPhone(String value) {
		this.put(PHONE, value);
	}
	
	public void setYearOfBirth(String value) {
		this.put(YEAR_OF_BIRTH, value);
	}
	
	public void setUIN(String value) {
		this.put(UIN, value);
	}
	
	public void setFace(String value) {
		this.put(FACE, value);
	}
	
	public void setDateOfBirth(String value) {
		this.put(DATE_OF_BIRTH, value);
	}
	
	public String getAttribute(String attributeName) {
		return attributeName == null ? null : String.valueOf(this.get(attributeName));
	}
	
	public void setAttribute(String attributeName, String value) {
		this.put(attributeName, value);
	}
	
	public void putAllAttributes(Map<String, String> attributesMap) {
		if(attributesMap != null) {
			this.putAll(attributesMap);
		}
	}

}
