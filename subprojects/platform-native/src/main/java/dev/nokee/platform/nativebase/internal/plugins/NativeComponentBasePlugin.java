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
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.ComponentTasksPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.ModelBackedTaskRegistry;
import dev.nokee.platform.nativebase.internal.AttachAttributesToConfigurationRule;
import dev.nokee.platform.nativebase.internal.AttachLinkLibrariesToLinkTaskRule;
import dev.nokee.platform.nativebase.internal.AttachObjectFilesToLinkTaskRule;
import dev.nokee.platform.nativebase.internal.ConfigureLinkTaskDefaultsRule;
import dev.nokee.platform.nativebase.internal.ConfigureLinkTaskFromBaseNameRule;
import dev.nokee.platform.nativebase.internal.ConfigureLinkTaskTargetPlatformFromBuildVariantRule;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.LinkLibrariesConfiguration;
import dev.nokee.platform.nativebase.internal.LinkLibrariesConfigurationRegistrationRule;
import dev.nokee.platform.nativebase.internal.NativeLinkTaskRegistrationRule;
import dev.nokee.platform.nativebase.internal.RegisterCompileTasksPropertyActionFactory;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfiguration;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfigurationRegistrationRule;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryRegistrationFactory;
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
			new RegisterCompileTasksPropertyActionFactory(
				() -> project.getExtensions().getByType(ModelRegistry.class),
				() -> project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class)
			),
			project.getObjects()
		));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new LinkLibrariesConfigurationRegistrationRule(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class), project.getObjects())));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new RuntimeLibrariesConfigurationRegistrationRule(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class), project.getObjects())));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new NativeLinkTaskRegistrationRule(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(TaskRegistrationFactory.class), new DefaultNativeToolChainSelector(((ProjectInternal) project).getModelRegistry(), project.getProviders()))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachLinkLibrariesToLinkTaskRule(project.getExtensions().getByType(ModelRegistry.class)));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ConfigureLinkTaskFromBaseNameRule());
		project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachAttributesToConfigurationRule<>(LinkLibrariesConfiguration.class, project.getExtensions().getByType(ModelRegistry.class), project.getObjects()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachAttributesToConfigurationRule<>(RuntimeLibrariesConfiguration.class, project.getExtensions().getByType(ModelRegistry.class), project.getObjects()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachObjectFilesToLinkTaskRule(project.getExtensions().getByType(ModelRegistry.class)));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ConfigureLinkTaskDefaultsRule(project.getExtensions().getByType(ModelRegistry.class)));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ConfigureLinkTaskTargetPlatformFromBuildVariantRule(project.getExtensions().getByType(ModelRegistry.class)));
	}

	public static Factory<DefaultNativeApplicationComponent> nativeApplicationProjection(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of(project));
		return () -> new DefaultNativeApplicationComponent(identifier, project.getObjects(), ModelBackedTaskRegistry.newInstance(project), project.getExtensions().getByType(ModelRegistry.class));
	}

	public static Factory<DefaultNativeLibraryComponent> nativeLibraryProjection(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of(project));
		return () -> new DefaultNativeLibraryComponent(identifier, project.getObjects(), ModelBackedTaskRegistry.newInstance(project), project.getExtensions().getByType(ModelRegistry.class));
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
