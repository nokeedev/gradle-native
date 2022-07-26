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

import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetTag;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.developmentvariant.HasDevelopmentVariantMixIn;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.ObjectiveCIosApplication;
import dev.nokee.platform.ios.ObjectiveCIosApplicationSources;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.IosApplicationComponentModelRegistrationFactory;
import dev.nokee.platform.ios.internal.ObjectiveCIosApplicationSourcesAdapter;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeComponentDependencies;
import dev.nokee.platform.objectivec.HasObjectiveCSourceSet;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain;
import org.gradle.util.GUtil;

import java.util.Arrays;

import static dev.nokee.language.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.platformNameFor;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.configureUsingProjection;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

public class ObjectiveCIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
		project.getPluginManager().apply(ToolChainMetadataRules.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class);

		// Create the component
		project.getPluginManager().apply(IosComponentBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);
		project.getPluginManager().apply(IosResourcePlugin.class);

		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(objectiveCIosApplication("main", project)).as(ObjectiveCIosApplication.class);
		componentProvider.configure(baseName(convention(GUtil.toCamelCase(project.getName()))));
		componentProvider.configure(configureUsingProjection(DefaultIosApplicationComponent.class, (t, projection) -> projection.getGroupId().set(GroupId.of(project::getGroup))));
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

	public static ModelRegistration objectiveCIosApplication(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("Objective-C iOS application").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		return new IosApplicationComponentModelRegistrationFactory(DefaultObjectiveCIosApplication.class, project).create(identifier).withComponent(tag(ObjectiveCSourceSetTag.class)).build();
	}

	public static abstract class DefaultObjectiveCIosApplication implements ObjectiveCIosApplication
		, ModelBackedDependencyAwareComponentMixIn<NativeComponentDependencies, ModelBackedNativeComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<IosApplication>
		, ModelBackedSourceAwareComponentMixIn<ObjectiveCIosApplicationSources, ObjectiveCIosApplicationSourcesAdapter>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedHasBaseNameMixIn
		, ModelBackedNamedMixIn
		, HasAssembleTaskMixIn
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetLinkageAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
		, HasDevelopmentVariantMixIn<IosApplication>
	{
		@Override
		public ObjectiveCSourceSet getObjectiveCSources() {
			return ((HasObjectiveCSourceSet) sourceViewOf(this)).getObjectiveC().get();
		}

		@Override
		public void objectiveCSources(Action<? super ObjectiveCSourceSet> action) {
			((HasObjectiveCSourceSet) sourceViewOf(this)).getObjectiveC().configure(action);
		}

		@Override
		public void objectiveCSources(@SuppressWarnings("rawtypes") Closure closure) {
			objectiveCSources(configureUsing(closure));
		}

		@Override
		public NativeHeaderSet getPrivateHeaders() {
			return ((HasHeadersSourceSet) sourceViewOf(this)).getHeaders().get();
		}

		@Override
		public void privateHeaders(Action<? super NativeHeaderSet> action) {
			((HasHeadersSourceSet) sourceViewOf(this)).getHeaders().configure(action);
		}

		@Override
		public void privateHeaders(@SuppressWarnings("rawtypes") Closure closure) {
			privateHeaders(configureUsing(closure));
		}
	}
}
