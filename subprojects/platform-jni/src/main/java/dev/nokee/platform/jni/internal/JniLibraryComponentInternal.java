package dev.nokee.platform.jni.internal;

import com.google.common.collect.Iterables;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.Finalizable;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.base.internal.variants.VariantViewInternal;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrarySources;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.DefaultTargetBuildTypeFactory;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.ConfigureUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

import static dev.nokee.runtime.core.Coordinates.coordinateTypeOf;
import static dev.nokee.runtime.core.Coordinates.toCoordinateSet;
import static dev.nokee.utils.TransformerUtils.collect;
import static dev.nokee.utils.TransformerUtils.toSetTransformer;

public class JniLibraryComponentInternal extends BaseComponent<JniLibraryInternal> implements DependencyAwareComponent<JavaNativeInterfaceLibraryComponentDependencies>, BinaryAwareComponent, Component, SourceAwareComponent<JavaNativeInterfaceLibrarySources>, Finalizable {
	private final DefaultJavaNativeInterfaceLibraryComponentDependencies dependencies;
	@Getter private final GroupId groupId;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	private final BinaryView<Binary> binaries;
	private final JavaNativeInterfaceComponentVariants componentVariants;

	@Inject
	public JniLibraryComponentInternal(ComponentIdentifier<?> identifier, GroupId groupId, ObjectFactory objects, ConfigurationContainer configurations, DependencyHandler dependencyHandler, ProviderFactory providers, TaskContainer tasks, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, ModelLookup modelLookup) {
		super(identifier, objects);
		this.configurations = configurations;
		this.dependencyHandler = dependencyHandler;
		this.providers = providers;
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurations), getDependencyHandler())));
		this.dependencies = objects.newInstance(DefaultJavaNativeInterfaceLibraryComponentDependencies.class, dependencyContainer);
		this.groupId = groupId;
		this.targetMachines = ConfigureUtils.configureDisplayName(objects.setProperty(TargetMachine.class), "targetMachines");
		this.componentVariants = new JavaNativeInterfaceComponentVariants(objects, this, configurations, dependencyHandler, providers, taskRegistry, eventPublisher, viewFactory, variantRepository, binaryViewFactory, taskViewFactory, modelLookup);
		this.binaries = binaryViewFactory.create(identifier);

		// Order here doesn't align with general native
		getDimensions().add(getTargetMachines()
			.map(assertNonEmpty("target machine", identifier.getName().toString()))
			.map(toSetTransformer(coordinateTypeOf(TargetMachine.class)).andThen(collect(toCoordinateSet()))));
		// TODO: Missing build type dimension
		getDimensions().add(CoordinateSet.of(DefaultBinaryLinkage.SHARED));

		getBuildVariants().convention(getFinalSpace().map(DefaultBuildVariant::fromSpace));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		getVariantCollection().whenElementKnown(new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
	}

	private static <I extends Iterable<T>, T> Transformer<I, I> assertNonEmpty(String propertyName, String componentName) {
		return values -> {
			if (Iterables.isEmpty(values)) {
				throw new IllegalArgumentException(String.format("A %s needs to be specified for component '%s'.", propertyName, componentName));
			}
			return values;
		};
	}

	@Override
	public DefaultJavaNativeInterfaceLibraryComponentDependencies getDependencies() {
		return dependencies;
	}

	//region Variant-awareness
	public VariantViewInternal<JniLibraryInternal> getVariants() {
		return getVariantCollection().getAsView(JniLibraryInternal.class);
	}
	//endregion

	public Configuration getJvmImplementationDependencies() {
		return dependencies.getJvmImplementation().getAsConfiguration();
	}

	@Override
	public Provider<JniLibraryInternal> getDevelopmentVariant() {
		return componentVariants.getDevelopmentVariant();
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

	@Override
	public void finalizeValue() {
		componentVariants.calculateVariants();
	}
}
