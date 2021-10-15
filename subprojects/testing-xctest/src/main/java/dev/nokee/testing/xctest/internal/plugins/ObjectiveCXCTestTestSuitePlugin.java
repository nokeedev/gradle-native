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

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.internal.BaseDomainObjectViewProjection;
import dev.nokee.model.internal.BaseNamedDomainObjectViewProjection;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.ios.ObjectiveCIosApplication;
import dev.nokee.platform.nativebase.NativeApplicationSources;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.xctest.internal.*;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.GUtil;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.core.ModelProjections.*;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;

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
			.withComponent(createdUsing(of(XCTestTestSuiteComponentVariants.class), () -> {
				val component = ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), BaseXCTestTestSuiteComponent.class);
				return new XCTestTestSuiteComponentVariants(project.getObjects(), component, project.getDependencies(), project.getConfigurations(), project.getProviders(), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(ModelLookup.class));
			}))
			.action(allDirectDescendants(mutate(of(LanguageSourceSet.class)))
				.apply(executeUsingProjection(of(LanguageSourceSet.class), withConventionOf(maven(ComponentName.of(name)))::accept)))
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

				// TODO: Should be created using CSourceSetSpec
				val objectiveC = registry.register(ModelRegistration.builder()
					.withComponent(path.child("objectiveC"))
					.withComponent(managed(of(ObjectiveCSourceSet.class)))
					.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
					.build());

				// TODO: Should be created using CHeaderSetSpec
				val headers = registry.register(ModelRegistration.builder()
					.withComponent(path.child("headers"))
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
					.build());

				registry.register(propertyFactory.create(path.child("sources").child("objectiveC"), ModelNodes.of(objectiveC)));
				registry.register(propertyFactory.create(path.child("sources").child("headers"), ModelNodes.of(headers)));

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
					.build());
			})))
			.action(allDirectDescendants(mutate(of(ObjectiveCSourceSet.class)))
				.apply(executeUsingProjection(of(ObjectiveCSourceSet.class), withConventionOf(maven(ComponentName.of(name)), defaultObjectiveCGradle(ComponentName.of(name)))::accept)))
			.action(self(stateOf(ModelState.Finalized)).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val component = ModelNodeUtils.get(entity, DefaultUnitTestXCTestTestSuiteComponent.class);
				component.finalizeExtension(project);
				component.getVariantCollection().whenElementKnown(knownVariant -> {
					registry.register(ModelRegistration.builder()
						.withComponent(path.child(knownVariant.getIdentifier().getUnambiguousName()))
						.withComponent(knownVariant.getIdentifier())
						.withComponent(IsVariant.tag())
						.withComponent(createdUsing(of(DefaultXCTestTestSuiteVariant.class), () -> knownVariant.map(noOpTransformer()).get()))
						.build());
				});
				component.getVariantCollection().realize(); // Force realization, for now
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
			.withComponent(createdUsing(of(XCTestTestSuiteComponentVariants.class), () -> {
				val component = ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), BaseXCTestTestSuiteComponent.class);
				return new XCTestTestSuiteComponentVariants(project.getObjects(), component, project.getDependencies(), project.getConfigurations(), project.getProviders(), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(ModelLookup.class));
			}))
			.action(allDirectDescendants(mutate(of(LanguageSourceSet.class)))
				.apply(executeUsingProjection(of(LanguageSourceSet.class), withConventionOf(maven(ComponentName.of(name)))::accept)))
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

				// TODO: Should be created using CSourceSetSpec
				val objectiveC = registry.register(ModelRegistration.builder()
					.withComponent(path.child("objectiveC"))
					.withComponent(managed(of(ObjectiveCSourceSet.class)))
					.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
					.build());

				// TODO: Should be created using CHeaderSetSpec
				val headers = registry.register(ModelRegistration.builder()
					.withComponent(path.child("headers"))
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
					.build());

				registry.register(propertyFactory.create(path.child("sources").child("objectiveC"), ModelNodes.of(objectiveC)));
				registry.register(propertyFactory.create(path.child("sources").child("headers"), ModelNodes.of(headers)));

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
					.build());
			})))
			.action(allDirectDescendants(mutate(of(ObjectiveCSourceSet.class)))
				.apply(executeUsingProjection(of(ObjectiveCSourceSet.class), withConventionOf(maven(ComponentName.of(name)), defaultObjectiveCGradle(ComponentName.of(name)))::accept)))
			.action(self(stateOf(ModelState.Finalized)).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val component = ModelNodeUtils.get(entity, DefaultUiTestXCTestTestSuiteComponent.class);
				component.finalizeExtension(project);
				component.getVariantCollection().whenElementKnown(knownVariant -> {
					registry.register(ModelRegistration.builder()
						.withComponent(path.child(knownVariant.getIdentifier().getUnambiguousName()))
						.withComponent(knownVariant.getIdentifier())
						.withComponent(IsVariant.tag())
						.withComponent(createdUsing(of(DefaultXCTestTestSuiteVariant.class), () -> knownVariant.map(noOpTransformer()).get()))
						.build());
				});
				component.getVariantCollection().realize(); // Force realization, for now
			})))

			;
	}

	private static DomainObjectFactory<DefaultUiTestXCTestTestSuiteComponent> newUiTestFactory(Project project) {
		return identifier -> {
			return new DefaultUiTestXCTestTestSuiteComponent((ComponentIdentifier<?>)identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class));
		};
	}
}
