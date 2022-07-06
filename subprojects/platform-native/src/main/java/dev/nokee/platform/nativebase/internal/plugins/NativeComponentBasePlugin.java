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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LegacySourceSetTag;
import dev.nokee.language.base.internal.SourcePropertyComponent;
import dev.nokee.language.c.internal.plugins.CSourceSetTag;
import dev.nokee.language.c.internal.plugins.DefaultCHeaderSet;
import dev.nokee.language.c.internal.plugins.LegacyCSourceSet;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetTag;
import dev.nokee.language.cpp.internal.plugins.DefaultCppHeaderSet;
import dev.nokee.language.cpp.internal.plugins.LegacyCppSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.HasConfigurableHeadersPropertyComponent;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.LegacyObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetTag;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.plugins.LegacyObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppSourceSetTag;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.LegacySwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetTag;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.AssembleTaskComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.Variants;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.ExtendsFromParentConfigurationAction;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencybuckets.ApiConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.CompileOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.ImplementationConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkedConfiguration;
import dev.nokee.platform.base.internal.dependencybuckets.RuntimeOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.ModelBackedTaskRegistry;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.internal.AttachAttributesToConfigurationRule;
import dev.nokee.platform.nativebase.internal.BundleBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationVariant;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryVariant;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.NativeApplicationTag;
import dev.nokee.platform.nativebase.internal.NativeLibraryTag;
import dev.nokee.platform.nativebase.internal.NativeVariantTag;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfiguration;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfigurationRegistrationRule;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.StaticLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.TargetBuildTypesPropertyRegistrationRule;
import dev.nokee.platform.nativebase.internal.TargetLinkagesPropertyComponent;
import dev.nokee.platform.nativebase.internal.TargetLinkagesPropertyRegistrationRule;
import dev.nokee.platform.nativebase.internal.TargetMachinesPropertyRegistrationRule;
import dev.nokee.platform.nativebase.internal.archiving.NativeArchiveCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.compiling.NativeCompileCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeApplicationOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeLibraryOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependenciesComponent;
import dev.nokee.platform.nativebase.internal.dependencies.SwiftLibraryOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.linking.LinkLibrariesConfiguration;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.internal.rules.LanguageSourceLayoutConvention;
import dev.nokee.platform.nativebase.internal.rules.LegacyObjectiveCSourceLayoutConvention;
import dev.nokee.platform.nativebase.internal.rules.LegacyObjectiveCppSourceLayoutConvention;
import dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer;
import dev.nokee.platform.nativebase.internal.services.UnbuildableWarningService;
import dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.utils.ActionUtils;
import dev.nokee.utils.Optionals;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.language.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.model.internal.DomainObjectEntities.newEntity;
import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.model.internal.actions.ModelAction.configureMatching;
import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.actions.ModelSpec.subtypeOf;
import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelNodes.withType;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.nativebase.internal.plugins.NativeApplicationPlugin.nativeApplicationVariant;
import static dev.nokee.platform.nativebase.internal.plugins.NativeLibraryPlugin.nativeLibraryVariant;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TransformerUtils.transformEach;

