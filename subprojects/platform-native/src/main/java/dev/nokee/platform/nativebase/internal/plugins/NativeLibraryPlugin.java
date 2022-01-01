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
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.c.internal.plugins.CHeaderSetRegistrationFactory;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.internal.FullyQualifiedNameComponent;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryConfigurer;
import dev.nokee.platform.base.internal.binaries.BinaryRepository;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.*;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Optional;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.*;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public class NativeLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public NativeLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(nativeLibrary("main", project)).as(NativeLibraryExtension.class);
		componentProvider.configure(configureUsingProjection(DefaultNativeLibraryComponent.class, baseNameConvention(project.getName())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(NativeLibraryExtension.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration nativeLibrary(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("native library").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		return new NativeLibraryComponentModelRegistrationFactory(NativeLibraryExtension.class, DefaultNativeLibraryExtension.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(entity.getComponent(ComponentIdentifier.class), "public")));
			registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(entity.getComponent(ComponentIdentifier.class), "headers")));

			registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "sources"), NativeLibrarySources.class, NativeLibrarySourcesAdapter::new));
		}).create(identifier);
	}

	public static abstract class DefaultNativeLibraryExtension implements NativeLibraryExtension
		, ModelBackedDependencyAwareComponentMixIn<NativeLibraryComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<NativeLibrary>
		, ModelBackedSourceAwareComponentMixIn<NativeLibrarySources>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
		, ModelBackedTargetLinkageAwareComponentMixIn
		, ModelBackedHasBaseNameLegacyMixIn
		, ModelBackedNamedMixIn
	{
	}

	public static NodeRegistration nativeLibraryVariant(VariantIdentifier<DefaultNativeLibraryVariant> identifier, DefaultNativeLibraryComponent component, Project project) {
		return NodeRegistration.unmanaged(identifier.getUnambiguousName(), of(NativeLibrary.class), () -> {
			val taskRegistry = project.getExtensions().getByType(TaskRegistry.class);
			val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier));

			val result = project.getObjects().newInstance(DefaultNativeLibraryVariant.class, identifier, project.getObjects(), project.getProviders(), assembleTask, project.getExtensions().getByType(BinaryViewFactory.class));
			result.getDevelopmentBinary().convention(result.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(result.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
			return result;
		})
			.withComponent(IsVariant.tag())
			.withComponent(identifier)
			.withComponent(new FullyQualifiedNameComponent(VariantNamer.INSTANCE.determineName(identifier)))
			.action(self().apply(once(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				entity.addComponent(new ModelBackedNativeIncomingDependencies(path, project.getObjects(), project.getProviders(), project.getExtensions().getByType(ModelLookup.class)));
			}))))
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);

				registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "binaries")));

				registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "tasks")));

				if (identifier.getBuildVariant().hasAxisOf(NativeRuntimeBasePlugin.TARGET_LINKAGE_FACTORY.getShared())) {
					val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("sharedLibrary"), SharedLibraryBinaryInternal.class, identifier); // TODO: Use input to get variant identifier
					val binaryEntity = registry.register(ModelRegistration.builder()
						.withComponent(path.child("sharedLibrary"))
						.withComponent(IsBinary.tag())
						.withComponent(binaryIdentifier)
						.withComponent(new FullyQualifiedNameComponent(BinaryNamer.INSTANCE.determineName(binaryIdentifier)))
						.withComponent(createdUsing(of(SharedLibraryBinaryInternal.class), () -> {
							ModelStates.realize(entity);
							return project.getExtensions().getByType(BinaryRepository.class).get(binaryIdentifier);
						}))
						.build());
					project.getExtensions().getByType(BinaryConfigurer.class)
						.configure(binaryIdentifier, binary -> ModelStates.realize(ModelNodes.of(binaryEntity)));
				} else {
					val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("staticLibrary"), StaticLibraryBinaryInternal.class, identifier); // TODO: Use input to get variant identifier
					val binaryEntity = registry.register(ModelRegistration.builder()
						.withComponent(path.child("staticLibrary"))
						.withComponent(IsBinary.tag())
						.withComponent(binaryIdentifier)
						.withComponent(new FullyQualifiedNameComponent(BinaryNamer.INSTANCE.determineName(binaryIdentifier)))
						.withComponent(createdUsing(of(StaticLibraryBinaryInternal.class), () -> {
							ModelStates.realize(entity);
							return project.getExtensions().getByType(BinaryRepository.class).get(binaryIdentifier);
						}))
						.build());
					project.getExtensions().getByType(BinaryConfigurer.class)
						.configure(binaryIdentifier, binary -> ModelStates.realize(ModelNodes.of(binaryEntity)));
				}

				val dependencies = registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "dependencies"), NativeLibraryComponentDependencies.class, ModelBackedNativeLibraryComponentDependencies::new));

				val bucketFactory = project.getExtensions().getByType(DeclarableDependencyBucketRegistrationFactory.class);
				val api = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("api"), identifier)));
				val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("implementation"), identifier)));
				val compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("compileOnly"), identifier)));
				val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("linkOnly"), identifier)));
				val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("runtimeOnly"), identifier)));
				implementation.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));

				val resolvableFactory = project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class);
				boolean hasSwift = project.getExtensions().getByType(ModelLookup.class).anyMatch(ModelSpecs.of(ModelNodes.withType(of(SwiftSourceSet.class))));
				if (hasSwift) {
					val importModules = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("importSwiftModules"), identifier)));
					importModules.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class))
						.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.SWIFT_API))))
						.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
						.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
				} else {
					val headerSearchPaths = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("headerSearchPaths"), identifier)));
					headerSearchPaths.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class))
						.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))))
						.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
						.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
				}
				val linkLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("linkLibraries"), identifier)));
				linkLibraries.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), linkOnly.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))))
					.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
				val runtimeLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("runtimeLibraries"), identifier)));
				runtimeLibraries.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
					.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));

				val consumableFactory = project.getExtensions().getByType(ConsumableDependencyBucketRegistrationFactory.class);
				ModelElement apiElements = null;
				if (hasSwift) {
					apiElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("apiElements"), identifier)));
					apiElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class))
						.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.SWIFT_API))))
						.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
						.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
				} else {
					apiElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("apiElements"), identifier)));
					apiElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class))
						.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))))
						.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
						.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
				}
				val linkElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("linkElements"), identifier)));
				linkElements.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), linkOnly.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))))
					.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects())));
				val runtimeElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("runtimeElements"), identifier)));
				runtimeElements.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
					.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects())));
				NativeOutgoingDependencies outgoing = null;
				if (hasSwift) {
					outgoing = entity.addComponent(new SwiftLibraryOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(apiElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(linkElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects()));
				} else {
					outgoing = entity.addComponent(new NativeLibraryOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(apiElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(linkElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects()));
				}
				val incoming = entity.getComponent(NativeIncomingDependencies.class);
				entity.addComponent(new VariantComponentDependencies<NativeComponentDependencies>(dependencies.as(NativeComponentDependencies.class)::get, incoming, outgoing));

				whenElementKnown(entity, ModelActionWithInputs.of(ModelComponentReference.ofProjection(Configuration.class).asConfigurableProvider(), ModelComponentReference.of(ModelPath.class), (e, configurationProvider, p) -> {
					configurationProvider.configure(configuration -> {
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

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}
}
