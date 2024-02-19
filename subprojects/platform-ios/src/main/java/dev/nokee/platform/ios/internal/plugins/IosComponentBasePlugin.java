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

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
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

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class IosComponentBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NativeComponentBasePlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(IosResourceSetSpec.class);
		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultIosApplicationVariant.class);

		model(project, mapOf(Component.class)).configureEach(DefaultIosApplicationComponent.class, component -> {
			component.getVariants().configureEach(DefaultIosApplicationVariant.class, variant -> {
				variant.getProductBundleIdentifier().convention(component.getGroupId().map(it -> it + "." + component.getModuleName().get()));
			});
		});

		model(project, mapOf(Variant.class)).configureEach(DefaultIosApplicationVariant.class, variant -> {
			variant.getDevelopmentBinary().convention(variant.getBinaries().getElements().flatMap(IosDevelopmentBinaryConvention.INSTANCE));
		});
		model(project, mapOf(Variant.class)).configureEach(DefaultIosApplicationVariant.class, variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) variant.getDevelopmentBinary()::get));
			}
		});
		model(project, mapOf(Variant.class)).configureEach(DefaultIosApplicationVariant.class, variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getObjectsTask().configure(configureDependsOn(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)));
			}
		});

		model(project, mapOf(Component.class)).configureEach(DefaultIosApplicationComponent.class, component -> {
			model(project, registryOf(LanguageSourceSet.class)).register(component.getIdentifier().child("resources"), IosResourceSetSpec.class).configure(sourceSet -> sourceSet.from("src/" + component.getName() + "/resources"));
		});
		project.afterEvaluate(__ -> {
			model(project, mapOf(Component.class)).configureEach(DefaultIosApplicationComponent.class, component -> {
				for (BuildVariant it : component.getBuildVariants().get()) {
					final BuildVariantInternal buildVariant = (BuildVariantInternal) it;
					final VariantIdentifier variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					model(project, registryOf(Variant.class)).register(variantIdentifier, DefaultIosApplicationVariant.class);
				}

				component.finalizeValue();
			});
		});

		model(project, mapOf(Component.class)).configureEach(ObjectiveCIosApplicationPlugin.DefaultObjectiveCIosApplication.class, component -> {
			component.getTargetMachines().convention(Collections.singletonList(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64()));
		});
		model(project, mapOf(Component.class)).configureEach(SwiftIosApplicationPlugin.DefaultSwiftIosApplication.class, component -> {
			component.getTargetMachines().convention(Collections.singletonList(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64()));
		});
		model(project, mapOf(Component.class)).configureEach(ObjectiveCIosApplicationPlugin.DefaultObjectiveCIosApplication.class, component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		});
		model(project, mapOf(Component.class)).configureEach(SwiftIosApplicationPlugin.DefaultSwiftIosApplication.class, component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		});
		model(project, mapOf(Component.class)).configureEach(ObjectiveCIosApplicationPlugin.DefaultObjectiveCIosApplication.class, component -> {
			component.getTargetBuildTypes().convention(Collections.singletonList(TargetBuildTypes.named("Default")));
		});
		model(project, mapOf(Component.class)).configureEach(SwiftIosApplicationPlugin.DefaultSwiftIosApplication.class, component -> {
			component.getTargetBuildTypes().convention(Collections.singletonList(TargetBuildTypes.named("Default")));
		});
		model(project, mapOf(Variant.class)).configureEach(DefaultIosApplicationVariant.class, variant -> {
			final IosApplicationOutgoingDependencies outgoing = new IosApplicationOutgoingDependencies(variant.getRuntimeElements().getAsConfiguration(), project.getObjects());
			outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
		});
	}
}
