package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactories;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.*;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class DefaultNativeLibraryComponent extends BaseNativeComponent<DefaultNativeLibraryVariant> implements DependencyAwareComponent<NativeLibraryComponentDependencies>, BinaryAwareComponent, Component {
	private final DefaultNativeLibraryComponentDependencies dependencies;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;
	private final TaskRegistry taskRegistry;

	@Inject
	public DefaultNativeLibraryComponent(ComponentIdentifier<?> identifier, NamingScheme names, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler) {
		super(identifier, names, DefaultNativeLibraryVariant.class, objects, providers, tasks, layout, configurations);
		this.dependencyHandler = dependencyHandler;
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(getConfigurations()), names::getConfigurationName), new DefaultDependencyFactory(getDependencyHandler()))));
		this.dependencies = objects.newInstance(DefaultNativeLibraryComponentDependencies.class, dependencyContainer);
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, BaseTargetBuildType.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
		this.taskRegistry = new TaskRegistryImpl(tasks);
	}

	@Override
	public DefaultNativeLibraryComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	protected VariantComponentDependencies<NativeLibraryComponentDependencies> newDependencies(NamingScheme names, BuildVariantInternal buildVariant) {
		var variantDependencies = getDependencies();
		if (getBuildVariants().get().size() > 1) {
			val dependencyContainer = getObjects().newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(getConfigurations()), names::getConfigurationName), new DefaultDependencyFactory(getDependencyHandler())));
			variantDependencies = getObjects().newInstance(DefaultNativeLibraryComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		boolean hasSwift = !getSourceCollection().withType(SwiftSourceSet.class).isEmpty();

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant);
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}
		val incoming = incomingDependenciesBuilder.buildUsing(getObjects());

		NativeOutgoingDependencies outgoing = null;
		if (hasSwift) {
			outgoing = getObjects().newInstance(SwiftLibraryOutgoingDependencies.class, names, buildVariant, variantDependencies);
		} else {
			outgoing = getObjects().newInstance(NativeLibraryOutgoingDependencies.class, names, buildVariant, variantDependencies);
		}

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	@Override
	protected DefaultNativeLibraryVariant createVariant(VariantIdentifier<?> identifier, VariantComponentDependencies<?> variantDependencies) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultNativeLibraryVariant result = getObjects().newInstance(DefaultNativeLibraryVariant.class, identifier, names, variantDependencies);
		return result;
	}

	public static DomainObjectFactory<DefaultNativeLibraryComponent> newFactory(ObjectFactory objects, NamingSchemeFactory namingSchemeFactory) {
		return identifier -> {
			NamingScheme names = namingSchemeFactory.forMainComponent().withComponentDisplayName(((ComponentIdentifier<?>)identifier).getDisplayName());
			return objects.newInstance(DefaultNativeLibraryComponent.class, identifier, names);
		};
	}

	public void finalizeExtension(Project project) {
		getVariantCollection().whenElementKnown(new CreateNativeBinaryLifecycleTaskRule(taskRegistry));
		getVariantCollection().whenElementKnown(this::createBinaries);
		getVariantCollection().whenElementKnown(new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		getVariantCollection().whenElementKnown(new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);

		calculateVariants();

		getVariantCollection().disallowChanges();
	}
}
