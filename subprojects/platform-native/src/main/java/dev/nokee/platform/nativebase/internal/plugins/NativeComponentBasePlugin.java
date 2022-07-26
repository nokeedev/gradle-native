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
import dev.nokee.language.base.internal.HasConfigurableSource;
import dev.nokee.language.base.internal.LegacySourceSetTag;
import dev.nokee.language.base.internal.SourcePropertyComponent;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.plugins.CSourceSetSpec;
import dev.nokee.language.c.internal.plugins.CSourceSetTag;
import dev.nokee.language.c.internal.plugins.DefaultCHeaderSet;
import dev.nokee.language.c.internal.plugins.LegacyCSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetSpec;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetTag;
import dev.nokee.language.cpp.internal.plugins.DefaultCppHeaderSet;
import dev.nokee.language.cpp.internal.plugins.LegacyCppSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.HasConfigurableHeaders;
import dev.nokee.language.nativebase.internal.HasConfigurableHeadersPropertyComponent;
import dev.nokee.language.nativebase.internal.HeaderSearchPathsConfigurationComponent;
import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.LegacyObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetSpec;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetTag;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.plugins.LegacyObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppSourceSetSpec;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppSourceSetTag;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.ImportModulesConfigurationComponent;
import dev.nokee.language.swift.internal.plugins.LegacySwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetSpec;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetTag;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.DomainObjectProvider;
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
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.AssembleTaskComponent;
import dev.nokee.platform.base.internal.BaseNameComponent;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.BinaryIdentity;
import dev.nokee.platform.base.internal.BuildVariantComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.Variants;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DependencyDefaultActionComponent;
import dev.nokee.platform.base.internal.dependencies.ExtendsFromParentConfigurationAction;
import dev.nokee.platform.base.internal.dependencybuckets.ApiConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.CompileOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.ImplementationConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkedConfiguration;
import dev.nokee.platform.base.internal.dependencybuckets.RuntimeOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.developmentvariant.DevelopmentVariantPropertyComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.AttachAttributesToConfigurationRule;
import dev.nokee.platform.nativebase.internal.BundleBinaryInternal;
import dev.nokee.platform.nativebase.internal.BundleBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationVariant;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryVariant;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.NativeApplicationTag;
import dev.nokee.platform.nativebase.internal.NativeExecutableBinaryComponent;
import dev.nokee.platform.nativebase.internal.NativeLibraryTag;
import dev.nokee.platform.nativebase.internal.NativeSharedLibraryBinaryComponent;
import dev.nokee.platform.nativebase.internal.NativeStaticLibraryBinaryComponent;
import dev.nokee.platform.nativebase.internal.NativeVariantTag;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfiguration;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfigurationRegistrationRule;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.StaticLibraryBinaryInternal;
import dev.nokee.platform.nativebase.internal.StaticLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.TargetBuildTypesPropertyRegistrationRule;
import dev.nokee.platform.nativebase.internal.TargetLinkagesPropertyComponent;
import dev.nokee.platform.nativebase.internal.TargetLinkagesPropertyRegistrationRule;
import dev.nokee.platform.nativebase.internal.TargetMachinesPropertyRegistrationRule;
import dev.nokee.platform.nativebase.internal.archiving.NativeArchiveCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.compiling.NativeCompileCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketTag;
import dev.nokee.platform.nativebase.internal.dependencies.NativeApplicationOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeLibraryOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependenciesComponent;
import dev.nokee.platform.nativebase.internal.dependencies.RequestFrameworkAction;
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
import dev.nokee.runtime.nativebase.BinaryLinkage;
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
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.util.GUtil;

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
import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.model.internal.actions.ModelAction.configureEach;
import static dev.nokee.model.internal.actions.ModelAction.configureMatching;
import static dev.nokee.model.internal.actions.ModelSpec.descendantOf;
import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.actions.ModelSpec.subtypeOf;
import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.canBeViewedAs;
import static dev.nokee.model.internal.core.ModelNodes.withType;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.platform.nativebase.internal.plugins.NativeApplicationPlugin.nativeApplicationVariant;
import static dev.nokee.platform.nativebase.internal.plugins.NativeLibraryPlugin.nativeLibraryVariant;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureDescription;
import static dev.nokee.utils.TransformerUtils.transformEach;

