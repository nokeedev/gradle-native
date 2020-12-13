package dev.nokee.model.internal.registry;

import com.google.common.base.Preconditions;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelActions;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.ProviderUtils;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

@EqualsAndHashCode
public class ModelNodeBackedKnownDomainObject<T> implements KnownDomainObject<T> {
	private final ModelIdentifier<T> identifier;
	private final ModelType<T> type;
	@EqualsAndHashCode.Exclude private final ModelNode node;

	public ModelNodeBackedKnownDomainObject(ModelType<T> type, ModelNode node) {
		Preconditions.checkArgument(node.canBeViewedAs(type), "node '%s' cannot be viewed as %s", node, type);
		this.identifier = ModelIdentifier.of(node.getPath(), type);
		this.type = type;
		this.node = node;
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
		return ModelIdentifier.of(node.getPath(), type);
	}

	@Override
	public Class<T> getType() {
		return type.getConcreteType();
	}

	@Override
	public void configure(Action<? super T> action) {
		node.applyToSelf(ModelNodes.stateAtLeast(ModelNode.State.Realized), ModelActions.executeUsingProjection(type, action));
	}

	private Provider<T> getAsProvider() {
		return ProviderUtils.supplied(() -> node.realize().get(type));
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		return getAsProvider().map(transformer);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return getAsProvider().flatMap(transformer);
	}

	@Override
	public String toString() {
		return "known object(node '" + node.getPath() + "', " + type + ")";
	}
}
