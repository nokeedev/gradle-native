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
package dev.nokee.platform.objectivecpp.internal.plugins;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.nativebase.internal.HasPublicHeadersMixIn;
import dev.nokee.language.nativebase.internal.PrivateHeadersMixIn;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourcesMixIn;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import dev.nokee.language.objectivecpp.internal.plugins.SupportObjectiveCppSourceSetTag;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelObjectFactory;
import dev.nokee.platform.base.internal.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.developmentvariant.HasDevelopmentVariantMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.NativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.NativeLibraryComponentModelRegistrationFactory;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibrary;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;

public class ObjectiveCppLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCppLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCppLanguageBasePlugin.class);

		model(project, factoryRegistryOf(Component.class)).registerFactory(DefaultObjectiveCppLibrary.class, new ModelObjectFactory<DefaultObjectiveCppLibrary>(project, IsComponent.class) {
			@Override
			protected DefaultObjectiveCppLibrary doCreate(String name) {
				return project.getObjects().newInstance(DefaultObjectiveCppLibrary.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<TaskView<Task>>>() {}));
			}
		});

		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(objectiveCppLibrary("main", project)).as(ObjectiveCppLibrary.class);
		componentProvider.configure(baseName(convention(project.getName())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(ObjectiveCppLibrary.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration objectiveCppLibrary(String name, Project project) {
		val identifier = ModelObjectIdentifier.builder().name(name.equals("main") ? ElementName.ofMain() : ElementName.of(name)).withParent(ProjectIdentifier.of(project)).build();
		return new NativeLibraryComponentModelRegistrationFactory(DefaultObjectiveCppLibrary.class, project).create(identifier).withComponentTag(SupportObjectiveCppSourceSetTag.class).build();
	}

	@DomainObjectEntities.Tag(IsComponent.class)
	public static /*final*/ abstract class DefaultObjectiveCppLibrary extends ModelElementSupport implements ObjectiveCppLibrary, ModelNodeAware
		, NativeLibraryComponent
		, ExtensionAwareMixIn
		, DependencyAwareComponentMixIn<NativeLibraryComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<NativeLibrary>
		, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
		, BinaryAwareComponentMixIn
		, TaskAwareComponentMixIn
		, HasDevelopmentVariantMixIn<NativeLibrary>
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
		, ModelBackedTargetLinkageAwareComponentMixIn
		, AssembleTaskMixIn
		, ObjectiveCppSourcesMixIn
		, PrivateHeadersMixIn
		, HasPublicHeadersMixIn
	{
		private final ModelNode entity = ModelNodeContext.getCurrentModelNode();

		@Inject
		public DefaultObjectiveCppLibrary(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry, Factory<BinaryView<Binary>> binariesFactory, Factory<SourceView<LanguageSourceSet>> sourcesFactory, Factory<TaskView<Task>> tasksFactory) {
			getExtensions().create("dependencies", DefaultNativeLibraryComponentDependencies.class, getIdentifier(), bucketRegistry);
			getExtensions().add("assembleTask", taskRegistry.register(getIdentifier().child(TaskName.of("assemble")), Task.class).asProvider());
			getExtensions().add("binaries", binariesFactory.create());
			getExtensions().add("sources", sourcesFactory.create());
			getExtensions().add("tasks", tasksFactory.create());
		}

		@Override
		public DefaultNativeLibraryComponentDependencies getDependencies() {
			return (DefaultNativeLibraryComponentDependencies) DependencyAwareComponentMixIn.super.getDependencies();
		}

		@Override
		public ModelNode getNode() {
			return entity;
		}

		@Override
		public String toString() {
			return "Objective-C++ library '" + getName() + "'";
		}
	}
}
