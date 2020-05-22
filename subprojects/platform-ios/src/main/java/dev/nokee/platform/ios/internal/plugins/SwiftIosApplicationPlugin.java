package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.SwiftIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultSwiftIosApplicationExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import javax.inject.Inject;

public abstract class SwiftIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		DefaultSwiftIosApplicationExtension extension = getObjects().newInstance(DefaultSwiftIosApplicationExtension.class,
			getObjects().newInstance(DefaultNativeComponentDependencies.class, NamingScheme.asMainComponent(project.getName())));
		project.getExtensions().add(SwiftIosApplicationExtension.class, EXTENSION_NAME, extension);
	}
}
