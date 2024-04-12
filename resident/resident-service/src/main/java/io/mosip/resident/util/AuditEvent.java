package io.mosip.resident.util;

public interface AuditEvent {

	public String getEventId();

	public String getType();

	public String getName();

	public String getDescription();

	public String getModuleId();

	public String getModuleName();

	public String getApplicationId();

	public String getApplicationName();

}
