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
package nokeebuild;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.SourceSet;

import java.util.Objects;

final class UseLombok implements Action<SourceSet> {
	private final Action<Configuration> useLombokAction;
	private final ConfigurationContainer configurations;

	public UseLombok(Project project, String version) {
		this.configurations = project.getConfigurations();
		this.useLombokAction = addDependency(project.getDependencies().create("org.projectlombok:lombok:" + version));
	}

	@Override
	public void execute(SourceSet sourceSet) {
		configurations.named(sourceSet.getAnnotationProcessorConfigurationName(), useLombokAction);
		configurations.named(sourceSet.getCompileOnlyConfigurationName(), useLombokAction);
	}

	private static Action<Configuration> addDependency(Dependency dependency) {
		return it -> it.getDependencies().add(dependency);
	}

	public static String lombokVersion(Project project) {
		return Objects.toString(project.property("lombokVersion"), null);
	}
}
