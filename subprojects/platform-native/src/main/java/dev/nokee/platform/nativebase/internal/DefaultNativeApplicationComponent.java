package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantCollection;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.*;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class DefaultNativeApplicationComponent extends BaseNativeComponent<DefaultNativeApplicationVariant> implements DependencyAwareComponent<NativeApplicationComponentDependencies>, BinaryAwareComponent, Component {
	private final DefaultNativeApplicationComponentDependencies dependencies;
	private final TaskRegistry taskRegistry;
	private final NativeApplicationComponentVariants componentVariants;
	private final BinaryView<Binary> binaries;

	@Inject
	public DefaultNativeApplicationComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ConfigurationContainer configurations, DependencyHandler dependencyHandler, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory) {
		super(identifier, DefaultNativeApplicationVariant.class, objects, tasks, eventPublisher);
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurations), dependencyHandler)));
		this.dependencies = objects.newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, BaseTargetBuildType.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
		this.taskRegistry = new TaskRegistryImpl(tasks);
		this.componentVariants = new NativeApplicationComponentVariants(objects, this, dependencyHandler, configurations, providers, taskRegistry, eventPublisher, viewFactory, variantRepository, binaryViewFactory);
		this.binaries = binaryViewFactory.create(identifier);
	}

	@Override
	public DefaultNativeApplicationComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public Provider<DefaultNativeApplicationVariant> getDevelopmentVariant() {
		return componentVariants.getDevelopmentVariant();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaries;
	}

	@Override
	public VariantCollection<DefaultNativeApplicationVariant> getVariantCollection() {
		return componentVariants.getVariantCollection();
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return componentVariants.getBuildVariants();
	}

	public void finalizeExtension(Project project) {
		getVariantCollection().whenElementKnown(new CreateNativeBinaryLifecycleTaskRule(taskRegistry));
		getVariantCollection().whenElementKnown(this::createBinaries);
		getVariantCollection().whenElementKnown(new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		getVariantCollection().whenElementKnown(new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);

		componentVariants.calculateVariants();
	}
}
