package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.NamedDomainObjectView;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelNodeBackedProvider;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.UnaryOperator;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodeContext.getCurrentModelNode;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;

public class BaseNamedDomainObjectViewProjection implements AbstractModelNodeBackedNamedDomainObjectView.Projection {
	private final ObjectFactory objectFactory;
	private final ModelNode node = getCurrentModelNode();

	@Inject
	public BaseNamedDomainObjectViewProjection(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public <T> DomainObjectProvider<T> get(String name, ModelType<T> type) {
		return new ModelNodeBackedProvider<>(type, checkType(name, type).apply(node.getDescendant(name)));
	}

	@Override
	public <T> void configure(String name, ModelType<T> type, Action<? super T> action) {
		checkType(name, type).apply(node.getDescendant(name)).applyToSelf(stateAtLeast(ModelNode.State.Realized), executeUsingProjection(type, action));
	}

//	protected String getTypeDisplayName() {
//		return getType().getSimpleName();
//	}

	protected static InvalidUserDataException createWrongTypeException(String name, Class expected, String actual) {
		return new InvalidUserDataException(String.format("The domain object '%s' (%s) is not a subclass of the given type (%s).", name, actual, expected.getCanonicalName()));
	}

	private static UnaryOperator<ModelNode> checkType(String name, ModelType<?> expected) {
		return node -> {
			if (node.canBeViewedAs(expected)) {
				return node;
			}
			throw createWrongTypeException(name, expected.getConcreteType(), node.getTypeDescription().orElse("<unknown>"));
		};
	}

	@Override
	public <T> Map<String, DomainObjectProvider<T>> getAsMap(ModelType<T> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> NamedDomainObjectView<T> createSubView(ModelType<T> type) {
		return (NamedDomainObjectView<T>) objectFactory.newInstance(SubView.class, type, node);
	}

	static class SubView<T> extends BaseNamedDomainObjectView<T> {
		@Inject
		public SubView(ModelType<T> elementType, ModelNode node) {
			super(elementType, node);
		}
	}
}
