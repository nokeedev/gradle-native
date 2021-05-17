package dev.nokee.model.internal;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Node;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
final class DefaultModelProjection implements ModelProjection {
	@EqualsAndHashCode.Exclude private final Graph graph;
	@EqualsAndHashCode.Include private final Node delegate;

	public DefaultModelProjection(Graph graph, Node delegate) {
		this.graph = graph;
		this.delegate = delegate;
	}

	@Override
	public <T> boolean canBeViewedAs(Class<T> type) {
		return ((ProjectionSpec) delegate.getProperty("spec")).canBeViewedAs(type);
	}

	@Override
	public <T> T get(Class<T> type) {
		return ((ProjectionSpec) delegate.getProperty("spec")).get(type);
	}
}