public class NativeComponentBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(NativeRuntimePlugin.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class); // for now, later we will be more smart
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(FrameworkAwareDependencyBucketTag.class), (entity, ignored) -> {
			entity.addComponent(new DependencyDefaultActionComponent(new RequestFrameworkAction(project.getObjects())));
		}));

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

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(HeaderSearchPathsConfigurationComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, headerSearchPaths, parent) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			registry.instantiate(configure(headerSearchPaths.get().getId(), Configuration.class, configureExtendsFrom((Callable<?>) () -> {
				val result = ImmutableList.builder();
				ParentUtils.stream(parent).flatMap(it -> Optionals.stream(it.find(ImplementationConfigurationComponent.class))).findFirst().map(it -> ModelNodeUtils.get(it.get(), Configuration.class)).ifPresent(result::add);
				ParentUtils.stream(parent).flatMap(it -> Optionals.stream(it.find(CompileOnlyConfigurationComponent.class))).findFirst().map(it -> ModelNodeUtils.get(it.get(), Configuration.class)).ifPresent(result::add);
				return result.build();
			})));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(HeaderSearchPathsConfigurationComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, headerSearchPaths, parent) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			ParentUtils.stream(parent).flatMap(it -> Optionals.stream(it.find(BuildVariantComponent.class))).findFirst().ifPresent(it -> {
				registry.instantiate(configure(headerSearchPaths.get().getId(), Configuration.class, ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) it.get(), project.getObjects())));
			});
			registry.instantiate(configure(headerSearchPaths.get().getId(), Configuration.class, ConfigurationUtilsEx::configureAsGradleDebugCompatible));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ImportModulesConfigurationComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, importModules, parent) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			registry.instantiate(configure(importModules.get().getId(), Configuration.class, configureExtendsFrom((Callable<?>) () -> {
				val result = ImmutableList.builder();
				ParentUtils.stream(parent).flatMap(it -> Optionals.stream(it.find(ImplementationConfigurationComponent.class))).findFirst().map(it -> ModelNodeUtils.get(it.get(), Configuration.class)).ifPresent(result::add);
				ParentUtils.stream(parent).flatMap(it -> Optionals.stream(it.find(CompileOnlyConfigurationComponent.class))).findFirst().map(it -> ModelNodeUtils.get(it.get(), Configuration.class)).ifPresent(result::add);
				return result.build();
			})));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ImportModulesConfigurationComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, importModules, parent) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			ParentUtils.stream(parent).flatMap(it -> Optionals.stream(it.find(BuildVariantComponent.class))).findFirst().ifPresent(it -> {
				registry.instantiate(configure(importModules.get().getId(), Configuration.class, ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) it.get(), project.getObjects())));
			});
			registry.instantiate(configure(importModules.get().getId(), Configuration.class, ConfigurationUtilsEx::configureAsGradleDebugCompatible));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(NativeVariantTag.class), ModelComponentReference.of(BuildVariantComponent.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, ignored1, buildVariantComponent, identifier, parent) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val buildVariant = (BuildVariantInternal) buildVariantComponent.get();

			if (buildVariant.hasAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)) {
				registry.instantiate(configureEach(descendantOf(entity.getId()).and(subtypeOf(of(HasConfigurableHeaders.class))), LanguageSourceSet.class, sourceSet -> {
					((HasConfigurableHeaders) sourceSet).getHeaders().setFrom((Callable<?>) () -> {
						return ((ModelLookup) registry).query(e -> canBeViewedAs(e, of(NativeHeaderSet.class)) && e.find(ParentComponent.class).map(it -> it.get().equals(parent.get())).orElse(false)).map(it -> ModelNodeUtils.get(it, NativeHeaderSet.class).getSourceDirectories());
					});
				}));
				registry.instantiate(configureEach(descendantOf(entity.getId()), CSourceSetSpec.class, sourceSet -> {
					((HasConfigurableSource) sourceSet).getSource().setFrom((Callable<?>) () -> ((ModelLookup) registry).query(e -> canBeViewedAs(e, of(CSourceSet.class)) && e.find(ParentComponent.class).map(it -> it.get().equals(parent.get())).orElse(false)).map(it -> ModelNodeUtils.get(it, CSourceSet.class).getAsFileTree()));
					sourceSet.getCompileTask().configure(task -> NativePlatformFactory.create(buildVariant).ifPresent(task.getTargetPlatform()::set));
					ModelNodes.of(sourceSet).addComponent(new BuildVariantComponent(buildVariant));
				}));
				registry.instantiate(configureEach(descendantOf(entity.getId()), CppSourceSetSpec.class, sourceSet -> {
					((HasConfigurableSource) sourceSet).getSource().setFrom((Callable<?>) () -> ((ModelLookup) registry).query(e -> canBeViewedAs(e, of(CppSourceSet.class)) && e.find(ParentComponent.class).map(it -> it.get().equals(parent.get())).orElse(false)).map(it -> ModelNodeUtils.get(it, CppSourceSet.class).getAsFileTree()));
					sourceSet.getCompileTask().configure(task -> NativePlatformFactory.create(buildVariant).ifPresent(task.getTargetPlatform()::set));
					ModelNodes.of(sourceSet).addComponent(new BuildVariantComponent(buildVariant));
				}));
				registry.instantiate(configureEach(descendantOf(entity.getId()), ObjectiveCSourceSetSpec.class, sourceSet -> {
					((HasConfigurableSource) sourceSet).getSource().setFrom((Callable<?>) () -> ((ModelLookup) registry).query(e -> canBeViewedAs(e, of(ObjectiveCSourceSet.class)) && e.find(ParentComponent.class).map(it -> it.get().equals(parent.get())).orElse(false)).map(it -> ModelNodeUtils.get(it, ObjectiveCSourceSet.class).getAsFileTree()));
					sourceSet.getCompileTask().configure(task -> NativePlatformFactory.create(buildVariant).ifPresent(task.getTargetPlatform()::set));
					ModelNodes.of(sourceSet).addComponent(new BuildVariantComponent(buildVariant));
				}));
				registry.instantiate(configureEach(descendantOf(entity.getId()), ObjectiveCppSourceSetSpec.class, sourceSet -> {
					((HasConfigurableSource) sourceSet).getSource().setFrom((Callable<?>) () -> ((ModelLookup) registry).query(e -> canBeViewedAs(e, of(ObjectiveCppSourceSet.class)) && e.find(ParentComponent.class).map(it -> it.get().equals(parent.get())).orElse(false)).map(it -> ModelNodeUtils.get(it, ObjectiveCppSourceSet.class).getAsFileTree()));
					sourceSet.getCompileTask().configure(task -> NativePlatformFactory.create(buildVariant).ifPresent(task.getTargetPlatform()::set));
					ModelNodes.of(sourceSet).addComponent(new BuildVariantComponent(buildVariant));
				}));
				registry.instantiate(configureEach(descendantOf(entity.getId()), SwiftSourceSetSpec.class, sourceSet -> {
					((HasConfigurableSource) sourceSet).getSource().setFrom((Callable<?>) () -> ((ModelLookup) registry).query(e -> canBeViewedAs(e, of(SwiftSourceSet.class)) && e.find(ParentComponent.class).map(it -> it.get().equals(parent.get())).orElse(false)).map(it -> ModelNodeUtils.get(it, SwiftSourceSet.class).getAsFileTree()));
					sourceSet.getCompileTask().configure(task -> NativePlatformFactory.create(buildVariant).ifPresent(task.getTargetPlatform()::set));
					sourceSet.getCompileTask().configure(task -> task.getModuleName().set(project.getProviders().provider(() -> GUtil.toCamelCase(ModelStates.finalize(ModelNodes.of(sourceSet).get(ParentComponent.class).get()).get(BaseNameComponent.class).get()))));
					ModelNodes.of(sourceSet).addComponent(new BuildVariantComponent(buildVariant));
				}));

				val linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
				if (linkage.isExecutable()) {
					val binaryIdentifier = BinaryIdentifier.of(identifier.get(), BinaryIdentity.ofMain("executable", "executable binary"));

					val executableBinary = registry.register(newEntity("executable", ExecutableBinaryInternal.class, it -> it.ownedBy(entity)
						.displayName("executable binary")
						.withComponent(new IdentifierComponent(binaryIdentifier))
						.withComponent(new BuildVariantComponent(buildVariant))
						.withTag(ExcludeFromQualifyingNameTag.class)
					));
					entity.addComponent(new NativeExecutableBinaryComponent(ModelNodes.of(executableBinary)));
				} else if (linkage.isShared()) {
					val binaryIdentifier = BinaryIdentifier.of(identifier.get(), BinaryIdentity.ofMain("sharedLibrary", "shared library binary"));

					val sharedLibraryBinary = registry.register(newEntity("sharedLibrary", SharedLibraryBinaryInternal.class, it -> it.ownedBy(entity)
						.displayName("shared library binary")
						.withComponent(new IdentifierComponent(binaryIdentifier))
						.withComponent(new BuildVariantComponent(buildVariant))
						.withTag(ExcludeFromQualifyingNameTag.class)
					));
					entity.addComponent(new NativeSharedLibraryBinaryComponent(ModelNodes.of(sharedLibraryBinary)));
				} else if (linkage.isBundle()) {
					val binaryIdentifier = BinaryIdentifier.of(identifier.get(), BinaryIdentity.ofMain("bundle", "bundle binary"));

					registry.register(newEntity("bundle", BundleBinaryInternal.class, it -> it.ownedBy(entity)
						.displayName("bundle binary")
						.withComponent(new IdentifierComponent(binaryIdentifier))
						.withComponent(new BuildVariantComponent(buildVariant))
						.withTag(ExcludeFromQualifyingNameTag.class)
					));
				} else if (linkage.isStatic()) {
					val binaryIdentifier = BinaryIdentifier.of(identifier.get(), BinaryIdentity.ofMain("staticLibrary", "static library binary"));

					val staticLibraryBinary = registry.register(newEntity("staticLibrary", StaticLibraryBinaryInternal.class, it -> it.ownedBy(entity)
							.displayName("static library binary")
							.withComponent(new IdentifierComponent(binaryIdentifier))
							.withComponent(new BuildVariantComponent(buildVariant))
							.withTag(ExcludeFromQualifyingNameTag.class)
						));
					entity.addComponent(new NativeStaticLibraryBinaryComponent(ModelNodes.of(staticLibraryBinary)));
				}

				if (linkage.isShared() || linkage.isStatic()) {
					registry.instantiate(configureEach(descendantOf(entity.getId()), SwiftCompileTask.class, task -> {
						task.getCompilerArgs().add("-parse-as-library");
					}));
				}
			}
		})));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(HasConfigurableHeadersPropertyComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, headers, parent) -> {
			// Configure headers according to convention
			((ConfigurableFileCollection) headers.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
				return ParentUtils.stream(parent).map(it -> it.find(FullyQualifiedNameComponent.class)).filter(Optional::isPresent).map(Optional::get).map(FullyQualifiedNameComponent::get).map(it -> "src/" + it + "/headers").collect(Collectors.toList());
			});
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new RuntimeLibrariesConfigurationRegistrationRule(project.getExtensions().getByType(ModelRegistry.class), project.getObjects())));
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

			val implementation = registry.register(newEntity("implementation", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val compileOnly = registry.register(newEntity("compileOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val linkOnly = registry.register(newEntity("linkOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val runtimeOnly = registry.register(newEntity("runtimeOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));

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

			val implementation = registry.register(newEntity("implementation", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val compileOnly = registry.register(newEntity("compileOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val linkOnly = registry.register(newEntity("linkOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val runtimeOnly = registry.register(newEntity("runtimeOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
			entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
			entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
			entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));

			val runtimeElements = registry.register(newEntity("runtimeElements", ConsumableDependencyBucketSpec.class, it -> it.ownedBy(entity)));
			runtimeElements.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
				.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects())));
			val outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new NativeApplicationOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects())));
			entity.addComponent(new VariantComponentDependencies<NativeComponentDependencies>(ModelProperties.getProperty(entity, "dependencies").as(NativeComponentDependencies.class)::get, outgoing.get()));

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

			val api = registry.register(newEntity("api", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val implementation = registry.register(newEntity("implementation", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val compileOnly = registry.register(newEntity("compileOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val linkOnly = registry.register(newEntity("linkOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val runtimeOnly = registry.register(newEntity("runtimeOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));

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

			val api = registry.register(newEntity("api", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val implementation = registry.register(newEntity("implementation", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val compileOnly = registry.register(newEntity("compileOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val linkOnly = registry.register(newEntity("linkOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val runtimeOnly = registry.register(newEntity("runtimeOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			implementation.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));

			entity.addComponent(new ApiConfigurationComponent(ModelNodes.of(api)));
			entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
			entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
			entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
			entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));

			boolean hasSwift = project.getExtensions().getByType(ModelLookup.class).anyMatch(ModelSpecs.of(ModelNodes.withType(of(SwiftSourceSet.class))));
			ModelElement apiElements = null;
			if (hasSwift) {
				apiElements = registry.register(newEntity("apiElements", ConsumableDependencyBucketSpec.class, it -> it.ownedBy(entity)));
				apiElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.SWIFT_API))))
					.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
			} else {
				apiElements = registry.register(newEntity("apiElements", ConsumableDependencyBucketSpec.class, it -> it.ownedBy(entity)));
				apiElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class))
					.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))))
					.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects()))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
			}
			val linkElements = registry.register(newEntity("linkElements", ConsumableDependencyBucketSpec.class, it -> it.ownedBy(entity)));
			linkElements.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), linkOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))))
				.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects())));
			val runtimeElements = registry.register(newEntity("runtimeElements", ConsumableDependencyBucketSpec.class, it -> it.ownedBy(entity)));
			runtimeElements.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
				.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects())));
			NativeOutgoingDependenciesComponent outgoing = null;
			if (hasSwift) {
				outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new SwiftLibraryOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(apiElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(linkElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects())));
			} else {
				outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new NativeLibraryOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(apiElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(linkElements), Configuration.class), ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects())));
			}
			entity.addComponent(new VariantComponentDependencies<NativeComponentDependencies>(ModelProperties.getProperty(entity, "dependencies").as(NativeComponentDependencies.class)::get, outgoing.get()));

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

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), ModelTags.referenceOf(NativeApplicationTag.class), (entity, ignored, tag) -> {
			new CalculateNativeApplicationVariantAction(project).execute(entity);
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), ModelTags.referenceOf(NativeLibraryTag.class), (entity, ignored, tag) -> {
			new CalculateNativeLibraryVariantAction(project).execute(entity);
		}));

		// TODO: Should be part of native-application-base plugin
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeVariantTag.class), ModelComponentReference.of(BuildVariantComponent.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(NativeExecutableBinaryComponent.class), (entity, ignored1, buildVariant, parent, binary) -> {
			if (parent.get().hasComponent(ModelTags.typeOf(NativeApplicationTag.class))) {
				val lifecycleTask = project.getExtensions().getByType(ModelRegistry.class).register(newEntity("executable", Task.class, it -> it.ownedBy(entity)));
				lifecycleTask.configure(Task.class, configureBuildGroup());
				lifecycleTask.configure(Task.class, configureDescription("Assembles a executable binary containing the objects files of %s.", binary.get().get(IdentifierComponent.class).get()));
				lifecycleTask.configure(Task.class, configureDependsOn((Callable<?>) () -> ModelNodeUtils.get(ModelStates.finalize(binary.get()), ExecutableBinary.class)));
			}
		}));

		// TODO: Should be part of native-library-base plugin
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeVariantTag.class), ModelComponentReference.of(BuildVariantComponent.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(NativeSharedLibraryBinaryComponent.class), (entity, ignored1, buildVariant, parent, binary) -> {
			if (parent.get().hasComponent(ModelTags.typeOf(NativeLibraryTag.class))) {
				val lifecycleTask = project.getExtensions().getByType(ModelRegistry.class).register(newEntity("sharedLibrary", Task.class, it -> it.ownedBy(entity)));
				lifecycleTask.configure(Task.class, configureBuildGroup());
				lifecycleTask.configure(Task.class, configureDescription("Assembles a shared library binary containing the objects files of %s.", binary.get().get(IdentifierComponent.class).get()));
				lifecycleTask.configure(Task.class, configureDependsOn((Callable<?>) () -> ModelNodeUtils.get(ModelStates.finalize(binary.get()), SharedLibraryBinary.class)));
			}
		}));
		// TODO: Should be part of native-library-base plugin
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeVariantTag.class), ModelComponentReference.of(BuildVariantComponent.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(NativeStaticLibraryBinaryComponent.class), (entity, ignored1, buildVariant, parent, binary) -> {
			if (parent.get().hasComponent(ModelTags.typeOf(NativeLibraryTag.class))) {
				val lifecycleTask = project.getExtensions().getByType(ModelRegistry.class).register(newEntity("staticLibrary", Task.class, it -> it.ownedBy(entity)));
				lifecycleTask.configure(Task.class, configureBuildGroup());
				lifecycleTask.configure(Task.class, configureDescription("Assembles a static library binary containing the objects files of %s.", binary.get().get(IdentifierComponent.class).get()));
				lifecycleTask.configure(Task.class, configureDependsOn((Callable<?>) () -> ModelNodeUtils.get(ModelStates.finalize(binary.get()), StaticLibraryBinary.class)));
			}
		}));

		// ComponentFromEntity<GradlePropertyComponent> read-write on DevelopmentVariantPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(DevelopmentVariantPropertyComponent.class), ModelTags.referenceOf(NativeApplicationTag.class), (entity, developmentVariant, ignored1) -> {
			((Property<NativeApplication>) developmentVariant.get().get(GradlePropertyComponent.class).get())
				.convention((Provider<? extends DefaultNativeApplicationVariant>) project.provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) ModelNodeUtils.get(entity, of(DefaultNativeApplicationComponent.class)).getVariants().map(VariantInternal.class::cast).get())));
		}));
		// ComponentFromEntity<GradlePropertyComponent> read-write on DevelopmentVariantPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(DevelopmentVariantPropertyComponent.class), ModelTags.referenceOf(NativeLibraryTag.class), (entity, developmentVariant, ignored1) -> {
			((Property<NativeLibrary>) developmentVariant.get().get(GradlePropertyComponent.class).get())
				.convention((Provider<? extends DefaultNativeLibraryVariant>) project.provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) ModelNodeUtils.get(entity, of(DefaultNativeLibraryComponent.class)).getVariants().map(VariantInternal.class::cast).get())));
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

	public static Factory<DefaultNativeApplicationComponent> nativeApplicationProjection(Project project) {
		return () -> project.getObjects().newInstance(DefaultNativeApplicationComponent.class, project.getExtensions().getByType(ModelRegistry.class));
	}

	public static Factory<DefaultNativeLibraryComponent> nativeLibraryProjection(Project project) {
		return () -> project.getObjects().newInstance(DefaultNativeLibraryComponent.class, project.getExtensions().getByType(ModelRegistry.class));
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

	private static class CalculateNativeApplicationVariantAction {
		private final Project project;

		public CalculateNativeApplicationVariantAction(Project project) {
			this.project = project;
		}

		public void execute(ModelNode entity) {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val component = ModelNodeUtils.get(entity, of(DefaultNativeApplicationComponent.class));

			val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
			component.getBuildVariants().get().forEach(new Consumer<BuildVariant>() {
				@Override
				public void accept(BuildVariant buildVariant) {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					val variant = registry.register(nativeApplicationVariant(variantIdentifier, component, project));
					ModelNodes.of(variant).addComponent(new BuildVariantComponent(buildVariant));

					variants.put(buildVariant, ModelNodes.of(variant));
					onEachVariantDependencies(variant.as(NativeApplication.class), ModelNodes.of(variant).getComponent(componentOf(VariantComponentDependencies.class)));
				}

				private void onEachVariantDependencies(DomainObjectProvider<NativeApplication> variant, VariantComponentDependencies<?> dependencies) {
					dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
				}
			});
			entity.addComponent(new Variants(variants.build()));

			component.finalizeExtension(null);
		}
	}

	private static class CalculateNativeLibraryVariantAction {
		private final Project project;

		private CalculateNativeLibraryVariantAction(Project project) {
			this.project = project;
		}

		public void execute(ModelNode entity) {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val component = ModelNodeUtils.get(entity, of(DefaultNativeLibraryComponent.class));

			val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
			component.getBuildVariants().get().forEach(new Consumer<BuildVariant>() {
				private final ModelLookup modelLookup = project.getExtensions().getByType(ModelLookup.class);

				@Override
				public void accept(BuildVariant buildVariant) {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					val variant = registry.register(nativeLibraryVariant(variantIdentifier, component, project));
					ModelNodes.of(variant).addComponent(new BuildVariantComponent(buildVariant));

					variants.put(buildVariant, ModelNodes.of(variant));
					onEachVariantDependencies(variant.as(NativeLibrary.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));
				}

				private void onEachVariantDependencies(DomainObjectProvider<NativeLibrary> variant, VariantComponentDependencies<?> dependencies) {
					if (NativeLibrary.class.isAssignableFrom(DefaultNativeLibraryVariant.class)) {
						if (modelLookup.anyMatch(ModelSpecs.of(withType(of(SwiftSourceSet.class))))) {
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
		}
	}
}
