package dev.nokee.model.graphdb.events;

import dev.nokee.model.graphdb.Entity;
import dev.nokee.model.graphdb.Graph;
import lombok.Builder;
import lombok.Value;

import javax.annotation.Nullable;

@Value
@Builder(builderClassName = "Builder")
public class PropertyChangedEvent {
	Graph graph;
	long entityId;
	String key;
	@Nullable Object previousValue;
	Object value;

	public Entity getEntity() {
		return graph.getEntityById(entityId);
	}
}
