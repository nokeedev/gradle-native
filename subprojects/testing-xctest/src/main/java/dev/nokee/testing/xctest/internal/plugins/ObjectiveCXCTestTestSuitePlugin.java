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
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.VariantIdentifier;
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
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.xctest.internal.BaseXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultUiTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultXCTestTestSuiteVariant;
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
import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
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

		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultXCTestTestSuiteVariant.class, name -> {
			return instantiator(project).newInstance(DefaultXCTestTestSuiteVariant.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}));
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
			model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(DefaultUnitTestXCTestTestSuiteComponent.class, name -> {
				return instantiator(project).newInstance(DefaultUnitTestXCTestTestSuiteComponent.class, model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), model(project, registryOf(Artifact.class)));
			});
			model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(DefaultUiTestXCTestTestSuiteComponent.class, name -> {
				return instantiator(project).newInstance(DefaultUiTestXCTestTestSuiteComponent.class, model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), model(project, registryOf(Artifact.class)));
			});

			BaseNativeComponent<?> application = (BaseNativeComponent<?>) project.getExtensions().getByType(ObjectiveCIosApplication.class);
			val testSuites = testSuites(project);

			val unitTestComponentProvider = testSuites.register("unitTest", DefaultUnitTestXCTestTestSuiteComponent.class, component -> {
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.getBaseName().set(TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
				component.getModuleName().set(TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
				component.getProductBundleIdentifier().set(project.getGroup().toString() + "." + TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
			});
			val unitTestComponent = unitTestComponentProvider.get();

			val uiTestComponentProvider = testSuites.register("uiTest", DefaultUiTestXCTestTestSuiteComponent.class, component -> {
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.getBaseName().set(TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
				component.getModuleName().set(TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
				component.getProductBundleIdentifier().set(project.getGroup().toString() + "." + TextCaseUtils.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().toString()));
			});
			val uiTestComponent = uiTestComponentProvider.get();
		});
	}
}
