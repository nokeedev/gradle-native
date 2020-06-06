package dev.nokee.platform.nativebase.internal.dependencies;

import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class NoHeaderIncomingDependencies implements HeaderIncomingDependencies {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public FileCollection getHeaderSearchPaths() {
		return getObjects().fileCollection();
	}

	@Override
	public FileCollection getFrameworkSearchPaths() {
		return getObjects().fileCollection();
	}
}
