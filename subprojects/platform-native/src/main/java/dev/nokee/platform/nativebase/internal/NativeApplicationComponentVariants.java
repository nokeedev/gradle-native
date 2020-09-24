package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

public final class NativeApplicationComponentVariants implements ComponentVariants {
	@Getter private final VariantCollection<DefaultNativeApplicationVariant> variantCollection;
	@Getter private final SetProperty<BuildVariantInternal> buildVariants;
	private final ObjectFactory objectFactory;
	private final DefaultNativeApplicationComponent component;
	private final DependencyHandler dependencyHandler;
	private final ConfigurationContainer configurationContainer;

	public NativeApplicationComponentVariants(ObjectFactory objectFactory, DefaultNativeApplicationComponent component, DependencyHandler dependencyHandler, ConfigurationContainer configurationContainer) {
		this.variantCollection = new VariantCollection<>(DefaultNativeApplicationVariant.class, objectFactory);
		this.buildVariants = objectFactory.setProperty(BuildVariantInternal.class);
		this.objectFactory = objectFactory;
		this.component = component;
		this.dependencyHandler = dependencyHandler;
		this.configurationContainer = configurationContainer;
	}

	public void calculateVariants() {
		getBuildVariants().get().forEach(buildVariant -> {
			val names = component.getNames().forBuildVariant(buildVariant, getBuildVariants().get());
			val variantIdentifier = VariantIdentifier.builder().withUnambiguousNameFromBuildVariants(buildVariant, getBuildVariants().get()).withComponentIdentifier(component.getIdentifier()).withType(DefaultNativeApplicationVariant.class).build();

			val dependencies = newDependencies(names.withComponentDisplayName(component.getIdentifier().getDisplayName()), buildVariant, variantIdentifier);
			val variant = getVariantCollection().registerVariant(variantIdentifier, (name, bv) -> createVariant(variantIdentifier, dependencies));

			onEachVariantDependencies(variant, dependencies);
		});
	}

	private VariantComponentDependencies<NativeApplicationComponentDependencies> newDependencies(NamingScheme names, BuildVariantInternal buildVariant, VariantIdentifier<DefaultNativeApplicationVariant> variantIdentifier) {
		var variantDependencies = component.getDependencies();
		if (getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				component.getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		boolean hasSwift = !component.getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant);
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		val incoming = incomingDependenciesBuilder.buildUsing(objectFactory);
		NativeOutgoingDependencies outgoing = objectFactory.newInstance(NativeApplicationOutgoingDependencies.class, names, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	private DefaultNativeApplicationVariant createVariant(VariantIdentifier<?> identifier, VariantComponentDependencies<?> variantDependencies) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		val names = component.getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultNativeApplicationVariant result = objectFactory.newInstance(DefaultNativeApplicationVariant.class, identifier, names, variantDependencies);
		return result;
	}

	private void onEachVariantDependencies(VariantProvider<DefaultNativeApplicationVariant> variant, VariantComponentDependencies<?> dependencies) {
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}
}
