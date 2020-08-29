package dev.nokee.platform.nativebase.internal;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

@AutoFactory
public final class DefaultNativeLibraryComponent extends BaseNativeComponent<DefaultNativeLibraryVariant> implements DependencyAwareComponent<NativeLibraryComponentDependencies>, BinaryAwareComponent, Component {
	private final NativeLibraryComponentDependenciesInternal dependencies;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;
	private final DefaultNativeLibraryDependenciesBuilderFactory dependenciesBuilderFactory;

	@Inject
	public DefaultNativeLibraryComponent(ComponentIdentifier identifier, @Provided Project project, @Provided ObjectFactory objects, @Provided ProviderFactory providers, @Provided TaskContainer tasks, @Provided ProjectLayout layout, @Provided ConfigurationContainer configurations, @Provided DependencyHandler dependencyHandler, @Provided NativeLibraryComponentDependenciesFactory dependenciesFactory, @Provided DefaultNativeLibraryDependenciesBuilderFactory dependenciesBuilderFactory) {
		super(NamingScheme.fromIdentifier(identifier, project.getName()), DefaultNativeLibraryVariant.class, objects, providers, tasks, layout, configurations);
		this.dependencyHandler = dependencyHandler;
		this.dependenciesBuilderFactory = dependenciesBuilderFactory;
		this.dependencies = dependenciesFactory.create(identifier);
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, BaseTargetBuildType.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
	}

	@Override
	public NativeLibraryComponentDependenciesInternal getDependencies() {
		return dependencies;
	}

	public void dependencies(Action<? super NativeLibraryComponentDependencies> action) {
		action.execute(dependencies);
	}

	@Override
	protected VariantComponentDependencies<NativeLibraryComponentDependencies> newDependencies(NamingScheme names, BuildVariantInternal buildVariant) {
		val identifier = new VariantIdentifier(names.getUnambiguousDimensionsAsString(), new ComponentIdentifier(names.getComponentName(), new ProjectIdentifier("")));
		val builder = dependenciesBuilderFactory.create().withIdentifier(identifier).withVariant(buildVariant).withParentDependencies(getDependencies());
		boolean hasSwift = !getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		if (hasSwift) {
			builder.withSwiftModules();
		} else {
			builder.withNativeHeaders();
		}
		return builder.build();
	}

	@Override
	protected DefaultNativeLibraryVariant createVariant(String name, BuildVariantInternal buildVariant, VariantComponentDependencies<?> variantDependencies) {
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultNativeLibraryVariant result = getObjects().newInstance(DefaultNativeLibraryVariant.class, name, names, buildVariant, variantDependencies);
		return result;
	}
}
