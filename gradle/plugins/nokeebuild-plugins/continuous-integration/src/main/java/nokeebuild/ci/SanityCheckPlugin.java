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
import static nokeebuild.ci.TaskUtils.useVerificationGroup;

/*final*/ class SanityCheckPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		final TaskProvider<Task> sanityCheck = project.getTasks().register("sanityCheck");
		sanityCheck.configure(useVerificationGroup());
		sanityCheck.configure(task -> task.setDescription("Run all basic checks (without tests) - to be run locally and on CI for early feedback"));

		// Detect any compilation errors, useful when enabling strict compilation.
		sanityCheck.configure(dependsOn(new CompileAllTaskIfPresent(project)));

		// Detect any Javadoc issues, e.g. missing doclet, etc.
		sanityCheck.configure(dependsOn(new JavadocTaskIfPresent(project)));

		// TODO: We should generalize to "code quality" instead of assuming spotless
		sanityCheck.configure(dependsOn(new SpotlessCheckTaskIfPresent(project)));

		// Checking included builds has two parts:
		//   1. Add a dependency on included build's root check task.
		//      E.g.`gradle.includedBuild('<build-name>').task(':check')
		//   2. Register check task on root project of included build.
		//      E.g. `tasks.register('check') { dependsOn(subprojects.collect { "${it.name}:check" }) }`
		// TODO: Check our included builds (when they have tests)
	}
}
