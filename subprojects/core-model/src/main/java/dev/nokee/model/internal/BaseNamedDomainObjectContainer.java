package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectContainer;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactories;
import dev.nokee.model.internal.type.ModelType;

import static dev.nokee.model.internal.core.ModelNodeContext.getCurrentModelNode;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.type.ModelType.of;

/**
 * Base implementation for domain object container with Groovy support.
 *
 * @param <T>
 */
public class BaseNamedDomainObjectContainer<T> extends AbstractModelNodeBackedNamedDomainObjectContainer<T> implements DomainObjectContainer<T> {
	protected BaseNamedDomainObjectContainer() {
		super(null, getCurrentModelNode());
	}

	protected BaseNamedDomainObjectContainer(Class<T> elementType) {
		super(of(elementType), getCurrentModelNode());
	}

	public static <T> NodeRegistration<T> namedContainer(String name, ModelType<T> viewType) {
		return NodeRegistration.of(name, viewType, elementTypeParameter(viewType, DomainObjectContainer.class))
			.withProjection(managed(of(BaseDomainObjectViewProjection.class)))
			.withProjection(managed(of(BaseNamedDomainObjectViewProjection.class)))
			.withProjection(managed(of(BaseNamedDomainObjectContainerProjection.class)))
			.withProjection(ofInstance(new NodeRegistrationFactories()));
	}
}
