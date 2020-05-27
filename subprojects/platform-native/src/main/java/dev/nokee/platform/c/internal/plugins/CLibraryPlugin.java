package dev.nokee.platform.c.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.c.CLibraryExtension;
import dev.nokee.platform.c.internal.DefaultCLibraryExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public abstract class CLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		NamingScheme names = NamingScheme.asMainComponent(project.getName());
		DefaultCLibraryExtension extension = getObjects().newInstance(DefaultCLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class, names), names);

		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(CLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
