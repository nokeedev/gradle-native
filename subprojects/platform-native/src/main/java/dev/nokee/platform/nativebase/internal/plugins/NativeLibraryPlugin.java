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
package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.IsVariant;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.tasks.ModelBackedTaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibraryExtension;
import dev.nokee.platform.nativebase.NativeLibrarySources;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryVariant;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.NativeLibraryComponentModelRegistrationFactory;
import dev.nokee.platform.nativebase.internal.NativeLibrarySourcesAdapter;
import dev.nokee.platform.nativebase.internal.NativeVariantTag;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.DomainObjectEntities.entityOf;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public class NativeLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public NativeLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(nativeLibrary("main", project)).as(NativeLibraryExtension.class);
		componentProvider.configure(baseName(convention(project.getName())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(NativeLibraryExtension.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration nativeLibrary(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("native library").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		return new NativeLibraryComponentModelRegistrationFactory(DefaultNativeLibraryExtension.class, project).create(identifier).build();
	}

	public static abstract class DefaultNativeLibraryExtension implements NativeLibraryExtension
		, ModelBackedDependencyAwareComponentMixIn<NativeLibraryComponentDependencies, ModelBackedNativeLibraryComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<NativeLibrary>
		, ModelBackedSourceAwareComponentMixIn<NativeLibrarySources, NativeLibrarySourcesAdapter>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
		, ModelBackedTargetLinkageAwareComponentMixIn
		, ModelBackedHasBaseNameMixIn
		, ModelBackedNamedMixIn
		, ModelBackedHasAssembleTaskMixIn
	{
	}

	public static ModelRegistration nativeLibraryVariant(VariantIdentifier identifier, DefaultNativeLibraryComponent component, Project project) {
		return ModelRegistration.builder()
			.withComponent(tag(IsVariant.class))
			.withComponent(tag(ConfigurableTag.class))
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(tag(NativeVariantTag.class))
			.mergeFrom(entityOf(DefaultNativeLibraryVariant.class))
			.withComponent(createdUsing(of(DefaultNativeLibraryVariant.class), () -> {
				val taskRegistry = ModelBackedTaskRegistry.newInstance(project);
				val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier));

				val result = project.getObjects().newInstance(DefaultNativeLibraryVariant.class, identifier, project.getObjects(), project.getProviders(), assembleTask);
				result.getDevelopmentBinary().convention(result.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(result.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
				return result;
			}))
			.build()
			;
	}
}
