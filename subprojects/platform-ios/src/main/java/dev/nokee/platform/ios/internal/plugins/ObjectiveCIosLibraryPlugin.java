package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.ObjectiveCIosLibraryExtension;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosLibraryExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public abstract class ObjectiveCIosLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		NamingScheme names = NamingScheme.asMainComponent(project.getName());
		DefaultObjectiveCIosLibraryExtension extension = getObjects().newInstance(DefaultObjectiveCIosLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class, names), names);

		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(ObjectiveCIosLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
