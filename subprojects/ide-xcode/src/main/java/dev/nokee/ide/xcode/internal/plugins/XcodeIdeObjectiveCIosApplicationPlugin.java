package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

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
						.put("SDKROOT", "iphoneos");
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
}
