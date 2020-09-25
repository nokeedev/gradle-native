package dev.nokee.testing.nativebase.internal;

import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

public final class NativeTestSuiteComponentVariants implements ComponentVariants {
	@Getter private final VariantCollection<DefaultNativeTestSuiteVariant> variantCollection;
	@Getter private final SetProperty<BuildVariantInternal> buildVariants;
	@Getter private final Provider<DefaultNativeTestSuiteVariant> developmentVariant;
	private final ObjectFactory objectFactory;
	private final DefaultNativeTestSuiteComponent component;
	private final DependencyHandler dependencyHandler;
	private final ConfigurationContainer configurationContainer;

	public NativeTestSuiteComponentVariants(ObjectFactory objectFactory, DefaultNativeTestSuiteComponent component, DependencyHandler dependencyHandler, ConfigurationContainer configurationContainer, ProviderFactory providerFactory) {
		this.variantCollection = new VariantCollection<>(DefaultNativeTestSuiteVariant.class, objectFactory);
		this.buildVariants = objectFactory.setProperty(BuildVariantInternal.class);
		this.developmentVariant = providerFactory.provider(new BuildableDevelopmentVariantConvention<>(() -> getVariantCollection().get()));
		this.objectFactory = objectFactory;
		this.component = component;
		this.dependencyHandler = dependencyHandler;
		this.configurationContainer = configurationContainer;
	}

	public void calculateVariants() {
		getBuildVariants().get().forEach(buildVariant -> {
			val names = component.getNames().forBuildVariant(buildVariant, getBuildVariants().get());
			val variantIdentifier = VariantIdentifier.builder().withUnambiguousNameFromBuildVariants(buildVariant, getBuildVariants().get()).withComponentIdentifier(component.getIdentifier()).withType(DefaultNativeTestSuiteVariant.class).build();

			val dependencies = newDependencies(names.withComponentDisplayName(component.getIdentifier().getDisplayName()), buildVariant, variantIdentifier);
			val variant = getVariantCollection().registerVariant(variantIdentifier, (name, bv) -> createVariant(variantIdentifier, dependencies));

			onEachVariantDependencies(variant, dependencies);
		});
	}

	private VariantComponentDependencies<DefaultNativeComponentDependencies> newDependencies(NamingScheme names, BuildVariantInternal buildVariant, VariantIdentifier<DefaultNativeTestSuiteVariant> variantIdentifier) {
		var variantDependencies = component.getDependencies();
		if (getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				component.getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant);
		boolean hasSwift = !component.getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(objectFactory);
		NativeOutgoingDependencies outgoing = objectFactory.newInstance(NativeApplicationOutgoingDependencies.class, names, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	private DefaultNativeTestSuiteVariant createVariant(VariantIdentifier<?> identifier, VariantComponentDependencies<?> variantDependencies) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		NamingScheme names = component.getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		val result = objectFactory.newInstance(DefaultNativeTestSuiteVariant.class, identifier, names, variantDependencies);
		return result;
	}

	private void onEachVariantDependencies(VariantProvider<DefaultNativeTestSuiteVariant> variant, VariantComponentDependencies<?> dependencies) {
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}
}
