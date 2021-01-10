package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucket;

import javax.annotation.Generated;

import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class ApiReferenceDocumentationDependenciesModelElement implements ApiReferenceDocumentation.Dependencies, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	public ModelNode getNode() {
		return node == null ? ModelNodeContext.getCurrentModelNode() : node;
	}

	@Override
	public DeclarableDependencyBucket getApi() {
		return getNode().getDescendant("api").realize().get(of(DeclarableDependencyBucket.class));
	}

	@Override
	public ConsumableDependencyBucket getManifestElements() {
		return getNode().getDescendant("manifestElements").realize().get(of(ConsumableDependencyBucket.class));
	}

	@Override
	public ResolvableDependencyBucket getManifest() {
		return getNode().getDescendant("manifest").realize().get(of(ResolvableDependencyBucket.class));
	}
}
