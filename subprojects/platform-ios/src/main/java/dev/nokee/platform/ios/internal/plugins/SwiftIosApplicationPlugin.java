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

import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.SwiftIosApplication;
import dev.nokee.platform.ios.SwiftIosApplicationSources;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.IosApplicationComponentModelRegistrationFactory;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.swift.HasSwiftSourceSet;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.gradle.util.GUtil;

import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.platform.ios.internal.plugins.ObjectiveCIosApplicationPlugin.configureBuildVariants;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static org.gradle.util.ConfigureUtil.configureUsing;

public class SwiftIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class);

		// Create the component
		project.getPluginManager().apply(ComponentModelBasePlugin.class);
		project.getPluginManager().apply(SwiftLanguageBasePlugin.class);

		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(swiftIosApplication("main", project)).as(SwiftIosApplication.class);
		componentProvider.configure(configureUsingProjection(DefaultIosApplicationComponent.class, baseNameConvention(GUtil.toCamelCase(project.getName())).andThen((t, projection) -> ((DefaultIosApplicationComponent) projection).getGroupId().set(GroupId.of(project::getGroup))).andThen(configureBuildVariants())));
		project.getExtensions().add(SwiftIosApplication.class, EXTENSION_NAME, componentProvider.get());

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));


		// TODO: This should be solve in a better way
		project.getTasks().withType(CreateIosApplicationBundleTask.class).configureEach(task -> {
			task.getSwiftSupportRequired().set(true);
		});
	}

	public static ModelRegistration swiftIosApplication(String name, Project project) {
		return new IosApplicationComponentModelRegistrationFactory(SwiftIosApplication.class, DefaultSwiftIosApplication.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			// TODO: Should be created using SwiftSourceSetSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("swift"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(SwiftSourceSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			// TODO: Should be created using IosResourceSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("resources"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(IosResourceSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(path.child("sources"), SwiftIosApplicationSources.class));
		}).create(ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of(project)));
	}

	public static abstract class DefaultSwiftIosApplication implements SwiftIosApplication
		, ModelBackedDependencyAwareComponentMixIn<NativeComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<IosApplication>
		, ModelBackedSourceAwareComponentMixIn<SwiftIosApplicationSources>
		, ModelBackedBinaryAwareComponentMixIn
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
