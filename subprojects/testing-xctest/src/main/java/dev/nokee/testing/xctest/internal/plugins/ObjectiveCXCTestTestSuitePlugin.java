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
import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.objectivec.internal.plugins.SupportObjectiveCSourceSetTag;
import dev.nokee.model.capabilities.variants.IsVariant;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.MainProjectionComponent;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.base.internal.ModelObjectFactory;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantViewFactory;
import dev.nokee.platform.ios.ObjectiveCIosApplication;
import dev.nokee.platform.ios.internal.IosApplicationOutgoingDependencies;
import dev.nokee.platform.ios.internal.rules.IosDevelopmentBinaryConvention;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.internal.rules.ToBinariesCompileTasksTransformer;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.testing.base.TestSuiteComponent;
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
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;
import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.DomainObjectEntities.tagsOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;
import static dev.nokee.testing.base.internal.plugins.TestingBasePlugin.testSuites;
import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class ObjectiveCXCTestTestSuitePlugin implements Plugin<Project> {
	private final ObjectFactory objects;

	@Inject
	public ObjectiveCXCTestTestSuitePlugin(ObjectFactory objectFactory) {
		this.objects = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(TestingBasePlugin.class);

		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultXCTestTestSuiteVariant.class, new ModelObjectFactory<DefaultXCTestTestSuiteVariant>(project, IsVariant.class) {
			@Override
			protected DefaultXCTestTestSuiteVariant doCreate(String name) {
				return project.getObjects().newInstance(DefaultXCTestTestSuiteVariant.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<TaskView<Task>>>() {}));
			}
		});

		variants(project).withType(DefaultXCTestTestSuiteVariant.class).configureEach(variant -> {
			variant.getDevelopmentBinary().convention(variant.getBinaries().getElements().flatMap(IosDevelopmentBinaryConvention.INSTANCE));
		});
		variants(project).withType(DefaultXCTestTestSuiteVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) variant.getDevelopmentBinary()::get));
			}
		});
		variants(project).withType(DefaultXCTestTestSuiteVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getObjectsTask().configure(configureDependsOn(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)));
			}
		});

		variants(project).withType(DefaultXCTestTestSuiteVariant.class).configureEach(variant -> {
			final IosApplicationOutgoingDependencies outgoing = new IosApplicationOutgoingDependencies(variant.getRuntimeElements().getAsConfiguration(), project.getObjects());
			outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
		});
		project.afterEvaluate(__ -> {
			components(project).withType(BaseXCTestTestSuiteComponent.class).configureEach(component -> {
				for (BuildVariant it : component.getBuildVariants().get()) {
					final BuildVariantInternal buildVariant = (BuildVariantInternal) it;
					final VariantIdentifier variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					model(project, registryOf(Variant.class)).register(variantIdentifier, DefaultXCTestTestSuiteVariant.class);
				}

				component.finalizeExtension(project);
				component.getDevelopmentVariant().convention(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> component.getVariants().get())));

				component.getVariants().get(); // Force realization, for now
			});
		});

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
			model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(DefaultUnitTestXCTestTestSuiteComponent.class, new ModelObjectFactory<DefaultUnitTestXCTestTestSuiteComponent>(project, IsTestComponent.class) {
				@Override
				protected DefaultUnitTestXCTestTestSuiteComponent doCreate(String name) {
					return project.getObjects().newInstance(DefaultUnitTestXCTestTestSuiteComponent.class, project.getExtensions().getByType(ModelRegistry.class), model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<TaskView<Task>>>() {}), model(project, registryOf(Artifact.class)), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(new TypeOf<Factory<DefaultVariantDimensions>>() {}));
				}
			});
			model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(DefaultUiTestXCTestTestSuiteComponent.class, new ModelObjectFactory<DefaultUiTestXCTestTestSuiteComponent>(project, IsTestComponent.class) {
				@Override
				protected DefaultUiTestXCTestTestSuiteComponent doCreate(String name) {
					return project.getObjects().newInstance(DefaultUiTestXCTestTestSuiteComponent.class, project.getExtensions().getByType(ModelRegistry.class), model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<TaskView<Task>>>() {}), model(project, registryOf(Artifact.class)), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(new TypeOf<Factory<DefaultVariantDimensions>>() {}));
				}
			});

			BaseNativeComponent<?> application = ModelNodeUtils.get(ModelNodes.of(project.getExtensions().getByType(ObjectiveCIosApplication.class)), BaseNativeComponent.class);
			val testSuites = testSuites(project);

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
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(IsTestComponent.class)
			.withComponentTag(XCTestTestSuiteComponentTag.class)
			.withComponentTag(SupportObjectiveCSourceSetTag.class)
			.mergeFrom(tagsOf(DefaultUnitTestXCTestTestSuiteComponent.class))
			.withComponent(new MainProjectionComponent(DefaultUnitTestXCTestTestSuiteComponent.class))
			.build()
			;
	}

	public static ModelRegistration uiTestXCTestTestSuite(String name, Project project) {
		val identifier = ModelObjectIdentifier.builder().name(ElementName.of(name)).withParent(ProjectIdentifier.of(project)).build();
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(IsTestComponent.class)
			.withComponentTag(XCTestTestSuiteComponentTag.class)
			.withComponentTag(SupportObjectiveCSourceSetTag.class)
			.mergeFrom(tagsOf(DefaultUiTestXCTestTestSuiteComponent.class))
			.withComponent(new MainProjectionComponent(DefaultUiTestXCTestTestSuiteComponent.class))
			.build()
			;
	}
}
