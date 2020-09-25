package dev.nokee.platform.jni.internal;

import com.google.common.base.Preconditions;
import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSet;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import lombok.Getter;
import lombok.val;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

public class JavaNativeInterfaceComponentVariants implements ComponentVariants {
	@Getter private final VariantCollection<JniLibraryInternal> variantCollection;
	@Getter private final SetProperty<BuildVariantInternal> buildVariants;
	private final ObjectFactory objectFactory;
	private final JniLibraryComponentInternal component;
	private final ConfigurationContainer configurationContainer;
	private final DependencyHandler dependencyHandler;

	public JavaNativeInterfaceComponentVariants(ObjectFactory objectFactory, JniLibraryComponentInternal component, ConfigurationContainer configurationContainer, DependencyHandler dependencyHandler) {
		this.variantCollection = new VariantCollection<>(JniLibraryInternal.class, objectFactory);
		this.buildVariants = objectFactory.setProperty(BuildVariantInternal.class);
		this.objectFactory = objectFactory;
		this.component = component;
		this.configurationContainer = configurationContainer;
		this.dependencyHandler = dependencyHandler;
	}

	public void calculateVariants() {
		buildVariants.get().forEach(buildVariant -> {
			val names = component.getNames().forBuildVariant(buildVariant, buildVariants.get());
			val variantIdentifier = VariantIdentifier.builder().withUnambiguousNameFromBuildVariants(buildVariant, buildVariants.get()).withComponentIdentifier(component.getIdentifier()).withType(JniLibraryInternal.class).build();

			val dependencies = newDependencies(names.withComponentDisplayName("JNI shared library"), buildVariant, component, variantIdentifier);
			variantCollection.registerVariant(variantIdentifier, (name, bv) -> createVariant(variantIdentifier, dependencies));
		});
	}

	private JniLibraryInternal createVariant(VariantIdentifier<JniLibraryInternal> identifier, VariantComponentDependencies variantDependencies) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		Preconditions.checkArgument(buildVariant.getDimensions().size() == 2);
		Preconditions.checkArgument(buildVariant.getDimensions().get(0) instanceof OperatingSystemFamily);
		Preconditions.checkArgument(buildVariant.getDimensions().get(1) instanceof MachineArchitecture);
		NamingScheme names = component.getNames().forBuildVariant(buildVariant, component.getBuildVariants().get());

		JniLibraryInternal result = objectFactory.newInstance(JniLibraryInternal.class, identifier, names, component.getSources(), component.getGroupId(), component.getBinaryCollection(), variantDependencies);
		return result;
	}

	private VariantComponentDependencies newDependencies(NamingScheme names, BuildVariantInternal buildVariant, JniLibraryComponentInternal component, VariantIdentifier<JniLibraryInternal> variantIdentifier) {
		DefaultJavaNativeInterfaceNativeComponentDependencies variantDependencies = component.getDependencies();
		if (component.getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultJavaNativeInterfaceNativeComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				component.getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(new NativeComponentDependenciesJavaNativeInterfaceAdapter(variantDependencies)).withVariant(buildVariant);
		boolean hasSwift = !component.getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		boolean hasHeader = !component.getSourceCollection().matching(it -> it instanceof CSourceSet || it instanceof CppSourceSet || it instanceof ObjectiveCSourceSet || it instanceof ObjectiveCppSourceSet).isEmpty();
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else if (hasHeader) {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(objectFactory);

		return new VariantComponentDependencies(variantDependencies, incoming);
	}
}
