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
package dev.nokee.language.swift.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetRegistrationFactory;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeCompileTaskRegistrationActionFactory;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.SwiftCompile;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

public class SwiftLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		DefaultImporter.forProject(project).defaultImport(SwiftSourceSet.class);

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		project.getExtensions().add("__nokee_swiftSourceSetFactory", new SwiftSourceSetRegistrationFactory(
			project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class)
		));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new RegisterSwiftSourceSetProjectionRule.LegacySourceSetRule(project.getObjects()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new RegisterSwiftSourceSetProjectionRule.DefaultSourceSetRule(project.getObjects()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(project.getExtensions().getByType(NativeCompileTaskRegistrationActionFactory.class).create(SwiftSourceSetTag.class, SwiftCompile.class, SwiftCompileTask.class));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ImportModulesConfigurationRegistrationAction(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class), project.getObjects()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachImportModulesToCompileTaskRule());
		project.getExtensions().getByType(ModelConfigurer.class).configure(new SwiftCompileTaskDefaultConfigurationRule());
	}
}
