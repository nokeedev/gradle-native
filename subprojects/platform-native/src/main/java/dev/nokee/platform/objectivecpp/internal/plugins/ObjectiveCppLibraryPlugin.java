package dev.nokee.platform.objectivecpp.internal.plugins;

import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.plugins.ComponentBasePlugin;
import dev.nokee.platform.base.internal.plugins.ProjectStorePlugin;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetLinkageRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibraryExtension;
import dev.nokee.platform.objectivecpp.internal.DefaultObjectiveCppLibraryExtension;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

import static dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent.newFactory;

public class ObjectiveCppLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCppLibraryPlugin(ObjectFactory objects) {
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
		components.registerFactory(DefaultObjectiveCppLibraryExtension.class, id -> {
			val identifier = ComponentIdentifier.of(((ComponentIdentifier<?>)id).getName(), DefaultNativeLibraryComponent.class, ProjectIdentifier.of(project));
			val component = store.register(identifier, DefaultNativeLibraryComponent.class, newFactory(getObjects(), new NamingSchemeFactory(project.getName())));
			return getObjects().newInstance(DefaultObjectiveCppLibraryExtension.class, component.get());
		});
		val extension = components.register("main", DefaultObjectiveCppLibraryExtension.class, component -> {
			component.getComponent().getBaseName().convention(project.getName());
		}).get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(ObjectiveCppLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
