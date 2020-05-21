package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.SwiftIosLibraryExtension;
import dev.nokee.platform.ios.internal.DefaultSwiftIosLibraryExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class SwiftIosLibraryPlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(SwiftIosLibraryExtension.class, "library", getObjects().newInstance(DefaultSwiftIosLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class, NamingScheme.asMainComponent(project.getName()))));
	}
}
