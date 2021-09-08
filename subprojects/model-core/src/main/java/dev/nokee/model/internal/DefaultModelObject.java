package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelProperty;
import dev.nokee.model.core.TypeAwareModelProjection;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static dev.nokee.model.internal.ModelSpecs.projectionOf;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode(callSuper = false)
final class DefaultModelObject<T> implements ModelObject<T>, Callable<Object> {
	@EqualsAndHashCode.Exclude private final ModelNode node;
	@EqualsAndHashCode.Include private final TypeAwareModelProjection<T> projection;

	public DefaultModelObject(TypeAwareModelProjection<T> projection) {
		this.node = projection.getOwner();
		this.projection = projection;
	}

	@Override
	public <S> ModelProperty<S> property(String name, Class<S> type) {
		requireNonNull(name);
		requireNonNull(type);
		@SuppressWarnings("unchecked")
		val projection = (TypeAwareModelProjection<S>) node.get(name).getProjections().filter(it -> it.canBeViewedAs(type)).findFirst()
			.orElseThrow(() -> new RuntimeException("Property is not known on this object."));
		return new DefaultModelProperty<>(new DefaultModelObject<>(projection));
	}

	@Override
	public <S> ModelProperty<S> newProperty(Object identity, Class<S> type) {
		val node = getOrCreateChildNode(identity);
		val projection = node.getProjections().filter(projectionOf(type))
			.map(it -> (TypeAwareModelProjection<S>) it)
			.findFirst()
			.orElseGet(() -> node.newProjection(builder -> builder.type(type)));
		return new DefaultModelProperty<>(new DefaultModelObject<>(projection));
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
}
