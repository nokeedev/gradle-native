package dev.nokee.model.graphdb;

import java.util.Map;

public interface Entity {
	long getId();

	Map<String, Object> getAllProperties();
	void setProperty(String key, Object value);

	Object getProperty(String key);
	Object getProperty(String key, Object defaultValue);

	boolean hasProperty(String key);

	Entity property(String key, Object value);
}
