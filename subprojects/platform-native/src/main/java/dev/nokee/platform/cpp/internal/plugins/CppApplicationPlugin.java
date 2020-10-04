package dev.nokee.platform.cpp.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.DomainObjectStore;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.plugins.*;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.cpp.CppApplicationExtension;
import dev.nokee.platform.cpp.internal.DefaultCppApplicationExtension;
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

public class CppApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public CppApplicationPlugin(ObjectFactory objects) {
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
		project.getPluginManager().apply(VariantBasePlugin.class);
		project.getPluginManager().apply(BinaryBasePlugin.class);
		project.getPluginManager().apply(TaskBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);
		components.registerFactory(DefaultCppApplicationExtension.class, id -> {
			val identifier = ComponentIdentifier.of(((ComponentIdentifier<?>)id).getName(), DefaultNativeApplicationComponent.class, ProjectIdentifier.of(project));

			val component = new DefaultNativeApplicationComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class));

			store.register(identifier, DefaultNativeApplicationComponent.class, ignored -> component).get();
			return new DefaultCppApplicationExtension(component, project.getObjects(), project.getProviders(), project.getLayout());
		});
		val extension = components.register("main", DefaultCppApplicationExtension.class, component -> {
			component.getComponent().getBaseName().convention(project.getName());
		}).get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(CppApplicationExtension.class, EXTENSION_NAME, extension);
	}
}
