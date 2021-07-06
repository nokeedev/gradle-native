package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.*;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.Provider;

import static dev.nokee.utils.ProviderUtils.notDefined;

@EqualsAndHashCode
final class DefaultModelProjection implements ModelProjection {
	@EqualsAndHashCode.Exclude private final Graph graph;
	@EqualsAndHashCode.Include private final Node delegate;
	@EqualsAndHashCode.Exclude private final ModelNodeFactory nodeFactory;

	public DefaultModelProjection(Graph graph, Node delegate) {
		this.graph = graph;
		this.delegate = delegate;
		this.nodeFactory = new DefaultModelNodeFactory(graph);
	}

	@Override
	public <T> boolean canBeViewedAs(Class<T> type) {
		return ((ProjectionSpec) delegate.getProperty("spec")).canBeViewedAs(type);
	}

	public <T> Provider<T> as(Class<T> type) {
		if (canBeViewedAs(type)) {
			return ((ProjectionSpec) delegate.getProperty("spec")).get(Provider.class);
		} else {
			return notDefined();
		}
	}

	@Override
	public ModelNode getOwner() {
		return nodeFactory.create(delegate.getSingleRelationship(DefaultModelNode.PROJECTION_RELATIONSHIP_TYPE, Direction.INCOMING).map(Relationship::getStartNode).orElseThrow(this::noProjectionOwnerException));
	}

	private RuntimeException noProjectionOwnerException() {
		return new RuntimeException("Projection is not attached to any node.");
	}

	@Override
	public <T> void whenRealized(Class<T> type, Action<? super T> action) {
		if (!canBeViewedAs(type)) {
			throw new RuntimeException();
		}

		// TODO: Add configuration node
		((ProjectionSpec) delegate.getProperty("spec")).configure(action);
	}

	@Override
	public <T> T get(Class<T> type) {
		if (canBeViewedAs(type)) {
			return ((ProjectionSpec) delegate.getProperty("spec")).get(type);
		} else {
			throw new RuntimeException(""); // TODO: Throw meaningful exception
		}
	}

	Node getDelegate() {
		return delegate;
	}

	@Override
	public Class<?> getType() {
		return ((ProjectionSpec) delegate.getProperty("spec")).getType();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder implements ModelProjection.Builder {
		private final ProjectionSpec.Builder builder = ProjectionSpec.builder();
		private Graph graph;

		public Builder graph(Graph graph) {
			this.graph = graph;
			return this;
		}

		@Override
		public Builder type(Class<?> type) {
			builder.type(type);
			return this;
		}

		@Override
		public Builder forProvider(NamedDomainObjectProvider<?> domainObjectProvider) {
			builder.forProvider(domainObjectProvider);
			return this;
		}

		@Override
		public Builder forInstance(Object instance) {
			builder.forInstance(instance);
			return this;
		}

		public DefaultModelProjection build() {
			val projectionNode = graph.createNode().addLabel(Label.label("PROJECTION")).property("spec", builder.build());
			return new DefaultModelProjection(graph, projectionNode);
		}
	}
}
