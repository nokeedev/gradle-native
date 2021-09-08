package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelProperty;
import dev.nokee.model.core.TypeAwareModelProjection;
import dev.nokee.model.streams.ModelStream;
import dev.nokee.model.streams.Topic;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static dev.nokee.model.internal.ModelSpecs.projectionOf;
import static java.util.Objects.requireNonNull;

final class DefaultModelObject<T> implements ModelObject<T>, Callable<Object> {
	private final ModelFactory factory;
	private final ModelNode node;
	private final TypeAwareModelProjection<T> projection;
	private final Topic<ModelProperty<?>> propertiesTopic = new Topic<ModelProperty<?>>() {
		@Override
		public Stream<ModelProperty<?>> get() {
			@SuppressWarnings("unchecked")
			val result = node.getChildNodes().flatMap(ModelNode::getProjections).map(TypeAwareModelProjection.class::cast).map(factory::createObject).map(it -> new DefaultModelProperty<>((ModelObject<? extends Object>) it));
			return result;
		}
	};

	public DefaultModelObject(ModelFactory factory, TypeAwareModelProjection<T> projection) {
		this.factory = factory;
		this.node = projection.getOwner();
		this.projection = projection;
	}

	@Override
	public ModelStream<ModelProperty<?>> getProperties() {
		return ModelStream.of(propertiesTopic);
	}

	@Override
	public <S> ModelProperty<S> property(String name, Class<S> type) {
		requireNonNull(name);
		requireNonNull(type);
		@SuppressWarnings("unchecked")
		val projection = (TypeAwareModelProjection<S>) node.get(name).getProjections().filter(projectionOf(type)).findFirst()
			.orElseThrow(() -> new RuntimeException("Property is not known on this object."));
		return new DefaultModelProperty<>(factory.createObject(projection));
	}

	@Override
	public <S> ModelObject<S> as(Class<S> type) {
		val projection = node.getProjections().filter(projectionOf(type))
			.map(it -> (TypeAwareModelProjection<S>) it)
			.findFirst()
			.orElseThrow(() -> new RuntimeException(String.format("Object is not of type '%s'.", type.getSimpleName())));
		return factory.createObject(projection);
	}

	@Override
	public boolean instanceOf(Class<?> type) {
		return projection.canBeViewedAs(type); // FIXME: not exactly right
	}

	@Override
	public Optional<ModelObject<?>> getParent() {
		@SuppressWarnings("unchecked")
		val result = (Optional<ModelObject<?>>) node.getParent().flatMap(it -> it.getProjections().findFirst())
			.map(TypeAwareModelProjection.class::cast).map(factory::createObject);
		return result;
	}

	@Override
	public <S> ModelProperty<S> newProperty(Object identity, Class<S> type) {
		val node = getOrCreateChildNode(identity);
		val projection = node.getProjections().filter(projectionOf(type))
			.map(it -> (TypeAwareModelProjection<S>) it)
			.findFirst()
			.orElseGet(() -> node.newProjection(builder -> builder.type(type)));
		val result = new DefaultModelProperty<>(factory.createObject(projection));
		propertiesTopic.accept(result);
		return result;
	}

	private ModelNode getOrCreateChildNode(Object identity) {
		// TODO: Assert identity is same or less info than selected node's identity:
		//  because if node was created with a String but trying to reference using machines.host... the identity has less information so we may be facing an out-of-order access.
		return node.getChildNodes().filter(ofName(identity)).findFirst().orElseGet(() -> node.newChildNode(identity));
	}

	private static Predicate<ModelNode> ofName(Object identity) {
		return it -> nameOf(identity).equals(it.getName());
	}

	// TODO:
	private static String nameOf(Object identity) {
		if (identity instanceof Named) {
			return ((Named) identity).getName();
		} else {
			return identity.toString();
		}
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		requireNonNull(transformer);
		return asProvider().map(transformer);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		requireNonNull(transformer);
		return asProvider().flatMap(transformer);
	}
	public Provider<T> asProvider() {
		return projection.as(getType());
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
		return new DefaultDomainObjectIdentifier(projection);
	}

	@Override
	public Class<T> getType() {
		return projection.getType();
	}

	@Override
	public ModelObject<T> configure(Action<? super T> action) {
		projection.whenRealized(action);
		return this;
	}

	@Override
	public ModelObject<T> configure(Consumer<? super ModelObject<? extends T>> action) {
		action.accept(this);
		return this;
	}

	@Override
	public Object call() throws Exception {
		return asProvider();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof DefaultModelObject) {
			DefaultModelObject<?> that = (DefaultModelObject<?>) o;
			return Objects.equals(projection, that.projection);
		} else if (o instanceof DefaultModelProperty) {
			return equals(((DefaultModelProperty)o).delegate); // unwrap
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(projection);
	}
}
