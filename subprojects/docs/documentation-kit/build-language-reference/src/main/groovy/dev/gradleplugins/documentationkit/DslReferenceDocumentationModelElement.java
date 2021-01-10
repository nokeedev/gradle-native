package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.type.TypeOf;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

import javax.annotation.Generated;

import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class DslReferenceDocumentationModelElement implements DslReferenceDocumentation, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	public DslMetaData getDslMetaData() {
		return getNode().getDescendant("dslMetaData").realize().get(of(DslMetaData.class));
	}

	@Override
	public DslContent getDslContent() {
		return getNode().getDescendant("dslContent").realize().get(of(DslContent.class));
	}

	@Override
	public Dependencies getDependencies() { // TODO: Have an injector that decorate this
		return getNode().getDescendant("dependencies").realize().get(of(Dependencies.class));
	}

	@Override
	public Property<String> getPermalink() {
		return getNode().getDescendant("permalink").realize().get(of(new TypeOf<Property<String>>() {}));
	}

	@Override
	public DirectoryProperty getClassDocbookDirectory() {
		return getNode().getDescendant("classDocbookDirectory").realize().get(of(DirectoryProperty.class));
	}

	@Override
	public ModelNode getNode() {
		return node == null ? ModelNodeContext.getCurrentModelNode() : node;
	}
}
