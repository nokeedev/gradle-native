package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.TypeAwareModelProjection;
import dev.nokee.model.graphdb.*;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

import static dev.nokee.utils.ProviderUtils.notDefined;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class DefaultModelProjection<T> implements TypeAwareModelProjection<T>, ModelProjection {
	@EqualsAndHashCode.Include private final Node delegate;
	@EqualsAndHashCode.Exclude private final ModelFactory factory;
	@EqualsAndHashCode.Exclude private ModelNode owner = null;

	public DefaultModelProjection(ModelFactory factory, Node delegate) {
		this.delegate = delegate;
		this.factory = factory;
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
		if (owner == null) {
			owner = factory.createNode(delegate.getSingleRelationship(DefaultModelNode.PROJECTION_RELATIONSHIP_TYPE, Direction.INCOMING).map(Relationship::getStartNode).orElseThrow(this::noProjectionOwnerException));
		}
		return owner;
	}

	private RuntimeException noProjectionOwnerException() {
		return new RuntimeException("Projection is not attached to any node.");
	}

	@Override
	public <T> void whenRealized(Class<T> type, Action<? super T> action) {
		requireNonNull(type);
		requireNonNull(action);
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
	public Class<T> getType() {
		return (Class<T>) ((ProjectionSpec) delegate.getProperty("spec")).getType();
	}

	@Override
	public void realize() {
		get(getType());
	}

	@Override
	public void finalizeProjection() {
		((ProjectionSpec) delegate.getProperty("spec")).finalizeProjection();
	}

	@Override
	public TypeAwareModelProjection<T> realizeOnFinalize() {
		((ProjectionSpec) delegate.getProperty("spec")).realizeOnFinalize();
		return this;
	}

	@Override
	public void whenFinalized(Action<? super T> action) {
		requireNonNull(action);
		((ProjectionSpec) delegate.getProperty("spec")).finalize(action);
	}

	@Override
	public <T> void whenFinalized(Class<T> type, Action<? super T> action) {
		requireNonNull(type);
		requireNonNull(action);
		if (!canBeViewedAs(type)) {
			throw new RuntimeException(String.format("Projection cannot be viewed as '%s'.", type.getSimpleName()));
		}

		((ProjectionSpec) delegate.getProperty("spec")).finalize(action);
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public T get() {
		return (T) get(getType());
	}

	@Override
	public void whenRealized(Action<? super T> action) {
		requireNonNull(action);
		whenRealized((Class<T>) getType(), action);
	}

	public static final class Builder implements ModelProjection.Builder {
		private final ProjectionSpec.Builder builder = ProjectionSpec.builder();
		private Graph graph;
		private ModelFactory modelFactory;

		public Builder graph(Graph graph) {
			this.graph = graph;
			return this;
		}

		@Override
		public <S> TypeAwareBuilder<S> type(Class<S> type) {
			builder.type(type);
			return new TypeAwareBuilder<>();
		}

		@Override
		public <S> TypeAwareBuilder<S> forProvider(NamedDomainObjectProvider<? extends S> provider) {
			builder.forProvider(provider);
			return new TypeAwareBuilder<>();
		}

		@Override
		public <S> TypeAwareBuilder<S> forInstance(S instance) {
			builder.forInstance(instance);
			return new TypeAwareBuilder<>();
		}

		public Builder owner(ModelNode owner) {
			builder.ownedBy(owner);
			return this;
		}

		public Builder registry(@Nullable NamedDomainObjectRegistry registry) {
			builder.registry(registry);
			return this;
		}

		public Builder modelFactory(ModelFactory modelFactory) {
			this.modelFactory = modelFactory;
			return this;
		}

		public Builder objectFactory(@Nullable ObjectFactory objectFactory) {
			builder.objectFactory(objectFactory);
			return this;
		}

		public final class TypeAwareBuilder<T> implements TypeAwareModelProjection.Builder<T> {
			@Override
			public <S> TypeAwareBuilder<S> type(Class<S> type) {
				builder.type(type);
				return new TypeAwareBuilder<>();
			}

			@Override
			public TypeAwareBuilder<T> forProvider(NamedDomainObjectProvider<? extends T> provider) {
				builder.forProvider(provider);
				return this;
			}

			@Override
			public TypeAwareBuilder<T> forInstance(T instance) {
				builder.forInstance(instance);
				return this;
			}

			public DefaultModelProjection<T> build() {
				return (DefaultModelProjection<T>) Builder.this.build();
			}
		}

		public DefaultModelProjection<?> build() {
			val projectionNode = graph.createNode().addLabel(Label.label("PROJECTION")).property("spec", builder.build());
			return new DefaultModelProjection(modelFactory, projectionNode);
		}
	}
}
