package dev.nokee.platform.c.internal.plugins;

import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.plugins.ComponentBasePlugin;
import dev.nokee.platform.base.internal.plugins.ProjectStorePlugin;
import dev.nokee.platform.c.CApplicationExtension;
import dev.nokee.platform.c.internal.DefaultCApplicationExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

import static dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent.newFactory;

public class CApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public CApplicationPlugin(ObjectFactory objects) {
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
		components.registerFactory(DefaultCApplicationExtension.class, id -> {
			val identifier = ComponentIdentifier.builder().withName(((ComponentIdentifier<?>)id).getName()).withProjectIdentifier(ProjectIdentifier.of(project)).withDisplayName("main native component").withType(DefaultNativeApplicationComponent.class).build();
			val component = store.register(identifier, DefaultNativeApplicationComponent.class, newFactory(getObjects(), new NamingSchemeFactory(project.getName())));
			return getObjects().newInstance(DefaultCApplicationExtension.class, component.get());
		});
		val extension = components.register("main", DefaultCApplicationExtension.class, component -> {
			component.getComponent().getBaseName().convention(project.getName());
		}).get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(CApplicationExtension.class, EXTENSION_NAME, extension);
	}
}
