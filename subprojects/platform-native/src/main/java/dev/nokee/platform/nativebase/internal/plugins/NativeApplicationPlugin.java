/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.nativebase.internal.plugins;

import com.google.common.collect.Iterables;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryConfigurer;
import dev.nokee.platform.base.internal.binaries.BinaryRepository;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.NativeApplicationExtension;
import dev.nokee.platform.nativebase.NativeApplicationSources;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Optional;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.core.ModelProjections.*;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public class NativeApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public NativeApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(nativeApplication("main", project)).as(NativeApplicationExtension.class);
		componentProvider.configure(configureUsingProjection(DefaultNativeApplicationComponent.class, baseNameConvention(project.getName()).andThen(configureBuildVariants())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(NativeApplicationExtension.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration nativeApplication(String name, Project project) {
		return new NativeApplicationComponentModelRegistrationFactory(NativeApplicationExtension.class, DefaultNativeApplicationExtension.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			// TODO: Should be created using CHeaderSetSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("headers"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(CHeaderSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(path.child("sources"), NativeApplicationSources.class));
		}).create(ComponentIdentifier.of(ComponentName.of(name), NativeApplicationExtension.class, ProjectIdentifier.of(project)));
	}


	public static NodeRegistration nativeApplicationVariant(VariantIdentifier<DefaultNativeApplicationVariant> identifier, DefaultNativeApplicationComponent component, Project project) {
		val variantDependencies = newDependencies((BuildVariantInternal) identifier.getBuildVariant(), identifier, component, project.getConfigurations(), project.getDependencies(), project.getObjects(), project.getExtensions().getByType(ModelLookup.class));
		return NodeRegistration.unmanaged(identifier.getUnambiguousName(), of(NativeApplication.class), () -> {
				val taskRegistry = project.getExtensions().getByType(TaskRegistry.class);
				val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier));

				val result = project.getObjects().newInstance(DefaultNativeApplicationVariant.class, identifier, project.getObjects(), project.getProviders(), assembleTask, project.getExtensions().getByType(BinaryViewFactory.class));
				result.getDevelopmentBinary().convention(result.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(result.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
				return result;
			})
			.withComponent(IsVariant.tag())
			.withComponent(identifier)
			.withComponent(variantDependencies)
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("executable"), ExecutableBinaryInternal.class, identifier); // TODO: Use input to get variant identifier
				val binaryEntity = registry.register(ModelRegistration.builder()
					.withComponent(path.child("executable"))
					.withComponent(IsBinary.tag())
					.withComponent(binaryIdentifier)
					.withComponent(createdUsing(of(ExecutableBinaryInternal.class), () -> {
						ModelStates.realize(entity);
						return project.getExtensions().getByType(BinaryRepository.class).get(binaryIdentifier);
					}))
					.build());
				project.getExtensions().getByType(BinaryConfigurer.class)
					.configure(binaryIdentifier, binary -> ModelStates.realize(ModelNodes.of(binaryEntity)));

				registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(path.child("binaries")));

				val dependencies = variantDependencies.getDependencies();
				registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(path.child("dependencies"), dependencies));

				registry.register(ModelRegistration.builder()
					.withComponent(path.child("implementation"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getImplementation().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getImplementation()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getImplementation().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("compileOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getCompileOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getCompileOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getCompileOnly().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("linkOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getLinkOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getLinkOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getLinkOnly().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getRuntimeOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getRuntimeOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getRuntimeOnly().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeElements"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> ((NativeApplicationOutgoingDependencies) variantDependencies.getOutgoing()).getRuntimeElements()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(((NativeApplicationOutgoingDependencies) variantDependencies.getOutgoing()).getRuntimeElements().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("linkLibraries"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> ((DefaultNativeIncomingDependencies) variantDependencies.getIncoming()).getLinkLibrariesBucket().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> ((DefaultNativeIncomingDependencies) variantDependencies.getIncoming()).getLinkLibrariesBucket()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(((DefaultNativeIncomingDependencies) variantDependencies.getIncoming()).getLinkLibrariesBucket().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeLibraries"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> ((DefaultNativeIncomingDependencies) variantDependencies.getIncoming()).getRuntimeLibrariesBucket().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> ((DefaultNativeIncomingDependencies) variantDependencies.getIncoming()).getRuntimeLibrariesBucket()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(((DefaultNativeIncomingDependencies) variantDependencies.getIncoming()).getRuntimeLibrariesBucket().getAsConfiguration().getName())))
					.build());
				// TODO: Missing incoming API configuration

				whenElementKnown(entity, ModelActionWithInputs.of(ModelComponentReference.ofAny(projectionOf(Configuration.class)), ModelComponentReference.of(ModelPath.class), (e, ignored, p) -> {
					((NamedDomainObjectProvider<Configuration>) ModelNodeUtils.get(e, NamedDomainObjectProvider.class)).configure(configuration -> {
						val parentConfigurationResult = project.getExtensions().getByType(ModelLookup.class).query(ModelSpecs.of(ModelNodes.withPath(path.getParent().get().child(p.getName()))));
						Optional.ofNullable(Iterables.getOnlyElement(parentConfigurationResult.get(), null)).ifPresent(parentConfigurationEntity -> {
							val parentConfiguration = ModelNodeUtils.get(parentConfigurationEntity, Configuration.class);
							if (!parentConfiguration.getName().equals(configuration.getName())) {
								configuration.extendsFrom(parentConfiguration);
							}
						});
					});
				}));
			})))
			;
	}

	public static abstract class DefaultNativeApplicationExtension implements NativeApplicationExtension
		, ModelBackedDependencyAwareComponentMixIn<NativeApplicationComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<NativeApplication>
		, ModelBackedSourceAwareComponentMixIn<NativeApplicationSources>
		, ModelBackedBinaryAwareComponentMixIn
	{
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}

	private static VariantComponentDependencies<DefaultNativeApplicationComponentDependencies> newDependencies(BuildVariantInternal buildVariant, VariantIdentifier<DefaultNativeApplicationVariant> variantIdentifier, DefaultNativeApplicationComponent component, ConfigurationContainer configurationContainer, DependencyHandler dependencyHandler, ObjectFactory objectFactory, ModelLookup modelLookup) {
		val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
		val variantDependencies = objectFactory.newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);

		boolean hasSwift = modelLookup.anyMatch(ModelSpecs.of(withType(of(SwiftSourceSet.class))));
		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant).withOwnerIdentifier(variantIdentifier).withBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		val incoming = incomingDependenciesBuilder.buildUsing(objectFactory);
		NativeOutgoingDependencies outgoing = objectFactory.newInstance(NativeApplicationOutgoingDependencies.class, variantIdentifier, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}
}
