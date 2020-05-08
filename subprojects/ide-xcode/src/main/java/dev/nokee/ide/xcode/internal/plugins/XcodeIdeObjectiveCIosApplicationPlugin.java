package dev.nokee.ide.xcode.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class XcodeIdeObjectiveCIosApplicationPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getExtensions().getByType(XcodeIdeProjectExtension.class).getProjects().register(project.getName(), xcodeProject -> {
			String moduleName = GUtil.toCamelCase(project.getName());

			// TODO: Lock properties to avoid breaking the assumption
			xcodeProject.getTargets().register(moduleName, xcodeTarget -> {

				xcodeTarget.getProductName().set(moduleName);
				xcodeTarget.getProductReference().set(moduleName + ".app");
				xcodeTarget.getProductType().set(XcodeIdeProductTypes.APPLICATION);

				xcodeTarget.getBuildConfigurations().register("Default", xcodeConfiguration -> {
					xcodeConfiguration.getProductLocation().set(getTasks().named("signApplicationBundle", SignIosApplicationBundleTask.class).flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
					xcodeConfiguration.getBuildSettings()
						.put("BUNDLE_LOADER", "$(TEST_HOST)")
						// Codesign
						.put("INFOPLIST_FILE", "src/main/resources/Info.plist")
						.put("IPHONEOS_DEPLOYMENT_TARGET", 13.2)
						.put("PRODUCT_BUNDLE_IDENTIFIER", project.getGroup().toString() + "." + moduleName)
						.put("PRODUCT_NAME", "$(TARGET_NAME)")
						.put("TARGETED_DEVICE_FAMILY", "1,2")
						.put("SDKROOT", "iphoneos")
						.put("HEADER_SEARCH_PATHS", getProviders().provider(this::getHeaderSearchPaths))
						.put("FRAMEWORK_SEARCH_PATHS", getProviders().provider(this::getFrameworkPaths))

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
		});
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProviderFactory getProviders();

	private String getHeaderSearchPaths() {
		return getTasks().withType(ObjectiveCCompile.class).stream()
			.flatMap(it -> Stream.concat(it.getIncludes().getFiles().stream(), it.getSystemIncludes().getFiles().stream()))
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
}
