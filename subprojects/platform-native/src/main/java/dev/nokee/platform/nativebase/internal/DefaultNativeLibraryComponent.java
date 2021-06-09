package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.Finalizable;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantCollection;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.*;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class DefaultNativeLibraryComponent extends BaseNativeComponent<DefaultNativeLibraryVariant> implements DependencyAwareComponent<NativeLibraryComponentDependencies>, BinaryAwareComponent, Component, SourceAwareComponent<ComponentSources>, Finalizable {
	private final DefaultNativeLibraryComponentDependencies dependencies;
	private final TaskRegistry taskRegistry;
	private final NativeLibraryComponentVariants componentVariants;
	private final BinaryView<Binary> binaries;

	@Inject
	public DefaultNativeLibraryComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, ModelLookup modelLookup) {
		super(identifier, DefaultNativeLibraryVariant.class, objects, tasks, eventPublisher, taskRegistry, taskViewFactory);
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurations), dependencyHandler)));
		this.dependencies = objects.newInstance(DefaultNativeLibraryComponentDependencies.class, dependencyContainer);
		this.taskRegistry = taskRegistry;
		this.componentVariants = new NativeLibraryComponentVariants(objects, this, dependencyHandler, configurations, providers, taskRegistry, eventPublisher, viewFactory, variantRepository, binaryViewFactory, modelLookup);
		this.binaries = binaryViewFactory.create(identifier);
	}

	@Override
	public DefaultNativeLibraryComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return componentVariants.getBuildVariants();
	}

	@Override
	public Provider<DefaultNativeLibraryVariant> getDevelopmentVariant() {
		return componentVariants.getDevelopmentVariant();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaries;
	}

	@Override
	public VariantCollection<DefaultNativeLibraryVariant> getVariantCollection() {
		return componentVariants.getVariantCollection();
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

	@Override
	public void finalizeValue() {
		finalizeExtension(null);
	}
}
