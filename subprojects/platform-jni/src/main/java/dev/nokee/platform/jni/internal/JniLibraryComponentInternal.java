package dev.nokee.platform.jni.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactories;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.internal.BaseTargetBuildType;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JniLibraryComponentInternal extends BaseComponent<JniLibraryInternal> implements DependencyAwareComponent<JavaNativeInterfaceLibraryComponentDependencies>, BinaryAwareComponent, Component {
	private final DefaultJavaNativeInterfaceLibraryComponentDependencies dependencies;
	private final GroupId groupId;
	private final DomainObjectSet<LanguageSourceSetInternal> sources;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	private final JavaNativeInterfaceComponentBinaries componentBinaries;

	@Inject
	public JniLibraryComponentInternal(ComponentIdentifier<JniLibraryComponentInternal> identifier, NamingScheme names, GroupId groupId, ObjectFactory objects, ConfigurationContainer configurations, DependencyHandler dependencyHandler, ProviderFactory providers, TaskContainer taskContainer, PluginContainer pluginContainer) {
		super(identifier, names, JniLibraryInternal.class, objects);
		this.configurations = configurations;
		this.dependencyHandler = dependencyHandler;
		this.providers = providers;
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.MaybeCreating(getConfigurations()), names::getConfigurationName), new DefaultDependencyFactory(getDependencyHandler()))));
		this.dependencies = objects.newInstance(DefaultJavaNativeInterfaceLibraryComponentDependencies.class, dependencyContainer);
		this.groupId = groupId;
		this.sources = objects.domainObjectSet(LanguageSourceSetInternal.class);
		this.targetMachines = objects.setProperty(TargetMachine.class);
		this.componentBinaries = new JavaNativeInterfaceComponentBinaries(new TaskRegistryImpl(taskContainer), this, objects, pluginContainer);
		((BinaryViewImpl<?>)getBinaries()).onRealize(() -> getVariantCollection().realize());

		getDimensions().convention(ImmutableSet.of(DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE, BaseTargetBuildType.DIMENSION_TYPE));
		getDimensions().disallowChanges(); // Let's disallow changing them for now.

		getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		getDevelopmentVariant().convention(providers.provider(new BuildableDevelopmentVariantConvention<>(getVariantCollection()::get)));
		getDevelopmentVariant().disallowChanges();
	}

	@Override
	public JavaNativeInterfaceComponentBinaries getComponentBinaries() {
		return componentBinaries;
	}

	@Override
	public DefaultJavaNativeInterfaceLibraryComponentDependencies getDependencies() {
		return dependencies;
	}

	//region Variant-awareness
	public VariantView<JniLibrary> getVariants() {
		return getVariantCollection().getAsView(JniLibrary.class);
	}

	public JniLibraryInternal createVariant(VariantIdentifier<JniLibraryInternal> identifier, VariantComponentDependencies variantDependencies) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		Preconditions.checkArgument(buildVariant.getDimensions().size() == 2);
		Preconditions.checkArgument(buildVariant.getDimensions().get(0) instanceof OperatingSystemFamily);
		Preconditions.checkArgument(buildVariant.getDimensions().get(1) instanceof MachineArchitecture);
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		JniLibraryInternal result = getObjects().newInstance(JniLibraryInternal.class, identifier, names, sources, groupId, variantDependencies, componentBinaries.getAsViewFor(identifier));
		return result;
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
		return componentBinaries.getAsView();
	}
}
