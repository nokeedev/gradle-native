package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.type.ModelType;

import static dev.nokee.model.internal.core.ModelNodeContext.getCurrentModelNode;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;

public class BaseDomainObjectView<T> extends AbstractModelNodeBackedDomainObjectView<T> implements DomainObjectView<T> {
	protected BaseDomainObjectView(Class<T> elementType) {
		this(of(elementType), getCurrentModelNode());
	}

	// Don't share beyond package
	BaseDomainObjectView(ModelType<T> elementType, ModelNode node) {
		super(elementType, node);
	}

	public static <T> NodeRegistration<T> newRegistration(String name, ModelType<T> viewType) {
		return NodeRegistration.of(name, viewType, elementTypeParameter(viewType, DomainObjectView.class))
			.withProjection(managed(of(BaseDomainObjectViewProjection.class)));
	}
}
