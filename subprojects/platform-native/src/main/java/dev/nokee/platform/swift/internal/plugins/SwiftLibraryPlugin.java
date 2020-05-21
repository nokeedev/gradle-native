package dev.nokee.platform.swift.internal.plugins;

import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.swift.SwiftLibraryExtension;
import dev.nokee.platform.swift.internal.DefaultSwiftLibraryExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class SwiftLibraryPlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(SwiftLibraryExtension.class, "library", getObjects().newInstance(DefaultSwiftLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class)));
	}
}
