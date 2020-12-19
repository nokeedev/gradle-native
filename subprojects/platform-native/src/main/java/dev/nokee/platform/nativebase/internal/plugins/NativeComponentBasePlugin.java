package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetRepository;
import dev.nokee.language.base.internal.LanguageSourceSetViewFactory;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.NameAwareDomainObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
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

public class NativeComponentBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		// Create the component
		project.getPluginManager().apply(ComponentModelBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);

		// Register native component factory
		components.registerFactory(DefaultNativeApplicationComponent.class, name -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(((NameAwareDomainObjectIdentifier)name).getName().toString()), DefaultNativeApplicationComponent.class, ProjectIdentifier.of(project));
			val component = new DefaultNativeApplicationComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getExtensions().getByType(LanguageSourceSetViewFactory.class));
			return component;
		});
		components.registerFactory(DefaultNativeLibraryComponent.class, name -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(((NameAwareDomainObjectIdentifier)name).getName().toString()), DefaultNativeLibraryComponent.class, ProjectIdentifier.of(project));
			val component = new DefaultNativeLibraryComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getExtensions().getByType(LanguageSourceSetViewFactory.class));
			return component;
		});
		components.registerFactory(DefaultMultiLanguageNativeApplicationComponent.class, name -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(((NameAwareDomainObjectIdentifier)name).getName().toString()), DefaultMultiLanguageNativeApplicationComponent.class, ProjectIdentifier.of(project));
			val component = new DefaultMultiLanguageNativeApplicationComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getExtensions().getByType(LanguageSourceSetViewFactory.class));
			return component;
		});
		components.registerFactory(DefaultMultiLanguageNativeLibraryComponent.class, name -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(((NameAwareDomainObjectIdentifier)name).getName().toString()), DefaultMultiLanguageNativeLibraryComponent.class, ProjectIdentifier.of(project));
			val component = new DefaultMultiLanguageNativeLibraryComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getExtensions().getByType(LanguageSourceSetViewFactory.class));
			return component;
		});
	}
}
