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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import dev.nokee.language.nativebase.internal.HasRuntimeElementsDependencyBucket;
import dev.nokee.language.objectivec.internal.plugins.SupportObjectiveCSourceSetTag;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.capabilities.variants.LinkedVariantsComponent;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ModelRegistrationFactory;
import dev.nokee.model.internal.core.NodeRegistrationFactoryRegistry;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasDevelopmentBinary;
import dev.nokee.platform.base.internal.BuildVariantComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.ios.ObjectiveCIosApplication;
import dev.nokee.platform.ios.internal.IosApplicationOutgoingDependencies;
import dev.nokee.platform.ios.internal.rules.IosDevelopmentBinaryConvention;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.NativeVariantTag;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependenciesComponent;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.IsTestComponent;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.xctest.internal.BaseXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultUiTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultXCTestTestSuiteVariant;
import dev.nokee.testing.xctest.internal.XCTestTestSuiteComponentTag;
import dev.nokee.utils.TextCaseUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Collections;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.DomainObjectEntities.tagsOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;

public class ObjectiveCXCTestTestSuitePlugin implements Plugin<Project> {
	private final ObjectFactory objects;

	@Inject
	public ObjectiveCXCTestTestSuitePlugin(ObjectFactory objectFactory) {
		this.objects = objectFactory;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(TestingBasePlugin.class);

		variants(project).withType(DefaultXCTestTestSuiteVariant.class).configureEach(variant -> {
			variant.getDevelopmentBinary().convention(variant.getBinaries().getElements().flatMap(IosDevelopmentBinaryConvention.INSTANCE));
		});

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeVariantTag.class), ModelComponentReference.of(ParentComponent.class), (entity, identifier, tag, parent) -> {
			if (!parent.get().hasComponent(typeOf(XCTestTestSuiteComponentTag.class))) {
				return;
			}

			val outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new IosApplicationOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(entity), HasRuntimeElementsDependencyBucket.class).getRuntimeElements().getAsConfiguration(), project.getObjects())));
			entity.addComponent(new VariantComponentDependencies<NativeComponentDependencies>(() -> (NativeComponentDependencies) ModelNodeUtils.get(entity, DependencyAwareComponent.class).getDependencies(), outgoing.get()));
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelTags.referenceOf(XCTestTestSuiteComponentTag.class), ModelComponentReference.of(LinkedVariantsComponent.class), (entity, path, tag, variants) -> {
			val component = ModelNodeUtils.get(entity, BaseXCTestTestSuiteComponent.class);

			Streams.zip(component.getBuildVariants().get().stream(), Streams.stream(variants), (buildVariant, variant) -> {
				val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();

				xcTestTestSuiteVariant(variantIdentifier, component, project).getComponents().forEach(variant::addComponent);
				variant.addComponent(new BuildVariantComponent(buildVariant));
				ModelStates.register(variant);

				onEachVariantDependencies(variant, variant.getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)), project.getProviders());
				return null;
			}).forEach(it -> {});

			component.finalizeExtension(project);
			component.getDevelopmentVariant().convention(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> component.getVariants().get())));

			component.getVariants().get(); // Force realization, for now
		}));

		components(project).withType(BaseXCTestTestSuiteComponent.class).configureEach(component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.BUNDLE));
		});
		components(project).withType(BaseXCTestTestSuiteComponent.class).configureEach(component -> {
			component.getTargetBuildTypes().convention(ImmutableSet.of(TargetBuildTypes.DEFAULT));
		});
		components(project).withType(BaseXCTestTestSuiteComponent.class).configureEach(component -> {
			component.getTargetMachines().convention(ImmutableSet.of(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64()));
		});

		project.getPluginManager().withPlugin("dev.nokee.objective-c-ios-application", appliedPlugin -> {
			BaseNativeComponent<?> application = ModelNodeUtils.get(ModelNodes.of(project.getExtensions().getByType(ObjectiveCIosApplication.class)), BaseNativeComponent.class);
			val testSuites = project.getExtensions().getByType(TestSuiteContainer.class);
			val registry = ModelNodeUtils.get(ModelNodes.of(testSuites), NodeRegistrationFactoryRegistry.class);
			registry.registerFactory(of(DefaultUnitTestXCTestTestSuiteComponent.class), (ModelRegistrationFactory) name -> unitTestXCTestTestSuite(name, project));
			registry.registerFactory(of(DefaultUiTestXCTestTestSuiteComponent.class), (ModelRegistrationFactory) name -> uiTestXCTestTestSuite(name, project));

			val unitTestComponentProvider = testSuites.register("unitTest", DefaultUnitTestXCTestTestSuiteComponent.class, component -> {
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.getBaseName().set(TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
				component.getModuleName().set(TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
				component.getProductBundleIdentifier().set(project.getGroup().toString() + "." + TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
			});
			val unitTestComponent = unitTestComponentProvider.get();
			project.afterEvaluate(finalizeModelNodeOf(unitTestComponent));

			val uiTestComponentProvider = testSuites.register("uiTest", DefaultUiTestXCTestTestSuiteComponent.class, component -> {
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.getBaseName().set(TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
				component.getModuleName().set(TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
				component.getProductBundleIdentifier().set(project.getGroup().toString() + "." + TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
			});
			val uiTestComponent = uiTestComponentProvider.get();
			project.afterEvaluate(finalizeModelNodeOf(uiTestComponent));
		});
	}

	public static ModelRegistration unitTestXCTestTestSuite(String name, Project project) {
		val identifier = ModelObjectIdentifier.builder().name(ElementName.of(name)).withParent(ProjectIdentifier.of(project)).build();
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(createdUsing(of(DefaultUnitTestXCTestTestSuiteComponent.class), () -> {
				return newUnitTestFactory(project).create(identifier);
			}))
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(IsTestComponent.class)
			.withComponentTag(XCTestTestSuiteComponentTag.class)
			.withComponentTag(SupportObjectiveCSourceSetTag.class)
			.mergeFrom(tagsOf(DefaultUnitTestXCTestTestSuiteComponent.class))
			.build()
			;
	}

	private static DomainObjectFactory<DefaultUnitTestXCTestTestSuiteComponent> newUnitTestFactory(Project project) {
		return identifier -> {
			return project.getObjects().newInstance(DefaultUnitTestXCTestTestSuiteComponent.class, project.getExtensions().getByType(ModelRegistry.class), model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)));
		};
	}

	public static ModelRegistration uiTestXCTestTestSuite(String name, Project project) {
		val identifier = ModelObjectIdentifier.builder().name(ElementName.of(name)).withParent(ProjectIdentifier.of(project)).build();
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(IsTestComponent.class)
			.withComponentTag(XCTestTestSuiteComponentTag.class)
			.withComponentTag(SupportObjectiveCSourceSetTag.class)
			.withComponent(createdUsing(of(DefaultUiTestXCTestTestSuiteComponent.class), () -> {
				return newUiTestFactory(project).create(identifier);
			}))
			.mergeFrom(tagsOf(DefaultUiTestXCTestTestSuiteComponent.class))
			.build()
			;
	}

	private static DomainObjectFactory<DefaultUiTestXCTestTestSuiteComponent> newUiTestFactory(Project project) {
		return identifier -> {
			return project.getObjects().newInstance(DefaultUiTestXCTestTestSuiteComponent.class, project.getExtensions().getByType(ModelRegistry.class), model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)));
		};
	}

	private static ModelRegistration xcTestTestSuiteVariant(VariantIdentifier identifier, BaseXCTestTestSuiteComponent component, Project project) {
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(NativeVariantTag.class)
			.mergeFrom(tagsOf(DefaultXCTestTestSuiteVariant.class))
			.withComponent(createdUsing(of(DefaultXCTestTestSuiteVariant.class), () -> {
				return project.getObjects().newInstance(DefaultXCTestTestSuiteVariant.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)));
			}))
			.build()
			;
	}

	private static void onEachVariantDependencies(ModelNode variant, VariantComponentDependencies<?> dependencies, ProviderFactory providers) {
		dependencies.getOutgoing().getExportedBinary().convention(providers.provider(() -> ModelNodeUtils.get(ModelStates.finalize(variant), HasDevelopmentBinary.class).getDevelopmentBinary()).flatMap(it -> it));
	}
}
