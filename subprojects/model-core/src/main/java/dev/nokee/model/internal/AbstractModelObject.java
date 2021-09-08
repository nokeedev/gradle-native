package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelProperty;
import dev.nokee.model.core.TypeAwareModelProjection;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static dev.nokee.model.internal.ModelSpecs.projectionOf;
import static java.util.Objects.requireNonNull;

abstract class AbstractModelObject<T> implements ModelObject<T> {
	@Override
	public <S> ModelProperty<S> newProperty(Object identity, Class<S> type) {
		val node = getOrCreateChildNode(identity);
		val projection = node.getProjections().filter(projectionOf(type))
			.map(it -> (TypeAwareModelProjection<S>) it)
			.findFirst()
			.orElseGet(() -> node.newProjection(builder -> builder.type(type)));
		return new DefaultModelProperty<>(projection);
	}

	private ModelNode getOrCreateChildNode(Object identity) {
		// TODO: Assert identity is same or less info than selected node's identity:
		//  because if node was created with a String but trying to reference using machines.host... the identity has less information so we may be facing an out-of-order access.
		return getNode().getChildNodes().filter(ofName(identity)).findFirst().orElseGet(() -> getNode().newChildNode(identity));
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

	public TypeAwareModelProjection<T> getProjection() {
		throw new UnsupportedOperationException("No projection for this object.");
	}

	protected abstract ModelNode getNode();

	public Provider<T> asProvider() {
		return getProjection().as(getType());
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
		return new DefaultDomainObjectIdentifier(getProjection());
	}

	@Override
	public Class<T> getType() {
		return getProjection().getType();
	}

	@Override
	public ModelObject<T> configure(Action<? super T> action) {
		getProjection().whenRealized(action);
		return this;
	}

	@Override
	public ModelObject<T> configure(Consumer<? super ModelObject<? extends T>> action) {
		action.accept(this);
		return this;
	}
}
