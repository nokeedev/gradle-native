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
package dev.nokee.testing.xctest.internal.plugins;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.BaseDomainObjectViewProjection;
import dev.nokee.model.internal.BaseNamedDomainObjectViewProjection;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.ios.ObjectiveCIosApplication;
import dev.nokee.platform.ios.internal.IosApplicationOutgoingDependencies;
import dev.nokee.platform.nativebase.NativeApplicationSources;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.xctest.internal.*;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.GUtil;

import javax.inject.Inject;

import java.util.Optional;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.core.ModelProjections.*;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public class ObjectiveCXCTestTestSuitePlugin implements Plugin<Project> {
	private final ObjectFactory objects;

	@Inject
	public ObjectiveCXCTestTestSuitePlugin(ObjectFactory objectFactory) {
		this.objects = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(TestingBasePlugin.class);
		project.getPluginManager().withPlugin("dev.nokee.objective-c-ios-application", appliedPlugin -> {
			BaseNativeComponent<?> application = ModelNodeUtils.get(ModelNodes.of(project.getExtensions().getByType(ObjectiveCIosApplication.class)), BaseNativeComponent.class);
			val testSuites = project.getExtensions().getByType(TestSuiteContainer.class);
			val registry = ModelNodeUtils.get(ModelNodes.of(testSuites), NodeRegistrationFactoryRegistry.class);
			registry.registerFactory(of(DefaultUnitTestXCTestTestSuiteComponent.class), name -> unitTestXCTestTestSuite(name, project));
			registry.registerFactory(of(DefaultUiTestXCTestTestSuiteComponent.class), name -> uiTestXCTestTestSuite(name, project));

			val unitTestComponentProvider = testSuites.register("unitTest", DefaultUnitTestXCTestTestSuiteComponent.class, component -> {
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.getBaseName().set(GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
				component.getModuleName().set(GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
				component.getProductBundleIdentifier().set(project.getGroup().toString() + "." + GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
			});
			val unitTestComponent = unitTestComponentProvider.get();
			project.afterEvaluate(finalizeModelNodeOf(unitTestComponentProvider));

			val uiTestComponentProvider = testSuites.register("uiTest", DefaultUiTestXCTestTestSuiteComponent.class, component -> {
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.getBaseName().set(GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
				component.getModuleName().set(GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
				component.getProductBundleIdentifier().set(project.getGroup().toString() + "." + GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
			});
			val uiTestComponent = uiTestComponentProvider.get();
			project.afterEvaluate(finalizeModelNodeOf(uiTestComponent));
		});
	}

	public static NodeRegistration unitTestXCTestTestSuite(String name, Project project) {
		return NodeRegistration.unmanaged(name, of(DefaultUnitTestXCTestTestSuiteComponent.class), () -> {
				val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultUnitTestXCTestTestSuiteComponent.class, ProjectIdentifier.of(project));
				return newUnitTestFactory(project).create(identifier);
			})
			.action(allDirectDescendants(mutate(of(LanguageSourceSet.class)))
				.apply(executeUsingProjection(of(LanguageSourceSet.class), withConventionOf(maven(ComponentName.of(name)))::accept)))
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

				// TODO: Should be created using CSourceSetSpec
				val objectiveC = registry.register(ModelRegistration.builder()
					.withComponent(path.child("objectiveC"))
					.withComponent(IsLanguageSourceSet.tag())
					.withComponent(managed(of(ObjectiveCSourceSet.class)))
					.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
					.build());

				// TODO: Should be created using CHeaderSetSpec
				val headers = registry.register(ModelRegistration.builder()
					.withComponent(path.child("headers"))
					.withComponent(IsLanguageSourceSet.tag())
					.withComponent(managed(of(CHeaderSet.class)))
					.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
					.build());

				// TODO: Should be created as ModelProperty (readonly) with CApplicationSources projection
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("sources"))
					.withComponent(IsModelProperty.tag())
					.withComponent(managed(of(NativeApplicationSources.class)))
					.withComponent(managed(of(BaseDomainObjectViewProjection.class)))
					.withComponent(managed(of(BaseNamedDomainObjectViewProjection.class)))
					.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (ee, pp, ignored) -> {
						if (path.child("sources").equals(pp)) {
							project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsLanguageSourceSet.class), ModelComponentReference.ofAny(projectionOf(LanguageSourceSet.class)), (e, p, ignored1, ignored2, projection) -> {
								if (path.isDescendant(p)) {
									val elementName = StringUtils.uncapitalize(Streams.stream(Iterables.skip(p, Iterables.size(path)))
										.filter(it -> !it.isEmpty())
										.map(StringUtils::capitalize)
										.collect(Collectors.joining()));
									registry.register(propertyFactory.create(path.child("sources").child(elementName), e));
								}
							}));
						}
					}))
					.build());

				registry.register(ModelRegistration.builder()
					.withComponent(path.child("binaries"))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(of(BinaryView.class), () -> ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), DefaultUnitTestXCTestTestSuiteComponent.class).getBinaries()))
					.build());

				registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(path.child("variants"), DefaultXCTestTestSuiteVariant.class));

				val componentIdentifier = ComponentIdentifier.of(ComponentName.of(name), DefaultUnitTestXCTestTestSuiteComponent.class, ProjectIdentifier.of(project));
				val dependencyContainer = project.getObjects().newInstance(DefaultComponentDependencies.class, componentIdentifier, new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.getConfigurations()), project.getDependencies())));
				val dependencies = project.getObjects().newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);

				registry.register(ModelRegistration.builder()
					.withComponent(path.child("dependencies"))
					.withComponent(IsModelProperty.tag())
					.withComponent(ofInstance(dependencies))
					.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsDependencyBucket.class), ModelComponentReference.ofAny(projectionOf(Configuration.class)), (e, p, ignored1, ignored2, projection) -> {
						if (path.isDirectDescendant(p)) {
							registry.register(propertyFactory.create(path.child("dependencies").child(p.getName()), e));
						}
					}))
					.build());

				val implementation = registry.register(ModelRegistration.builder()
					.withComponent(path.child("implementation"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getImplementation().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getImplementation()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getImplementation().getAsConfiguration().getName())))
					.build());
				val compileOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("compileOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getCompileOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getCompileOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getCompileOnly().getAsConfiguration().getName())))
					.build());
				val linkOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("linkOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getLinkOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getLinkOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getLinkOnly().getAsConfiguration().getName())))
					.build());
				val runtimeOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getRuntimeOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getRuntimeOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getRuntimeOnly().getAsConfiguration().getName())))
					.build());
			})))
			.action(allDirectDescendants(mutate(of(ObjectiveCSourceSet.class)))
				.apply(executeUsingProjection(of(ObjectiveCSourceSet.class), withConventionOf(maven(ComponentName.of(name)), defaultObjectiveCGradle(ComponentName.of(name)))::accept)))
			.action(self(stateOf(ModelState.Finalized)).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val taskRegistry = project.getExtensions().getByType(TaskRegistry.class);
				val component = ModelNodeUtils.get(entity, DefaultUnitTestXCTestTestSuiteComponent.class);
				component.finalizeExtension(project);

				val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
				component.getBuildVariants().get().forEach(buildVariant -> {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultXCTestTestSuiteVariant.class).build();

					val variant = ModelNodeUtils.register(entity, xcTestTestSuiteVariant(variantIdentifier, component, project));
					variants.put(buildVariant, ModelNodes.of(variant));
					onEachVariantDependencies(variant.as(DefaultXCTestTestSuiteVariant.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));
				});
				entity.addComponent(new Variants(variants.build()));
			})))
			;
	}

	private static DomainObjectFactory<DefaultUnitTestXCTestTestSuiteComponent> newUnitTestFactory(Project project) {
		return identifier -> {
			return new DefaultUnitTestXCTestTestSuiteComponent((ComponentIdentifier<?>)identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class));
		};
	}

	public static NodeRegistration uiTestXCTestTestSuite(String name, Project project) {
		return NodeRegistration.unmanaged(name, of(DefaultUiTestXCTestTestSuiteComponent.class), () -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultUiTestXCTestTestSuiteComponent.class, ProjectIdentifier.of(project));
			return newUiTestFactory(project).create(identifier);
		})
			.action(allDirectDescendants(mutate(of(LanguageSourceSet.class)))
				.apply(executeUsingProjection(of(LanguageSourceSet.class), withConventionOf(maven(ComponentName.of(name)))::accept)))
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

				// TODO: Should be created using CSourceSetSpec
				val objectiveC = registry.register(ModelRegistration.builder()
					.withComponent(path.child("objectiveC"))
					.withComponent(IsLanguageSourceSet.tag())
					.withComponent(managed(of(ObjectiveCSourceSet.class)))
					.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
					.build());

				// TODO: Should be created using CHeaderSetSpec
				val headers = registry.register(ModelRegistration.builder()
					.withComponent(path.child("headers"))
					.withComponent(IsLanguageSourceSet.tag())
					.withComponent(managed(of(CHeaderSet.class)))
					.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
					.build());

				// TODO: Should be created as ModelProperty (readonly) with CApplicationSources projection
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("sources"))
					.withComponent(IsModelProperty.tag())
					.withComponent(managed(of(NativeApplicationSources.class)))
					.withComponent(managed(of(BaseDomainObjectViewProjection.class)))
					.withComponent(managed(of(BaseNamedDomainObjectViewProjection.class)))
					.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (ee, pp, ignored) -> {
						if (path.child("sources").equals(pp)) {
							project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsLanguageSourceSet.class), ModelComponentReference.ofAny(projectionOf(LanguageSourceSet.class)), (e, p, ignored1, ignored2, projection) -> {
								if (path.isDescendant(p)) {
									val elementName = StringUtils.uncapitalize(Streams.stream(Iterables.skip(p, Iterables.size(path)))
										.filter(it -> !it.isEmpty())
										.map(StringUtils::capitalize)
										.collect(Collectors.joining()));
									registry.register(propertyFactory.create(path.child("sources").child(elementName), e));
								}
							}));
						}
					}))
					.build());

				registry.register(ModelRegistration.builder()
					.withComponent(path.child("binaries"))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(of(BinaryView.class), () -> ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), DefaultUiTestXCTestTestSuiteComponent.class).getBinaries()))
					.build());

				registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(path.child("variants"), DefaultXCTestTestSuiteVariant.class));

				val componentIdentifier = ComponentIdentifier.of(ComponentName.of(name), DefaultUnitTestXCTestTestSuiteComponent.class, ProjectIdentifier.of(project));
				val dependencyContainer = project.getObjects().newInstance(DefaultComponentDependencies.class, componentIdentifier, new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.getConfigurations()), project.getDependencies())));
				val dependencies = project.getObjects().newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);

				registry.register(ModelRegistration.builder()
					.withComponent(path.child("dependencies"))
					.withComponent(IsModelProperty.tag())
					.withComponent(ofInstance(dependencies))
					.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsDependencyBucket.class), ModelComponentReference.ofAny(projectionOf(Configuration.class)), (e, p, ignored1, ignored2, projection) -> {
						if (path.isDirectDescendant(p)) {
							registry.register(propertyFactory.create(path.child("dependencies").child(p.getName()), e));
						}
					}))
					.build());

				val implementation = registry.register(ModelRegistration.builder()
					.withComponent(path.child("implementation"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getImplementation().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getImplementation()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getImplementation().getAsConfiguration().getName())))
					.build());
				val compileOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("compileOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getCompileOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getCompileOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getCompileOnly().getAsConfiguration().getName())))
					.build());
				val linkOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("linkOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getLinkOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getLinkOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getLinkOnly().getAsConfiguration().getName())))
					.build());
				val runtimeOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getRuntimeOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getRuntimeOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getRuntimeOnly().getAsConfiguration().getName())))
					.build());
			})))
			.action(allDirectDescendants(mutate(of(ObjectiveCSourceSet.class)))
				.apply(executeUsingProjection(of(ObjectiveCSourceSet.class), withConventionOf(maven(ComponentName.of(name)), defaultObjectiveCGradle(ComponentName.of(name)))::accept)))
			.action(self(stateOf(ModelState.Finalized)).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val component = ModelNodeUtils.get(entity, DefaultUiTestXCTestTestSuiteComponent.class);
				component.finalizeExtension(project);

				val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
				component.getBuildVariants().get().forEach(buildVariant -> {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultXCTestTestSuiteVariant.class).build();

					val variant = ModelNodeUtils.register(entity, xcTestTestSuiteVariant(variantIdentifier, component, project));
					variants.put(buildVariant, ModelNodes.of(variant));
					onEachVariantDependencies(variant.as(DefaultXCTestTestSuiteVariant.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));
				});
				entity.addComponent(new Variants(variants.build()));
				component.getVariants().get(); // Force realization, for now
			})))

			;
	}

	private static DomainObjectFactory<DefaultUiTestXCTestTestSuiteComponent> newUiTestFactory(Project project) {
		return identifier -> {
			return new DefaultUiTestXCTestTestSuiteComponent((ComponentIdentifier<?>)identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class));
		};
	}

	private static NodeRegistration xcTestTestSuiteVariant(VariantIdentifier<DefaultXCTestTestSuiteVariant> variantIdentifier, BaseXCTestTestSuiteComponent component, Project project) {
		val taskRegistry = project.getExtensions().getByType(TaskRegistry.class);
		val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), variantIdentifier));
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();

		val variantDependencies = newDependencies(buildVariant, variantIdentifier, component, project.getObjects(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(ModelLookup.class));
		return NodeRegistration.unmanaged(variantIdentifier.getUnambiguousName(), of(DefaultXCTestTestSuiteVariant.class), () -> {
			return project.getObjects().newInstance(DefaultXCTestTestSuiteVariant.class, variantIdentifier, variantDependencies.getIncoming(), project.getObjects(), project.getProviders(), assembleTask, project.getExtensions().getByType(BinaryViewFactory.class));
		})
			.withComponent(variantIdentifier)
			.withComponent(IsVariant.tag())
			.withComponent(variantDependencies)
			.withComponent(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

				val dependencies = variantDependencies.getDependencies();
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("dependencies"))
					.withComponent(IsModelProperty.tag())
					.withComponent(ofInstance(dependencies))
					.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsDependencyBucket.class), ModelComponentReference.ofAny(projectionOf(Configuration.class)), (e, p, ignored1, ignored2, projection) -> {
						if (path.isDirectDescendant(p)) {
							registry.register(propertyFactory.create(path.child("dependencies").child(p.getName()), e));
						}
					}))
					.build());

				val implementation = registry.register(ModelRegistration.builder()
					.withComponent(path.child("implementation"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getImplementation().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getImplementation()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getImplementation().getAsConfiguration().getName())))
					.build());
				val compileOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("compileOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getCompileOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getCompileOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getCompileOnly().getAsConfiguration().getName())))
					.build());
				val linkOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("linkOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getLinkOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getLinkOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getLinkOnly().getAsConfiguration().getName())))
					.build());
				val runtimeOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getRuntimeOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getRuntimeOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getRuntimeOnly().getAsConfiguration().getName())))
					.build());

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

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}

	private static VariantComponentDependencies<DefaultNativeComponentDependencies> newDependencies(BuildVariantInternal buildVariant, VariantIdentifier<DefaultXCTestTestSuiteVariant> variantIdentifier, BaseXCTestTestSuiteComponent component, ObjectFactory objectFactory, ConfigurationContainer configurationContainer, DependencyHandler dependencyHandler, ModelLookup modelLookup) {
		var variantDependencies = component.getDependencies();
		if (component.getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
		}

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant).withOwnerIdentifier(variantIdentifier).withBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
		boolean hasSwift = modelLookup.anyMatch(ModelSpecs.of(ModelNodes.withType(of(SwiftSourceSet.class))));
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(objectFactory);
		NativeOutgoingDependencies outgoing = objectFactory.newInstance(IosApplicationOutgoingDependencies.class, variantIdentifier, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	private static void onEachVariantDependencies(DomainObjectProvider<DefaultXCTestTestSuiteVariant> variant, VariantComponentDependencies<?> dependencies) {
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}
}
