package dev.nokee.platform.swift.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.runtime.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.runtime.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.swift.SwiftApplicationExtension;
import dev.nokee.platform.swift.internal.DefaultSwiftApplicationExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import javax.inject.Inject;

public abstract class SwiftApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		NamingScheme names = NamingScheme.asMainComponent(project.getName());
		DefaultSwiftApplicationExtension extension = getObjects().newInstance(DefaultSwiftApplicationExtension.class,
			getObjects().newInstance(DefaultNativeComponentDependencies.class, names), names);

		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(SwiftApplicationExtension.class, EXTENSION_NAME, extension);
	}
}
