package dev.nokee.model.internal;

import dev.nokee.model.dsl.ModelNode;

public final class DefaultModelNodeDslFactory implements ModelNodeFactory {
	private final NamedDomainObjectRegistry registry;

	public DefaultModelNodeDslFactory(NamedDomainObjectRegistry registry) {
		this.registry = registry;
	}

	@Override
	public ModelNode create(dev.nokee.model.core.ModelNode node) {
		return new DefaultModelNodeDsl(registry, node, this);
	}
}
