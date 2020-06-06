package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
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

public abstract class NativeApplicationOutgoingDependencies implements NativeOutgoingDependencies {
	private final NamingScheme names;
	private final BuildVariant buildVariant;

	@Inject
	public NativeApplicationOutgoingDependencies(NamingScheme names, BuildVariant buildVariant, DefaultNativeComponentDependencies dependencies) {
		this.names = names;
		this.buildVariant = buildVariant;

		ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);
		DefaultTargetMachine targetMachine = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));

		Configuration runtimeElements = getConfigurations().create(names.getConfigurationName("runtimeElements"), builder.asOutgoingRuntimeLibrariesFrom(dependencies.getImplementationDependencies(), dependencies.getRuntimeOnlyDependencies()).withSharedLinkage().forTargetMachine(targetMachine).withDescription(names.getConfigurationDescription("Runtime elements for %s.")));

		runtimeElements.getOutgoing().artifact(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary));
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	private Provider<RegularFile> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof ExecutableBinaryInternal) {
			return ((ExecutableBinaryInternal) binary).getLinkTask().flatMap(LinkExecutable::getLinkedFile);
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}

	public abstract DirectoryProperty getExportedHeaders();
	public abstract RegularFileProperty getExportedSwiftModule();
	public abstract Property<Binary> getExportedBinary();
}
