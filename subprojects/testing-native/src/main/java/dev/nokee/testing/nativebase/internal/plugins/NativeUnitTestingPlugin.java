/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.testing.nativebase.internal.plugins;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.c.internal.plugins.SupportCSourceSetTag;
import dev.nokee.language.cpp.internal.plugins.CppLanguageBasePlugin;
import dev.nokee.language.cpp.internal.plugins.SupportCppSourceSetTag;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.language.objectivec.internal.plugins.SupportObjectiveCSourceSetTag;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import dev.nokee.language.objectivecpp.internal.plugins.SupportObjectiveCppSourceSetTag;
import dev.nokee.language.swift.internal.plugins.SupportSwiftSourceSetTag;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.model.capabilities.variants.LinkedVariantsComponent;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelProperty;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactoryRegistry;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BuildVariantComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.ExtendsFromParentConfigurationAction;
import dev.nokee.platform.base.internal.dependencybuckets.CompileOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.ImplementationConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.RuntimeOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.developmentbinary.DevelopmentBinaryPropertyComponent;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.NativeVariantTag;
import dev.nokee.platform.nativebase.internal.TargetBuildTypesPropertyComponent;
import dev.nokee.platform.nativebase.internal.TargetLinkagesPropertyComponent;
import dev.nokee.platform.nativebase.internal.TargetMachinesPropertyComponent;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketTag;
import dev.nokee.platform.nativebase.internal.dependencies.NativeApplicationOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependenciesComponent;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.IsTestComponent;
import dev.nokee.testing.base.internal.TestedComponentPropertyComponent;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.nativebase.NativeTestSuite;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteComponent;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteVariant;
import dev.nokee.testing.nativebase.internal.NativeTestSuiteComponentTag;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import java.util.Collections;
import java.util.Set;

import static dev.nokee.model.internal.actions.ModelAction.configureMatching;
import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.actions.ModelSpec.subtypeOf;
import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.platform.base.internal.DomainObjectEntities.tagsOf;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;

