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
package dev.nokee.buildadapter.xcode.internal;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.initialization.Settings;

public final class GradleBuildLayout {
	private final Settings settings;

	private GradleBuildLayout(Settings settings) {
		this.settings = settings;
	}

	public static GradleBuildLayout forSettings(Settings settings) {
		return new GradleBuildLayout(settings);
	}

	public void include(String projectPath) {
		settings.include(projectPath);
	}

	public void include(String projectPath, Action<? super ProjectDescriptor> action) {
		settings.include(projectPath);
		action.execute(settings.project(projectPath));
	}

	public void project(String path, Action<? super Project> action) {
		settings.getGradle().rootProject(project -> project.project(path, action));
	}
}
