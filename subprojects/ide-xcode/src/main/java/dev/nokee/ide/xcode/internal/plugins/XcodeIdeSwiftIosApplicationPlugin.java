package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import dev.nokee.ide.xcode.internal.XcodeIdePropertyAdapter;
import dev.nokee.ide.xcode.internal.tasks.SyncXcodeIdeProduct;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.SwiftIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultSwiftIosApplicationExtension;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.nativebase.internal.BundleBinaryInternal;
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
public abstract class XcodeIdeSwiftIosApplicationPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		DefaultIosApplicationComponent application = ((DefaultSwiftIosApplicationExtension)project.getExtensions().getByType(SwiftIosApplicationExtension.class)).getComponent();
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
						.put("SWIFT_INCLUDE_PATHS", binary.flatMap(ExecutableBinaryInternal::getImportSearchPaths).map(this::toSpaceSeparatedList))
						.put("FRAMEWORK_SEARCH_PATHS", binary.flatMap(ExecutableBinaryInternal::getFrameworkSearchPaths).map(this::toSpaceSeparatedList))

						.put("COMPILER_INDEX_STORE_ENABLE", "YES")

						// The headermap is another feature that seems to hinder indexing.
						// The observation shows the behavior is a bit arbitrary on which files are not indexed properly.
						.put("USE_HEADERMAP", "NO");
				});

				xcodeTarget.getSources().from(project.fileTree("src/main/swift", it -> it.include("*")));
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

	@Inject
	protected abstract ObjectFactory getObjects();

	private String toSpaceSeparatedList(Set<FileSystemLocation> paths) {
		return paths.stream().map(location -> location.getAsFile().getAbsolutePath()).collect(Collectors.joining(" "));
	}
}
