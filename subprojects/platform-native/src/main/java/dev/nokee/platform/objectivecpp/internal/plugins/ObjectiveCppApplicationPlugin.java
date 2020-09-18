package dev.nokee.platform.objectivecpp.internal.plugins;

import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.plugins.ComponentBasePlugin;
import dev.nokee.platform.base.internal.plugins.ProjectStorePlugin;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.objectivecpp.ObjectiveCppApplicationExtension;
import dev.nokee.platform.objectivecpp.internal.DefaultObjectiveCppApplicationExtension;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

import static dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent.newFactory;

public class ObjectiveCppApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCppApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		// Load the store
		project.getPluginManager().apply(ProjectStorePlugin.class);
		val store = project.getExtensions().getByType(DomainObjectStore.class);

		// Create the component
		project.getPluginManager().apply(ComponentBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);
		components.registerFactory(DefaultObjectiveCppApplicationExtension.class, id -> {
			val identifier = ComponentIdentifier.of(((ComponentIdentifier<?>)id).getName(), DefaultNativeApplicationComponent.class, ProjectIdentifier.of(project));
			val component = store.register(identifier, DefaultNativeApplicationComponent.class, newFactory(getObjects(), new NamingSchemeFactory(project.getName())));
			return getObjects().newInstance(DefaultObjectiveCppApplicationExtension.class, component.get());
		});
		val extension = components.register("main", DefaultObjectiveCppApplicationExtension.class, component -> {
			component.getComponent().getBaseName().convention(project.getName());
		}).get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(ObjectiveCppApplicationExtension.class, EXTENSION_NAME, extension);
	}
}
