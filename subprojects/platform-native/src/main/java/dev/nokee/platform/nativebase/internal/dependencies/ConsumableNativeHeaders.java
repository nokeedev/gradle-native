package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.runtime.nativebase.internal.ArtifactTypes;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.io.File;

public final class ConsumableNativeHeaders extends BaseNativeConsumableDependencyBucket {
	@Inject
	public ConsumableNativeHeaders(ObjectFactory objectFactory) {
		super(objectFactory);
		getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.C_PLUS_PLUS_API));
	}

	public void headerSearchPath(File directory) {
		getOutgoing().artifact(directory, it -> it.setType(ArtifactTypes.DIRECTORY_TYPE));
	}

	public void headerSearchPath(Provider<Directory> directory) {
		getOutgoing().artifact(directory, it -> it.setType(ArtifactTypes.DIRECTORY_TYPE));
	}
}
