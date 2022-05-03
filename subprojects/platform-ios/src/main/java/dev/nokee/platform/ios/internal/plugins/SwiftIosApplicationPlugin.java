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

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetRegistrationFactory;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.SwiftIosApplication;
import dev.nokee.platform.ios.SwiftIosApplicationSources;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.IosApplicationComponentModelRegistrationFactory;
import dev.nokee.platform.ios.internal.IosResourceSetRegistrationFactory;
import dev.nokee.platform.ios.internal.SwiftIosApplicationSourcesAdapter;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeComponentDependencies;
import dev.nokee.platform.swift.HasSwiftSourceSet;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.gradle.util.GUtil;

import static dev.nokee.language.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.configureUsingProjection;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

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
		componentProvider.configure(baseName(convention(GUtil.toCamelCase(project.getName()))));
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
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("Swift iOS application").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		return new IosApplicationComponentModelRegistrationFactory(SwiftIosApplication.class, DefaultSwiftIosApplication.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			registry.register(project.getExtensions().getByType(SwiftSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(entity.get(IdentifierComponent.class).get(), "swift"), true));
			registry.register(project.getExtensions().getByType(IosResourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(entity.get(IdentifierComponent.class).get(), "resources")));
		}).create(identifier);
	}

	public static abstract class DefaultSwiftIosApplication implements SwiftIosApplication
		, ModelBackedDependencyAwareComponentMixIn<NativeComponentDependencies, ModelBackedNativeComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<IosApplication>
		, ModelBackedSourceAwareComponentMixIn<SwiftIosApplicationSources, SwiftIosApplicationSourcesAdapter>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedHasBaseNameMixIn
		, ModelBackedNamedMixIn
		, ModelBackedHasAssembleTaskMixIn
	{
		@Override
		public SwiftSourceSet getSwiftSources() {
			return ((HasSwiftSourceSet) sourceViewOf(this)).getSwift().get();
		}

		@Override
		public void swiftSources(Action<? super SwiftSourceSet> action) {
			((HasSwiftSourceSet) sourceViewOf(this)).getSwift().configure(action);
		}

		@Override
		public void swiftSources(@SuppressWarnings("rawtypes") Closure closure) {
			swiftSources(configureUsing(closure));
		}
	}
}
