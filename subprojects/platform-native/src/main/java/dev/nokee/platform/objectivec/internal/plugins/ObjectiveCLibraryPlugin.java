package dev.nokee.platform.objectivec.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.DomainObjectStore;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.plugins.ComponentBasePlugin;
import dev.nokee.platform.base.internal.plugins.ProjectStorePlugin;
import dev.nokee.platform.base.internal.plugins.VariantBasePlugin;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetLinkageRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension;
import dev.nokee.platform.objectivec.internal.DefaultObjectiveCLibraryExtension;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public class ObjectiveCLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCLibraryPlugin(ObjectFactory objects) {
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
		val components = project.getExtensions().getByType(ComponentContainer.class);
		components.registerFactory(DefaultObjectiveCLibraryExtension.class, id -> {
			val identifier = ComponentIdentifier.of(((ComponentIdentifier<?>)id).getName(), DefaultNativeLibraryComponent.class, ProjectIdentifier.of(project));

			val component = new DefaultNativeLibraryComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class));

			store.register(identifier, DefaultNativeLibraryComponent.class, ignored -> component).get();
			return new DefaultObjectiveCLibraryExtension(component, project.getObjects(), project.getProviders(), project.getLayout());
		});
		val extension = components.register("main", DefaultObjectiveCLibraryExtension.class, component -> {
			component.getComponent().getBaseName().convention(project.getName());
		}).get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(ObjectiveCLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
