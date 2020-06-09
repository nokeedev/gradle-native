package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.internal.Cast;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.OperatingSystemOperations;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.util.GUtil;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class XcodeIdeSwiftLibraryPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getExtensions().getByType(XcodeIdeProjectExtension.class).getProjects().register(project.getName(), xcodeProject -> {
			ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", project.getExtensions().getByType(ComponentCollection.class));
			components.configureEach(DefaultNativeLibraryComponent.class, library -> {
				xcodeProject.getTargets().register(GUtil.toCamelCase(project.getName()), xcodeTarget -> {
					xcodeTarget.getProductName().set(GUtil.toCamelCase(project.getName()));
					xcodeTarget.getProductReference().set(project.provider(() -> OperatingSystemOperations.of(library.getDevelopmentVariant().flatMap(it -> it.getBinaries().withType(SharedLibraryBinaryInternal.class).getElements().map(b -> b.iterator().next())).get().getTargetMachine().getOperatingSystemFamily()).getSharedLibraryName(GUtil.toCamelCase(project.getName()))));
					xcodeTarget.getProductType().set(XcodeIdeProductTypes.DYNAMIC_LIBRARY);

					xcodeTarget.getBuildConfigurations().register("Default", xcodeConfiguration -> {
						Provider<SharedLibraryBinaryInternal> binary = library.getDevelopmentVariant().flatMap(it -> it.getBinaries().withType(SharedLibraryBinaryInternal.class).getElements().map(b -> b.iterator().next()));

						xcodeConfiguration.getProductLocation().set(binary.flatMap(SharedLibraryBinary::getLinkTask).flatMap(LinkSharedLibrary::getLinkedFile));
						xcodeConfiguration.getBuildSettings()
							.put("PRODUCT_NAME", "$(TARGET_NAME)")
							.put("SWIFT_VERSION", "5.2")
							.put("SWIFT_INCLUDE_PATHS", binary.flatMap(SharedLibraryBinaryInternal::getImportSearchPaths).map(this::toSpaceSeparatedList))
							.put("FRAMEWORK_SEARCH_PATHS", binary.flatMap(SharedLibraryBinaryInternal::getFrameworkSearchPaths).map(this::toSpaceSeparatedList))
							.put("COMPILER_INDEX_STORE_ENABLE", "YES")
							.put("USE_HEADERMAP", "NO");
					});

					xcodeTarget.getSources().from(project.fileTree("src/main/headers", it -> it.include("*")));
					library.getSourceCollection().forEach(sourceSet -> {
						xcodeTarget.getSources().from(sourceSet.getAsFileTree());
					});
				});
			});
		});
	}

	private String toSpaceSeparatedList(Set<FileSystemLocation> paths) {
		return paths.stream().map(location -> location.getAsFile().getAbsolutePath()).collect(Collectors.joining(" "));
	}
}
