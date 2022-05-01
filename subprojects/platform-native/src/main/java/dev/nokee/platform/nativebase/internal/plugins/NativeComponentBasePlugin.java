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

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.Factory;
import dev.nokee.language.nativebase.internal.HasConfigurableHeadersPropertyComponent;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.AssembleTaskComponent;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencybuckets.ImplementationConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkedConfiguration;
import dev.nokee.platform.base.internal.dependencybuckets.RuntimeOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.ModelBackedTaskRegistry;
import dev.nokee.platform.nativebase.internal.AttachAttributesToConfigurationRule;
import dev.nokee.platform.nativebase.internal.BundleBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.NativeApplicationTag;
import dev.nokee.platform.nativebase.internal.NativeLibraryTag;
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
import dev.nokee.platform.nativebase.internal.linking.LinkLibrariesConfiguration;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.rules.LanguageSourceLayoutConvention;
import dev.nokee.platform.nativebase.internal.rules.LegacyObjectiveCSourceLayoutConvention;
import dev.nokee.platform.nativebase.internal.rules.LegacyObjectiveCppSourceLayoutConvention;
import dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer;
import dev.nokee.platform.nativebase.internal.rules.WarnUnbuildableLogger;
import dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.utils.DeferUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.RunnableUtils.onlyOnce;
import static dev.nokee.utils.TaskUtils.configureDependsOn;

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

		project.getExtensions().getByType(ModelConfigurer.class).configure(new LanguageSourceLayoutConvention());
		project.getExtensions().getByType(ModelConfigurer.class).configure(new LegacyObjectiveCSourceLayoutConvention());
		project.getExtensions().getByType(ModelConfigurer.class).configure(new LegacyObjectiveCppSourceLayoutConvention());

		project.getPluginManager().apply(NativeCompileCapabilityPlugin.class);
		project.getPluginManager().apply(NativeLinkCapabilityPlugin.class);
		project.getPluginManager().apply(NativeArchiveCapabilityPlugin.class);

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new TargetMachinesPropertyRegistrationRule(project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelRegistry.class), project.getObjects().newInstance(ToolChainSelectorInternal.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new TargetBuildTypesPropertyRegistrationRule(project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelRegistry.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new TargetLinkagesPropertyRegistrationRule(project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelRegistry.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(NativeApplicationTag.class), ModelComponentReference.of(TargetLinkagesPropertyComponent.class), (entity, tag, targetLinkages) -> {
			((SetProperty<TargetLinkage>) targetLinkages.get().get(GradlePropertyComponent.class).get()).convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(NativeLibraryTag.class), ModelComponentReference.of(TargetLinkagesPropertyComponent.class), (entity, tag, targetLinkages) -> {
			((SetProperty<TargetLinkage>) targetLinkages.get().get(GradlePropertyComponent.class).get()).convention(Collections.singletonList(TargetLinkages.SHARED));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(LinkLibrariesConfiguration.class), ModelComponentReference.of(ParentComponent.class), (e, linkLibraries, parent) -> {
			project.getExtensions().getByType(ModelRegistry.class).instantiate(configure(linkLibraries.get().getId(), Configuration.class, configureExtendsFrom(firstParentConfigurationOf(parent, ImplementationConfigurationComponent.class), firstParentConfigurationOf(parent, LinkOnlyConfigurationComponent.class))));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(RuntimeLibrariesConfiguration.class), ModelComponentReference.of(ParentComponent.class), (e, runtimeLibraries, parent) -> {
			project.getExtensions().getByType(ModelRegistry.class).instantiate(configure(runtimeLibraries.get().getId(), Configuration.class, configureExtendsFrom(firstParentConfigurationOf(parent, ImplementationConfigurationComponent.class), firstParentConfigurationOf(parent, RuntimeOnlyConfigurationComponent.class))));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(AssembleTaskComponent.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.ofProjection(HasDevelopmentVariant.class), (entity, assembleTask, identifier, tag) -> {
			// The "component" assemble task was most likely added by the 'lifecycle-base' plugin
			//   then we configure the dependency.
			//   Note that the dependency may already exists for single variant component but it's not a big deal.
			@SuppressWarnings("unchecked")
			final Provider<HasDevelopmentVariant<?>> component = project.getProviders().provider(() -> ModelNodeUtils.get(entity, HasDevelopmentVariant.class));
			Provider<? extends Variant> developmentVariant = component.flatMap(HasDevelopmentVariant::getDevelopmentVariant);
			val logger = new WarnUnbuildableLogger((ComponentIdentifier) identifier.get());

			val registry = project.getExtensions().getByType(ModelRegistry.class);
			registry.instantiate(configure(assembleTask.get().getId(), Task.class, configureDependsOn(developmentVariant.flatMap(ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY).map(Arrays::asList)
				.orElse(DeferUtils.executes(onlyOnce(logger::warn))))));
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
}
