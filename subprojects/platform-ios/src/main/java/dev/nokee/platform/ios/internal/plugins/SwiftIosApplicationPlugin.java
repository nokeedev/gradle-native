package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.plugins.ComponentBasePlugin;
import dev.nokee.platform.base.internal.plugins.ProjectStorePlugin;
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

import static dev.nokee.platform.ios.internal.DefaultIosApplicationComponent.newFactory;

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
		val components = project.getExtensions().getByType(ComponentContainer.class);
		components.registerFactory(DefaultSwiftIosApplicationExtension.class, id -> {
			val identifier = ComponentIdentifier.ofMain(DefaultIosApplicationComponent.class, ProjectIdentifier.of(project));
			val component = store.register(identifier, DefaultIosApplicationComponent.class, newFactory(getObjects(), new NamingSchemeFactory(project.getName())));
			return new DefaultSwiftIosApplicationExtension(component.get(), project.getObjects(), project.getProviders());
		});
		val extension = components.register("main", DefaultSwiftIosApplicationExtension.class, component -> {
			component.getComponent().getBaseName().convention(GUtil.toCamelCase(project.getName()));
			component.getComponent().getGroupId().set(GroupId.of(project::getGroup));
		}).get();

		// Other configurations
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(SwiftIosApplicationExtension.class, EXTENSION_NAME, extension);

		// TODO: This should be solve in a better way
		getTasks().withType(CreateIosApplicationBundleTask.class).configureEach(task -> {
			task.getSwiftSupportRequired().set(true);
		});
	}
}
