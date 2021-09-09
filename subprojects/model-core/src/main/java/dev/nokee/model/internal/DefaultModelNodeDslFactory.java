package dev.nokee.model.internal;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.streams.ModelStream;
import org.gradle.api.model.ObjectFactory;

@Deprecated
final class DefaultModelNodeDslFactory implements ModelNodeFactory {
	private final ModelStream<ModelProjection> stream;
	private final ObjectFactory objectFactory;

	public DefaultModelNodeDslFactory(ModelStream<ModelProjection> stream, ObjectFactory objectFactory) {
		this.stream = stream;
		this.objectFactory = objectFactory;
	}

	@Override
	public ModelNode create(dev.nokee.model.core.ModelNode node) {
		return new DefaultModelNodeDsl(node, this, stream, objectFactory);
	}
}
