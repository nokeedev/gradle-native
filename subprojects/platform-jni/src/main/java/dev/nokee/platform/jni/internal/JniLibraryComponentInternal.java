package dev.nokee.platform.jni.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.internal.BaseTargetBuildType;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JniLibraryComponentInternal extends BaseComponent<JniLibraryInternal> implements DependencyAwareComponent<JavaNativeInterfaceLibraryComponentDependencies>, BinaryAwareComponent, Component {
	private final DefaultJavaNativeInterfaceLibraryComponentDependencies dependencies;
	@Getter private final GroupId groupId;
	private final DomainObjectSet<LanguageSourceSetInternal> sources;
	private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	private final BinaryView<Binary> binaries;
	private final JavaNativeInterfaceComponentVariants componentVariants;

	@Inject
	public JniLibraryComponentInternal(ComponentIdentifier<JniLibraryComponentInternal> identifier, NamingScheme names, GroupId groupId, ObjectFactory objects, ConfigurationContainer configurations, DependencyHandler dependencyHandler, ProviderFactory providers) {
		super(identifier, names, JniLibraryInternal.class, objects);
		this.objects = objects;
		this.configurations = configurations;
		this.dependencyHandler = dependencyHandler;
		this.providers = providers;
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurations), getDependencyHandler())));
		this.dependencies = objects.newInstance(DefaultJavaNativeInterfaceLibraryComponentDependencies.class, dependencyContainer);
		this.groupId = groupId;
		this.sources = objects.domainObjectSet(LanguageSourceSetInternal.class);
		this.targetMachines = objects.setProperty(TargetMachine.class);
		this.componentVariants = new JavaNativeInterfaceComponentVariants(objects, this, configurations, dependencyHandler);
		this.binaries = Cast.uncheckedCast(objects.newInstance(VariantAwareBinaryView.class, new DefaultMappingView<>(componentVariants.getVariantCollection().getAsView(JniLibraryInternal.class), Variant::getBinaries)));

		getDimensions().convention(ImmutableSet.of(DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE, BaseTargetBuildType.DIMENSION_TYPE));
		getDimensions().disallowChanges(); // Let's disallow changing them for now.

		getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		getDevelopmentVariant().convention(providers.provider(new BuildableDevelopmentVariantConvention<>(getVariantCollection()::get)));
		getDevelopmentVariant().disallowChanges();
	}

	@Override
	public DefaultJavaNativeInterfaceLibraryComponentDependencies getDependencies() {
		return dependencies;
	}

	//region Variant-awareness
	public VariantView<JniLibrary> getVariants() {
		return getVariantCollection().getAsView(JniLibrary.class);
	}

	private List<BuildVariantInternal> createBuildVariants() {
		Set<TargetMachine> targetMachines = getTargetMachines().get();
		return targetMachines.stream().map(it -> (DefaultTargetMachine)it).map(it -> DefaultBuildVariant.of(it.getOperatingSystemFamily(), it.getArchitecture())).collect(Collectors.toList());
	}
	//endregion

	public Configuration getJvmImplementationDependencies() {
		return dependencies.getJvmImplementation().getAsConfiguration();
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
		return sources;
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaries;
	}

	@Override
	public VariantCollection<JniLibraryInternal> getVariantCollection() {
		return componentVariants.getVariantCollection();
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return componentVariants.getBuildVariants();
	}

	public void finalizeExtension(Project project) {
		componentVariants.calculateVariants();
	}
}
