package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.platform.ios.tasks.internal.*;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.tasks.LinkExecutable;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.util.stream.Collectors;

public abstract class ObjectiveCIosApplicationPlugin implements Plugin<Project> {
	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LifecycleBasePlugin.class);
		project.getPluginManager().apply("dev.nokee.objective-c-language");

		project.getPluginManager().withPlugin("dev.nokee.objective-c-language", appliedPlugin -> project.getPluginManager().apply(IosApplicationRules.class));

		String moduleName = GUtil.toCamelCase(project.getName());

		TaskProvider<StoryboardCompileTask> compileStoryboardTask = getTasks().register("compileStoryboard", StoryboardCompileTask.class, task -> {
			task.getDestinationDirectory().set(getLayout().getBuildDirectory().dir("ios/storyboards/compiled/main"));
			task.getModule().set(moduleName);
			task.getSources().from(project.fileTree("src/main/resources", it -> it.include("*.lproj/*.storyboard")));
		});

		TaskProvider<StoryboardLinkTask> linkStoryboardTask = getTasks().register("linkStoryboard", StoryboardLinkTask.class, task -> {
			task.getDestinationDirectory().set(getLayout().getBuildDirectory().dir("ios/storyboards/linked/main"));
			task.getModule().set(moduleName);
			task.getSources().from(compileStoryboardTask.flatMap(StoryboardCompileTask::getDestinationDirectory));
		});

		TaskProvider<AssetCatalogCompileTask> assetCatalogCompileTaskTask = getTasks().register("compileAssetCatalog", AssetCatalogCompileTask.class, task -> {
			task.getSource().set(project.file("src/main/resources/Assets.xcassets"));
			task.getIdentifier().set(project.provider(() -> project.getGroup().toString() + "." + moduleName));
			task.getDestinationDirectory().set(getLayout().getBuildDirectory().dir("ios/assets/main"));
		});

		TaskProvider<ProcessPropertyListTask> processPropertyListTask = getTasks().register("processPropertyList", ProcessPropertyListTask.class, task -> {
			task.getIdentifier().set(project.provider(() -> project.getGroup().toString() + "." + moduleName));
			task.getModule().set(moduleName);
			task.getSources().from("src/main/resources/Info.plist");
			task.getOutputFile().set(getLayout().getBuildDirectory().file("ios/Info.plist"));
		});

		TaskProvider<CreateIosApplicationBundleTask> createApplicationBundleTask = getTasks().register("createApplicationBundle", CreateIosApplicationBundleTask.class, task -> {
			task.getApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/main/" + moduleName + "-unsigned.app"));
			task.getSources().from(linkStoryboardTask.flatMap(StoryboardLinkTask::getDestinationDirectory));
			// Linked file is configured in IosApplicationRules
			task.getSources().from(assetCatalogCompileTaskTask.flatMap(AssetCatalogCompileTask::getDestinationDirectory));
			task.getSources().from(processPropertyListTask.flatMap(ProcessPropertyListTask::getOutputFile));
		});

		TaskProvider<SignIosApplicationBundleTask> signApplicationBundleTask = getTasks().register("signApplicationBundle", SignIosApplicationBundleTask.class, task -> {
			task.getUnsignedApplicationBundle().set(createApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
			task.getSignedApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/main/" + moduleName + ".app"));
		});

		getTasks().named("assemble", it -> it.dependsOn(signApplicationBundleTask));
	}
}
