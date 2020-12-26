package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetRepository;
import dev.nokee.language.base.internal.LanguageSourceSetViewFactory;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactoryRegistry;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.internal.DefaultMultiLanguageNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultMultiLanguageNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.type.ModelType.of;

public class NativeComponentBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		// Create the component
		project.getPluginManager().apply(ComponentModelBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);

		// Register native component initializer
		val registry = ModelNodes.of(components).get(NodeRegistrationFactoryRegistry.class);
		registry.registerFactory(of(DefaultNativeApplicationComponent.class), name -> nativeApplication(name, project));
		registry.registerFactory(of(DefaultNativeLibraryComponent.class), name -> nativeLibrary(name, project));
		registry.registerFactory(of(DefaultMultiLanguageNativeApplicationComponent.class), name -> multiLanguageNativeApplication(name, project));
		registry.registerFactory(of(DefaultMultiLanguageNativeLibraryComponent.class), name -> multiLanguageNativeLibrary(name, project));
	}

	private static NodeRegistration<DefaultNativeApplicationComponent> nativeApplication(String name, Project project) {
		return NodeRegistration.unmanaged(name, of(DefaultNativeApplicationComponent.class), () -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultNativeApplicationComponent.class, ProjectIdentifier.of(project));
			val component = new DefaultNativeApplicationComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getExtensions().getByType(LanguageSourceSetViewFactory.class));
			return component;
		});
	}

	private static NodeRegistration<DefaultNativeLibraryComponent> nativeLibrary(String name, Project project) {
		return NodeRegistration.unmanaged(name, of(DefaultNativeLibraryComponent.class), () -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultNativeLibraryComponent.class, ProjectIdentifier.of(project));
			val component = new DefaultNativeLibraryComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getExtensions().getByType(LanguageSourceSetViewFactory.class));
			return component;
		});
	}

	private static NodeRegistration<DefaultMultiLanguageNativeApplicationComponent> multiLanguageNativeApplication(String name, Project project) {
		return NodeRegistration.unmanaged(name, of(DefaultMultiLanguageNativeApplicationComponent.class), () -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultMultiLanguageNativeApplicationComponent.class, ProjectIdentifier.of(project));
			val component = new DefaultMultiLanguageNativeApplicationComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getExtensions().getByType(LanguageSourceSetViewFactory.class));
			return component;
		});
	}

	private static NodeRegistration<DefaultMultiLanguageNativeLibraryComponent> multiLanguageNativeLibrary(String name, Project project) {
		return NodeRegistration.unmanaged(name, of(DefaultMultiLanguageNativeLibraryComponent.class), () -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultMultiLanguageNativeLibraryComponent.class, ProjectIdentifier.of(project));
			val component = new DefaultMultiLanguageNativeLibraryComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getExtensions().getByType(LanguageSourceSetViewFactory.class));
			return component;
		});
	}
}
