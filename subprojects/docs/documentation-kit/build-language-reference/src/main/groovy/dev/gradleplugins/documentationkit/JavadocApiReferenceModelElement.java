package dev.gradleplugins.documentationkit;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.type.TypeOf;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import javax.annotation.Generated;
import java.net.URI;

import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class JavadocApiReferenceModelElement implements JavadocApiReference, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	public LanguageSourceSet getSources() {
		return getNode().getDescendant("sources").realize().get(of(LanguageSourceSet.class));
	}

	@Override
	public Property<String> getPermalink() {
		return getNode().getDescendant("permalink").realize().get(of(new TypeOf<Property<String>>() {}));
	}

	@Override
	public DirectoryProperty getDestinationDirectory() {
		return getNode().getDescendant("destinationDirectory").realize().get(of(DirectoryProperty.class));
	}

	@Override
	public SetProperty<URI> getLinks() {
		return getNode().getDescendant("links").realize().get(of(new TypeOf<SetProperty<URI>>() {}));
	}

	@Override
	public ConfigurableFileCollection getClasspath() {
		return getNode().getDescendant("classpath").realize().get(of(ConfigurableFileCollection.class));
	}

	@Override
	public ModelNode getNode() {
		return node == null ? ModelNodeContext.getCurrentModelNode() : node;
	}
}
