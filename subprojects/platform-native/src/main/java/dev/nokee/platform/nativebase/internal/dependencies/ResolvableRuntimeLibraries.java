package dev.nokee.platform.nativebase.internal.dependencies;

import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public final class ResolvableRuntimeLibraries extends BaseNativeResolvableDependencyBucket {
	@Inject
	public ResolvableRuntimeLibraries(ObjectFactory objectFactory) {
		super(objectFactory);
		getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.NATIVE_RUNTIME));
	}

	public FileCollection getAsFiles() {
		return getIncoming().getFiles();
	}
}
