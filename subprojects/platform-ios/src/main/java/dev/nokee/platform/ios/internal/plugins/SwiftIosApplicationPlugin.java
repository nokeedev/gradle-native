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
package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.language.base.internal.SourceAwareComponentMixIn;
import dev.nokee.language.swift.internal.plugins.SupportSwiftSourceSetTag;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.language.swift.internal.plugins.SwiftSourcesMixIn;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.DependentComponentSpec;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.SwiftIosApplication;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.NativeComponentSpec;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import dev.nokee.utils.TextCaseUtils;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import javax.inject.Inject;

import static dev.nokee.model.internal.names.ElementName.ofMain;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;

public class SwiftIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class);

		// Create the component
		project.getPluginManager().apply(IosComponentBasePlugin.class);
		project.getPluginManager().apply(SwiftLanguageBasePlugin.class);
		project.getPluginManager().apply(IosResourcePlugin.class);

		model(project, factoryRegistryOf(Component.class)).registerFactory(DefaultSwiftIosApplication.class);

		final NamedDomainObjectProvider<DefaultSwiftIosApplication> componentProvider = model(project, registryOf(Component.class)).register(ProjectIdentifier.of(project).child(ofMain()), DefaultSwiftIosApplication.class).asProvider();
		componentProvider.configure(baseName(convention(TextCaseUtils.toCamelCase(project.getName()))));
		componentProvider.configure(it -> {
//			assert it instanceof DefaultIosApplicationComponent;
//			((DefaultIosApplicationComponent) it).getGroupId().set(GroupId.of(project::getGroup));
			throw new UnsupportedOperationException("fix me");
		});
		project.getExtensions().add(SwiftIosApplication.class, EXTENSION_NAME, componentProvider.get());

		// TODO: This should be solve in a better way
		project.getTasks().withType(CreateIosApplicationBundleTask.class).configureEach(task -> {
			task.getSwiftSupportRequired().set(true);
		});
	}

	public static /*final*/ abstract class DefaultSwiftIosApplication extends ModelElementSupport implements SwiftIosApplication
		, NativeComponentSpec
		, ExtensionAwareMixIn
		, DependentComponentSpec<NativeComponentDependencies>
		, VariantAwareComponentMixIn<IosApplication>
		, SourceAwareComponentMixIn
		, BinaryAwareComponentMixIn
		, TaskAwareComponentMixIn
		, AssembleTaskMixIn
		, HasDevelopmentVariant<IosApplication>
		, SwiftSourcesMixIn
	{
		@Inject
		public DefaultSwiftIosApplication() {
			getExtensions().create("$swiftSupport", SupportSwiftSourceSetTag.class);
		}

		@Override
		@NestedObject
		public abstract DefaultNativeComponentDependencies getDependencies();

		@Override
		protected String getTypeName() {
			return "Swift iOS application";
		}
	}
}
