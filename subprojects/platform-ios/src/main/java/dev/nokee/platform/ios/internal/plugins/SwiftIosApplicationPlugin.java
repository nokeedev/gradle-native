package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.platform.base.internal.DomainObjectStore;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.NamingSchemeFactory;
import dev.nokee.platform.base.internal.plugins.ProjectStorePlugin;
import dev.nokee.platform.ios.SwiftIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultSwiftIosApplicationExtension;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import javax.inject.Inject;

public abstract class SwiftIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract TaskContainer getTasks();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class);
		project.getPluginManager().apply(ProjectStorePlugin.class);

		val store = project.getExtensions().getByType(DomainObjectStore.class);
		val component = store.register(DefaultIosApplicationComponent.newMain(getObjects(), new NamingSchemeFactory(project.getName())));
		component.configure(it -> it.getGroupId().set(GroupId.of(project::getGroup)));
		DefaultSwiftIosApplicationExtension extension = getObjects().newInstance(DefaultSwiftIosApplicationExtension.class, component.get());

		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(SwiftIosApplicationExtension.class, EXTENSION_NAME, extension);

		// TODO: This should be solve in a better way
		getTasks().withType(CreateIosApplicationBundleTask.class).configureEach(task -> {
			task.getSwiftSupportRequired().set(true);
		});
	}
}
