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
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.PolymorphicDomainObjectRegistry;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.BuildVariant;
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
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.ios.ObjectiveCIosApplication;
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal;
import dev.nokee.platform.ios.internal.IosApplicationOutgoingDependencies;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal;
import dev.nokee.platform.nativebase.NativeApplicationSources;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.internal.rules.RegisterAssembleLifecycleTaskRule;
import dev.nokee.runtime.nativebase.*;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.IsTestComponent;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.xctest.internal.*;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.util.Optional;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.managed;
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
			registry.registerFactory(of(DefaultUnitTestXCTestTestSuiteComponent.class), (ModelRegistrationFactory) name -> unitTestXCTestTestSuite(name, project));
			registry.registerFactory(of(DefaultUiTestXCTestTestSuiteComponent.class), (ModelRegistrationFactory) name -> uiTestXCTestTestSuite(name, project));

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

	public static ModelRegistration unitTestXCTestTestSuite(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("XCTest test suite").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		val entityPath = ModelPath.path(name);
		return ModelRegistration.builder()
			.withComponent(entityPath)
			.withComponent(identifier)
			.withComponent(createdUsing(of(DefaultUnitTestXCTestTestSuiteComponent.class), () -> {
				return newUnitTestFactory(project).create(identifier);
			}))
			.withComponent(IsComponent.tag())
			.withComponent(IsTestComponent.tag())
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(LanguageSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPath, ModelState>() {
				private boolean alreadyExecuted = false;

				@Override
				public void execute(ModelNode entity, ModelPath path, ModelState state) {
					if (entityPath.equals(path) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
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

						registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(path.child("sources"), NativeApplicationSources.class));

						registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(path.child("binaries")));

						registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(path.child("variants"), DefaultXCTestTestSuiteVariant.class));

						registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(path.child("tasks")));

						val componentIdentifier = ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of(project));
						val dependencyContainer = project.getObjects().newInstance(DefaultComponentDependencies.class, componentIdentifier, new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.getConfigurations()), DependencyFactory.forProject(project))));
						val dependencies = project.getObjects().newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);

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
							.withComponent(path.child("developmentVariant"))
							.withComponent(IsModelProperty.tag())
							.withComponent(createdUsing(of(new TypeOf<Property<DefaultXCTestTestSuiteVariant>>() {}), () -> project.getObjects().property(DefaultXCTestTestSuiteVariant.class)))
							.build());

						val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
						val buildVariants = entity.addComponent(new BuildVariants(entity, project.getProviders(), project.getObjects()));
						registry.register(dimensions.newAxisProperty(path.child("targetLinkages"))
							.elementType(TargetLinkage.class)
							.axis(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)
							.defaultValue(TargetLinkages.BUNDLE)
							.build());
						registry.register(dimensions.newAxisProperty(path.child("targetBuildTypes"))
							.elementType(TargetBuildType.class)
							.axis(BuildType.BUILD_TYPE_COORDINATE_AXIS)
							.defaultValue(TargetBuildTypes.DEFAULT)
							.build());
						registry.register(dimensions.newAxisProperty(path.child("targetMachines"))
							.axis(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)
							.defaultValue(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64())
							.build());
						registry.register(dimensions.buildVariants(path.child("buildVariants"), buildVariants.get()));
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPath, ModelState>() {
				private boolean alreadyExecuted = false;
				@Override
				public void execute(ModelNode entity, ModelPath path, ModelState state) {
					if (entityPath.equals(path) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						ModelNodeUtils.get(entity, BaseComponent.class).getDimensions().convention(entity.getComponent(componentOf(BuildVariants.class)).dimensions());
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(ObjectiveCSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName()), defaultObjectiveCGradle(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.action(new RegisterAssembleLifecycleTaskRule(identifier, PolymorphicDomainObjectRegistry.of(project.getTasks()), project.getExtensions().getByType(ModelRegistry.class), project.getProviders()))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, path, ignored) -> {
				if (entityPath.equals(path)) {
					val registry = project.getExtensions().getByType(ModelRegistry.class);
					val taskRegistry = project.getExtensions().getByType(TaskRegistry.class);
					val component = ModelNodeUtils.get(entity, DefaultUnitTestXCTestTestSuiteComponent.class);
					component.finalizeExtension(project);
					component.getDevelopmentVariant().convention(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> component.getVariants().get())));

					val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
					component.getBuildVariants().get().forEach(buildVariant -> {
						val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultXCTestTestSuiteVariant.class).build();

						val variant = ModelNodeUtils.register(entity, xcTestTestSuiteVariant(variantIdentifier, component, project));
						variants.put(buildVariant, ModelNodes.of(variant));
						onEachVariantDependencies(variant.as(DefaultXCTestTestSuiteVariant.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));

						val binaryRepository = project.getExtensions().getByType(BinaryRepository.class);
						val binaryConfigurer = project.getExtensions().getByType(BinaryConfigurer.class);
						val binaryIdentifierXCTestBundle = BinaryIdentifier.of(BinaryName.of("unitTestXCTestBundle"), IosXCTestBundle.class, variantIdentifier);
						val xcTestBundleEntity = registry.register(ModelRegistration.builder()
							.withComponent(path.child(variantIdentifier.getUnambiguousName()).child("unitTestXCTestBundle"))
							.withComponent(IsBinary.tag())
							.withComponent(binaryIdentifierXCTestBundle)
							.withComponent(createdUsing(of(IosXCTestBundle.class), () -> binaryRepository.get(binaryIdentifierXCTestBundle)))
							.build());
						binaryConfigurer.configure(binaryIdentifierXCTestBundle, binary -> ModelStates.realize(ModelNodes.of(xcTestBundleEntity)));

						val binaryIdentifierApplicationBundle = BinaryIdentifier.of(BinaryName.of("signedApplicationBundle"), SignedIosApplicationBundleInternal.class, variantIdentifier);
						val applicationBundleEntity = registry.register(ModelRegistration.builder()
							.withComponent(path.child(variantIdentifier.getUnambiguousName()).child("signedApplicationBundle"))
							.withComponent(IsBinary.tag())
							.withComponent(binaryIdentifierApplicationBundle)
							.withComponent(createdUsing(of(SignedIosApplicationBundleInternal.class), () -> binaryRepository.get(binaryIdentifierApplicationBundle)))
							.build());
						binaryConfigurer.configure(binaryIdentifierApplicationBundle, binary -> ModelStates.realize(ModelNodes.of(applicationBundleEntity)));
					});
					entity.addComponent(new Variants(variants.build()));
				}
			}))
			.build()
			;
	}

	private static DomainObjectFactory<DefaultUnitTestXCTestTestSuiteComponent> newUnitTestFactory(Project project) {
		return identifier -> {
			return new DefaultUnitTestXCTestTestSuiteComponent((ComponentIdentifier)identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class));
		};
	}

	public static ModelRegistration uiTestXCTestTestSuite(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("XCTest test suite").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		val entityPath = ModelPath.path(name);
		return ModelRegistration.builder()
			.withComponent(entityPath)
			.withComponent(identifier)
			.withComponent(IsComponent.tag())
			.withComponent(IsTestComponent.tag())
			.withComponent(createdUsing(of(DefaultUiTestXCTestTestSuiteComponent.class), () -> {
				return newUiTestFactory(project).create(identifier);
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(LanguageSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPath, ModelState>() {
				private boolean alreadyExecuted = false;

				@Override
				public void execute(ModelNode entity, ModelPath path, ModelState state) {
					if (entityPath.equals(path) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						val registry = project.getExtensions().getByType(ModelRegistry.class);

						// TODO: Should be created using CSourceSetSpec
						registry.register(ModelRegistration.builder()
							.withComponent(path.child("objectiveC"))
							.withComponent(IsLanguageSourceSet.tag())
							.withComponent(managed(of(ObjectiveCSourceSet.class)))
							.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
							.build());

						// TODO: Should be created using CHeaderSetSpec
						registry.register(ModelRegistration.builder()
							.withComponent(path.child("headers"))
							.withComponent(IsLanguageSourceSet.tag())
							.withComponent(managed(of(CHeaderSet.class)))
							.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
							.build());

						registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(path.child("sources"), NativeApplicationSources.class));

						registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(path.child("binaries")));

						registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(path.child("variants"), DefaultXCTestTestSuiteVariant.class));

						registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(path.child("tasks")));

						val dependencyContainer = project.getObjects().newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.getConfigurations()), DependencyFactory.forProject(project))));
						val dependencies = project.getObjects().newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);

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
							.withComponent(path.child("developmentVariant"))
							.withComponent(IsModelProperty.tag())
							.withComponent(createdUsing(of(new TypeOf<Property<DefaultXCTestTestSuiteVariant>>() {}), () -> project.getObjects().property(DefaultXCTestTestSuiteVariant.class)))
							.build());

						val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
						val buildVariants = entity.addComponent(new BuildVariants(entity, project.getProviders(), project.getObjects()));
						registry.register(dimensions.newAxisProperty(path.child("targetLinkages"))
							.elementType(TargetLinkage.class)
							.axis(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)
							.defaultValue(TargetLinkages.BUNDLE)
							.build());
						registry.register(dimensions.newAxisProperty(path.child("targetBuildTypes"))
							.elementType(TargetBuildType.class)
							.axis(BuildType.BUILD_TYPE_COORDINATE_AXIS)
							.defaultValue(TargetBuildTypes.DEFAULT)
							.build());
						registry.register(dimensions.newAxisProperty(path.child("targetMachines"))
							.axis(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)
							.defaultValue(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64())
							.build());
						registry.register(dimensions.buildVariants(path.child("buildVariants"), buildVariants.get()));
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPath, ModelState>() {
				private boolean alreadyExecuted = false;
				@Override
				public void execute(ModelNode entity, ModelPath path, ModelState state) {
					if (entityPath.equals(path) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						ModelNodeUtils.get(entity, BaseComponent.class).getDimensions().convention(entity.getComponent(componentOf(BuildVariants.class)).dimensions());
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(ObjectiveCSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName()), defaultObjectiveCGradle(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.action(new RegisterAssembleLifecycleTaskRule(identifier, PolymorphicDomainObjectRegistry.of(project.getTasks()), project.getExtensions().getByType(ModelRegistry.class), project.getProviders()))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, path, ignored) -> {
				if (entityPath.equals(path)) {
					val component = ModelNodeUtils.get(entity, DefaultUiTestXCTestTestSuiteComponent.class);
					component.finalizeExtension(project);
					component.getDevelopmentVariant().convention(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> component.getVariants().get())));

					val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
					component.getBuildVariants().get().forEach(buildVariant -> {
						val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultXCTestTestSuiteVariant.class).build();

						val variant = ModelNodeUtils.register(entity, xcTestTestSuiteVariant(variantIdentifier, component, project));
						variants.put(buildVariant, ModelNodes.of(variant));
						onEachVariantDependencies(variant.as(DefaultXCTestTestSuiteVariant.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));

						val registry = project.getExtensions().getByType(ModelRegistry.class);
						val binaryRepository = project.getExtensions().getByType(BinaryRepository.class);
						val binaryConfigurer = project.getExtensions().getByType(BinaryConfigurer.class);
						val binaryIdentifierApplicationBundle = BinaryIdentifier.of(BinaryName.of("launcherApplicationBundle"), IosApplicationBundleInternal.class, variantIdentifier);
						val launcherApplicationBundleEntity = registry.register(ModelRegistration.builder()
							.withComponent(path.child(variantIdentifier.getUnambiguousName()).child("unitTestXCTestBundle"))
							.withComponent(IsBinary.tag())
							.withComponent(binaryIdentifierApplicationBundle)
							.withComponent(createdUsing(of(IosApplicationBundleInternal.class), () -> binaryRepository.get(binaryIdentifierApplicationBundle)))
							.build());
						binaryConfigurer.configure(binaryIdentifierApplicationBundle, binary -> ModelStates.realize(ModelNodes.of(launcherApplicationBundleEntity)));

						val binaryIdentifierSignedApplicationBundle = BinaryIdentifier.of(BinaryName.of("signedLauncherApplicationBundle"), SignedIosApplicationBundleInternal.class, variantIdentifier);
						val signedLauncherApplicationBundleEntity = registry.register(ModelRegistration.builder()
							.withComponent(path.child(variantIdentifier.getUnambiguousName()).child("signedLauncherApplicationBundle"))
							.withComponent(IsBinary.tag())
							.withComponent(binaryIdentifierSignedApplicationBundle)
							.withComponent(createdUsing(of(SignedIosApplicationBundleInternal.class), () -> binaryRepository.get(binaryIdentifierSignedApplicationBundle)))
							.build());
						binaryConfigurer.configure(binaryIdentifierSignedApplicationBundle, binary -> ModelStates.realize(ModelNodes.of(signedLauncherApplicationBundleEntity)));
					});
					entity.addComponent(new Variants(variants.build()));
					component.getVariants().get(); // Force realization, for now
				}
			}))
			.build()
			;
	}

	private static DomainObjectFactory<DefaultUiTestXCTestTestSuiteComponent> newUiTestFactory(Project project) {
		return identifier -> {
			return new DefaultUiTestXCTestTestSuiteComponent((ComponentIdentifier)identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class));
		};
	}

	private static NodeRegistration xcTestTestSuiteVariant(VariantIdentifier<DefaultXCTestTestSuiteVariant> variantIdentifier, BaseXCTestTestSuiteComponent component, Project project) {
		val taskRegistry = project.getExtensions().getByType(TaskRegistry.class);
		val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), variantIdentifier));
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();

		val variantDependencies = newDependencies(buildVariant, variantIdentifier, component, project.getObjects(), project.getConfigurations(), DependencyFactory.forProject(project), project.getExtensions().getByType(ModelLookup.class));
		return NodeRegistration.unmanaged(variantIdentifier.getUnambiguousName(), of(DefaultXCTestTestSuiteVariant.class), () -> {
			return project.getObjects().newInstance(DefaultXCTestTestSuiteVariant.class, variantIdentifier, project.getObjects(), project.getProviders(), assembleTask, project.getExtensions().getByType(BinaryViewFactory.class));
		})
			.withComponent(variantIdentifier)
			.withComponent(IsVariant.tag())
			.withComponent(variantDependencies)
			.withComponent(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);

				registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(path.child("binaries")));

				registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(path.child("tasks")));

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

	private static VariantComponentDependencies<DefaultNativeComponentDependencies> newDependencies(BuildVariantInternal buildVariant, VariantIdentifier<DefaultXCTestTestSuiteVariant> variantIdentifier, BaseXCTestTestSuiteComponent component, ObjectFactory objectFactory, ConfigurationContainer configurationContainer, DependencyFactory dependencyFactory, ModelLookup modelLookup) {
		val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyFactory));
		val variantDependencies = objectFactory.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant).withOwnerIdentifier(variantIdentifier).withBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyFactory));
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
