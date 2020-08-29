package dev.nokee.platform.ios.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.DependencyBucketName;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.nativebase.internal.dependencies.ConsumableRuntimeLibraries;
import dev.nokee.platform.nativebase.internal.dependencies.NativeComponentDependenciesInternal;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependencies;
import lombok.Getter;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public class IosApplicationOutgoingDependencies implements NativeOutgoingDependencies {
	@Getter private final DirectoryProperty exportedHeaders;
	@Getter private final RegularFileProperty exportedSwiftModule;
	@Getter private final Property<Binary> exportedBinary;

	@Inject
	public IosApplicationOutgoingDependencies(BuildVariantInternal buildVariant, NativeComponentDependenciesInternal dependencies, ObjectFactory objectFactory) {
		this.exportedHeaders = objectFactory.directoryProperty();
		this.exportedSwiftModule = objectFactory.fileProperty();
		this.exportedBinary = objectFactory.property(Binary.class);

		dependencies.register(DependencyBucketName.of("runtimeElements"), ConsumableRuntimeLibraries.class, it -> {
			it.extendsFrom(dependencies.getImplementation(), dependencies.getRuntimeOnly());
			it.variant(buildVariant);
			it.artifact(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary));
		});
	}

	private Provider<FileSystemLocation> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof SignedIosApplicationBundleInternal) {
			return ((SignedIosApplicationBundleInternal) binary).getApplicationBundleLocation();
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}
}
