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

package nokeebuild.ci;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import static nokeebuild.ci.TaskUtils.dependsOn;
import static nokeebuild.ci.TaskUtils.useCiLifecycleGroup;

/*final*/ class QuickTestPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		final TaskProvider<Task> quickTest = project.getTasks().register("quickTest");
		quickTest.configure(useCiLifecycleGroup());
		quickTest.configure(task -> task.setDescription("Run all unit, integration and functional (against latest release) tests."));
		quickTest.configure(dependsOn(new AllPluginDevelopmentUnitTestsIfPresent(project)));
		quickTest.configure(dependsOn(new AllPluginDevelopmentIntegrationTestsIfPresent(project)));
		quickTest.configure(dependsOn(new LatestGlobalAvailablePluginDevelopmentFunctionalTestsIfPresent(project)));
	}
}
