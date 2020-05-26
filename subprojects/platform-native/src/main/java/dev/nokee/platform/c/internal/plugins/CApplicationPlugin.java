package dev.nokee.platform.c.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.c.CApplicationExtension;
import dev.nokee.platform.c.internal.DefaultCApplicationExtension;
import dev.nokee.runtime.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.runtime.nativebase.internal.TargetMachineRule;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public abstract class CApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		NamingScheme names = NamingScheme.asMainComponent(project.getName());
		DefaultCApplicationExtension extension = getObjects().newInstance(DefaultCApplicationExtension.class,
			getObjects().newInstance(DefaultNativeComponentDependencies.class, names), names);

		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(CApplicationExtension.class, EXTENSION_NAME, extension);
	}
}
