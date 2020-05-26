package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.internal.PathAwareCommandLineTool;
import dev.nokee.core.exec.internal.VersionedCommandLineTool;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.DescriptorCommandLineTool;
import dev.nokee.platform.ios.tasks.internal.*;
import dev.nokee.runtime.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;
import org.gradle.util.GUtil;
import org.gradle.util.VersionNumber;

import javax.inject.Inject;
import java.io.File;

public abstract class ObjectiveCIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		NamingScheme names = NamingScheme.asMainComponent(project.getName());
		DefaultObjectiveCIosApplicationExtension extension = getObjects().newInstance(DefaultObjectiveCIosApplicationExtension.class,
			getObjects().newInstance(DefaultNativeComponentDependencies.class, names), names);

		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(ObjectiveCIosApplicationExtension.class, EXTENSION_NAME, extension);

		project.getPluginManager().apply(LifecycleBasePlugin.class);
		project.getPluginManager().apply("objective-c"); // Until we move away from the software model like platform JNI
		project.getPluginManager().apply("dev.nokee.objective-c-language");
		project.getPluginManager().apply(DarwinRuntimePlugin.class);

		project.getPluginManager().withPlugin("dev.nokee.objective-c-language", appliedPlugin -> project.getPluginManager().apply(IosApplicationRules.class));

		Configuration interfaceBuilderToolConfiguration = project.getConfigurations().create("interfaceBuilderTool");
		interfaceBuilderToolConfiguration.getDependencies().add(project.getDependencies().create("dev.nokee.tool:ibtool:latest.release"));
		Provider<CommandLineTool> interfaceBuilderTool = getProviders().provider(() -> new DescriptorCommandLineTool(interfaceBuilderToolConfiguration.getSingleFile()));

		Provider<CommandLineTool> assetCompilerTool = getProviders().provider(() -> new VersionedCommandLineTool(new File("/usr/bin/actool"), VersionNumber.parse("11.3.1")));
		Provider<CommandLineTool> codeSignatureTool = getProviders().provider(() -> new PathAwareCommandLineTool(new File("/usr/bin/codesign")));

		String moduleName = GUtil.toCamelCase(project.getName());

		TaskProvider<StoryboardCompileTask> compileStoryboardTask = getTasks().register("compileStoryboard", StoryboardCompileTask.class, task -> {
			task.getDestinationDirectory().set(getLayout().getBuildDirectory().dir("ios/storyboards/compiled/main"));
			task.getModule().set(moduleName);
			task.getSources().from(project.fileTree("src/main/resources", it -> it.include("*.lproj/*.storyboard")));
			task.getInterfaceBuilderTool().set(interfaceBuilderTool);
			task.getInterfaceBuilderTool().finalizeValueOnRead();
		});

		TaskProvider<StoryboardLinkTask> linkStoryboardTask = getTasks().register("linkStoryboard", StoryboardLinkTask.class, task -> {
			task.getDestinationDirectory().set(getLayout().getBuildDirectory().dir("ios/storyboards/linked/main"));
			task.getModule().set(moduleName);
			task.getSources().from(compileStoryboardTask.flatMap(StoryboardCompileTask::getDestinationDirectory));
			task.getInterfaceBuilderTool().set(interfaceBuilderTool);
			task.getInterfaceBuilderTool().finalizeValueOnRead();
		});

		TaskProvider<AssetCatalogCompileTask> assetCatalogCompileTaskTask = getTasks().register("compileAssetCatalog", AssetCatalogCompileTask.class, task -> {
			task.getSource().set(project.file("src/main/resources/Assets.xcassets"));
			task.getIdentifier().set(project.provider(() -> project.getGroup().toString() + "." + moduleName));
			task.getDestinationDirectory().set(getLayout().getBuildDirectory().dir("ios/assets/main"));
			task.getAssetCompilerTool().set(assetCompilerTool);
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
			task.getCodeSignatureTool().set(codeSignatureTool);
		});

		getTasks().named("assemble", it -> it.dependsOn(signApplicationBundleTask));
	}
}
