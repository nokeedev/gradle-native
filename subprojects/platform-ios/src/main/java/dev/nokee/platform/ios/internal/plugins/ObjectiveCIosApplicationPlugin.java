/*
 * Copyright 2020-2021 the original author or authors.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.model.internal.BaseDomainObjectViewProjection;
import dev.nokee.model.internal.BaseNamedDomainObjectViewProjection;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.ObjectiveCIosApplication;
import dev.nokee.platform.ios.ObjectiveCIosApplicationSources;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.IosApplicationComponentModelRegistrationFactory;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain;
import org.gradle.util.GUtil;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodes.mutate;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.platform.nativebase.internal.NativePlatformFactory.platformNameFor;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;

public class ObjectiveCIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
		project.getPluginManager().apply(ToolChainMetadataRules.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class);

		// Create the component
		project.getPluginManager().apply(ComponentModelBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);

		val components = project.getExtensions().getByType(ComponentContainer.class);
		val registry = ModelNodeUtils.get(ModelNodes.of(components), NodeRegistrationFactoryRegistry.class);
		registry.registerFactory(of(ObjectiveCIosApplication.class), name -> objectiveCIosApplication(name, project));
		val componentProvider = components.register("main", ObjectiveCIosApplication.class, configureUsingProjection(DefaultIosApplicationComponent.class, baseNameConvention(GUtil.toCamelCase(project.getName())).andThen((t, projection) -> ((DefaultIosApplicationComponent) projection).getGroupId().set(GroupId.of(project::getGroup))).andThen(configureBuildVariants())));
		project.getExtensions().add(ObjectiveCIosApplication.class, EXTENSION_NAME, componentProvider.get());

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));
	}

	public static class ToolChainMetadataRules extends RuleSource {
		@Mutate
		public void configureToolchain(NativeToolChainRegistry toolchains) {
			toolchains.withType(Clang.class, toolchain -> {
				toolchain.target(platformNameFor(OperatingSystemFamily.forName(OperatingSystemFamily.IOS), MachineArchitecture.forName(MachineArchitecture.X86_64)), platform -> {
					// Although this should be correct, clearing the args to remove the -m64 (which is not technically, exactly, required in this instance) and adding the target with the correct sysroot...
					// Gradle forcefully append the macOS SDK sysroot to the configured args.
					// The sysroot used is the macOS not the iPhoneSimulator.
					// To solve this, we can reprobe the compiler right before the task executes.
					((DefaultGccPlatformToolChain) platform).getCompilerProbeArgs().clear();
					((DefaultGccPlatformToolChain) platform).getCompilerProbeArgs().addAll(Arrays.asList("-target", "x86_64-apple-ios13.2-simulator", "-isysroot", getSdkPath()));
				});
			});
		}
	}

	public static <T, PROJECTION extends BaseComponent<?>> BiConsumer<T, PROJECTION> configureBuildVariants() {
		return (t, projection) -> {
			projection.getBuildVariants().set(projection.getFinalSpace().map(DefaultBuildVariant::fromSpace));
			projection.getBuildVariants().finalizeValueOnRead();
			projection.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.
		};
	}

	public static NodeRegistration objectiveCIosApplication(String name, Project project) {
		return new IosApplicationComponentModelRegistrationFactory(ObjectiveCIosApplication.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

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

			// TODO: Should be created using IosResourceSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("resources"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(IosResourceSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			// TODO: Should be created as ModelProperty (readonly) with CApplicationSources projection
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("sources"))
				.withComponent(IsModelProperty.tag())
				.withComponent(managed(of(ObjectiveCIosApplicationSources.class)))
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
		})
			.create(name)
			.action(allDirectDescendants(mutate(of(ObjectiveCSourceSet.class)))
				.apply(executeUsingProjection(of(ObjectiveCSourceSet.class), withConventionOf(maven(ComponentName.of(name)), defaultObjectiveCGradle(ComponentName.of(name)))::accept)))
			;
	}
}
