package dev.nokee.model.internal;

import dev.nokee.model.NamedDomainObjectView;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.type.ModelType;

import static dev.nokee.model.internal.core.ModelNodeContext.getCurrentModelNode;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;

/**
 * Base implementation for domain object container with Groovy support.
 *
 * @param <T>
 */
public class BaseNamedDomainObjectView<T> extends AbstractModelNodeBackedNamedDomainObjectView<T> implements NamedDomainObjectView<T> {
	protected BaseNamedDomainObjectView(Class<T> elementType) {
		this(of(elementType), getCurrentModelNode());
	}

	// Don't share beyond package
	BaseNamedDomainObjectView(ModelType<T> elementType, ModelNode node) {
		super(elementType, node);
	}

	public static <T> NodeRegistration<T> namedView(String name, ModelType<T> viewType) {
		return NodeRegistration.of(name, viewType, elementTypeParameter(viewType, NamedDomainObjectView.class))
			.withProjection(managed(of(BaseDomainObjectViewProjection.class)))
			.withProjection(managed(of(BaseNamedDomainObjectViewProjection.class)));
	}
}
