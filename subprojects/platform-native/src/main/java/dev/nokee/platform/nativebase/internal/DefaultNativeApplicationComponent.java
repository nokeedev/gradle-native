package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactories;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.*;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.utils.Cast;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
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
	public DefaultNativeApplicationComponent(ComponentIdentifier<?> identifier, NamingScheme names, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler) {
		super(identifier, names, DefaultNativeApplicationVariant.class, objects, providers, tasks, layout, configurations);
		this.componentVariants = new NativeApplicationComponentVariants(objects, this, dependencyHandler, configurations);
		this.binaries = Cast.uncheckedCastBecauseOfTypeErasure(objects.newInstance(VariantAwareBinaryView.class, new DefaultMappingView<>(getVariantCollection().getAsView(DefaultNativeApplicationVariant.class), Variant::getBinaries)));
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(configurations), names::getConfigurationName), new DefaultDependencyFactory(dependencyHandler))));
		this.dependencies = objects.newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, BaseTargetBuildType.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
		this.taskRegistry = new TaskRegistryImpl(tasks);
	}

	@Override
	public DefaultNativeApplicationComponentDependencies getDependencies() {
		return dependencies;
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

	public static DomainObjectFactory<DefaultNativeApplicationComponent> newFactory(ObjectFactory objects, NamingSchemeFactory namingSchemeFactory) {
		return identifier -> {
			NamingScheme names = namingSchemeFactory.forMainComponent().withComponentDisplayName(((ComponentIdentifier<?>)identifier).getDisplayName());
			return objects.newInstance(DefaultNativeApplicationComponent.class, identifier, names);
		};
	}

	public void finalizeExtension(Project project) {
		getVariantCollection().whenElementKnown(new CreateNativeBinaryLifecycleTaskRule(taskRegistry));
		getVariantCollection().whenElementKnown(this::createBinaries);
		getVariantCollection().whenElementKnown(new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		getVariantCollection().whenElementKnown(new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);

		componentVariants.calculateVariants();

		getVariantCollection().disallowChanges();
	}
}
