package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import dev.nokee.ide.xcode.internal.XcodeIdePropertyAdapter;
import dev.nokee.ide.xcode.internal.tasks.SyncXcodeIdeProduct;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.*;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.objectivec.tasks.ObjectiveCCompile;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Gradle plugin that configure the Xcode IDE model for iOS application integration with Xcode IDE.
 * The following is the manual test plan to ensure the integration works properly:
 * - Can build from Xcode: cmd+B -> builds iOS application
 * - Can run from Xcode: Press play button -> builds and run iOS application inside simulator
 * - Can execute unit test from side dot: Press failing side dot -> build, run tests and fail
 *                                        Press succeeding side dot -> build, run tests and succeeds
 * - Can execute ui test from side dot: Press failing side dot -> build, run tests and fail
 *                                      Press succeeding side dot -> build, run tests and succeeds
 * - Can execute unit and ui tests from testing tab
 */
public abstract class XcodeIdeObjectiveCIosApplicationPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		DefaultIosApplicationComponent application = ((DefaultObjectiveCIosApplicationExtension) project.getExtensions().getByType(ObjectiveCIosApplicationExtension.class)).getComponent();
		String moduleName = GUtil.toCamelCase(project.getName());
		project.getExtensions().getByType(XcodeIdeProjectExtension.class).getProjects().register(project.getName(), xcodeProject -> {

			// TODO: Lock properties to avoid breaking the assumption
			NamedDomainObjectProvider<XcodeIdeTarget> appTarget = xcodeProject.getTargets().register(moduleName, xcodeTarget -> {

				xcodeTarget.getProductName().set(moduleName);
				xcodeTarget.getProductReference().set(moduleName + ".app");
				xcodeTarget.getProductType().set(XcodeIdeProductTypes.APPLICATION);

				xcodeTarget.getBuildConfigurations().register("Default", xcodeConfiguration -> {
					Provider<ExecutableBinaryInternal> binary = application.getDevelopmentVariant().flatMap(it -> it.getBinaries().withType(ExecutableBinaryInternal.class).getElements().map(b -> b.iterator().next()));

					xcodeConfiguration.getProductLocation().set(application.getVariants().getElements().flatMap(it -> it.iterator().next().getBinaries().withType(SignedIosApplicationBundleInternal.class).get().iterator().next().getApplicationBundleLocation()));
					xcodeConfiguration.getBuildSettings()
						.put("BUNDLE_LOADER", "$(TEST_HOST)")
						// Codesign
						.put("INFOPLIST_FILE", "src/main/resources/Info.plist")
						.put("IPHONEOS_DEPLOYMENT_TARGET", 13.2)
						.put("PRODUCT_BUNDLE_IDENTIFIER", project.getGroup().toString() + "." + moduleName)
						.put("PRODUCT_NAME", "$(TARGET_NAME)")
						.put("TARGETED_DEVICE_FAMILY", "1,2")
						.put("SDKROOT", "iphoneos")
						.put("USER_HEADER_SEARCH_PATHS", binary.flatMap(BaseNativeBinary::getHeaderSearchPaths).map(this::toSpaceSeparatedList))
						.put("FRAMEWORK_SEARCH_PATHS", binary.flatMap(BaseNativeBinary::getFrameworkSearchPaths).map(this::toSpaceSeparatedList))

						// Oh boy, talk about hacking Xcode! Let's capture some important information here.
						// The indexing is taken care by SourceKit, started in background by Xcode.
						// It uses the build settings to create the compiler args which are then used on each compilation unit.
						// This means the C headers are ignored from this process.
						// Through this process SourceKit gets access to the symbols within the compilation unit.
						// At this stage, the indexing looks complete, but in fact, we only have access to the symbols.
						// We have access to their location inside the source files.
						// However, the process seems to be completely blind with regards to #import/#include inside C header files.
						// For example, you can _jump to definition_ from a source file to a C header file through its #import/#include.
						// But we can't _jump to definition_ from a C header file to the same C header file through its #import/#include.
						// We can however _jump to definition_ on any symbols in both C header files and compilation unit.
						// My guess is the process used by SourceKit parsed a preprocessed result of the compilation unit.
						// The #import/#include macros are already resolved.
						//
						// As an experiment, you can trick Xcode to treat the C header files are a compilation unit.
						// To do so, change the known type of the C header files to something that can be compiled, i.e. Objective-C sources.
						// Looking at the SourceKit logs, we can see the file being picked up by the normal process.
						// The side effect of this approach is the C header files will be shown as Objective-C sources.
						// That is pretty bad when we want to achieve a vanilla experience with Xcode.
						//
						// Comparing the SourceKit log of a normal Xcode target with our indexer target, we see that SourceKit uses `-index-store-path`.
						// We can use the OTHER_CFLAGS to pass an array like: ["-index-store-path", "$(BUILD_ROOT)/../../Index/DataStore"].
						// The basic idea is to point `-index-store-path` to the folders `Index/DataStore` under the project-specific derived data.
						// Since there is no build settings pointing to the project-specific derived data, we need to pick another close enough variable.
						// BUILD_ROOT point to only 2 directory deep inside the derived data directory.
						// There are other build settings we can use but that one is good enough.
						// This solution is clean, but not clean enough.
						//
						// It happens the `-index-store-path` is managed by the COMPILER_INDEX_STORE_ENABLE build setting.
						// On a vanilla Xcode project the default value seems to be _YES_.
						// However, on our indexer target, the default value seems to be _NO_.
						// If we explicitly set the build setting to _YES_, there is no need to use OTHER_CFLAGS.
						//
						// IMPORTANT: The Index/DataStore seems to get out of sync when doing a lot of trial and error with our indexer target.
						// It's best to nuke the project-specific derived data directory before each attempt.
						// It's also important to quit Xcode completely between attempt to avoid any cached states.
						//
						// NOTE: It is possible to compute the index store ahead of time.
						// If the index store is coming from a different machine, there may be a need to relocate the data.
						// For relocating, we can use https://github.com/lyft/index-import
						// At the moment, it's best to let Xcode compute the index store as it's the most transparent and provide a more vanilla experience.
						// Computing the index store outside of Xcode is a performance optimization and should be dealt with when it becomes a bottleneck.
						.put("COMPILER_INDEX_STORE_ENABLE", "YES")

						// The headermap is another feature that seems to hinder indexing.
						// The observation shows the behavior is a bit arbitrary on which files are not indexed properly.
						.put("USE_HEADERMAP", "NO");
				});

				xcodeTarget.getSources().from(project.fileTree("src/main/headers", it -> it.include("*")));
				xcodeTarget.getSources().from(project.fileTree("src/main/objc", it -> it.include("*")));
				xcodeTarget.getSources().from(getProviders().provider(() -> {
					try {
						List<Path> result = new ArrayList<>();
						Files.walkFileTree(project.file("src/main/resources").toPath(), new FileVisitor<Path>() {
							@Override
							public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
								if (path.equals(project.file("src/main/resources").toPath())) {
									return FileVisitResult.CONTINUE;
								} else if (path.getFileName().toString().endsWith(".lproj")) {
									return FileVisitResult.CONTINUE;
								}
								result.add(path);
								return FileVisitResult.SKIP_SUBTREE;
							}

							@Override
							public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
								result.add(path);
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
								return FileVisitResult.TERMINATE;
							}

							@Override
							public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
								return FileVisitResult.CONTINUE;
							}
						});
						return result;
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}));
			});

			project.getPluginManager().withPlugin("dev.nokee.objective-c-xctest-test-suite", appliedPlugin -> {
//				// Overwrite the product location to use the xctest one
//				appTarget.configure(xcodeTarget -> {
//					xcodeTarget.getBuildConfigurations().configureEach(xcodeConfiguration -> {
//						xcodeConfiguration.getProductLocation().set(getTasks().named("signUnitTestLauncherApplicationBundle", SignIosApplicationBundleTask.class).flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
//					});
//				});

				xcodeProject.getTargets().register(moduleName + "UnitTest", xcodeTarget -> {
					xcodeTarget.getProductName().set(moduleName + "UnitTest");
					xcodeTarget.getProductReference().set(moduleName + "UnitTest.xctest");
					xcodeTarget.getProductType().set(XcodeIdeProductTypes.UNIT_TEST);

					xcodeTarget.getBuildConfigurations().register("Default", xcodeConfiguration -> {
						// Use the unsigned bundle as Xcode will perform the signing
						xcodeConfiguration.getProductLocation().set(getTasks().named("createUnitTestXCTestBundle", CreateIosXCTestBundleTask.class).flatMap(CreateIosXCTestBundleTask::getXCTestBundle));
						xcodeConfiguration.getBuildSettings()
							.put("BUNDLE_LOADER", "$(TEST_HOST)")
							// FIXME: The TEST_HOST should be set in theory but doesn't seems to work as expected in practice.
//							.put("TEST_HOST", "$(BUILT_PRODUCTS_DIR)/ObjectiveCIosApplicationUnitTest.app/ObjectiveCIosApplicationUnitTest")
							// Codesign
							.put("INFOPLIST_FILE", "src/unitTest/resources/Info.plist")
							.put("IPHONEOS_DEPLOYMENT_TARGET", 13.2)
							.put("PRODUCT_BUNDLE_IDENTIFIER", project.getGroup().toString() + "." + moduleName + "UnitTest")
							.put("PRODUCT_NAME", "$(TARGET_NAME)")
							.put("TARGETED_DEVICE_FAMILY", "1,2")
							.put("SDKROOT", "iphoneos")
							.put("USER_HEADER_SEARCH_PATHS", getProviders().provider(this::getHeaderSearchPaths))
							.put("FRAMEWORK_SEARCH_PATHS", getProviders().provider(this::getXCTestFrameworkPaths))
							.put("COMPILER_INDEX_STORE_ENABLE", "YES")
							.put("USE_HEADERMAP", "NO");
					});
					xcodeTarget.getSources().from(project.fileTree("src/unitTest/headers", it -> it.include("*")));
					xcodeTarget.getSources().from(project.fileTree("src/unitTest/objc", it -> it.include("*")));
					xcodeTarget.getSources().from(project.fileTree("src/unitTest/resources", it -> it.include("*")));
				});

				xcodeProject.getTargets().register(moduleName + "UiTest", xcodeTarget -> {
					xcodeTarget.getProductName().set(moduleName + "UiTest");
					xcodeTarget.getProductReference().set(moduleName + "UiTest.xctest");
					xcodeTarget.getProductType().set(XcodeIdeProductTypes.UI_TEST);

					xcodeTarget.getBuildConfigurations().register("Default", xcodeConfiguration -> {
						xcodeConfiguration.getProductLocation().set(getTasks().named("createUiTestLauncherApplicationBundle", CreateIosApplicationBundleTask.class).flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
						xcodeConfiguration.getBuildSettings()
							.put("BUNDLE_LOADER", "$(TEST_HOST)")
							// Codesign
							.put("INFOPLIST_FILE", "src/uiTest/resources/Info.plist")
							.put("IPHONEOS_DEPLOYMENT_TARGET", 13.2)
							.put("PRODUCT_BUNDLE_IDENTIFIER", project.getGroup().toString() + "." + moduleName + "UiTest")
							.put("PRODUCT_NAME", "$(TARGET_NAME)")
							.put("TARGETED_DEVICE_FAMILY", "1,2")
							.put("SDKROOT", "iphoneos")
							.put("USER_HEADER_SEARCH_PATHS", getProviders().provider(this::getHeaderSearchPaths))
							.put("FRAMEWORK_SEARCH_PATHS", getProviders().provider(this::getXCTestFrameworkPaths))
							.put("COMPILER_INDEX_STORE_ENABLE", "YES")
							.put("USE_HEADERMAP", "NO")
							.put("TEST_TARGET_NAME", moduleName);
					});
					xcodeTarget.getSources().from(project.fileTree("src/uiTest/headers", it -> it.include("*")));
					xcodeTarget.getSources().from(project.fileTree("src/uiTest/objc", it -> it.include("*")));
					xcodeTarget.getSources().from(project.fileTree("src/uiTest/resources", it -> it.include("*")));
				});
			});
		});

		project.getPluginManager().withPlugin("dev.nokee.objective-c-xctest-test-suite", appliedPlugin -> {
			// The Xcode IDE model will sync the `.xctest` product but we need the `-Runner.app`.
			// We can't just sync the `-Runner.app` as Xcode will complain about a missing `.xctest`.
			// Also the file under the `Products` group depends what product is synced.
			// It's better to sync both files over to the BUILT_PRODUCTS_DIR.
			getTasks().register("syncUiTestRunner", SyncXcodeIdeProduct.class, task -> {
				task.getProductLocation().set(getTasks().named("createUiTestLauncherApplicationBundle", CreateIosApplicationBundleTask.class).flatMap(CreateIosApplicationBundleTask::getApplicationBundle));

				// TODO: To improve the situation, we should provide XcodeIdePropertyAdapter and/or XcodeIdeRequest through this task.
				//  If we clean up the situation by exposing the lifecycle/sync task through the model, we could avoid exposing the information on the task
				//  However, it would be convenient for the users that need to hack things together.
				//  It should also be possible to achieve similar integration in term of complexity only via public APIs.
				//  It will need to be evaluated in the bigger context.
				task.getDestinationLocation().set(getObjects().directoryProperty().fileValue(new File(new XcodeIdePropertyAdapter(project).getBuiltProductsDir())).file(moduleName + "UiTest-Runner.app"));
			});

			getTasks().withType(SyncXcodeIdeProduct.class).configureEach(task -> {
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
							XcodeIdePlugin.XcodeIdeRequest request = XcodeIdePlugin.XcodeIdeRequest.of(task.getName());
							FileCollection sources = project.getExtensions().getByType(XcodeIdeProjectExtension.class).getProjects().getByName(request.getProjectName()).getTargets().getByName(request.getTargetName()).getSources();
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

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract ObjectFactory getObjects();

	private String getHeaderSearchPaths() {
		return getTasks().withType(ObjectiveCCompile.class).stream()
			// TODO: We should reprobe the compiler with proper sysroot to avoid filtering the System includes here.
			.flatMap(it -> Stream.concat(it.getIncludes().getFiles().stream(), it.getSystemIncludes().getFiles().stream().filter(f -> !f.getAbsolutePath().contains("/MacOSX.platform/"))))
			.map(File::getAbsolutePath)
			.collect(Collectors.joining(" "));
	}

	private String getFrameworkPaths() {
		try {
			Process process = new ProcessBuilder("xcrun", "--sdk", "iphonesimulator", "--show-sdk-path").start();
			process.waitFor();
			return IOUtils.toString(process.getInputStream(), Charset.defaultCharset()).trim() + "/System/Library/Frameworks";
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getXCTestFrameworkPaths() {
		try {
			Process process = new ProcessBuilder("xcrun", "--sdk", "iphonesimulator", "--show-sdk-platform-path").start();
			process.waitFor();
			return getFrameworkPaths() + " " + IOUtils.toString(process.getInputStream(), Charset.defaultCharset()).trim() + "/Developer/Library/Frameworks";
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String toSpaceSeparatedList(Set<FileSystemLocation> paths) {
		return paths.stream().map(location -> location.getAsFile().getAbsolutePath()).collect(Collectors.joining(" "));
	}
}
