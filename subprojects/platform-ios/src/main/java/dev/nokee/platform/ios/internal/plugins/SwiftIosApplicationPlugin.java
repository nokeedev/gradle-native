package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.base.internal.LanguageSourceSetRepository;
import dev.nokee.language.base.internal.LanguageSourceSetViewFactory;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.DomainObjectStore;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.plugins.*;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.ios.SwiftIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultSwiftIosApplicationExtension;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.gradle.util.GUtil;

import javax.inject.Inject;

public class SwiftIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;

	@Inject
	public SwiftIosApplicationPlugin(ObjectFactory objects, TaskContainer tasks) {
		this.objects = objects;
		this.tasks = tasks;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class);

		// Load the store
		project.getPluginManager().apply(ProjectStorePlugin.class);
		val store = project.getExtensions().getByType(DomainObjectStore.class);

		// Create the component
		project.getPluginManager().apply(ComponentBasePlugin.class);
		project.getPluginManager().apply(VariantBasePlugin.class);
		project.getPluginManager().apply(BinaryBasePlugin.class);
		project.getPluginManager().apply(TaskBasePlugin.class);
		project.getPluginManager().apply(SwiftLanguageBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);
		components.registerFactory(DefaultIosApplicationComponent.class, id -> {
			val identifier = ComponentIdentifier.ofMain(DefaultIosApplicationComponent.class, ProjectIdentifier.of(project));

			val component = new DefaultIosApplicationComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getExtensions().getByType(LanguageSourceSetViewFactory.class));
			store.register(identifier, DefaultIosApplicationComponent.class, ignored -> component).get();
			return component;
		});
		val componentProvider = components.register("main", DefaultIosApplicationComponent.class, component -> {
			component.getBaseName().convention(GUtil.toCamelCase(project.getName()));
			component.getGroupId().set(GroupId.of(project::getGroup));
		});
		val extension = new DefaultSwiftIosApplicationExtension(componentProvider.get(), project.getObjects(), project.getProviders(), project.getExtensions().getByType(LanguageSourceSetRegistry.class));

		// Other configurations
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(SwiftIosApplicationExtension.class, EXTENSION_NAME, extension);

		// TODO: This should be solve in a better way
		getTasks().withType(CreateIosApplicationBundleTask.class).configureEach(task -> {
			task.getSwiftSupportRequired().set(true);
		});
	}
}
