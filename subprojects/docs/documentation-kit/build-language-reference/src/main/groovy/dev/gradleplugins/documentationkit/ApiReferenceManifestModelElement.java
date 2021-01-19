package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.type.TypeOf;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.SetProperty;

import javax.annotation.Generated;
import java.net.URI;

import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class ApiReferenceManifestModelElement implements ApiReferenceManifest, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	public ConfigurableFileCollection getSources() {
		return getNode().getDescendant("sources").realize().get(of(ConfigurableFileCollection.class));
	}

	@Override
	public SetProperty<Dependency> getDependencies() {
		return getNode().getDescendant("dependencies").realize().get(of(new TypeOf<SetProperty<Dependency>>() {}));
	}

	@Override
	public SetProperty<URI> getRepositories() {
		return getNode().getDescendant("repositories").realize().get(of(new TypeOf<SetProperty<URI>>() {}));
	}

	@Override
	public DirectoryProperty getDestinationLocation() {
		return getNode().getDescendant("destinationLocation").realize().get(of(DirectoryProperty.class));
	}

	@Override
	public ModelNode getNode() {
		return node == null ? ModelNodeContext.getCurrentModelNode() : node;
	}
}
