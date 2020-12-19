package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.type.ModelType;

import java.util.Map;

public abstract class ModelNodeBackedNamedDomainObjectCollectionDynamicObject extends DomainElementsDynamicObject {
	private final ModelType<?> elementType;
	private final ModelNode node;

	protected ModelNodeBackedNamedDomainObjectCollectionDynamicObject(ModelType<?> elementType, ModelNode node) {
		this.elementType = elementType;
		this.node = node;
	}

	@Override
	protected ModelType<?> getElementType() {
		return elementType;
	}

	@Override
	protected boolean hasElement(String name) {
		return node.hasDescendant(name);
	}

	@Override
	protected DomainObjectProvider<?> getElement(String name, ModelType<?> type) {
		return node.get(BaseNamedDomainObjectViewProjection.class).get(name, type);
	}

	@Override
	protected Map<String, ? extends DomainObjectProvider<?>> getElementsAsMap() {
		return node.get(BaseNamedDomainObjectViewProjection.class).getAsMap(getElementType());
	}

	@Override
	protected DomainObjectProvider<?> doRegister(String name, ModelType<?> type) {
		return node.get(BaseNamedDomainObjectContainerProjection.class).register(name, type);
	}
}
