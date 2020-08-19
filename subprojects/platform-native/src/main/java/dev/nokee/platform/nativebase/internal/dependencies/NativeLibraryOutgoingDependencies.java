package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class NativeLibraryOutgoingDependencies extends AbstractNativeLibraryOutgoingDependencies implements NativeOutgoingDependencies {
	private final ConfigurationUtils builder;

	@Inject
	public NativeLibraryOutgoingDependencies(NamingScheme names, BuildVariantInternal buildVariant, DefaultNativeLibraryComponentDependencies dependencies, ConfigurationContainer configurations, ObjectFactory objects) {
		super(names, buildVariant, dependencies, configurations, objects);
		builder = objects.newInstance(ConfigurationUtils.class);

		Configuration apiElements = getConfigurations().create(names.getConfigurationName("apiElements"), builder.asOutgoingHeaderSearchPathFrom(dependencies.getApi().getAsConfiguration(), dependencies.getCompileOnly().getAsConfiguration()).withVariant(buildVariant).withDescription("API elements for %s."));

		apiElements.getOutgoing().artifact(getExportedHeaders());
	}
}
