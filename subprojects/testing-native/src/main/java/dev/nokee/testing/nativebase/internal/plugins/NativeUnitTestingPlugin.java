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
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
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
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteComponent;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteVariant;
import dev.nokee.utils.ProviderUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.testing.base.internal.plugins.TestingBasePlugin.testSuites;
import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class NativeUnitTestingPlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(TestingBasePlugin.class);

		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultNativeTestSuiteVariant.class, name -> {
			return project.getObjects().newInstance(DefaultNativeTestSuiteVariant.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}));
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

		model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(DefaultNativeTestSuiteComponent.class, name -> {
			return project.getObjects().newInstance(DefaultNativeTestSuiteComponent.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}));
		});

		variants(project).withType(DefaultNativeTestSuiteVariant.class).configureEach(variant -> {
			final NativeApplicationOutgoingDependencies outgoing = new NativeApplicationOutgoingDependencies(variant.getRuntimeElements().getAsConfiguration(), project.getObjects());
			outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
		});
		project.afterEvaluate(__ -> {
			testSuites(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
				for (BuildVariant it : component.getBuildVariants().get()) {
					final BuildVariantInternal buildVariant = (BuildVariantInternal) it;
					final VariantIdentifier variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					model(project, registryOf(Variant.class)).register(variantIdentifier, DefaultNativeTestSuiteVariant.class);
				}

				component.finalizeExtension(project);
				component.getDevelopmentVariant().convention((Provider<? extends DefaultNativeTestSuiteVariant>) project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) component.getVariants().map(VariantInternal.class::cast).get())));
			});
		});
		testSuites(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		});
		testSuites(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
			component.getTargetBuildTypes().convention(component.getTestedComponent()
				.flatMap(it -> {
					if (it instanceof TargetBuildTypeAwareComponent) {
						return ((TargetBuildTypeAwareComponent) it).getTargetBuildTypes();
					} else {
						return ProviderUtils.notDefined();
					}
				}).orElse(ImmutableSet.of(TargetBuildTypes.DEFAULT)));
		});
		testSuites(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
			component.getTargetMachines().convention(component.getTestedComponent()
				.flatMap(it -> {
					if (it instanceof TargetMachineAwareComponent) {
						return ((TargetMachineAwareComponent) it).getTargetMachines();
					} else {
						return ProviderUtils.notDefined();
					}
				}).orElse(ImmutableSet.of(TargetMachines.host())));
		});
		testSuites(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(testSuite -> {
			if (project.getPlugins().hasPlugin(CLanguageBasePlugin.class)) {
				testSuite.getExtensions().create("$cSupport", SupportCSourceSetTag.class);
			} else if (project.getPlugins().hasPlugin(CppLanguageBasePlugin.class)) {
				testSuite.getExtensions().create("$cppSupport", SupportCppSourceSetTag.class);
			} else if (project.getPlugins().hasPlugin(ObjectiveCLanguageBasePlugin.class)) {
				testSuite.getExtensions().create("$objectiveCSupport", SupportObjectiveCSourceSetTag.class);
			} else if (project.getPlugins().hasPlugin(ObjectiveCppLanguageBasePlugin.class)) {
				testSuite.getExtensions().create("$objectiveCppSupport", SupportObjectiveCppSourceSetTag.class);
			} else if (project.getPlugins().hasPlugin(SwiftLanguageBasePlugin.class)) {
				testSuite.getExtensions().create("$swiftSupport", SupportSwiftSourceSetTag.class);
			}
		});

		project.afterEvaluate(proj -> {
			// TODO: We delay as late as possible to "fake" a finalize action.
			testSuites(proj).withType(DefaultNativeTestSuiteComponent.class).configureEach(it -> {
				it.finalizeExtension(proj);
			});
		});
	}
}
