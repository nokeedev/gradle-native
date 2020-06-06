package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public abstract class AbstractNativeLibraryOutgoingDependencies {
	private final ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);
	private final NamingScheme names;
	private final BuildVariant buildVariant;
	private final DefaultTargetMachine targetMachine;

	@Inject
	public AbstractNativeLibraryOutgoingDependencies(NamingScheme names, BuildVariant buildVariant, DefaultNativeLibraryDependencies dependencies) {
		this.names = names;
		this.buildVariant = buildVariant;
		this.targetMachine = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));

		Configuration linkElements = getConfigurations().create(names.getConfigurationName("linkElements"), builder.asOutgoingLinkLibrariesFrom(dependencies.getApiDependencies(), dependencies.getLinkOnlyDependencies()).withSharedLinkage().forTargetMachine(targetMachine).withDescription(names.getConfigurationDescription("Link elements for %s.")));
		Configuration runtimeElements = getConfigurations().create(names.getConfigurationName("runtimeElements"), builder.asOutgoingRuntimeLibrariesFrom(dependencies.getImplementationDependencies(), dependencies.getRuntimeOnlyDependencies()).withSharedLinkage().forTargetMachine(targetMachine).withDescription(names.getConfigurationDescription("Runtime elements for %s.")));

		linkElements.getOutgoing().artifact(getExportedBinary().flatMap(this::getOutgoingLinkLibrary));
		runtimeElements.getOutgoing().artifact(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary));
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	private Provider<RegularFile> getOutgoingLinkLibrary(Binary binary) {
		if (binary instanceof SharedLibraryBinaryInternal) {
			if (((SharedLibraryBinaryInternal) binary).getTargetMachine().getOperatingSystemFamily().isWindows()) {
				return ((SharedLibraryBinaryInternal) binary).getLinkTask().flatMap(it -> ((LinkSharedLibraryTask) it).getImportLibrary());
			}
			return ((SharedLibraryBinaryInternal) binary).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile);
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}

	private Provider<RegularFile> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof SharedLibraryBinaryInternal) {
			return ((SharedLibraryBinaryInternal) binary).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile);
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}

	public abstract DirectoryProperty getExportedHeaders();
	public abstract RegularFileProperty getExportedSwiftModule();
	public abstract Property<Binary> getExportedBinary();
}
