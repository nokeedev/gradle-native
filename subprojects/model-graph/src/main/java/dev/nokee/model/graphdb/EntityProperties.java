package dev.nokee.model.graphdb;

import lombok.val;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static dev.nokee.model.graphdb.NotFoundException.createPropertyNotFoundException;
import static java.util.Objects.requireNonNull;

final class EntityProperties implements Iterable<Map.Entry<String, Object>> {
	private final Map<String, Object> properties = new HashMap<>();
	private final long id;
	private final GraphEventNotifier eventNotifier;

	public EntityProperties(long id, GraphEventNotifier eventNotifier) {
		this.id = id;
		this.eventNotifier = eventNotifier;
	}

	public static EntityProperties of(long id) {
		return new EntityProperties(id, GraphEventNotifier.noOpNotifier());
	}

	public long getId() {
		return id;
	}

	public Object put(String key, Object value) {
		requireNonNull(key);
		val previousValue = properties.put(key, value);
		eventNotifier.firePropertyChangedEvent(builder -> builder.entityId(id).key(key).previousValue(previousValue).value(value));
		return previousValue;
	}

	public Object get(String key) {
		requireNonNull(key);
		if (!properties.containsKey(key)) {
			throw createPropertyNotFoundException(key);
		}
		return properties.get(key);
	}

	public Object getOrDefault(String key, Object defaultValue) {
		requireNonNull(key);
		return properties.getOrDefault(key, defaultValue);
	}

	public boolean has(String key) {
		requireNonNull(key);
		return properties.containsKey(key);
	}

	@Override
	public Iterator<Map.Entry<String, Object>> iterator() {
		return properties.entrySet().iterator();
	}
}
