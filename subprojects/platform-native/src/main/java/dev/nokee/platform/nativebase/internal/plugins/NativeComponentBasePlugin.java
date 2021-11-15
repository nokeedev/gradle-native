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

import dev.nokee.internal.Factory;
import dev.nokee.language.nativebase.internal.DefaultNativeToolChainSelector;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;

import java.util.function.BiConsumer;

public class NativeComponentBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NativeRuntimePlugin.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class); // for now, later we will be more smart
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		project.getExtensions().add("__nokee_sharedLibraryFactory", new SharedLibraryBinaryRegistrationFactory(
			new LinkLibrariesConfigurationRegistrationActionFactory(
				() -> project.getExtensions().getByType(ModelRegistry.class),
				() -> project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class),
				() -> project.getObjects()
			),
			new RuntimeLibrariesConfigurationRegistrationActionFactory(
				() -> project.getExtensions().getByType(ModelRegistry.class),
				() -> project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class),
				() -> project.getObjects()
			),
			new NativeLinkTaskRegistrationActionFactory(
				() -> project.getExtensions().getByType(ModelRegistry.class),
				() -> project.getExtensions().getByType(TaskRegistrationFactory.class),
				() -> project.getExtensions().getByType(ModelPropertyRegistrationFactory.class),
				() -> new DefaultNativeToolChainSelector(((ProjectInternal) project).getModelRegistry(), project.getProviders())
			),
			new BaseNamePropertyRegistrationActionFactory(
				() -> project.getExtensions().getByType(ModelRegistry.class),
				() -> project.getExtensions().getByType(ModelPropertyRegistrationFactory.class)
			),
			new RegisterCompileTasksPropertyActionFactory(
				() -> project.getExtensions().getByType(ModelRegistry.class),
				() -> project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class)
			),
			new AttachAttributesToConfigurationRuleFactory(
				() -> project.getObjects()
			)
		));
	}

	public static Factory<DefaultNativeApplicationComponent> nativeApplicationProjection(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of(project));
		return () -> new DefaultNativeApplicationComponent(identifier, project.getObjects(), project.getTasks(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class));
	}

	public static Factory<DefaultNativeLibraryComponent> nativeLibraryProjection(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of(project));
		return () -> new DefaultNativeLibraryComponent(identifier, project.getObjects(), project.getTasks(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class));
	}

	public static <T extends Component, PROJECTION> Action<T> configureUsingProjection(Class<PROJECTION> type, BiConsumer<? super T, ? super PROJECTION> action) {
		return t -> action.accept(t, ModelNodeUtils.get(ModelNodes.of(t), type));
	}

	public static <T extends Component, PROJECTION extends BaseComponent<?>> BiConsumer<T, PROJECTION> baseNameConvention(String baseName) {
		return (t, projection) -> projection.getBaseName().convention(baseName);
	}

	public static Action<Project> finalizeModelNodeOf(Object target) {
		return project -> {
			ModelNodeUtils.finalizeProjections(ModelNodes.of(target));
			ModelStates.finalize(ModelNodes.of(target));
		};
	}
}
