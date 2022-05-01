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

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.c.internal.plugins.CHeaderSetRegistrationFactory;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.IsVariant;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.ExtendsFromParentConfigurationAction;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencybuckets.ApiConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.CompileOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.ImplementationConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.RuntimeOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.ModelBackedTaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibraryExtension;
import dev.nokee.platform.nativebase.NativeLibrarySources;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryVariant;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.NativeLibraryComponentModelRegistrationFactory;
import dev.nokee.platform.nativebase.internal.NativeLibrarySourcesAdapter;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeLibraryOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependenciesComponent;
import dev.nokee.platform.nativebase.internal.dependencies.SwiftLibraryOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.actions.ModelAction.configureMatching;
import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.actions.ModelSpec.subtypeOf;
import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.consumable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.resolvable;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;
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
		componentProvider.configure(baseName(convention(project.getName())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(NativeLibraryExtension.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration nativeLibrary(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("native library").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		return new NativeLibraryComponentModelRegistrationFactory(NativeLibraryExtension.class, DefaultNativeLibraryExtension.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(entity.get(IdentifierComponent.class).get(), "public")));
			registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(entity.get(IdentifierComponent.class).get(), "headers")));
		}).create(identifier);
	}

	public static abstract class DefaultNativeLibraryExtension implements NativeLibraryExtension
		, ModelBackedDependencyAwareComponentMixIn<NativeLibraryComponentDependencies, ModelBackedNativeLibraryComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<NativeLibrary>
		, ModelBackedSourceAwareComponentMixIn<NativeLibrarySources, NativeLibrarySourcesAdapter>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
		, ModelBackedTargetLinkageAwareComponentMixIn
		, ModelBackedHasBaseNameMixIn
		, ModelBackedNamedMixIn
		, ModelBackedHasAssembleTaskMixIn
	{
	}

	public static ModelRegistration nativeLibraryVariant(VariantIdentifier identifier, DefaultNativeLibraryComponent component, Project project) {
		return ModelRegistration.builder()
			.withComponent(IsVariant.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(createdUsing(of(DefaultNativeLibraryVariant.class), () -> {
				val taskRegistry = ModelBackedTaskRegistry.newInstance(project);
				val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier));

				val result = project.getObjects().newInstance(DefaultNativeLibraryVariant.class, identifier, project.getObjects(), project.getProviders(), assembleTask);
				result.getDevelopmentBinary().convention(result.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(result.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
				return result;
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ModelPathComponent.class), (entity, id, path) -> {
				if (id.get().equals(identifier)) {
					entity.addComponent(new ModelBackedNativeIncomingDependencies(path.get(), project.getObjects(), project.getProviders(), project.getExtensions().getByType(ModelLookup.class)));
				}
			}))
			.action(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ModelPathComponent.class), (entity, id, path) -> {
				if (id.get().equals(identifier)) {
					val registry = project.getExtensions().getByType(ModelRegistry.class);

					val bucketFactory = project.getExtensions().getByType(DeclarableDependencyBucketRegistrationFactory.class);
					val api = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("api"), identifier)));
					val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("implementation"), identifier)));
					val compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("compileOnly"), identifier)));
					val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("linkOnly"), identifier)));
					val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("runtimeOnly"), identifier)));
					implementation.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));

					entity.addComponent(new ApiConfigurationComponent(ModelNodes.of(api)));
					entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
					entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
					entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
					entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));

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
					NativeOutgoingDependenciesComponent outgoing = null;
					if (hasSwift) {
						outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new SwiftLibraryOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(apiElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(linkElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects())));
					} else {
						outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new NativeLibraryOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(apiElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(linkElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects())));
					}
					val incoming = entity.get(ModelBackedNativeIncomingDependencies.class);
					entity.addComponent(new VariantComponentDependencies<NativeComponentDependencies>(ModelProperties.getProperty(entity, "dependencies").as(NativeComponentDependencies.class)::get, incoming, outgoing.get()));

					registry.instantiate(configureMatching(ownedBy(entity.getId()).and(subtypeOf(of(Configuration.class))), new ExtendsFromParentConfigurationAction()));
				}
			})))
			.build()
			;
	}
}
