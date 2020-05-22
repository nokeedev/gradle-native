package dev.nokee.platform.objectivecpp.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.objectivecpp.ObjectiveCppApplicationExtension;
import dev.nokee.platform.objectivecpp.internal.DefaultObjectiveCppApplicationExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public abstract class ObjectiveCppApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		DefaultObjectiveCppApplicationExtension extension = getObjects().newInstance(DefaultObjectiveCppApplicationExtension.class,getObjects().newInstance(DefaultNativeComponentDependencies.class, NamingScheme.asMainComponent(project.getName())));

		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));

		project.getExtensions().add(ObjectiveCppApplicationExtension.class, EXTENSION_NAME, extension);
	}
}
