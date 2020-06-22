package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.artifacts.Configuration;

import javax.inject.Inject;

public abstract class NativeLibraryOutgoingDependencies extends AbstractNativeLibraryOutgoingDependencies implements NativeOutgoingDependencies {
	private final ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);

	@Inject
	public NativeLibraryOutgoingDependencies(NamingScheme names, BuildVariant buildVariant, DefaultNativeLibraryDependencies dependencies) {
		super(names, buildVariant, dependencies);

		DefaultTargetMachine targetMachine = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));
		Configuration apiElements = getConfigurations().create(names.getConfigurationName("apiElements"), builder.asOutgoingHeaderSearchPathFrom(dependencies.getApiDependencies(), dependencies.getCompileOnlyDependencies()).forTargetMachine(targetMachine).withDescription("API elements for %s."));

		apiElements.getOutgoing().artifact(getExportedHeaders());
	}
}
