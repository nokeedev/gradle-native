package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.SwiftIosLibraryExtension;
import dev.nokee.platform.ios.internal.DefaultSwiftIosLibraryExtension;
import dev.nokee.runtime.nativebase.internal.DefaultNativeLibraryDependencies;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import javax.inject.Inject;

public abstract class SwiftIosLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		NamingScheme names = NamingScheme.asMainComponent(project.getName());
		DefaultSwiftIosLibraryExtension extension = getObjects().newInstance(DefaultSwiftIosLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class, names), names);

		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(SwiftIosLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