public class NativeComponentBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(NativeRuntimePlugin.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class); // for now, later we will be more smart
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		project.getExtensions().add("__nokee_sharedLibraryFactory", new SharedLibraryBinaryRegistrationFactory(
			project.getObjects()
		));
		project.getExtensions().add("__nokee_staticLibraryFactory", new StaticLibraryBinaryRegistrationFactory(
			project.getObjects()
		));
		project.getExtensions().add("__nokee_executableFactory", new ExecutableBinaryRegistrationFactory(
			project.getObjects()
		));
		project.getExtensions().add("__nokee_bundleFactory", new BundleBinaryRegistrationFactory(
			project.getObjects()
		));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(HasConfigurableHeadersPropertyComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, headers, parent) -> {
			// Configure headers according to convention
			((ConfigurableFileCollection) headers.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
				return ParentUtils.stream(parent).map(it -> it.find(FullyQualifiedNameComponent.class)).filter(Optional::isPresent).map(Optional::get).map(FullyQualifiedNameComponent::get).map(it -> "src/" + it + "/headers").collect(Collectors.toList());
			});
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new RuntimeLibrariesConfigurationRegistrationRule(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class), project.getObjects())));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachAttributesToConfigurationRule<>(RuntimeLibrariesConfiguration.class, project.getExtensions().getByType(ModelRegistry.class), project.getObjects()));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeApplicationTag.class), (entity, identifier, tag) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			if (entity.hasComponent(typeOf(CSourceSetTag.class))) {
				registry.register(newEntity("c", LegacyCSourceSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("headers", DefaultCHeaderSet.class, it -> it.ownedBy(entity)));
			} else if (entity.hasComponent(typeOf(CppSourceSetTag.class))) {
				registry.register(newEntity("cpp", LegacyCppSourceSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("headers", DefaultCppHeaderSet.class, it -> it.ownedBy(entity)));
			} else if (entity.hasComponent(typeOf(ObjectiveCSourceSetTag.class))) {
				registry.register(newEntity("objectiveC", LegacyObjectiveCSourceSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("headers", DefaultCHeaderSet.class, it -> it.ownedBy(entity)));
			} else if (entity.hasComponent(typeOf(ObjectiveCppSourceSetTag.class))) {
				registry.register(newEntity("objectiveCpp", LegacyObjectiveCppSourceSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("headers", DefaultCppHeaderSet.class, it -> it.ownedBy(entity)));
			} else if (entity.hasComponent(typeOf(SwiftSourceSetTag.class))) {
				registry.register(newEntity("swift", LegacySwiftSourceSet.class, it -> it.ownedBy(entity)));
			}

			val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))), project.getExtensions().getByType(ModelLookup.class), project.getObjects());

			val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("implementation", identifier.get())));
			val compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("compileOnly", identifier.get())));
			val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("linkOnly", identifier.get())));
			val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("runtimeOnly", identifier.get())));

			entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
			entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
			entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
			entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeVariantTag.class), ModelComponentReference.of(ParentComponent.class), (entity, identifier, tag, parent) -> {
			if (!parent.get().hasComponent(typeOf(NativeApplicationTag.class))) {
				return;
			}

			val registry = project.getExtensions().getByType(ModelRegistry.class);

			val bucketFactory = project.getExtensions().getByType(DeclarableDependencyBucketRegistrationFactory.class);
			val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("implementation", identifier.get())));
			val compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("compileOnly", identifier.get())));
			val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("linkOnly", identifier.get())));
			val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("runtimeOnly", identifier.get())));
			entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
			entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
			entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
			entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));

			val resolvableFactory = project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class);
			boolean hasSwift = project.getExtensions().getByType(ModelLookup.class).anyMatch(ModelSpecs.of(ModelNodes.withType(of(SwiftSourceSet.class))));
			if (hasSwift) {
				val importModules = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of("importSwiftModules", identifier.get())));
				importModules.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.SWIFT_API))))
					.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
			} else {
				val headerSearchPaths = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of("headerSearchPaths", identifier.get())));
				headerSearchPaths.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))))
					.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
			}
			val linkLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of("linkLibraries", identifier.get())));
			linkLibraries.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), linkOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))))
				.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
				.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
			val runtimeLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of("runtimeLibraries", identifier.get())));
			runtimeLibraries.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
				.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
				.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));

			val consumableFactory = project.getExtensions().getByType(ConsumableDependencyBucketRegistrationFactory.class);
			val runtimeElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of("runtimeElements", identifier.get())));
			runtimeElements.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
				.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects())));
			val outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new NativeApplicationOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects())));
			val incoming = entity.get(ModelBackedNativeIncomingDependencies.class);
			entity.addComponent(new VariantComponentDependencies<NativeComponentDependencies>(ModelProperties.getProperty(entity, "dependencies").as(NativeComponentDependencies.class)::get, incoming, outgoing.get()));

			registry.instantiate(configureMatching(ownedBy(entity.getId()).and(subtypeOf(of(Configuration.class))), new ExtendsFromParentConfigurationAction()));
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeLibraryTag.class), (entity, identifier, tag) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			if (entity.hasComponent(typeOf(CSourceSetTag.class))) {
				registry.register(newEntity("c", LegacyCSourceSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("headers", DefaultCHeaderSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("public", DefaultCHeaderSet.class, it -> it.ownedBy(entity)));
			} else if (entity.hasComponent(typeOf(CppSourceSetTag.class))) {
				registry.register(newEntity("cpp", LegacyCppSourceSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("headers", DefaultCppHeaderSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("public", DefaultCppHeaderSet.class, it -> it.ownedBy(entity)));
			} else if (entity.hasComponent(typeOf(ObjectiveCSourceSetTag.class))) {
				registry.register(newEntity("objectiveC", LegacyObjectiveCSourceSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("headers", DefaultCHeaderSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("public", DefaultCHeaderSet.class, it -> it.ownedBy(entity)));
			} else if (entity.hasComponent(typeOf(ObjectiveCppSourceSetTag.class))) {
				registry.register(newEntity("objectiveCpp", LegacyObjectiveCppSourceSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("headers", DefaultCppHeaderSet.class, it -> it.ownedBy(entity)));
				registry.register(newEntity("public", DefaultCppHeaderSet.class, it -> it.ownedBy(entity)));
			} else if (entity.hasComponent(typeOf(SwiftSourceSetTag.class))) {
				registry.register(newEntity("swift", LegacySwiftSourceSet.class, it -> it.ownedBy(entity)));
			}

			val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))), project.getExtensions().getByType(ModelLookup.class), project.getObjects());

			val api = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("api", identifier.get())));
			val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("implementation", identifier.get())));
			val compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("compileOnly", identifier.get())));
			val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("linkOnly", identifier.get())));
			val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("runtimeOnly", identifier.get())));

			entity.addComponent(new ApiConfigurationComponent(ModelNodes.of(api)));
			entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
			entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
			entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
			entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeVariantTag.class), ModelComponentReference.of(ParentComponent.class), (entity, identifier, tag, parent) -> {
			if (!parent.get().hasComponent(typeOf(NativeLibraryTag.class))) {
				return;
			}

			val registry = project.getExtensions().getByType(ModelRegistry.class);

			val bucketFactory = project.getExtensions().getByType(DeclarableDependencyBucketRegistrationFactory.class);
			val api = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("api", identifier.get())));
			val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("implementation", identifier.get())));
			val compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("compileOnly", identifier.get())));
			val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("linkOnly", identifier.get())));
			val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of("runtimeOnly", identifier.get())));
			implementation.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));

			entity.addComponent(new ApiConfigurationComponent(ModelNodes.of(api)));
			entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
			entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
			entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
			entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));

			val resolvableFactory = project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class);
			boolean hasSwift = project.getExtensions().getByType(ModelLookup.class).anyMatch(ModelSpecs.of(ModelNodes.withType(of(SwiftSourceSet.class))));
			if (hasSwift) {
				val importModules = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of("importSwiftModules", identifier.get())));
				importModules.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.SWIFT_API))))
					.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
			} else {
				val headerSearchPaths = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of("headerSearchPaths", identifier.get())));
				headerSearchPaths.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))))
					.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
			}
			val linkLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of("linkLibraries", identifier.get())));
			linkLibraries.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), linkOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))))
				.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
				.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
			val runtimeLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of("runtimeLibraries", identifier.get())));
			runtimeLibraries.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
				.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
				.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));

			val consumableFactory = project.getExtensions().getByType(ConsumableDependencyBucketRegistrationFactory.class);
			ModelElement apiElements = null;
			if (hasSwift) {
				apiElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of("apiElements", identifier.get())));
				apiElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.SWIFT_API))))
					.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
			} else {
				apiElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of("apiElements", identifier.get())));
				apiElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))))
					.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
			}
			val linkElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of("linkElements", identifier.get())));
			linkElements.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), linkOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))))
				.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects())));
			val runtimeElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of("runtimeElements", identifier.get())));
			runtimeElements.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
				.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects())));
			NativeOutgoingDependenciesComponent outgoing = null;
			if (hasSwift) {
				outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new SwiftLibraryOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(apiElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(linkElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects())));
			} else {
				outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new NativeLibraryOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(apiElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(linkElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects())));
			}
			val incoming = entity.get(ModelBackedNativeIncomingDependencies.class);
			entity.addComponent(new VariantComponentDependencies<NativeComponentDependencies>(ModelProperties.getProperty(entity, "dependencies").as(NativeComponentDependencies.class)::get, incoming, outgoing.get()));

			registry.instantiate(configureMatching(ownedBy(entity.getId()).and(subtypeOf(of(Configuration.class))), new ExtendsFromParentConfigurationAction()));
		})));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new LanguageSourceLayoutConvention());
		project.getExtensions().getByType(ModelConfigurer.class).configure(new LegacyObjectiveCSourceLayoutConvention());
		project.getExtensions().getByType(ModelConfigurer.class).configure(new LegacyObjectiveCppSourceLayoutConvention());

		project.getPluginManager().apply(NativeCompileCapabilityPlugin.class);
		project.getPluginManager().apply(NativeLinkCapabilityPlugin.class);
		project.getPluginManager().apply(NativeArchiveCapabilityPlugin.class);

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new TargetMachinesPropertyRegistrationRule(project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelRegistry.class), project.getObjects().newInstance(ToolChainSelectorInternal.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new TargetBuildTypesPropertyRegistrationRule(project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelRegistry.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new TargetLinkagesPropertyRegistrationRule(project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelRegistry.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeApplicationTag.class), ModelComponentReference.of(TargetLinkagesPropertyComponent.class), (entity, tag, targetLinkages) -> {
			((SetProperty<TargetLinkage>) targetLinkages.get().get(GradlePropertyComponent.class).get()).convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeLibraryTag.class), ModelComponentReference.of(TargetLinkagesPropertyComponent.class), (entity, tag, targetLinkages) -> {
			((SetProperty<TargetLinkage>) targetLinkages.get().get(GradlePropertyComponent.class).get()).convention(Collections.singletonList(TargetLinkages.SHARED));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(LinkLibrariesConfiguration.class), ModelComponentReference.of(ParentComponent.class), (e, linkLibraries, parent) -> {
			project.getExtensions().getByType(ModelRegistry.class).instantiate(configure(linkLibraries.get().getId(), Configuration.class, configureExtendsFrom(firstParentConfigurationOf(parent, ImplementationConfigurationComponent.class), firstParentConfigurationOf(parent, LinkOnlyConfigurationComponent.class))));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(RuntimeLibrariesConfiguration.class), ModelComponentReference.of(ParentComponent.class), (e, runtimeLibraries, parent) -> {
			project.getExtensions().getByType(ModelRegistry.class).instantiate(configure(runtimeLibraries.get().getId(), Configuration.class, configureExtendsFrom(firstParentConfigurationOf(parent, ImplementationConfigurationComponent.class), firstParentConfigurationOf(parent, RuntimeOnlyConfigurationComponent.class))));
		}));

		val unbuildableWarningService = forUseAtConfigurationTime(project.getGradle().getSharedServices().registerIfAbsent("unbuildableWarningService", UnbuildableWarningService.class, ActionUtils.doNothing()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(AssembleTaskComponent.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.ofProjection(HasDevelopmentVariant.class), (entity, assembleTask, identifier, tag) -> {
			// The "component" assemble task was most likely added by the 'lifecycle-base' plugin
			//   then we configure the dependency.
			//   Note that the dependency may already exists for single variant component but it's not a big deal.
			@SuppressWarnings("unchecked")
			final Provider<HasDevelopmentVariant<?>> component = project.getProviders().provider(() -> ModelNodeUtils.get(entity, HasDevelopmentVariant.class));
			Provider<? extends Variant> developmentVariant = component.flatMap(HasDevelopmentVariant::getDevelopmentVariant);

			val registry = project.getExtensions().getByType(ModelRegistry.class);
			registry.instantiate(configure(assembleTask.get().getId(), Task.class, configureDependsOn(developmentVariant.flatMap(ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY).map(Arrays::asList)
				.orElse(unbuildableWarningService.map(it -> {
					it.warn((ComponentIdentifier) identifier.get());
					return Collections.emptyList();
				})))));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(LegacySourceSetTag.class), ModelComponentReference.of(SourcePropertyComponent.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(ElementNameComponent.class), (entity, tag, source, parent, elementName) -> {
			val builder = ImmutableList.<String>builder();
			val parentPath = ParentUtils.stream(parent).flatMap(it -> Optionals.stream(it.find(ElementNameComponent.class).map(ElementNameComponent::get))).map(Objects::toString).collect(Collectors.joining("/"));
			builder.add("src/" + parentPath + "/" + elementName.get());
			if (ModelNodeUtils.canBeViewedAs(entity, of(ObjectiveCSourceSet.class))) {
				builder.add("src/" + parentPath + "/objc");
			} else if (ModelNodeUtils.canBeViewedAs(entity, of(ObjectiveCppSourceSet.class))) {
				builder.add("src/" + parentPath + "/objcpp");
			}
			((ConfigurableFileCollection) source.get().get(GradlePropertyComponent.class).get()).from(builder.build());
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ModelPathComponent.class), ModelTags.referenceOf(NativeVariantTag.class), (entity, id, path, tag) -> {
			entity.addComponent(new ModelBackedNativeIncomingDependencies(path.get(), project.getObjects(), project.getProviders(), project.getExtensions().getByType(ModelLookup.class)));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), ModelTags.referenceOf(NativeApplicationTag.class), (entity, path, ignored, tag) -> {
			new CalculateNativeApplicationVariantAction(project).execute(entity, path);
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), ModelTags.referenceOf(NativeLibraryTag.class), (entity, path, ignored, tag) -> {
			new CalculateNativeLibraryVariantAction(project).execute(entity, path);
		}));
	}

	private static <T extends ModelComponent & LinkedConfiguration> Callable<Iterable<Configuration>> firstParentConfigurationOf(ParentComponent parent, Class<T> type) {
		return () -> {
			return ParentUtils.stream(parent)
				.flatMap(it -> {
					if (it.has(type)) {
						return Stream.of(it.get(type).get());
					} else {
						return Stream.empty();
					}
				})
				.map(it -> {
					ModelStates.realize(it);
					return ImmutableList.of(ModelNodeUtils.get(it, Configuration.class));
				})
				.findFirst()
				.orElse(ImmutableList.of());
		};
	}

	public static Factory<DefaultNativeApplicationComponent> nativeApplicationProjection(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of(project));
		return () -> new DefaultNativeApplicationComponent(identifier, project.getObjects(), ModelBackedTaskRegistry.newInstance(project), project.getExtensions().getByType(ModelRegistry.class));
	}

	public static Factory<DefaultNativeLibraryComponent> nativeLibraryProjection(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of(project));
		return () -> new DefaultNativeLibraryComponent(identifier, project.getObjects(), ModelBackedTaskRegistry.newInstance(project), project.getExtensions().getByType(ModelRegistry.class));
	}

	public static <T extends Component, PROJECTION> Action<T> configureUsingProjection(Class<PROJECTION> type, BiConsumer<? super T, ? super PROJECTION> action) {
		return t -> action.accept(t, ModelNodeUtils.get(ModelNodes.of(t), type));
	}

	public static Action<Project> finalizeModelNodeOf(Object target) {
		return project -> {
			ModelNodeUtils.finalizeProjections(ModelNodes.of(target));
			ModelStates.finalize(ModelNodes.of(target));
		};
	}

	private static class CalculateNativeApplicationVariantAction extends ModelActionWithInputs.ModelAction1<ModelPathComponent> {
		private final Project project;

		public CalculateNativeApplicationVariantAction(Project project) {
			this.project = project;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void execute(ModelNode entity, ModelPathComponent path) {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val component = ModelNodeUtils.get(entity, ModelType.of(DefaultNativeApplicationComponent.class));

			val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
			component.getBuildVariants().get().forEach(new Consumer<BuildVariant>() {
				@Override
				public void accept(BuildVariant buildVariant) {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					val variant = registry.register(nativeApplicationVariant(variantIdentifier, component, project));

					variants.put(buildVariant, ModelNodes.of(variant));
					onEachVariantDependencies(variant.as(NativeApplication.class), ModelNodes.of(variant).getComponent(componentOf(VariantComponentDependencies.class)));
				}

				private void onEachVariantDependencies(DomainObjectProvider<NativeApplication> variant, VariantComponentDependencies<?> dependencies) {
					dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
				}
			});
			entity.addComponent(new Variants(variants.build()));

			component.finalizeExtension(null);
			component.getDevelopmentVariant().convention((Provider<? extends DefaultNativeApplicationVariant>) project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) component.getVariants().map(VariantInternal.class::cast).get())));
		}
	}

	private static class CalculateNativeLibraryVariantAction extends ModelActionWithInputs.ModelAction1<ModelPathComponent> {
		private final Project project;

		private CalculateNativeLibraryVariantAction(Project project) {
			this.project = project;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void execute(ModelNode entity, ModelPathComponent path) {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val component = ModelNodeUtils.get(entity, ModelType.of(DefaultNativeLibraryComponent.class));

			val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
			component.getBuildVariants().get().forEach(new Consumer<BuildVariant>() {
				private final ModelLookup modelLookup = project.getExtensions().getByType(ModelLookup.class);

				@Override
				public void accept(BuildVariant buildVariant) {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					val variant = registry.register(nativeLibraryVariant(variantIdentifier, component, project));

					variants.put(buildVariant, ModelNodes.of(variant));
					onEachVariantDependencies(variant.as(NativeLibrary.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));
				}

				private void onEachVariantDependencies(DomainObjectProvider<NativeLibrary> variant, VariantComponentDependencies<?> dependencies) {
					if (NativeLibrary.class.isAssignableFrom(DefaultNativeLibraryVariant.class)) {
						if (modelLookup.anyMatch(ModelSpecs.of(withType(ModelType.of(SwiftSourceSet.class))))) {
							dependencies.getOutgoing().getExportedSwiftModule().convention(variant.flatMap(it -> {
								List<? extends Provider<RegularFile>> result = it.getBinaries().withType(NativeBinary.class).flatMap(binary -> {
									List<? extends Provider<RegularFile>> modules = binary.getCompileTasks().withType(SwiftCompileTask.class).map(task -> task.getModuleFile()).get();
									return modules;
								}).get();
								return one(result);
							}));
						}
						dependencies.getOutgoing().getExportedHeaders().from(sourceViewOf(component).filter(it -> (it instanceof NativeHeaderSet) && it.getName().equals("public")).map(transformEach(LanguageSourceSet::getSourceDirectories)));
					}
					dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
				}

				private <T> T one(Iterable<T> c) {
					Iterator<T> iterator = c.iterator();
					Preconditions.checkArgument(iterator.hasNext(), "collection needs to have one element, was empty");
					T result = iterator.next();
					Preconditions.checkArgument(!iterator.hasNext(), "collection needs to only have one element, more than one element found");
					return result;
				}
			});
			entity.addComponent(new Variants(variants.build()));

			component.finalizeExtension(null);
			component.getDevelopmentVariant().convention((Provider<? extends DefaultNativeLibraryVariant>) project.provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) component.getVariants().map(VariantInternal.class::cast).get())));
		}
	}
}
