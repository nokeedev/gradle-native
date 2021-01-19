package dev.gradleplugins.documentationkit;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;

import javax.annotation.Generated;

import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class DslMetaDataModelElement implements DslMetaData, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	public LanguageSourceSet getSources() {
		return getNode().getDescendant("sources").realize().get(of(LanguageSourceSet.class));
	}

	@Override
	public ConfigurableFileCollection getClassDocbookFiles() {
		return getNode().getDescendant("classDocbookFiles").realize().get(of(ConfigurableFileCollection.class));
	}

	@Override
	public DirectoryProperty getExtractedMetaDataFile() {
		return getNode().getDescendant("extractedMetaDataFile").realize().get(of(DirectoryProperty.class));
	}

	@Override
	public ModelNode getNode() {
		return node == null ? ModelNodeContext.getCurrentModelNode() : node;
	}
}
