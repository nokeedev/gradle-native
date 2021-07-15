package dev.nokee.model.graphdb;

import java.util.Map;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toMap;

abstract class AbstractEntity implements Entity {
	private final EntityProperties properties;

	protected AbstractEntity(EntityProperties properties) {
		this.properties = properties;
	}

	@Override
	public long getId() {
		return properties.getId();
	}

	@Override
	public Map<String, Object> getAllProperties() {
		return stream(properties).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	@Override
	public Entity property(String key, Object value) {
		setProperty(key, value);
		return this;
	}

	@Override
	public Object getProperty(String key) {
		return properties.get(key);
	}

	@Override
	public Object getProperty(String key, Object defaultValue) {
		return properties.getOrDefault(key, defaultValue);
	}

	@Override
	public boolean hasProperty(String key) {
		return properties.has(key);
	}
}