public class NativeUnitTestingPlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(TestingBasePlugin.class);

		val testSuites = project.getExtensions().getByType(TestSuiteContainer.class);
		val componentRegistry = ModelNodeUtils.get(ModelNodes.of(testSuites), NodeRegistrationFactoryRegistry.class);
		componentRegistry.registerFactory(of(NativeTestSuite.class), name -> nativeTestSuite(name, project));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new ModelActionWithInputs.ModelAction2<IdentifierComponent, ModelComponentTag<NativeTestSuiteComponentTag>>() {
			protected void execute(ModelNode entity, IdentifierComponent identifier, ModelComponentTag<NativeTestSuiteComponentTag> tag) {
				val registry = project.getExtensions().getByType(ModelRegistry.class);

				val implementation = registry.register(newEntity("implementation", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
				val compileOnly = registry.register(newEntity("compileOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
				val linkOnly = registry.register(newEntity("linkOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
				val runtimeOnly = registry.register(newEntity("runtimeOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));

				entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
				entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
				entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
				entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));

				val testedComponentProperty = registry.register(builder().withComponent(new ElementNameComponent("testedComponent")).withComponent(new ParentComponent(entity)).mergeFrom(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(Component.class)).build());
				entity.addComponent(new TestedComponentPropertyComponent(ModelNodes.of(testedComponentProperty)));
			}
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new ModelActionWithInputs.ModelAction3<IdentifierComponent, ModelComponentTag<NativeVariantTag>, ParentComponent>() {
			protected void execute(ModelNode entity, IdentifierComponent identifier, ModelComponentTag<NativeVariantTag> tag, ParentComponent parent) {
				if (!parent.get().hasComponent(typeOf(NativeTestSuiteComponentTag.class))) {
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
			}
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction3<ModelPathComponent, ModelComponentTag<NativeTestSuiteComponentTag>, LinkedVariantsComponent>() {
			protected void execute(ModelNode entity, ModelPathComponent path, ModelComponentTag<NativeTestSuiteComponentTag> tag, LinkedVariantsComponent variants) {
				val component = ModelNodeUtils.get(entity, DefaultNativeTestSuiteComponent.class);

				Streams.zip(component.getBuildVariants().get().stream(), Streams.stream(variants), (buildVariant, variant) -> {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();

					nativeTestSuiteVariant(variantIdentifier, component, project).getComponents().forEach(variant::addComponent);
					variant.addComponent(new BuildVariantComponent(buildVariant));
					ModelStates.register(variant);

					onEachVariantDependencies(variant, variant.getComponent(componentOf(VariantComponentDependencies.class)), project.getProviders());
					return null;
				}).forEach(it -> {});

				component.finalizeExtension(project);
				component.getDevelopmentVariant().convention((Provider<? extends DefaultNativeTestSuiteVariant>) project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) component.getVariants().map(VariantInternal.class::cast).get())));
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<ModelComponentTag<NativeTestSuiteComponentTag>, TargetLinkagesPropertyComponent>() {
			protected void execute(ModelNode entity, ModelComponentTag<NativeTestSuiteComponentTag> tag, TargetLinkagesPropertyComponent targetLinkages) {
				((SetProperty<TargetLinkage>) targetLinkages.get().get(GradlePropertyComponent.class).get()).convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction3<ModelComponentTag<NativeTestSuiteComponentTag>, TargetBuildTypesPropertyComponent, TestedComponentPropertyComponent>() {
			protected void execute(ModelNode entity, ModelComponentTag<NativeTestSuiteComponentTag> tag, TargetBuildTypesPropertyComponent targetBuildTypes, TestedComponentPropertyComponent testedComponent) {
				((SetProperty<TargetBuildType>) targetBuildTypes.get().get(GradlePropertyComponent.class).get())
					.convention(((Property<Component>) testedComponent.get().get(GradlePropertyComponent.class).get())
						.flatMap(component -> {
							val property = ModelProperties.findProperty(component, "targetBuildTypes");
							if (property.isPresent()) {
								return ((ModelProperty<Set<TargetBuildType>>) property.get()).asProvider();
							} else {
								return ProviderUtils.notDefined();
							}
						}).orElse(ImmutableSet.of(TargetBuildTypes.DEFAULT)));
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction3<ModelComponentTag<NativeTestSuiteComponentTag>, TargetMachinesPropertyComponent, TestedComponentPropertyComponent>() {
			protected void execute(ModelNode entity, ModelComponentTag<NativeTestSuiteComponentTag> tag, TargetMachinesPropertyComponent targetMachines, TestedComponentPropertyComponent testedComponent) {
				((SetProperty<TargetMachine>) targetMachines.get().get(GradlePropertyComponent.class).get())
					.convention(((Property<Component>) testedComponent.get().get(GradlePropertyComponent.class).get())
						.flatMap(component -> {
							val property = ModelProperties.findProperty(component, "targetMachines");
							if (property.isPresent()) {
								return ((ModelProperty<Set<TargetMachine>>) property.get()).asProvider();
							} else {
								return ProviderUtils.notDefined();
							}
						}).orElse(ImmutableSet.of(TargetMachines.host())));
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction1<ModelComponentTag<NativeTestSuiteComponentTag>>() {
			protected void execute(ModelNode entity, ModelComponentTag<NativeTestSuiteComponentTag> ignored1) {
				if (project.getPlugins().hasPlugin(CLanguageBasePlugin.class)) {
					entity.addComponentTag(SupportCSourceSetTag.class);
				} else if (project.getPlugins().hasPlugin(CppLanguageBasePlugin.class)) {
					entity.addComponentTag(SupportCppSourceSetTag.class);
				} else if (project.getPlugins().hasPlugin(ObjectiveCLanguageBasePlugin.class)) {
					entity.addComponentTag(SupportObjectiveCSourceSetTag.class);
				} else if (project.getPlugins().hasPlugin(ObjectiveCppLanguageBasePlugin.class)) {
					entity.addComponentTag(SupportObjectiveCppSourceSetTag.class);
				} else if (project.getPlugins().hasPlugin(SwiftLanguageBasePlugin.class)) {
					entity.addComponentTag(SupportSwiftSourceSetTag.class);
				}
			}
		});

		project.afterEvaluate(proj -> {
			// TODO: We delay as late as possible to "fake" a finalize action.
			testSuites.configureEach(DefaultNativeTestSuiteComponent.class, it -> {
				ModelStates.finalize(it.getNode());
			});
		});
	}

	public static ModelRegistration nativeTestSuite(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("native test suite").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		val entityPath = ModelPath.path(identifier.getName().get());
		return builder()
			.withComponent(new ModelPathComponent(entityPath))
			.withComponent(createdUsing(of(DefaultNativeTestSuiteComponent.class), () -> project.getObjects().newInstance(DefaultNativeTestSuiteComponent.class, project.getExtensions().getByType(ModelLookup.class), project.getExtensions().getByType(ModelRegistry.class))))
			.withComponentTag(IsTestComponent.class)
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(NativeTestSuiteComponentTag.class)
			.withComponent(new IdentifierComponent(identifier))
			.mergeFrom(tagsOf(DefaultNativeTestSuiteComponent.class))
			.build()
			;
	}

	private static ModelRegistration nativeTestSuiteVariant(VariantIdentifier identifier, DefaultNativeTestSuiteComponent component, Project project) {
		return builder()
			.withComponentTag(ConfigurableTag.class)
			.withComponent(new IdentifierComponent(identifier))
			.withComponentTag(NativeVariantTag.class)
			.mergeFrom(tagsOf(DefaultNativeTestSuiteVariant.class))
			.withComponent(createdUsing(of(DefaultNativeTestSuiteVariant.class), () -> {
				return project.getObjects().newInstance(DefaultNativeTestSuiteVariant.class);
			}))
			.build()
			;
	}

	@SuppressWarnings("unchecked")
	private static void onEachVariantDependencies(ModelNode variant, VariantComponentDependencies<?> dependencies, ProviderFactory providers) {
		dependencies.getOutgoing().getExportedBinary().convention(providers.provider(() -> (Provider<Binary>) ModelStates.finalize(variant).get(DevelopmentBinaryPropertyComponent.class).get().get(GradlePropertyComponent.class).get()).flatMap(it -> it));
	}
}
