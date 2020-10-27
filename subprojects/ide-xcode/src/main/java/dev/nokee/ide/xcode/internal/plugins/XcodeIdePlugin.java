package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.ide.xcode.internal.XcodeIdePropertyAdapter;
import dev.nokee.ide.xcode.internal.XcodeIdeRequest;
import dev.nokee.ide.xcode.internal.rules.CreateNativeComponentXcodeIdeProject;
import dev.nokee.ide.xcode.internal.tasks.SyncXcodeIdeProduct;
import dev.nokee.language.base.internal.LanguageSourceSetRepository;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.components.ComponentConfigurer;
import dev.nokee.platform.base.internal.components.KnownComponent;
import dev.nokee.platform.base.internal.components.KnownComponentFactory;
import dev.nokee.platform.base.internal.plugins.ComponentBasePlugin;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.reflect.TypeOf;
import org.gradle.util.GUtil;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class XcodeIdePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("dev.nokee.xcode-ide-base");

		project.getPlugins().withType(ComponentBasePlugin.class, mapComponentToXcodeIdeProjects(project, (XcodeIdeProjectExtension) project.getExtensions().getByName(XcodeIdeBasePlugin.XCODE_EXTENSION_NAME)));
		project.getPluginManager().withPlugin("dev.nokee.objective-c-xctest-test-suite", appliedPlugin -> {
			String moduleName = GUtil.toCamelCase(project.getName());

			// The Xcode IDE model will sync the `.xctest` product but we need the `-Runner.app`.
			// We can't just sync the `-Runner.app` as Xcode will complain about a missing `.xctest`.
			// Also the file under the `Products` group depends what product is synced.
			// It's better to sync both files over to the BUILT_PRODUCTS_DIR.
			project.getTasks().register("syncUiTestRunner", SyncXcodeIdeProduct.class, task -> {
				task.getProductLocation().set(project.getTasks().named("createUiTestLauncherApplicationBundle", CreateIosApplicationBundleTask.class).flatMap(CreateIosApplicationBundleTask::getApplicationBundle));

				// TODO: To improve the situation, we should provide XcodeIdePropertyAdapter and/or XcodeIdeRequest through this task.
				//  If we clean up the situation by exposing the lifecycle/sync task through the model, we could avoid exposing the information on the task
				//  However, it would be convenient for the users that need to hack things together.
				//  It should also be possible to achieve similar integration in term of complexity only via public APIs.
				//  It will need to be evaluated in the bigger context.
				val properties = project.getObjects().newInstance(XcodeIdePropertyAdapter.class);
				task.getDestinationLocation().set(project.getObjects().directoryProperty().fileProvider(properties.getBuiltProductsDir().map(File::new)).file(moduleName + "UiTest-Runner.app"));
			});

			project.getTasks().withType(SyncXcodeIdeProduct.class).configureEach(task -> {
				// Complete wiring for syncing the `-Runner.app` for UI testing.
				if (task.getName().contains(moduleName + "UiTest")) {
					task.dependsOn("syncUiTestRunner");

					// We also depends on the tested application lifecycle task here (very poorly), as it's referenced via the TEST_TARGET_NAME build setting.
					task.dependsOn(task.getName().replace("UiTest", ""));
				}

//				// MAYBE???
//				if (task.getName().contains(moduleName + "UnitTest")) {
//					task.getDestinationLocation().set(getObjects().directoryProperty().fileValue(new File(new XcodeIdePropertyAdapter(project).getBuiltProductsDir()/*.replace("/Default-", "/__NokeeTestRunner_Default-")*/)).file(moduleName + "UnitTest.xctest"));
//				}
				// TODO: Environment variable should be part of the task inputs for instant execution.
				//  We are also over reaching quite a bit to get the list of files required, etc.
				//  If we add the lifecycle tasks to the XcodeIde* model, we could clean up that over reaching.
				if (task.getName().contains(moduleName + "UnitTest") || task.getName().contains(moduleName + "UiTest")) {
					task.doLast(new Action<Task>() {
						@Override
						public void execute(Task task) {
							XcodeIdeRequest request = project.getObjects().newInstance(XcodeIdeRequest.class, task.getName());
							FileCollection sources = project.getProject().getExtensions().getByType(XcodeIdeProjectExtension.class).getProjects().getByName(request.getProjectName()).getTargets().getByName(request.getTargetName()).getSources();
							for (String arch : StringUtils.split(System.getenv("ARCHS"), ' ')) {
								String objectFileDir = System.getenv("OBJECT_FILE_DIR");
								String productName = System.getenv("PRODUCT_NAME");
								File dependencyInfo = new File(objectFileDir + "-normal/" + arch, productName + "_dependency_info.dat");
								dependencyInfo.getParentFile().mkdirs();
								try {
									FileUtils.writeByteArrayToFile(dependencyInfo, new byte[] {0, 0x31, 0});
									for (File file : sources) {
										new File(objectFileDir + "-normal/" + arch, FilenameUtils.removeExtension(file.getName()) + ".d").createNewFile();
									}
								} catch (IOException e) {
									throw new UncheckedIOException(e);
								}
							}
						}
					});
				}
			});
		});
	}

	private Action<ComponentBasePlugin> mapComponentToXcodeIdeProjects(Project project, XcodeIdeProjectExtension extension) {
		return new Action<ComponentBasePlugin>() {
			private KnownComponentFactory knownComponentFactory;

			private KnownComponentFactory getKnownComponentFactory() {
				if (knownComponentFactory == null) {
					knownComponentFactory = project.getExtensions().getByType(KnownComponentFactory.class);
				}
				return knownComponentFactory;
			}

			@Override
			public void execute(ComponentBasePlugin appliedPlugin) {
				val componentConfigurer = project.getExtensions().getByType(ComponentConfigurer.class);
				componentConfigurer.whenElementKnown(ProjectIdentifier.of(project), getComponentImplementationType(), asKnownComponent(new CreateNativeComponentXcodeIdeProject(extension, project.getProviders(), project.getObjects(), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getLayout(), project.getTasks(), ProjectIdentifier.of(project))));
			}

			private <T extends Component> Action<? super TypeAwareDomainObjectIdentifier<T>> asKnownComponent(Action<? super KnownComponent<T>> action) {
				return identifier -> action.execute(getKnownComponentFactory().create(identifier));
			}

			private Class<BaseComponent<?>> getComponentImplementationType() {
				return new TypeOf<BaseComponent<?>>() {}.getConcreteClass();
			}
		};
	}
}
