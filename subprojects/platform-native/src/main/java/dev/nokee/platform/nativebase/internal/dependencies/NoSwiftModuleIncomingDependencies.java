package dev.nokee.platform.nativebase.internal.dependencies;

import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class NoSwiftModuleIncomingDependencies implements SwiftModuleIncomingDependencies {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public FileCollection getSwiftModules() {
		return getObjects().fileCollection();
	}

	@Override
	public FileCollection getFrameworkSearchPaths() {
		return getObjects().fileCollection();
	}
}
