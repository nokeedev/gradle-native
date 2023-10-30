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

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.swift.internal.plugins.HasSwiftSourcesMixIn;
import dev.nokee.language.swift.internal.plugins.SupportSwiftSourceSetTag;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentMixIn;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.developmentvariant.HasDevelopmentVariantMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.SwiftIosApplication;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.IosApplicationComponentModelRegistrationFactory;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeComponentDependencies;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import dev.nokee.utils.TextCaseUtils;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.configureUsingProjection;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;

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

		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(swiftIosApplication("main", project)).as(SwiftIosApplication.class);
		componentProvider.configure(baseName(convention(TextCaseUtils.toCamelCase(project.getName()))));
		componentProvider.configure(configureUsingProjection(DefaultIosApplicationComponent.class, (t, projection) -> projection.getGroupId().set(GroupId.of(project::getGroup))));
		project.getExtensions().add(SwiftIosApplication.class, EXTENSION_NAME, componentProvider.get());

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));


		// TODO: This should be solve in a better way
		project.getTasks().withType(CreateIosApplicationBundleTask.class).configureEach(task -> {
			task.getSwiftSupportRequired().set(true);
		});
	}

	public static ModelRegistration swiftIosApplication(String name, Project project) {
		val identifier = ModelObjectIdentifier.builder().name(name.equals("main") ? ElementName.ofMain() : ElementName.of(name)).withParent(ProjectIdentifier.of(project)).build();
		return new IosApplicationComponentModelRegistrationFactory(DefaultSwiftIosApplication.class, project).create(identifier).withComponentTag(SupportSwiftSourceSetTag.class).build();
	}

	public static abstract class DefaultSwiftIosApplication implements SwiftIosApplication, ModelNodeAware
		, ComponentMixIn
		, ExtensionAwareMixIn
		, ModelBackedDependencyAwareComponentMixIn<NativeComponentDependencies, ModelBackedNativeComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<IosApplication>
		, ModelBackedSourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedHasBaseNameMixIn
		, HasAssembleTaskMixIn
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetLinkageAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
		, HasDevelopmentVariantMixIn<IosApplication>
		, HasSwiftSourcesMixIn
	{
		private final ModelNode entity = ModelNodeContext.getCurrentModelNode();

		@Override
		public ModelNode getNode() {
			return entity;
		}

		@Override
		public String toString() {
			return "Swift iOS application '" + getName() + "'";
		}
	}
}
