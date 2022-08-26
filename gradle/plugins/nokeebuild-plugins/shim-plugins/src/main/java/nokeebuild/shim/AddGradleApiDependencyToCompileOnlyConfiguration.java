/*
 * Copyright 2022 the original author or authors.
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
package nokeebuild.shim;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.SourceSet;

final class AddGradleApiDependencyToCompileOnlyConfiguration implements Action<SourceSet> {
	private final ConfigurationContainer configurations;
	private final DependencyHandler dependencies;

	AddGradleApiDependencyToCompileOnlyConfiguration(Project project) {
		this.configurations = project.getConfigurations();
		this.dependencies = project.getDependencies();
	}

	@Override
	public void execute(SourceSet sourceSet) {
		configurations.getByName(sourceSet.getCompileOnlyConfigurationName()).getDependencies().add(GradlePluginDevelopmentDependencyExtension.from(dependencies).gradleApi(toGradleVersion(sourceSet.getName())));
	}

	private static String toGradleVersion(String name) {
		return name.substring(1); // remove 'v' suffix
	}
}
