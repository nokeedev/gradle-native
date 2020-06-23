package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.internal.Cast;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.SwiftIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultSwiftIosApplicationExtension;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
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

		NamingScheme names = NamingScheme.asMainComponent(project.getName()).withComponentDisplayName("main iOS application");
		ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", project.getExtensions().create("components", ComponentCollection.class));
		DefaultIosApplicationComponent component = components.register(DefaultIosApplicationComponent.class, names).get();
		component.getGroupId().set(GroupId.of(project::getGroup));
		DefaultSwiftIosApplicationExtension extension = getObjects().newInstance(DefaultSwiftIosApplicationExtension.class, component);

		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(SwiftIosApplicationExtension.class, EXTENSION_NAME, extension);

		// TODO: This should be solve in a better way
		getTasks().withType(CreateIosApplicationBundleTask.class).configureEach(task -> {
			task.getSwiftSupportRequired().set(true);
		});
	}
}
