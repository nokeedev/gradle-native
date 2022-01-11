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

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import org.gradle.api.Action;
import org.gradle.api.Project;

import java.util.Objects;

final class UseJUnitJupiter implements Action<GradlePluginDevelopmentTestSuite> {
	private final String version;

	public UseJUnitJupiter(String version) {
		this.version = version;
	}

	@Override
	public void execute(GradlePluginDevelopmentTestSuite testSuite) {
		testSuite.dependencies(it -> {
			it.implementation(it.platform("org.junit:junit-bom:" + version));
			it.implementation("org.junit.jupiter:junit-jupiter");
			it.runtimeOnly("org.junit.vintage:junit-vintage-engine");
		});
		testSuite.getTestTasks().configureEach(task -> {
			task.useJUnitPlatform();
		});
	}

	public static String junitVersion(Project project) {
		return Objects.toString(project.property("junitVersion"), null);
	}
}
