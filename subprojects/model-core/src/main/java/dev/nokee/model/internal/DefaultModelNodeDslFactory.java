package dev.nokee.model.internal;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.streams.ModelStream;
import org.gradle.api.model.ObjectFactory;

public final class DefaultModelNodeDslFactory implements ModelNodeFactory {
	private final NamedDomainObjectRegistry registry;
	private final ModelStream<ModelProjection> stream;
	private final ObjectFactory objectFactory;

	public DefaultModelNodeDslFactory(NamedDomainObjectRegistry registry, ModelStream<ModelProjection> stream, ObjectFactory objectFactory) {
		this.registry = registry;
		this.stream = stream;
		this.objectFactory = objectFactory;
	}

	@Override
	public ModelNode create(dev.nokee.model.core.ModelNode node) {
		return new DefaultModelNodeDsl(registry, node, this, stream, objectFactory);
	}
}
