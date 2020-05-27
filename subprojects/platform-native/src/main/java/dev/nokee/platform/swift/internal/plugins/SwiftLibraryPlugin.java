package dev.nokee.platform.swift.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.swift.SwiftLibraryExtension;
import dev.nokee.platform.swift.internal.DefaultSwiftLibraryExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import javax.inject.Inject;

public abstract class SwiftLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		NamingScheme names = NamingScheme.asMainComponent(project.getName());
		DefaultSwiftLibraryExtension extension = getObjects().newInstance(DefaultSwiftLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class, names), names);

		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(SwiftLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
