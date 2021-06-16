package dev.nokee.model.internal;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Label;
import dev.nokee.model.graphdb.Node;
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

	public DefaultModelProjection(Graph graph, Node delegate) {
		this.graph = graph;
		this.delegate = delegate;
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
