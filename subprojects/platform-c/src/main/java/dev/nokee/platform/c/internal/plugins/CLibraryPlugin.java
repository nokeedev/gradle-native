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
package dev.nokee.platform.c.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.c.internal.HasCSourcesMixIn;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.c.internal.plugins.SupportCSourceSetTag;
import dev.nokee.language.nativebase.internal.HasPrivateHeadersMixIn;
import dev.nokee.language.nativebase.internal.HasPublicHeadersMixIn;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
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
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ComponentMixIn;
import dev.nokee.platform.base.internal.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelObjectFactory;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.developmentvariant.HasDevelopmentVariantMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.c.CLibrary;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.NativeLibraryComponentModelRegistrationFactory;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;

public class CLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public CLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CLanguageBasePlugin.class);

		model(project, factoryRegistryOf(Component.class)).registerFactory(DefaultCLibrary.class, new ModelObjectFactory<DefaultCLibrary>(project, IsComponent.class) {
			@Override
			protected DefaultCLibrary doCreate(String name) {
				return project.getObjects().newInstance(DefaultCLibrary.class, model(project, registryOf(DependencyBucket.class)));
			}
		});

		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(cLibrary("main", project)).as(CLibrary.class);
		componentProvider.configure(baseName(convention(project.getName())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(CLibrary.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration cLibrary(String name, Project project) {
		val identifier = ModelObjectIdentifier.builder().name(name.equals("main") ? ElementName.ofMain() : ElementName.of(name)).withParent(ProjectIdentifier.of(project)).build();
		return new NativeLibraryComponentModelRegistrationFactory(DefaultCLibrary.class, project).create(identifier).withComponentTag(SupportCSourceSetTag.class).build();
	}

	public static abstract class DefaultCLibrary extends ModelElementSupport implements CLibrary, ModelNodeAware
		, ComponentMixIn
		, ExtensionAwareMixIn
		, DependencyAwareComponentMixIn<NativeLibraryComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<NativeLibrary>
		, ModelBackedSourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, HasDevelopmentVariantMixIn<NativeLibrary>
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
		, ModelBackedTargetLinkageAwareComponentMixIn
		, ModelBackedHasBaseNameMixIn
		, HasAssembleTaskMixIn
		, HasPrivateHeadersMixIn
		, HasPublicHeadersMixIn
		, HasCSourcesMixIn
	{
		private final ModelNode entity = ModelNodeContext.getCurrentModelNode();

		@Inject
		public DefaultCLibrary(ModelObjectRegistry<DependencyBucket> bucketRegistry) {
			getExtensions().create("dependencies", DefaultNativeLibraryComponentDependencies.class, getIdentifier(), bucketRegistry);
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
			return "C library '" + getName() + "'";
		}
	}
}
