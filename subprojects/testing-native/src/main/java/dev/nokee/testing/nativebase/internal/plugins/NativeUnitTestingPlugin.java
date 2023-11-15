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
import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
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
import dev.nokee.model.capabilities.variants.IsVariant;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.base.internal.MainProjectionComponent;
import dev.nokee.platform.base.internal.ModelObjectFactory;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.VariantViewFactory;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.internal.dependencies.NativeApplicationOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention;
import dev.nokee.platform.nativebase.internal.rules.ToBinariesCompileTasksTransformer;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.internal.IsTestComponent;
import dev.nokee.testing.base.internal.TestedComponentPropertyComponent;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteComponent;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteVariant;
import dev.nokee.testing.nativebase.internal.NativeTestSuiteComponentTag;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.DomainObjectEntities.tagsOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.testing.base.internal.plugins.TestingBasePlugin.testSuites;
import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class NativeUnitTestingPlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(TestingBasePlugin.class);

		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultNativeTestSuiteVariant.class, new ModelObjectFactory<DefaultNativeTestSuiteVariant>(project, IsVariant.class) {
			@Override
			protected DefaultNativeTestSuiteVariant doCreate(String name) {
				return project.getObjects().newInstance(DefaultNativeTestSuiteVariant.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<TaskView<Task>>>() {}));
			}
		});

		variants(project).withType(DefaultNativeTestSuiteVariant.class).configureEach(variant -> {
			variant.getDevelopmentBinary().convention(variant.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(variant.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
		});
		variants(project).withType(DefaultNativeTestSuiteVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) variant.getDevelopmentBinary()::get));
			}
		});
		variants(project).withType(DefaultNativeTestSuiteVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getObjectsTask().configure(configureDependsOn(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)));
			}
		});

		model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(DefaultNativeTestSuiteComponent.class, new ModelObjectFactory<DefaultNativeTestSuiteComponent>(project, IsTestComponent.class) {
			@Override
			protected DefaultNativeTestSuiteComponent doCreate(String name) {
				return project.getObjects().newInstance(DefaultNativeTestSuiteComponent.class, project.getExtensions().getByType(ModelLookup.class), project.getExtensions().getByType(ModelRegistry.class), model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(new TypeOf<Factory<DefaultVariantDimensions>>() {}));
			}
		});

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeTestSuiteComponentTag.class), (entity, identifier, tag) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			val testedComponentProperty = registry.register(builder().withComponent(new ElementNameComponent("testedComponent")).withComponent(new ParentComponent(entity)).mergeFrom(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(Component.class)).build());
			entity.addComponent(new TestedComponentPropertyComponent(ModelNodes.of(testedComponentProperty)));
		})));
		variants(project).withType(DefaultNativeTestSuiteVariant.class).configureEach(variant -> {
			final NativeApplicationOutgoingDependencies outgoing = new NativeApplicationOutgoingDependencies(variant.getRuntimeElements().getAsConfiguration(), project.getObjects());
			outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
		});
		project.afterEvaluate(__ -> {
			components(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
				for (BuildVariant it : component.getBuildVariants().get()) {
					final BuildVariantInternal buildVariant = (BuildVariantInternal) it;
					final VariantIdentifier variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					model(project, registryOf(Variant.class)).register(variantIdentifier, DefaultNativeTestSuiteVariant.class);
				}

				component.finalizeExtension(project);
				component.getDevelopmentVariant().convention((Provider<? extends DefaultNativeTestSuiteVariant>) project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) component.getVariants().map(VariantInternal.class::cast).get())));
			});
		});
		components(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		});
		components(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
			component.getTargetBuildTypes().convention(component.getTestedComponent()
				.flatMap(it -> {
					if (it instanceof TargetBuildTypeAwareComponent) {
						return ((TargetBuildTypeAwareComponent) it).getTargetBuildTypes();
					} else {
						return ProviderUtils.notDefined();
					}
				}).orElse(ImmutableSet.of(TargetBuildTypes.DEFAULT)));
		});
		components(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
			component.getTargetMachines().convention(component.getTestedComponent()
				.flatMap(it -> {
					if (it instanceof TargetMachineAwareComponent) {
						return ((TargetMachineAwareComponent) it).getTargetMachines();
					} else {
						return ProviderUtils.notDefined();
					}
				}).orElse(ImmutableSet.of(TargetMachines.host())));
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeTestSuiteComponentTag.class), (entity, ignored1) -> {
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
		}));

		project.afterEvaluate(proj -> {
			// TODO: We delay as late as possible to "fake" a finalize action.
			testSuites(proj).withType(DefaultNativeTestSuiteComponent.class).configureEach(it -> {
				ModelNodes.safeOf(it).ifPresent(ModelStates::finalize);
			});
		});
	}

	public static ModelRegistration nativeTestSuite(String name, Project project) {
		val identifier = ModelObjectIdentifier.builder().name(ElementName.of(name)).withParent(ProjectIdentifier.of(project)).build();
		val entityPath = ModelPath.path(identifier.getName().toString());
		return builder()
			.withComponent(new ModelPathComponent(entityPath))
			.withComponentTag(IsTestComponent.class)
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(NativeTestSuiteComponentTag.class)
			.withComponent(new IdentifierComponent(identifier))
			.mergeFrom(tagsOf(DefaultNativeTestSuiteComponent.class))
			.withComponent(new MainProjectionComponent(DefaultNativeTestSuiteComponent.class))
			.build()
			;
	}
}
