package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.xcode.XcodeIdeProductType;
import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import dev.nokee.internal.Cast;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class XcodeIdeNativeLibraryPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getExtensions().getByType(XcodeIdeProjectExtension.class).getProjects().register(project.getName(), xcodeProject -> {
			ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", project.getExtensions().getByType(ComponentCollection.class));
			components.configureEach(DefaultNativeLibraryComponent.class, library -> {

				val linkages = library.getBuildVariants().get().stream().map(b -> b.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)).collect(Collectors.toSet()); // TODO Maybe use linkedhashset to keep the ordering
				if (linkages.size() > 1) {
					linkages.forEach(linkage -> {
						xcodeProject.getTargets().register(library.getBaseName().get() + StringUtils.capitalize(linkage.getName()), configureTargetForLinkage(library, linkage));
					});
				} else {
					val linkage = linkages.iterator().next();
					xcodeProject.getTargets().register(library.getBaseName().get(), configureTargetForLinkage(library, linkage));
				}
			});
		});
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	private Action<XcodeIdeTarget> configureTargetForLinkage(DefaultNativeLibraryComponent library, DefaultBinaryLinkage linkage) {
		return xcodeTarget -> {
			xcodeTarget.getProductName().set(library.getBaseName());
			xcodeTarget.getProductReference().set(getProviders().provider(() -> {
				val osOperations = OperatingSystemOperations.of(library.getDevelopmentVariant().flatMap(it -> it.getBinaries().withType(BaseNativeBinary.class).getElements().map(b -> b.iterator().next())).get().getTargetMachine().getOperatingSystemFamily());
				if (linkage.isShared()) {
					return osOperations.getSharedLibraryName(library.getBaseName().get());
				} else if (linkage.isStatic()) {
					return osOperations.getStaticLibraryName(library.getBaseName().get());
				}
				throw unsupportedLinkage(linkage);
			}));
			xcodeTarget.getProductType().set(toProductType(linkage));

			if (linkage.isShared()) {
				xcodeTarget.getBuildConfigurations().register("Default", xcodeConfiguration -> {
					Provider<SharedLibraryBinaryInternal> binary = library.getDevelopmentVariant().flatMap(it -> it.getBinaries().withType(SharedLibraryBinaryInternal.class).getElements().map(b -> b.iterator().next()));

					xcodeConfiguration.getProductLocation().set(binary.flatMap(SharedLibraryBinary::getLinkTask).flatMap(LinkSharedLibrary::getLinkedFile));
					xcodeConfiguration.getBuildSettings()
						.put("PRODUCT_NAME", "$(TARGET_NAME)")
						.put("HEADER_SEARCH_PATHS", binary.flatMap(SharedLibraryBinaryInternal::getHeaderSearchPaths).map(this::toSpaceSeparatedList))
						.put("FRAMEWORK_SEARCH_PATHS", binary.flatMap(SharedLibraryBinaryInternal::getFrameworkSearchPaths).map(this::toSpaceSeparatedList))
						.put("COMPILER_INDEX_STORE_ENABLE", "YES")
						.put("USE_HEADERMAP", "NO");

					if (!library.getSourceCollection().withType(SwiftSourceSet.class).isEmpty()) {
						xcodeConfiguration.getBuildSettings()
							.put("SWIFT_VERSION", "5.2")
							.put("SWIFT_INCLUDE_PATHS", binary.flatMap(SharedLibraryBinaryInternal::getImportSearchPaths).map(this::toSpaceSeparatedList));
					}
				});
			} else if (linkage.isStatic()) {
				xcodeTarget.getBuildConfigurations().register("Default", xcodeConfiguration -> {
					Provider<StaticLibraryBinaryInternal> binary = library.getBinaries().withType(StaticLibraryBinaryInternal.class).filter(it -> it.isBuildable()).map(it -> it.iterator().next());

					xcodeConfiguration.getProductLocation().set(binary.flatMap(StaticLibraryBinaryInternal::getCreateTask).flatMap(CreateStaticLibrary::getOutputFile));
					xcodeConfiguration.getBuildSettings()
						.put("PRODUCT_NAME", "$(TARGET_NAME)")
						.put("HEADER_SEARCH_PATHS", binary.flatMap(StaticLibraryBinaryInternal::getHeaderSearchPaths).map(this::toSpaceSeparatedList))
						.put("FRAMEWORK_SEARCH_PATHS", binary.flatMap(StaticLibraryBinaryInternal::getFrameworkSearchPaths).map(this::toSpaceSeparatedList))
						.put("COMPILER_INDEX_STORE_ENABLE", "YES")
						.put("USE_HEADERMAP", "NO");

					if (!library.getSourceCollection().withType(SwiftSourceSet.class).isEmpty()) {
						xcodeConfiguration.getBuildSettings()
							.put("SWIFT_VERSION", "5.2")
							.put("SWIFT_INCLUDE_PATHS", binary.flatMap(StaticLibraryBinaryInternal::getImportSearchPaths).map(this::toSpaceSeparatedList));
					}
				});
			} else {
				throw unsupportedLinkage(linkage);
			}


			xcodeTarget.getSources().from(getProviders().provider(() -> library.getSourceCollection().stream().map(SourceSet::getAsFileTree).collect(Collectors.toList())));
		};
	}

	private XcodeIdeProductType toProductType(DefaultBinaryLinkage linkage) {
		if (linkage.isShared()) {
			return XcodeIdeProductTypes.DYNAMIC_LIBRARY;
		} else if (linkage.isStatic()) {
			return XcodeIdeProductTypes.STATIC_LIBRARY;
		}
		throw unsupportedLinkage(linkage);
	}

	private String toSpaceSeparatedList(Set<FileSystemLocation> paths) {
		return paths.stream().map(location -> location.getAsFile().getAbsolutePath()).collect(Collectors.joining(" "));
	}

	private static IllegalArgumentException unsupportedLinkage(DefaultBinaryLinkage linkage) {
		return new IllegalArgumentException(String.format("Unsupported linkage '%s'.", linkage));
	}
}
