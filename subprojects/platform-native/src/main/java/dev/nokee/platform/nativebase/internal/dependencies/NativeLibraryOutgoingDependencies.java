package dev.nokee.platform.nativebase.internal.dependencies;

import com.google.common.collect.Iterables;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.runtime.nativebase.internal.NativeArtifactTypes;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class NativeLibraryOutgoingDependencies extends AbstractNativeLibraryOutgoingDependencies implements NativeOutgoingDependencies {
	private final ConfigurationUtils builder;

	@Inject
	public NativeLibraryOutgoingDependencies(DomainObjectIdentifierInternal ownerIdentifier, BuildVariantInternal buildVariant, DefaultNativeLibraryComponentDependencies dependencies, ConfigurationContainer configurationContainer, ObjectFactory objects) {
		super(ownerIdentifier, buildVariant, dependencies, configurationContainer, objects);
		builder = objects.newInstance(ConfigurationUtils.class);

		val configurationRegistry = new ConfigurationBucketRegistryImpl(configurationContainer);
		val identifier = DependencyBucketIdentifier.of(DependencyBucketName.of("apiElements"),
			ConsumableDependencyBucket.class, ownerIdentifier);
		// TODO: Introduce compileOnlyApi which apiElements should extends from
		val apiElements = configurationRegistry.createIfAbsent(identifier.getConfigurationName(), ConfigurationBucketType.CONSUMABLE, builder.asOutgoingHeaderSearchPathFrom(dependencies.getApi().getAsConfiguration()).withVariant(buildVariant).withDescription(identifier.getDisplayName()));

		// See https://github.com/gradle/gradle/issues/15146 to learn more about splitting the implicit dependencies
		apiElements.getOutgoing().artifact(getExportedHeaders().getElements().flatMap(it -> ProviderUtils.fixed(Iterables.getOnlyElement(it))), it -> {
			it.builtBy(getExportedHeaders());
			it.setType(NativeArtifactTypes.NATIVE_HEADERS_DIRECTORY);
		});
	}
}
