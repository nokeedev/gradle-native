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

import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.model.internal.core.*;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.SwiftIosApplication;
import dev.nokee.platform.ios.SwiftIosApplicationSources;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.gradle.util.GUtil;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.register;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.component;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.componentSourcesOf;
import static dev.nokee.platform.ios.internal.plugins.ObjectiveCIosApplicationPlugin.configureBuildVariants;
import static dev.nokee.platform.ios.internal.plugins.ObjectiveCIosApplicationPlugin.create;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static dev.nokee.platform.objectivec.internal.ObjectiveCSourceSetModelHelpers.configureObjectiveCSourceSetConventionUsingMavenAndGradleCoreNativeLayout;

public class SwiftIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class);

		// Create the component
		project.getPluginManager().apply(ComponentModelBasePlugin.class);
		project.getPluginManager().apply(SwiftLanguageBasePlugin.class);

		val components = project.getExtensions().getByType(ComponentContainer.class);
		val registry = ModelNodeUtils.get(ModelNodes.of(components), NodeRegistrationFactoryRegistry.class);
		registry.registerFactory(of(SwiftIosApplication.class), name -> swiftIosApplication(name, project));
		val componentProvider = components.register("main", SwiftIosApplication.class, configureUsingProjection(DefaultIosApplicationComponent.class, baseNameConvention(GUtil.toCamelCase(project.getName())).andThen((t, projection) -> ((DefaultIosApplicationComponent) projection).getGroupId().set(GroupId.of(project::getGroup))).andThen(configureBuildVariants())));
		project.getExtensions().add(SwiftIosApplication.class, EXTENSION_NAME, componentProvider.get());

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));


		// TODO: This should be solve in a better way
		project.getTasks().withType(CreateIosApplicationBundleTask.class).configureEach(task -> {
			task.getSwiftSupportRequired().set(true);
		});
	}

	public static NodeRegistration<SwiftIosApplication> swiftIosApplication(String name, Project project) {
		return component(name, SwiftIosApplication.class)
			.withProjection(ModelProjections.createdUsing(of(DefaultIosApplicationComponent.class), () -> create(name, project)))
			.action(self(discover()).apply(register(sources())))
			.action(configureObjectiveCSourceSetConventionUsingMavenAndGradleCoreNativeLayout(ComponentName.of(name)));
	}

	private static NodeRegistration<SwiftIosApplicationSources> sources() {
		return componentSourcesOf(SwiftIosApplicationSources.class)
			.action(self(discover()).apply(register(sourceSet("swift", SwiftSourceSet.class))))
			.action(self(discover()).apply(register(sourceSet("resources", IosResourceSet.class))));
	}
}
