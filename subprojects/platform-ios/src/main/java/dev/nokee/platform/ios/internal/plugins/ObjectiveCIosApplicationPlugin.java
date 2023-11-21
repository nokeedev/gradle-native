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

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.nativebase.internal.PrivateHeadersMixIn;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.internal.ObjectiveCSourcesMixIn;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.language.objectivec.internal.plugins.SupportObjectiveCSourceSetTag;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.ObjectiveCIosApplication;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.TargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.TargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.utils.TextCaseUtils;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.reflect.TypeOf;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain;

import java.util.Arrays;

import static dev.nokee.language.nativebase.internal.NativePlatformFactory.platformNameFor;
import static dev.nokee.model.internal.names.ElementName.ofMain;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;

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

		model(project, factoryRegistryOf(Component.class)).registerFactory(DefaultObjectiveCIosApplication.class, name -> {
			return project.getObjects().newInstance(DefaultObjectiveCIosApplication.class, model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}));
		});

		final NamedDomainObjectProvider<DefaultObjectiveCIosApplication> componentProvider = model(project, registryOf(Component.class)).register(ProjectIdentifier.of(project).child(ofMain()), DefaultObjectiveCIosApplication.class).asProvider();
		componentProvider.configure(baseName(convention(TextCaseUtils.toCamelCase(project.getName()))));
		componentProvider.configure(it -> {
//			assert it instanceof DefaultIosApplicationComponent;
//			((DefaultIosApplicationComponent) it).getGroupId().set(GroupId.of(project::getGroup));
			throw new UnsupportedOperationException("fix me");
		});
		project.getExtensions().add(ObjectiveCIosApplication.class, EXTENSION_NAME, componentProvider.get());
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

	public static /*final*/ abstract class DefaultObjectiveCIosApplication extends ModelElementSupport implements ObjectiveCIosApplication
		, ExtensionAwareMixIn
		, DependencyAwareComponentMixIn<NativeComponentDependencies, DefaultNativeComponentDependencies>
		, VariantAwareComponentMixIn<IosApplication>
		, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
		, BinaryAwareComponentMixIn
		, TaskAwareComponentMixIn
		, AssembleTaskMixIn
		, TargetMachineAwareComponentMixIn
		, TargetLinkageAwareComponentMixIn
		, TargetBuildTypeAwareComponentMixIn
		, HasDevelopmentVariant<IosApplication>
		, ObjectiveCSourcesMixIn
		, PrivateHeadersMixIn
	{
		public DefaultObjectiveCIosApplication(ModelObjectRegistry<Task> taskRegistry, Factory<SourceView<LanguageSourceSet>> sourcesFactory) {
			getExtensions().add("sources", sourcesFactory.create());
			getExtensions().create("$objectiveCSupport", SupportObjectiveCSourceSetTag.class);
		}

		@Override
		protected String getTypeName() {
			return "Objective-C iOS application";
		}
	}
}
