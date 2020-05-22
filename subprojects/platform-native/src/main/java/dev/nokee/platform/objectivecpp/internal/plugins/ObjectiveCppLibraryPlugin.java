package dev.nokee.platform.objectivecpp.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.cpp.internal.DefaultCppLibraryExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibraryExtension;
import dev.nokee.platform.objectivecpp.internal.DefaultObjectiveCppLibraryExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public abstract class ObjectiveCppLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		DefaultObjectiveCppLibraryExtension extension = getObjects().newInstance(DefaultObjectiveCppLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class, NamingScheme.asMainComponent(project.getName())));

		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));

		project.getExtensions().add(ObjectiveCppLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
