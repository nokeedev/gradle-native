package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactories;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyFactory;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class DefaultNativeApplicationComponent extends BaseNativeComponent<DefaultNativeApplicationVariant> implements DependencyAwareComponent<NativeApplicationComponentDependencies>, BinaryAwareComponent, Component {
	private final DefaultNativeApplicationComponentDependencies dependencies;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;

	@Inject
	public DefaultNativeApplicationComponent(NamingScheme names, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler) {
		super(names, DefaultNativeApplicationVariant.class, objects, providers, tasks, layout, configurations);
		this.dependencyHandler = dependencyHandler;
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(getConfigurations()), names::getConfigurationName), new DefaultDependencyFactory(getDependencyHandler()))));
		this.dependencies = objects.newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, BaseTargetBuildType.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
	}

	@Override
	public DefaultNativeApplicationComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	protected VariantComponentDependencies<NativeApplicationComponentDependencies> newDependencies(NamingScheme names, BuildVariantInternal buildVariant) {
		var variantDependencies = getDependencies();
		if (getBuildVariants().get().size() > 1) {
			val dependencyContainer = getObjects().newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(getConfigurations()), names::getConfigurationName), new DefaultDependencyFactory(getDependencyHandler())));
			variantDependencies = getObjects().newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);
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
		NativeOutgoingDependencies outgoing = getObjects().newInstance(NativeApplicationOutgoingDependencies.class, names, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	@Override
	protected DefaultNativeApplicationVariant createVariant(String name, BuildVariantInternal buildVariant, VariantComponentDependencies<?> variantDependencies) {
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultNativeApplicationVariant result = getObjects().newInstance(DefaultNativeApplicationVariant.class, name, names, buildVariant, variantDependencies);
		return result;
	}

	public static DomainObjectFactory<DefaultNativeApplicationComponent> newMain(ObjectFactory objects, NamingSchemeFactory namingSchemeFactory) {
		return new DomainObjectFactory<DefaultNativeApplicationComponent>() {
			@Override
			public DefaultNativeApplicationComponent create() {
				NamingScheme names = namingSchemeFactory.forMainComponent().withComponentDisplayName("main native component");
				return objects.newInstance(DefaultNativeApplicationComponent.class, names);
			}

			@Override
			public Class<DefaultNativeApplicationComponent> getType() {
				return DefaultNativeApplicationComponent.class;
			}

			@Override
			public Class<? extends DefaultNativeApplicationComponent> getImplementationType() {
				return DefaultNativeApplicationComponent.class;
			}

			@Override
			public DomainObjectIdentity getIdentity() {
				return DomainObjectIdentity.named("main");
			}
		};
	}
}
