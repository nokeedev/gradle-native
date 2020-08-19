package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public class NativeApplicationOutgoingDependencies implements NativeOutgoingDependencies {
	@Getter private final DirectoryProperty exportedHeaders;
	@Getter private final RegularFileProperty exportedSwiftModule;
	@Getter private final Property<Binary> exportedBinary;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public NativeApplicationOutgoingDependencies(NamingScheme names, BuildVariantInternal buildVariant, DefaultNativeComponentDependencies dependencies, ConfigurationContainer configurations, ObjectFactory objects) {
		this.configurations = configurations;
		this.objects = objects;
		this.exportedHeaders = objects.directoryProperty();
		this.exportedSwiftModule = objects.fileProperty();
		this.exportedBinary = objects.property(Binary.class);
		ConfigurationUtils builder = objects.newInstance(ConfigurationUtils.class);
		Configuration runtimeElements = getConfigurations().create(names.getConfigurationName("runtimeElements"), builder.asOutgoingRuntimeLibrariesFrom(dependencies.getImplementation().getAsConfiguration(), dependencies.getRuntimeOnly().getAsConfiguration()).withVariant(buildVariant).withDescription(names.getConfigurationDescription("Runtime elements for %s.")));

		runtimeElements.getOutgoing().artifact(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary));
	}

	private Provider<RegularFile> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof ExecutableBinaryInternal) {
			return ((ExecutableBinaryInternal) binary).getLinkTask().flatMap(LinkExecutable::getLinkedFile);
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}
}
