package dev.nokee.platform.ios.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependencies;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public class IosApplicationOutgoingDependencies implements NativeOutgoingDependencies {
	@Getter(AccessLevel.PROTECTED) private final DefaultNativeComponentDependencies dependencies;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter private final DirectoryProperty exportedHeaders;
	@Getter private final RegularFileProperty exportedSwiftModule;
	@Getter private final Property<Binary> exportedBinary;

	@Inject
	public IosApplicationOutgoingDependencies(NamingScheme names, BuildVariantInternal buildVariant, DefaultNativeComponentDependencies dependencies, ConfigurationContainer configurations, ObjectFactory objects) {
		this.dependencies = dependencies;
		this.configurations = configurations;
		this.objects = objects;
		this.exportedHeaders = objects.directoryProperty();
		this.exportedSwiftModule = objects.fileProperty();
		this.exportedBinary = objects.property(Binary.class);

		ConfigurationUtils builder = objects.newInstance(ConfigurationUtils.class);
		Configuration runtimeElements = getConfigurations().create(names.getConfigurationName("runtimeElements"), builder.asOutgoingRuntimeLibrariesFrom(dependencies.getImplementation().getAsConfiguration(), dependencies.getRuntimeOnly().getAsConfiguration()).withVariant(buildVariant));

		runtimeElements.getOutgoing().artifact(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary));
	}

	private Provider<FileSystemLocation> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof SignedIosApplicationBundleInternal) {
			return ((SignedIosApplicationBundleInternal) binary).getApplicationBundleLocation();
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}
}
