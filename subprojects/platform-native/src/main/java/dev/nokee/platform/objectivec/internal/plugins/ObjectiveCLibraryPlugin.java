package dev.nokee.platform.objectivec.internal.plugins;

import dev.nokee.internal.Cast;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension;
import dev.nokee.platform.objectivec.internal.DefaultObjectiveCLibraryExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public abstract class ObjectiveCLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		NamingScheme names = NamingScheme.asMainComponent(project.getName()).withComponentDisplayName("main native component");
		ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", project.getExtensions().create("components", ComponentCollection.class));
		DefaultNativeLibraryComponent component = components.register(DefaultNativeLibraryComponent.class, names).get();
		DefaultObjectiveCLibraryExtension extension = getObjects().newInstance(DefaultObjectiveCLibraryExtension.class, component);

		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(ObjectiveCLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
