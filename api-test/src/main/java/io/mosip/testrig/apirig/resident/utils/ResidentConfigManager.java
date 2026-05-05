package io.mosip.testrig.apirig.resident.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.resident.testrunner.MosipTestRunner;
import io.mosip.testrig.apirig.utils.ConfigManager;

public class ResidentConfigManager extends ConfigManager{
	private static final Logger LOGGER = Logger.getLogger(ResidentConfigManager.class);

	public static void init() {
		Logger configManagerLogger = Logger.getLogger(ConfigManager.class);
		configManagerLogger.setLevel(Level.WARN);
		
		Map<String, Object> moduleSpecificPropertiesMap = new HashMap<>();
		// Load scope specific properties
		try {
			String path = MosipTestRunner.getGlobalResourcePath() + "/config/resident.properties";
			Properties props = getproperties(path);
			// Convert Properties to Map and add to moduleSpecificPropertiesMap.
			// When a value is blank in the file, fall back to an environment variable
			// so secrets (client secrets, keycloak passwords, db passwords, ...) can be
			// supplied at runtime without committing them.
			for (String key : props.stringPropertyNames()) {
				String value = props.getProperty(key);
				if (value == null || value.trim().isEmpty()) {
					String envValue = resolveFromEnv(key);
					if (envValue != null) {
						value = envValue;
						LOGGER.info("Resolved blank property '" + key + "' from environment.");
					}
				}
				// Never store null — downstream callers cast/use these as String
				// and would NPE. Preserve the original blank if nothing resolved.
				moduleSpecificPropertiesMap.put(key, (value == null) ? "" : value);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		// Add module specific properties as well.
		init(moduleSpecificPropertiesMap);
	}

	/**
	 * Looks up the value for the given property key in process environment.
	 * Tries the key as-is first, then an UPPER_SNAKE_CASE form (dots, dashes
	 * and slashes converted to underscores) for the typical CI convention.
	 * Returns null when neither variant is set or both are empty.
	 */
	private static String resolveFromEnv(String key) {
		String value = System.getenv(key);
		if (value != null && !value.trim().isEmpty()) {
			return value;
		}
		String upperKey = key.replaceAll("[\\.\\-/]", "_").toUpperCase();
		if (!upperKey.equals(key)) {
			value = System.getenv(upperKey);
			if (value != null && !value.trim().isEmpty()) {
				return value;
			}
		}
		return null;
	}

}