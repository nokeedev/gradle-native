package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class SwiftLibraryOutgoingDependencies extends AbstractNativeLibraryOutgoingDependencies implements NativeOutgoingDependencies {
	private final ConfigurationUtils builder;

	@Inject
	public SwiftLibraryOutgoingDependencies(DomainObjectIdentifierInternal ownerIdentifier, BuildVariantInternal buildVariant, DefaultNativeLibraryComponentDependencies dependencies, ConfigurationContainer configurationContainer, ObjectFactory objects) {
		super(ownerIdentifier, buildVariant, dependencies, configurationContainer, objects);
		this.builder = objects.newInstance(ConfigurationUtils.class);

		val configurationRegistry = new ConfigurationBucketRegistryImpl(configurationContainer);
		val identifier = DependencyBucketIdentifier.of(DependencyBucketName.of("apiElements"),
			ConsumableDependencyBucket.class, ownerIdentifier);
		Configuration apiElements = configurationRegistry.createIfAbsent(identifier.getConfigurationName(), ConfigurationBucketType.CONSUMABLE, builder.asOutgoingSwiftModuleFrom(dependencies.getApi().getAsConfiguration(), dependencies.getCompileOnly().getAsConfiguration()).withVariant(buildVariant).withDescription(identifier.getDisplayName()));

		apiElements.getOutgoing().artifact(getExportedSwiftModule());
	}
}
