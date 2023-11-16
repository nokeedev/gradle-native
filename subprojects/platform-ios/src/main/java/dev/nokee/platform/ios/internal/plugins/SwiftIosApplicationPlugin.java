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

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.swift.internal.plugins.SupportSwiftSourceSetTag;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.language.swift.internal.plugins.SwiftSourcesMixIn;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.base.internal.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantViewFactory;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.SwiftIosApplication;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.IosApplicationComponentModelRegistrationFactory;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.TargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.TargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import dev.nokee.utils.TextCaseUtils;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.reflect.TypeOf;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import javax.inject.Inject;

import static dev.nokee.model.internal.names.ElementName.ofMain;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.configureUsingProjection;

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

		model(project, factoryRegistryOf(Component.class)).registerFactory(DefaultSwiftIosApplication.class, name -> {
			return project.getObjects().newInstance(DefaultSwiftIosApplication.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), project.getExtensions().getByType(new TypeOf<TaskView<Task>>() {}), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(new TypeOf<Factory<DefaultVariantDimensions>>() {}));
		});

		final NamedDomainObjectProvider<DefaultSwiftIosApplication> componentProvider = model(project, registryOf(Component.class)).register(ProjectIdentifier.of(project).child(ofMain()), DefaultSwiftIosApplication.class).asProvider();
		componentProvider.configure(baseName(convention(TextCaseUtils.toCamelCase(project.getName()))));
		componentProvider.configure(configureUsingProjection(DefaultIosApplicationComponent.class, (t, projection) -> projection.getGroupId().set(GroupId.of(project::getGroup))));
		project.getExtensions().add(SwiftIosApplication.class, EXTENSION_NAME, componentProvider.get());

		// TODO: This should be solve in a better way
		project.getTasks().withType(CreateIosApplicationBundleTask.class).configureEach(task -> {
			task.getSwiftSupportRequired().set(true);
		});
	}

	public static ModelRegistration swiftIosApplication(String name, Project project) {
		val identifier = ModelObjectIdentifier.builder().name(name.equals("main") ? ofMain() : ElementName.of(name)).withParent(ProjectIdentifier.of(project)).build();
		return new IosApplicationComponentModelRegistrationFactory(DefaultSwiftIosApplication.class, project).create(identifier).build();
	}

	public static /*final*/ abstract class DefaultSwiftIosApplication extends ModelElementSupport implements SwiftIosApplication
		, ExtensionAwareMixIn
		, DependencyAwareComponentMixIn<NativeComponentDependencies>
		, VariantAwareComponentMixIn<IosApplication>
		, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
		, BinaryAwareComponentMixIn
		, TaskAwareComponentMixIn
		, AssembleTaskMixIn
		, TargetMachineAwareComponentMixIn
		, TargetLinkageAwareComponentMixIn
		, TargetBuildTypeAwareComponentMixIn
		, HasDevelopmentVariant<IosApplication>
		, SwiftSourcesMixIn
	{
		@Inject
		public DefaultSwiftIosApplication(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry, Factory<BinaryView<Binary>> binariesFactory, Factory<SourceView<LanguageSourceSet>> sourcesFactory, Factory<TaskView<Task>> tasksFactory, VariantViewFactory variantsFactory, Factory<DefaultVariantDimensions> dimensionsFactory) {
			getExtensions().create("dependencies", DefaultNativeComponentDependencies.class, getIdentifier(), bucketRegistry);
			getExtensions().add("assembleTask", taskRegistry.register(getIdentifier().child(TaskName.of("assemble")), Task.class).asProvider());
			getExtensions().add("binaries", binariesFactory.create());
			getExtensions().add("sources", sourcesFactory.create());
			getExtensions().add("tasks", tasksFactory.create());
			getExtensions().add("variants", variantsFactory.create(IosApplication.class));
			getExtensions().add("dimensions", dimensionsFactory.create());
			getExtensions().create("$swiftSupport", SupportSwiftSourceSetTag.class);
		}

		@Override
		public String toString() {
			return "Swift iOS application '" + getName() + "'";
		}
	}
}
