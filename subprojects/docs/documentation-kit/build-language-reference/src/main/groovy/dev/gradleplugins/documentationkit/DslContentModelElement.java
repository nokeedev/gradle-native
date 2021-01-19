package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.type.TypeOf;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import javax.annotation.Generated;

import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class DslContentModelElement implements DslContent, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	public ConfigurableFileCollection getClassDocbookDirectories() {
		return getNode().getDescendant("classDocbookDirectories").realize().get(of(ConfigurableFileCollection.class));
	}

	@Override
	public ConfigurableFileCollection getClassMetaDataFiles() {
		return getNode().getDescendant("classMetaDataFiles").realize().get(of(ConfigurableFileCollection.class));
	}

	@Override
	public RegularFileProperty getTemplateFile() {
		return getNode().getDescendant("templateFile").realize().get(of(RegularFileProperty.class));
	}

	@Override
	public SetProperty<String> getClassNames() {
		return getNode().getDescendant("classNames").realize().get(of(new TypeOf<SetProperty<String>>() {}));
	}

	@Override
	public Property<String> getPermalink() {
		return getNode().getDescendant("permalink").realize().get(of(new TypeOf<Property<String>>() {}));
	}

	@Override
	public DirectoryProperty getContentDirectory() {
		return getNode().getDescendant("contentDirectory").realize().get(of(DirectoryProperty.class));
	}

	@Override
	public ModelNode getNode() {
		return node == null ? ModelNodeContext.getCurrentModelNode() : node;
	}
}
