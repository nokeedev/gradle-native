package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;

import javax.annotation.Generated;

import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class ApiReferenceDocumentationModelElement implements ApiReferenceDocumentation, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	public ModelNode getNode() {
		return node == null ? ModelNodeContext.getCurrentModelNode() : node;
	}

	@Override
	public ApiReferenceManifest getManifest() {
		return getNode().getDescendant("manifest").realize().get(of(ApiReferenceManifest.class));
	}

	@Override
	public Dependencies getDependencies() {
		return getNode().getDescendant("dependencies").realize().get(of(Dependencies.class));
	}
}
