package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import org.gradle.api.artifacts.Configuration;

import javax.inject.Inject;

public abstract class SwiftLibraryOutgoingDependencies extends AbstractNativeLibraryOutgoingDependencies implements NativeOutgoingDependencies {
	private final ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);

	@Inject
	public SwiftLibraryOutgoingDependencies(NamingScheme names, BuildVariant buildVariant, DefaultNativeLibraryDependencies dependencies) {
		super(names, buildVariant, dependencies);

		Configuration apiElements = getConfigurations().create(names.getConfigurationName("apiElements"), builder.asOutgoingSwiftModuleFrom(dependencies.getApiDependencies(), dependencies.getCompileOnlyDependencies()).withDescription(names.getConfigurationDescription("API elements for %s.")));

		apiElements.getOutgoing().artifact(getExportedSwiftModule());
	}
}
