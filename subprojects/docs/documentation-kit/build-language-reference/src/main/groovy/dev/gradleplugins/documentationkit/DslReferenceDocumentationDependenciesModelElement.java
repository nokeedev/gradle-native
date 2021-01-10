package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucket;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Generated;
import javax.inject.Inject;

import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class DslReferenceDocumentationDependenciesModelElement extends DslReferenceDocumentation.Dependencies implements ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Inject
	public DslReferenceDocumentationDependenciesModelElement(ObjectFactory objects) {
		super(objects);
	}

	@Override
	public ResolvableDependencyBucket getDslMetaData() {
		return getNode().getDescendant("dslMetaData").get(of(ResolvableDependencyBucket.class));
	}

	@Override
	public ConsumableDependencyBucket getDslMetaDataElements() {
		return getNode().getDescendant("dslMetaDataElements").get(of(ConsumableDependencyBucket.class));
	}

	@Override
	public ModelNode getNode() {
		return node == null ? ModelNodeContext.getCurrentModelNode() : node;
	}
}
