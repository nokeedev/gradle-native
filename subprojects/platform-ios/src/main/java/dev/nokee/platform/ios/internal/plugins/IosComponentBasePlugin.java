/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultIosApplicationVariant;
import dev.nokee.platform.ios.internal.IosApplicationOutgoingDependencies;
import dev.nokee.platform.ios.internal.IosResourceSetSpec;
import dev.nokee.platform.ios.internal.rules.IosDevelopmentBinaryConvention;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.internal.rules.ToBinariesCompileTasksTransformer;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class IosComponentBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NativeComponentBasePlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(IosResourceSetSpec.class, name -> {
			return project.getObjects().newInstance(IosResourceSetSpec.class);
		});
		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultIosApplicationVariant.class, name -> {
			return project.getObjects().newInstance(DefaultIosApplicationVariant.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), (Factory<ComponentSources>) () -> project.getObjects().newInstance(ComponentSources.class));
		});

		components(project).withType(DefaultIosApplicationComponent.class).configureEach(component -> {
			component.getVariants().configureEach(DefaultIosApplicationVariant.class, variant -> {
				variant.getProductBundleIdentifier().convention(component.getGroupId().map(it -> it + "." + component.getModuleName().get()));
			});
		});

		variants(project).withType(DefaultIosApplicationVariant.class).configureEach(variant -> {
			variant.getDevelopmentBinary().convention(variant.getBinaries().getElements().flatMap(IosDevelopmentBinaryConvention.INSTANCE));
		});
		variants(project).withType(DefaultIosApplicationVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) variant.getDevelopmentBinary()::get));
			}
		});
		variants(project).withType(DefaultIosApplicationVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getObjectsTask().configure(configureDependsOn(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)));
			}
		});

		components(project).withType(DefaultIosApplicationComponent.class).configureEach(component -> {
			model(project, registryOf(LanguageSourceSet.class)).register(component.getIdentifier().child("resources"), IosResourceSetSpec.class).configure(sourceSet -> sourceSet.from("src/" + component.getName() + "/resources"));
		});
		project.afterEvaluate(__ -> {
			components(project).withType(DefaultIosApplicationComponent.class).configureEach(component -> {
				for (BuildVariant it : component.getBuildVariants().get()) {
					final BuildVariantInternal buildVariant = (BuildVariantInternal) it;
					final VariantIdentifier variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					model(project, registryOf(Variant.class)).register(variantIdentifier, DefaultIosApplicationVariant.class);
				}

				component.finalizeValue();
			});
		});

		components(project).withType(ObjectiveCIosApplicationPlugin.DefaultObjectiveCIosApplication.class).configureEach(component -> {
			component.getTargetMachines().convention(Collections.singletonList(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64()));
		});
		components(project).withType(SwiftIosApplicationPlugin.DefaultSwiftIosApplication.class).configureEach(component -> {
			component.getTargetMachines().convention(Collections.singletonList(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64()));
		});
		components(project).withType(ObjectiveCIosApplicationPlugin.DefaultObjectiveCIosApplication.class).configureEach(component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		});
		components(project).withType(SwiftIosApplicationPlugin.DefaultSwiftIosApplication.class).configureEach(component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		});
		components(project).withType(ObjectiveCIosApplicationPlugin.DefaultObjectiveCIosApplication.class).configureEach(component -> {
			component.getTargetBuildTypes().convention(Collections.singletonList(TargetBuildTypes.named("Default")));
		});
		components(project).withType(SwiftIosApplicationPlugin.DefaultSwiftIosApplication.class).configureEach(component -> {
			component.getTargetBuildTypes().convention(Collections.singletonList(TargetBuildTypes.named("Default")));
		});
		variants(project).withType(DefaultIosApplicationVariant.class).configureEach(variant -> {
			final IosApplicationOutgoingDependencies outgoing = new IosApplicationOutgoingDependencies(variant.getRuntimeElements().getAsConfiguration(), project.getObjects());
			outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
		});
	}
}
