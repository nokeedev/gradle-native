package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.internal.Cast;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.OperatingSystemOperations;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class XcodeIdeNativeApplicationPlugin implements Plugin<Project> {
	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public void apply(Project project) {
		project.getExtensions().getByType(XcodeIdeProjectExtension.class).getProjects().register(project.getName(), xcodeProject -> {
			ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", project.getExtensions().getByType(ComponentCollection.class));
			components.configureEach(DefaultNativeApplicationComponent.class, application -> {
				xcodeProject.getTargets().register(project.getName(), xcodeTarget -> {
					xcodeTarget.getProductName().set(project.getName());
					xcodeTarget.getProductReference().set(getProviders().provider(() -> {
						val osOperations = OperatingSystemOperations.of(application.getDevelopmentVariant().flatMap(it -> it.getBinaries().withType(BaseNativeBinary.class).getElements().map(b -> b.iterator().next())).get().getTargetMachine().getOperatingSystemFamily());
						return osOperations.getExecutableName(application.getBaseName().get());
					}));
					xcodeTarget.getProductType().set(XcodeIdeProductTypes.TOOL);

					xcodeTarget.getBuildConfigurations().register("Default", xcodeConfiguration -> {
						Provider<ExecutableBinaryInternal> binary = application.getDevelopmentVariant().flatMap(it -> it.getBinaries().withType(ExecutableBinaryInternal.class).getElements().map(b -> b.iterator().next()));

						xcodeConfiguration.getProductLocation().set(binary.flatMap(ExecutableBinary::getLinkTask).flatMap(LinkExecutable::getLinkedFile));
						xcodeConfiguration.getBuildSettings()
							.put("PRODUCT_NAME", "$(TARGET_NAME)")
							.put("HEADER_SEARCH_PATHS", binary.flatMap(ExecutableBinaryInternal::getHeaderSearchPaths).map(this::toSpaceSeparatedList))
							.put("FRAMEWORK_SEARCH_PATHS", binary.flatMap(ExecutableBinaryInternal::getFrameworkSearchPaths).map(this::toSpaceSeparatedList))
							.put("COMPILER_INDEX_STORE_ENABLE", "YES")
							.put("USE_HEADERMAP", "NO");
					});

					xcodeTarget.getSources().from(project.fileTree("src/main/headers", it -> it.include("*")));
					application.getSourceCollection().forEach(sourceSet -> {
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
