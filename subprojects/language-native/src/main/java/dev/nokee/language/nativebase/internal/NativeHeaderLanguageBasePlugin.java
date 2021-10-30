/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.internal.SourceSetFactory;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class NativeHeaderLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		project.getExtensions().add("__nokee_headersPropertyFactory",
			new HeadersPropertyRegistrationActionFactory(
				() -> project.getExtensions().getByType(ModelRegistry.class),
				() -> project.getExtensions().getByType(SourceSetFactory.class)));
		project.getExtensions().add("__nokee_headerSearchPathsFactory",
			new HeaderSearchPathsConfigurationRegistrationActionFactory(
				() -> project.getExtensions().getByType(ModelRegistry.class),
				() -> project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class),
				() -> project.getObjects()
			));
	}
}
