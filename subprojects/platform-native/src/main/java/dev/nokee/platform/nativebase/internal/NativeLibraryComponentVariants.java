package dev.nokee.platform.nativebase.internal;

import com.google.common.base.Preconditions;
import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public final class NativeLibraryComponentVariants implements ComponentVariants {
	@Getter private final VariantCollection<DefaultNativeLibraryVariant> variantCollection;
	@Getter private final SetProperty<BuildVariantInternal> buildVariants;
	@Getter private final Provider<DefaultNativeLibraryVariant> developmentVariant;
	private final ObjectFactory objectFactory;
	private final DefaultNativeLibraryComponent component;
	private final DependencyHandler dependencyHandler;
	private final ConfigurationContainer configurationContainer;

	public NativeLibraryComponentVariants(ObjectFactory objectFactory, DefaultNativeLibraryComponent component, DependencyHandler dependencyHandler, ConfigurationContainer configurationContainer, ProviderFactory providerFactory) {
		this.variantCollection = new VariantCollection<>(DefaultNativeLibraryVariant.class, objectFactory);
		this.buildVariants = objectFactory.setProperty(BuildVariantInternal.class);
		this.developmentVariant = providerFactory.provider(new BuildableDevelopmentVariantConvention<>(() -> getVariantCollection().get()));
		this.objectFactory = objectFactory;
		this.component = component;
		this.dependencyHandler = dependencyHandler;
		this.configurationContainer = configurationContainer;
	}

	public void calculateVariants() {
		buildVariants.get().forEach(buildVariant -> {
			val names = component.getNames().forBuildVariant(buildVariant, getBuildVariants().get());
			val variantIdentifier = VariantIdentifier.builder().withUnambiguousNameFromBuildVariants(buildVariant, getBuildVariants().get()).withComponentIdentifier(component.getIdentifier()).withType(DefaultNativeLibraryVariant.class).build();

			val dependencies = newDependencies(names.withComponentDisplayName(component.getIdentifier().getDisplayName()), buildVariant, variantIdentifier);
			val variant = variantCollection.registerVariant(variantIdentifier, (name, bv) -> createVariant(variantIdentifier, dependencies));

			onEachVariantDependencies(variant, dependencies);
		});
	}

	private VariantComponentDependencies<NativeLibraryComponentDependencies> newDependencies(NamingScheme names, BuildVariantInternal buildVariant, VariantIdentifier<DefaultNativeLibraryVariant> variantIdentifier) {
		var variantDependencies = component.getDependencies();
		if (getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultNativeLibraryComponentDependencies.class, dependencyContainer);
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

		NativeOutgoingDependencies outgoing = null;
		if (hasSwift) {
			outgoing = objectFactory.newInstance(SwiftLibraryOutgoingDependencies.class, names, buildVariant, variantDependencies);
		} else {
			outgoing = objectFactory.newInstance(NativeLibraryOutgoingDependencies.class, names, buildVariant, variantDependencies);
		}

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	private DefaultNativeLibraryVariant createVariant(VariantIdentifier<?> identifier, VariantComponentDependencies<?> variantDependencies) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		NamingScheme names = component.getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultNativeLibraryVariant result = objectFactory.newInstance(DefaultNativeLibraryVariant.class, identifier, names, variantDependencies);
		return result;
	}

	private void onEachVariantDependencies(VariantProvider<DefaultNativeLibraryVariant> variant, VariantComponentDependencies<?> dependencies) {
		if (NativeLibrary.class.isAssignableFrom(DefaultNativeLibraryVariant.class)) {
			if (!component.getSourceCollection().withType(SwiftSourceSet.class).isEmpty()) {
				dependencies.getOutgoing().getExportedSwiftModule().convention(variant.flatMap(it -> {
					List<? extends Provider<RegularFile>> result = it.getBinaries().withType(NativeBinary.class).flatMap(binary -> {
						List<? extends Provider<RegularFile>> modules = binary.getCompileTasks().withType(SwiftCompileTask.class).map(task -> task.getModuleFile()).get();
						return modules;
					}).get();
					return one(result);
				}));
			}
			component.getSourceCollection().matching(it -> (it instanceof CHeaderSet || it instanceof CppHeaderSet) && it.getName().equals("public")).configureEach(sourceSet -> {
				// TODO: Allow to export more than one folder
				File directory = null;
				if (sourceSet instanceof CHeaderSet) {
					directory = ((CHeaderSet) sourceSet).getHeaderDirectory();
				} else if (sourceSet instanceof CppHeaderSet) {
					directory = ((CppHeaderSet) sourceSet).getHeaderDirectory();
				}

				dependencies.getOutgoing().getExportedHeaders().fileValue(directory);
			});
		}
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}

	private static <T> T one(Iterable<T> c) {
		Iterator<T> iterator = c.iterator();
		Preconditions.checkArgument(iterator.hasNext(), "collection needs to have one element, was empty");
		T result = iterator.next();
		Preconditions.checkArgument(!iterator.hasNext(), "collection needs to only have one element, more than one element found");
		return result;
	}
}
