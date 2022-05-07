/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.ide.xcode.internal.XcodeIdePropertyAdapter;
import dev.nokee.ide.xcode.internal.XcodeIdeRequest;
import dev.nokee.ide.xcode.internal.rules.CreateNativeComponentXcodeIdeProject;
import dev.nokee.ide.xcode.internal.tasks.SyncXcodeIdeProduct;
import dev.nokee.model.internal.ModelElementFactory;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
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
import org.gradle.util.GUtil;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class XcodeIdePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("dev.nokee.xcode-ide-base");

		project.getPlugins().withType(ComponentModelBasePlugin.class, mapComponentToXcodeIdeProjects(project, (XcodeIdeProjectExtension) project.getExtensions().getByName(XcodeIdeBasePlugin.XCODE_EXTENSION_NAME)));
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

	private Action<ComponentModelBasePlugin> mapComponentToXcodeIdeProjects(Project project, XcodeIdeProjectExtension extension) {
		return new Action<ComponentModelBasePlugin>() {
			@Override
			public void execute(ComponentModelBasePlugin appliedPlugin) {
				val modelConfigurer = project.getExtensions().getByType(ModelConfigurer.class);
				val action = new CreateNativeComponentXcodeIdeProject(extension, project.getProviders(), project.getObjects(), project.getLayout(), project.getTasks(), ProjectIdentifier.of(project), project.getExtensions().getByType(ModelLookup.class));
				modelConfigurer.configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(IsComponent.class), ModelComponentReference.of(ModelElementFactory.class), (entity, tag, factory) -> {
					action.execute(factory.createElement(entity));
				})));
			}
		};
	}
}
